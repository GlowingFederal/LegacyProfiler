# Legacy Profiler

Legacy Profiler 1.0.0 is a standalone, domain-independent Java 8 profiling library suitable for embedded Forge 1.7.10 mods. Its frozen facade supports sessions, nested timings, counters, events, stage and writer registration. The core has no Forge dependency; command and game adapters belong in consumers.

## Embed and record

Depend on the `LegacyProfiler` project (or its published JAR), register stages during initialization, and retain the returned identifier:

```java
Profiler.registerStage(new StageMetadata("NETWORK_DECODE", null, StageKind.TIMING,
    "Network", "Packet decode", true, "ExampleMod", 0));
Stage decode = StageRegistry.stage("NETWORK_DECODE");
Profiler.beginSession(ProfileSessionInfo.builder()
    .source("ExampleMod")
    .displayName("Example Mod")
    .purpose("Network performance investigation")
    .build());
Profiler.enter(decode);
try { decodePacket(); } finally { Profiler.exit(decode); }
Profiler.recordEvent("queue drained");
Profiler.endSession();
```

Counters use `StageKind.COUNTER` and `Profiler.recordCounter`. Writers implement `ReportWriter` and are added with `Profiler.registerWriter`.

`ProfileSessionInfo` identifies the consumer that requested the recording. This is distinct
from Legacy Profiler's own identity and from `StageMetadata.source`, which identifies who
registered each stage. Compatibility entry points that omit session information use the
deterministic source `Unknown`; blank sources are rejected, while blank optional display names
and purposes are omitted.

The JSON report places this attribution in the root `profile` object. The timeline CSV adds
`record_type`, `profile_source`, and `profile_purpose` columns and emits one
`session_metadata` row, avoiding repetition on high-frequency sample rows. Subsequent rows are
explicitly classified as `sample` or `event`.

## Runtime model and guarantees

Nested timing stacks are thread-local; session aggregates are synchronized and safe for concurrent producers. Timeline storage, sampled traces, histograms, and longest-call tracking are bounded. An inactive timing call performs no allocation. Extensions are discovered once through `ServiceLoader`; implement `ProfilerPlugin`, `StageProvider`, `TimelineProvider`, `ReportProvider`, or `MetadataProvider` and register the implementation under `META-INF/services`.

Sampling is configured with `configureSampling`; validation warnings are included in reports. Integrations own their domain stages and lifecycle hooks.

## Versioning

Legacy Profiler follows semantic versioning independently of its host. The 1.x line preserves source compatibility of the facade and adds JSON fields only compatibly; removals or semantic breaks require 2.0.0.
