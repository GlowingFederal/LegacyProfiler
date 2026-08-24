# Legacy Profiler

Legacy Profiler is a Java 8 profiling framework for legacy Minecraft integrations, including
Minecraft Forge 1.7.10 mods. It gives host mods and consuming mods a shared vocabulary of
structured stages, counters, sessions, and reports instead of requiring every mod to implement
its own instrumentation. It can measure Minecraft-facing work supplied by a host adapter and work
performed by other mods that register stages through the public API.

Legacy Profiler is instrumentation: its reports describe observed work and profiler overhead;
they do not promise a performance improvement by themselves.

> **Current packaging:** version 1.0.0 is a domain-neutral Java library, not a directly loadable
> Forge mod. This repository does not currently contain an `@Mod` entry point, Forge command,
> configuration GUI/file, or Minecraft lifecycle adapter. A host mod must embed or depend on the
> library and own session lifecycle. Server and modpack users should obtain the host mod's complete
> distribution rather than placing `legacy-profiler-1.0.0.jar` in `mods/` by itself.

## Features

- Deterministically registered `TIMING`, `COUNTER`, and host-reserved `INTERNAL` stages, with
  optional parent identifiers for organizing a hierarchy.
- Nested, per-thread timing stacks with inclusive and exclusive duration aggregates.
- Bounded reservoir statistics, percentile estimates, histograms, longest-call tracking, and
  profiler self-statistics.
- Counter stages for occurrences, separate from duration stages.
- Aggregate recording by default and opt-in, bounded sampled traces controlled by the host.
- A bounded timeline of host ticks, memory observations, progress counts, and named events.
- `summary.json`, `timeline.csv`, and a concise human-readable `profile.log` for every completed
  session. See [the JSON contract](docs/json-schema.md).
- Stage and session consumer attribution in exports.
- Service-loaded stage, report-writer, and initialization extension points.

Aggregate stage statistics summarize the entire session. Sampled traces instead retain individual
stage entries only inside samples explicitly opened by a host adapter; enabling `sampled` mode does
not create a sampler thread or discover Minecraft hooks automatically.

## Requirements and compatibility

| Item | Implemented requirement |
| --- | --- |
| Java | Java 8 source and bytecode target |
| Gradle | Gradle 4.4.1 wrapper |
| Minecraft | Intended integration target: Minecraft 1.7.10; the library itself has no Minecraft dependency |
| Forge | Intended integration target: Forge 10.13.4.1614; the library itself has no Forge dependency |
| Other libraries | None |

No broader Minecraft, Forge, or Java compatibility is established by the current build.

## Installation

### Server and modpack users

Install a mod that integrates Legacy Profiler and follow that mod's instructions. The standalone
JAR has no Forge entry point and will not add commands when copied to `mods/`. There are no required
third-party runtime libraries beyond Java 8, but the host mod is responsible for packaging and
calling Legacy Profiler.

### Mod developers

Compile against the normal JAR, and ensure the same classes are available at runtime—either by
shipping Legacy Profiler as a declared required library/mod dependency or, where licensing permits,
embedding it without duplicate copies. There is no public Maven repository in this repository.
See [Developer API integration](docs/API.md) for local-JAR setup and working code.

## Basic usage

There is currently no `/legacyprofiler` command or built-in configuration. The real lifecycle is a
Java API lifecycle owned by an integrating mod:

1. Register all stages during deterministic mod initialization.
2. Optionally call `Profiler.configureSampling("aggregate", 100)` (aggregate is already the default).
3. Start a session with consumer identity using `Profiler.beginSession(info)`.
4. Enter and exit timing stages around the workload and record counters/events as needed.
5. Call `Profiler.endSession()`; the returned directory contains the reports.

For example, a host can profile a generation run as follows:

```java
ProfileSession session = Profiler.beginSession(ProfileSessionInfo.builder()
    .source("examplemod")
    .displayName("Example Mod")
    .purpose("Investigate world-generation time")
    .build());
try {
    runGenerationWork(); // consuming code instruments its registered stages
} finally {
    File reportDirectory = Profiler.endSession();
}
```

The default output root is `logs/legacyprofiler/`. A completed session is written below it as
`profile-YYYY-MM-dd_HH-mm-ss/`. Ending a session may throw `IOException`; hosts should report that
failure to their users. The complete registration and timing example is in
[`examples/ExampleModIntegration.java`](examples/ExampleModIntegration.java).

## Output

- **`summary.json`** — machine-readable session metadata, stage/counter aggregates, validation
  warnings, profiler overhead statistics, and sampled traces.
- **`timeline.csv`** — spreadsheet-friendly, bounded timeline samples and events. Its first row
  records session attribution; subsequent rows are classified as `sample` or `event`.
- **`profile.log`** — short, human-readable stage totals suitable for quick inspection.

The JSON stage `source` records the consumer that registered each stage. The root JSON `profile`
object and CSV metadata row identify the consumer that started the session, so consuming mods retain
their attribution in exported profiles when they supply it during registration/session creation.

## Building

Use a Java 8 JDK:

```text
./gradlew clean build
```

The distributable and compile-time API are the same artifact:
`build/libs/legacy-profiler-1.0.0.jar`. No sources/Javadoc artifacts or publication repository are
currently configured. The wrapper may need to download Gradle 4.4.1 on its first run.

## API, issues, and licensing

- [Developer API guide](docs/API.md)
- [JSON schema notes](docs/json-schema.md)
- [Contributing and issue reports](CONTRIBUTING.md)
- [Change log](changelog.md)

The repository is **source available**, not offered as OSI open source. The project-specific license
text has not yet been supplied; [`LICENSE`](LICENSE) is an explicit placeholder. Viewing this public
source does not by itself grant permissions beyond applicable law. Obtain the final license before
redistributing, embedding, or releasing the library/API.
