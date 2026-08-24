# Legacy Profiler developer API

Legacy Profiler 1.0.0 (API version `1.0`) is a directly loadable Forge 1.7.10 mod. Its Forge
container deterministically initializes one process-wide service in pre-initialization, owns
operator-driven sessions, registers its command at server start, and finalizes an active session at
server stop. Consumer mods register stages and emit observations; they do not bootstrap a profiler
implementation or own global lifecycle.

## Dependency and load order

Publish the repository artifact with `./gradlew publishToMavenLocal`, then add:

```groovy
repositories { mavenLocal() }
dependencies { compile "com.glowingfederal:legacy-profiler:1.0.0" }
```

The consumer JAR must not shade or embed Legacy Profiler. Install `LegacyProfiler-1.0.0.jar`
separately and declare direct API linkage to Forge 1.7.10:

```java
@Mod(modid = "examplemod", dependencies = "required-after:legacyprofiler")
public final class ExampleMod {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ExampleStages.register();
    }
}
```

`required-after:legacyprofiler` makes Forge verify presence and order initialization. Direct bytecode
linkage is not magically optional; optional support must be isolated by the consumer so referenced
classes are not loaded when the mod is absent.

## Public surface and shared access

`com.glowingfederal.legacyprofiler.api.Profiler` is the stable static facade. Static publication is
thread-safe and leads every consumer to the same runtime. The Forge container calls
`Profiler.initialize()` in pre-initialization; consumers normally just use the facade during their
own initialization. `Profiler.isSessionActive()` is available for observational integration.

The supported public packages are:

- `com.glowingfederal.legacyprofiler.api` — stable consumer facade and session attribution value;
- documented immutable stage types and `ReportWriter` in `.core` — currently required model/SPI;
- `com.glowingfederal.legacyprofiler.extension` — documented service-provider contracts.

Other `.core` classes and everything in `.forge` are implementation details and are not
compatibility-stable.

## Registering stages

Register once from the consumer mod's FML initialization handler, before a session starts, and
cache the returned lookup handle:

```java
Profiler.registerStage("examplemod", new StageMetadata(
    "examplemod.generation", null, StageKind.TIMING,
    "World generation", "ExampleMod generation work", true, "examplemod", 100));
Stage generation = Profiler.stage("examplemod.generation");
```

Use `TIMING` for durations and `COUNTER` for occurrences. `INTERNAL` is reserved for profiler/host
bookkeeping. `consumerId` is a validated lowercase identifier (`[a-z0-9][a-z0-9_-]*`) and is stored as immutable ownership on the returned stage handle. `source` remains descriptive provider metadata and is preserved in JSON reports. Ownership is never inferred from a stage-name prefix. Parent
names are organizational metadata and must name separately registered stages.

API 1.0 stage IDs are process-global and `Stage` equality is name-based. Prefix every ID with a
stable mod ID, as above. The synchronized copy-on-write registry rejects duplicate names with an
`IllegalArgumentException`; it never silently overwrites an unrelated consumer. This global-name
requirement is the remaining limitation for multiple independent consumers that want identical
local stage names.

## Recording

```java
Profiler.enter(generation);
try {
    generateChunk();
} finally {
    Profiler.exit(generation);
}
```

Enter and exit on the same thread and nest stages strictly. Each producer thread has an independent
stack. Inactive calls return without recording. A mismatched exit clears that thread's stack and
adds a validation warning. Increment a `COUNTER` stage with `Profiler.recordCounter(stage)` and add
timeline events with `Profiler.recordEvent(description)`.

Aggregate recording is the default. `Profiler.configureSampling("sampled", interval)` enables
retention only when a host adapter supplies sample boundaries; the public facade does not create a
sampler thread. `serverTick()` and `recordChunkGenerated()` are adapter hooks and ordinary consumers
should not call them merely to force output.

## Session ownership, commands, and output

Exactly one process-wide session may be active. A second start throws `IllegalStateException` rather than replacing the active session. Legacy Profiler operators use:

- `/legacyprofiler start` — create one attributed `GLOBAL` session that accepts all owners;
- `/legacyprofiler stop` — finalize it and synchronously export reports;
- `/legacyprofiler status` — show idle/running state.

Reports go to `profiles/legacy-profiler/profile-YYYY-MM-dd_HH-mm-ss/` under the instance directory:
`summary.json`, `timeline.csv`, and `profile.log`. Stage `source` remains in JSON while session
source appears in JSON and CSV. Server shutdown calls the same finalization path; even when export
throws, stopped global state is detached to prevent cross-server leakage in the JVM.

`beginGlobalSession(info)` explicitly starts a global session. The legacy `beginSession()` and
`beginSession(info)` entry points remain binary/source compatible and retain global semantics.
`beginConsumerSession(consumerId, info)` starts a session accepting only stages registered to that
consumer. The `ProfileSessionInfo` source identifies who initiated the profile; session scope and
consumer identify what is measured, and stage ownership identifies who supplied each observation.

A consumer can wrap a scoped session with its own command or UI:

```java
Profiler.beginConsumerSession("examplemod", ProfileSessionInfo.builder()
    .source("examplemod").displayName("Example Mod").purpose("Generation diagnosis").build());
// Later, after checking Profiler.isSessionActive():
Profiler.endSession();
```

All consumers share the same lifecycle, so integrations must handle `IllegalStateException` from a
start race cleanly and must not stop a session they did not initiate. Namespaced stage identifiers
remain recommended for global uniqueness, but do not establish ownership.

## Extensions and threading

First facade initialization loads Java `ServiceLoader` providers once. `StageProvider` supplies
deterministic metadata, `ReportProvider` supplies synchronous custom writers, and
`ProfilerPlugin.initialize()` performs one-time integration initialization. Namespace stages and
filenames and avoid slow discovery. `MetadataProvider` and `TimelineProvider` are not consumed in
API 1.0 and must not be relied upon.

Registry reads use immutable snapshots and registration is synchronized. Aggregate recording,
counters, timeline events, and profiler statistics accept concurrent producers. Session start/stop
is synchronized and report writers execute synchronously on the controlling server/command thread.
Custom writers must synchronize access to their own external state.

See [`../examples/ExampleModIntegration.java`](../examples/ExampleModIntegration.java) for a small
consumer-only registration and recording example. JSON compatibility is documented in
[`json-schema.md`](json-schema.md).
