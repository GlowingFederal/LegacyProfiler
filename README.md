# Legacy Profiler

Legacy Profiler is a standalone Forge 1.7.10 profiling mod **and** a public profiling API for
other mods. One installed copy owns the shared process-wide profiler runtime; consumer mods own
their stage catalogues and only register and record work through the API.

Legacy Profiler is instrumentation: its reports describe observed work and profiler overhead;
they do not themselves promise a performance improvement.

## Features

- Process-wide `TIMING` and `COUNTER` stage registration with consumer/source attribution.
- Concurrent per-thread nested timing and aggregate statistics.
- Bounded sampled traces and a bounded server timeline.
- JSON, CSV, and concise text output for every completed session.
- A Forge lifecycle owner and `/legacyprofiler` operator command.
- Service-loaded stage and report-writer extensions.

## Requirements

- Minecraft 1.7.10
- Forge 10.13.4.1614
- Java 8

## For server and modpack users

1. Put `LegacyProfiler-1.0.0.jar` in the instance's `mods/` directory.
2. Put any consumer mods that require it in the same `mods/` directory.
3. As an operator, run `/legacyprofiler start`, reproduce the workload, then run
   `/legacyprofiler stop`. `/legacyprofiler status` reports whether a session is active.

Legacy Profiler writes each completed session beneath `profiles/legacy-profiler/` in a directory
named `profile-YYYY-MM-dd_HH-mm-ss`. Each directory contains:

- `summary.json` — metadata, attributed stage/counter aggregates, warnings, overhead, and traces;
- `timeline.csv` — session attribution, bounded timeline samples, and events;
- `profile.log` — a short human-readable aggregate summary.

An active session is finalized during server shutdown. Export errors are logged, and stopped state
is detached even if a writer fails so it cannot leak into a later integrated-server run.

## For mod developers

Publish this checkout to Maven Local with:

```text
./gradlew publishToMavenLocal
```

Then consume the same artifact used at runtime:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compile "com.glowingfederal:legacy-profiler:1.0.0"
}
```

A directly linked Forge mod must also declare load order and presence:

```java
@Mod(modid = "examplemod", dependencies = "required-after:legacyprofiler")
```

Register consumer-owned stages from the consumer's normal FML initialization handler, after
Legacy Profiler pre-initialization and before any profile starts. Do **not** shade or embed Legacy
Profiler: end users install its JAR separately, Forge supplies load ordering, and all consumers
therefore reach the same static service. Record work through
`com.glowingfederal.legacyprofiler.api.Profiler`; consumers do not create or stop global sessions.
See [the developer API guide](docs/API.md) and the registration example in
[`examples/ExampleModIntegration.java`](examples/ExampleModIntegration.java).

The supported compatibility facade is `com.glowingfederal.legacyprofiler.api`. The immutable stage
model and report-writer SPI currently exposed from `com.glowingfederal.legacyprofiler.core` and
`com.glowingfederal.legacyprofiler.extension` remain usable where documented, but other core and
Forge implementation classes are internal and are not compatibility-stable.

Stage identifiers are process-global in API 1.0. Consumers must prefix them with a stable mod ID.
Duplicate registration is rejected with the conflicting identifier rather than silently
overwriting another consumer. See [the API guide](docs/API.md) for this existing multi-consumer
constraint and full threading rules.

## Building and distribution

The legacy ForgeGradle build produces one directly installable artifact:

```text
./gradlew clean build
build/libs/LegacyProfiler-1.0.0.jar
```

That JAR contains the Forge container, API, implementation, and `mcmod.info`; no separate core JAR
is needed. The Maven coordinate is `com.glowingfederal:legacy-profiler:1.0.0`.

## API, schema, issues, and licensing

- [Developer API guide](docs/API.md)
- [JSON schema notes](docs/json-schema.md)
- [Contributing and issue reports](CONTRIBUTING.md)
- [Change log](changelog.md)

The repository is **source available**, not offered as OSI open source. [`LICENSE`](LICENSE) retains
the project's rights-reserved placeholder terms. Public API consumption is an intended use, but
viewing the source does not grant redistribution, embedding, or other rights beyond applicable law;
obtain the final license before distribution.
