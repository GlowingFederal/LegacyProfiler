# Legacy Profiler developer API

This guide describes the code shipped in Legacy Profiler 1.0.0 (`API_VERSION` `1.0`). Using this
public Java API from another mod is the supported integration mechanism. Legacy Profiler currently
has no Forge `@Mod` container, command layer, configuration file, or automatic lifecycle hooks.

## Packages and dependency setup

The intended consumer facade is `com.glowingfederal.legacyprofiler.api`. Stage value types and the
report-writer contract currently live in `com.glowingfederal.legacyprofiler.core`. Intentional
service-provider extension points live in `com.glowingfederal.legacyprofiler.extension`.

There is no configured Maven publication and no public repository/coordinates to paste into a
build. Build Legacy Profiler with Java 8 using `./gradlew clean build`, then copy
`build/libs/legacy-profiler-1.0.0.jar` to a consuming mod's local `libs/` directory:

```groovy
dependencies {
    compile files('libs/legacy-profiler-1.0.0.jar')
}
```

That is only **compile-time integration**. Direct references to these classes also require them at
runtime. A host must arrange exactly one compatible copy, either as a required runtime library or
embedded in its own distribution where the final Legacy Profiler license permits that. The current
library is not independently recognized by Forge, so merely placing it in `mods/` does not establish
a Forge dependency or start profiling.

There is no optional-dependency shim. If a mod loads a class whose signatures or bytecode directly
reference Legacy Profiler while the library is absent, normal JVM class loading may fail. An
optional integration must isolate all direct references behind the consuming mod's own presence
check and only load that integration class when the host has made Legacy Profiler available. If
profiling is required, fail clearly during the host mod's initialization instead.

## Registering stages

Register stages once during deterministic mod initialization, before any session begins. Stage
names are process-global, uppercase identifiers matching `[A-Z][A-Z0-9_]*`; prefix them with a
stable mod identifier to prevent collisions. Registration is synchronized, rejects duplicates, and
is not idempotent. Sessions snapshot registered stages, so registering after session start will
produce a handle that the active session cannot record safely.

```java
import com.glowingfederal.legacyprofiler.api.Profiler;
import com.glowingfederal.legacyprofiler.core.Stage;
import com.glowingfederal.legacyprofiler.core.StageKind;
import com.glowingfederal.legacyprofiler.core.StageMetadata;

public final class ExampleStages {
    public static Stage GENERATION;
    public static Stage BLOCKS_PLACED;

    public static void register() {
        Profiler.registerStage(new StageMetadata(
            "EXAMPLEMOD_GENERATION", // globally unique stable identifier
            null,                    // parent stage identifier, or null
            StageKind.TIMING,
            "World generation",
            "ExampleMod generation work",
            true,
            "examplemod",           // owning consumer/mod ID
            100));
        GENERATION = Profiler.stage("EXAMPLEMOD_GENERATION");

        Profiler.registerStage(new StageMetadata(
            "EXAMPLEMOD_BLOCKS_PLACED", null, StageKind.COUNTER,
            "World generation", "Blocks placed by ExampleMod", true, "examplemod", 110));
        BLOCKS_PLACED = Profiler.stage("EXAMPLEMOD_BLOCKS_PLACED");
    }
}
```

Use `TIMING` for durations and `COUNTER` for occurrences. `INTERNAL` is available but intended for
host/profiler bookkeeping, not ordinary consuming-mod work. A child stage's `parent` is metadata
for report organization; it must name a separately registered stage and does not automatically
enter the parent. Keep identifiers and parent relationships stable across versions so exported
profiles remain comparable. The `enabled` field is descriptive in 1.0.0; it is not a runtime switch.

`Stage` is immutable, compares by name, and is safe to cache for the life of the process. Metadata
is also immutable. `Profiler.stage(name)` throws if registration has not occurred.

## Recording timings

The v1 facade uses explicit `enter`/`exit`; it does not supply an `AutoCloseable` scope. Always use
`try/finally`, on the same thread, with properly nested stages:

```java
Profiler.enter(ExampleStages.GENERATION);
try {
    generateChunk();
} finally {
    Profiler.exit(ExampleStages.GENERATION);
}
```

Each producer thread has an independent timing stack. Nested stages calculate inclusive time and
subtract completed child time to calculate exclusive time. A mismatched exit clears the calling
thread's stack and adds a validation warning; it does not throw. When profiling is inactive or
paused, `enter`, `exit`, and counter recording return without recording; inactive timing calls are
implemented as a volatile read/branch with no timing-frame allocation. Do not mix an `enter` made
during an active interval with an `exit` after pausing or ending a session.

## Counters

Increment a registered `COUNTER` stage once per occurrence:

```java
Profiler.recordCounter(ExampleStages.BLOCKS_PLACED);
```

Counters report call counts and must not be entered/exited as duration stages. Conversely, do not
use `recordCounter` on a timing stage. The hot-path facade does not dynamically validate the kind,
so selecting the correct kind is the consumer's contract. Calls are ignored while inactive.

## Sessions and consumer attribution

Only the integrating host should own the process-wide session lifecycle. Supply a stable mod ID or
integration ID as `source`; do not use a human display name as the machine identity:

```java
import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;

Profiler.beginSession(ProfileSessionInfo.builder()
    .source("examplemod")
    .displayName("Example Mod")       // optional
    .purpose("Profile terrain pass")  // optional
    .build());
try {
    runMeasuredWork();
} finally {
    Profiler.endSession(); // writes reports synchronously and may throw IOException
}
```

`ProfileSessionInfo` is immutable and cacheable. A blank/null source is rejected; optional blank
fields become null. The no-argument session overload uses `Unknown` and exists for compatibility,
not as the preferred integration. Session source appears in the JSON `profile` object and CSV
metadata row. Each stage's independent `StageMetadata.source` appears with that stage in JSON,
making both the session initiator and registering consumer visible.

The default output root is `logs/legacyprofiler`, with timestamped session directories. The public
facade does not currently expose output-root, pause/resume, or reset controls. Starting while a
session is already active replaces the old session without exporting it; hosts must serialize and
balance lifecycle calls. `endSession()` returns null when inactive and otherwise returns the report
directory.

## Aggregate recording and sampled traces

`Profiler.configureSampling("aggregate", interval)` selects aggregate-only recording (the default).
`"sampled"` allows host adapters to retain individual traces, but the public API facade does not
currently expose sample-boundary methods. It also does not install a background sampler. Consuming
mods should rely on aggregate timing unless they are integrating with a host adapter that explicitly
supports sampled boundaries. `Profiler.serverTick()` and `recordChunkGenerated()` are host hooks,
not lifecycle automation.

## Extensions

The first use of the API loads Java `ServiceLoader` providers once. An integration JAR may declare
implementations in `META-INF/services/<fully-qualified-interface-name>`:

- `ProfilerPlugin.initialize()` performs one-time integration initialization.
- `StageProvider.stages()` supplies deterministic stage metadata.
- `ReportProvider.writers()` supplies synchronous custom `ReportWriter` instances.

Provider collections and their elements must be non-null. Providers should namespace stage names
and output filenames with their mod ID, perform no slow work during discovery, and avoid depending
on provider iteration order. Custom writers own their format, run while the session is ending, and
may throw `IOException`, which is propagated to the host.

`MetadataProvider` and `TimelineProvider` types exist in the source but are not consumed by the 1.0.0
loader or built-in writers. The JSON `extensions` object is currently always empty. They are
therefore **not usable public extension mechanisms yet**, and consumers must not advertise or rely
on custom extension metadata. `ComparisonResult`/`ComparisonWriter` are similarly reserved and
have no built-in execution path.

## Thread safety and lifecycle rules

- Stage registry reads are lock-free snapshots; registration is synchronized. Finish registration
  before starting worker threads or a session.
- Timing stacks and sampled-trace state are thread-local. Enter and exit the same stage on the same
  thread and nest calls strictly.
- Duration aggregates, counter increments, timeline events, and profiler statistics accept
  concurrent producer threads. Cached `Stage` and `ProfileSessionInfo` objects are immutable.
- Session start/stop operations are synchronized internally, but they replace global state and write
  reports synchronously. Treat them as host/server-lifecycle operations on one controlling thread.
- `serverTick()` should be called by the host's server tick thread. Ordinary consuming mods should
  not call it merely to force output.
- Custom report writers execute on the thread calling `endSession()` and must provide their own
  synchronization if they access external mutable state.

## API stability

`Profiler.VERSION` is `1.0.0`; `Profiler.API_VERSION` is `1.0`. The facade is the compatibility
boundary for the 1.x line. The project promises compatible additions and additive JSON fields in
1.x; semantic breaks require a new major version. Core and extension surface that the facade does
not expose should be treated more cautiously, especially the explicitly unused provider and
comparison types. The JSON compatibility rules are recorded in [json-schema.md](json-schema.md).

## Complete small example

See [`../examples/ExampleModIntegration.java`](../examples/ExampleModIntegration.java). It is a
copyable documentation example and is intentionally outside Gradle's compiled source set.
