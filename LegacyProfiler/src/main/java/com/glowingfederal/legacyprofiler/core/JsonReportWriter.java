package com.glowingfederal.legacyprofiler.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

final class JsonReportWriter implements ReportWriter {
    public String fileName(ProfileSession session) { return "summary.json"; }
    public void write(File file, ProfileSession session) throws IOException {
        long serializationStarted = System.nanoTime();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            ProfileMetadata m = session.getMetadata();
            out.write("{\n  \"schema_version\": \"1.0\",\n  \"profiler_version\": \"" + esc(m.profilerVersion) + "\",");
            out.write("\n  \"build_version\": \"" + esc(m.profilerVersion) + "\",");
            out.write("\n  \"started_at_epoch_ms\": " + session.getStartedAt().getTime() + ",");
            out.write("\n  \"ended_at_epoch_ms\": " + session.getEndedAt().getTime() + ",");
            out.write("\n  \"duration_nanos\": " + session.elapsedNanos(System.nanoTime()) + ",");
            out.write("\n  \"chunks_generated\": " + session.getChunksGenerated() + ",");
            out.write("\n  \"recording_mode\": \"" + esc(session.getMode()) + "\",");
            out.write("\n  \"profile\": {\"source\":\"" + esc(session.getSessionInfo().getSource()) + "\"");
            if (session.getSessionInfo().getDisplayName() != null) out.write(",\"display_name\":\"" + esc(session.getSessionInfo().getDisplayName()) + "\"");
            if (session.getSessionInfo().getPurpose() != null) out.write(",\"purpose\":\"" + esc(session.getSessionInfo().getPurpose()) + "\"");
            out.write("},");
            out.write("\n  \"metadata\": {\"minecraft\":\"" + esc(m.minecraftVersion) + "\",\"forge\":\"" + esc(m.forgeVersion)
                + "\",\"java\":\"" + esc(m.javaVersion) + "\",\"os\":\"" + esc(m.os) + "\",\"cpu\":\"" + esc(m.cpu)
                + "\",\"maximum_heap\":" + m.maximumHeap + ",\"allocated_heap\":" + m.allocatedHeap
                + ",\"git_commit\":\"" + esc(m.gitCommit) + "\",\"configuration_hash\":\"" + esc(m.configurationHash)
                + "\",\"worker_count\":\"" + esc(m.workerCount) + "\",\"loaded_mods\":" + strings(m.loadedMods)
                + ",\"loaded_dimensions\":" + m.loadedDimensions.toString() + "},");
            out.write("\n  \"validation_warnings\": " + strings(ProfileValidator.validate(session)) + ",");
            out.write("\n  \"stages\": {");
            boolean first = true;
            for (Map.Entry<Stage, StageStatistics> entry : session.getStatistics().entrySet()) {
                if (StageRegistry.get(entry.getKey().name()).kind == StageKind.COUNTER) continue;
                if (!first) out.write(','); first = false;
                StageStatistics.Snapshot s = entry.getValue().snapshot();
                StageMetadata md = StageRegistry.get(entry.getKey().name());
                out.write("\n    \"" + entry.getKey().name() + "\": {");
                out.write("\"name\":\"" + esc(md.name) + "\",\"parent\":" + nullable(md.parent)
                    + ",\"kind\":\"" + md.kind.name() + "\",\"category\":\"" + esc(md.category) + "\",\"description\":\"" + esc(md.description)
                    + "\",\"enabled\":" + md.enabled + ",\"source\":\"" + esc(md.source) + "\",\"display_order\":" + md.displayOrder
                    + ",\"calls\":" + s.calls + ",\"inclusive_nanos\":" + s.inclusiveNanos
                    + ",\"exclusive_nanos\":" + s.exclusiveNanos + ",\"average_nanos\":" + s.averageNanos
                    + ",\"minimum_nanos\":" + s.minimumNanos + ",\"maximum_nanos\":" + s.maximumNanos
                    + ",\"median_nanos\":" + s.medianNanos + ",\"p95_nanos\":" + s.p95Nanos
                    + ",\"p99_nanos\":" + s.p99Nanos + ",\"standard_deviation_nanos\":" + s.standardDeviationNanos
                    + ",\"inclusive_contribution_percent\":" + percent(s.inclusiveNanos, session.elapsedNanos(System.nanoTime()))
                    + ",\"exclusive_contribution_percent\":" + percent(s.exclusiveNanos, session.elapsedNanos(System.nanoTime()))
                    + ",\"coefficient_of_variation\":" + s.coefficientOfVariation + ",\"spike_ratio\":" + finite(s.spikeRatio)
                    + ",\"histogram_0_1_1_2_2_3_3_5_5_10_10plus_ms\":" + java.util.Arrays.toString(s.histogram)
                    + ",\"longest_nanos\":" + java.util.Arrays.toString(s.longestNanos) + "}");
            }
            ProfilerStatistics ps = session.getProfilerStatistics();
            out.write("\n  },\n  \"counters\": {"); boolean firstCounter = true;
            for (Map.Entry<Stage, StageStatistics> entry : session.getStatistics().entrySet()) {
                StageMetadata md = StageRegistry.get(entry.getKey().name()); if (md.kind != StageKind.COUNTER) continue;
                if (!firstCounter) out.write(','); firstCounter = false;
                out.write("\n    \"" + entry.getKey().name() + "\": " + entry.getValue().snapshot().calls);
            }
            out.write("\n  },\n  \"profiler_statistics\": {\"average_enter_nanos\":" + ps.averageEnterNanos()
                + ",\"average_exit_nanos\":" + ps.averageExitNanos() + ",\"average_stage_lookup_nanos\":" + ps.averageLookupNanos()
                + ",\"timeline_write_nanos\":" + ps.timelineWriteNanos() + ",\"json_serialization_nanos\":" + ps.jsonSerializationNanos()
                + ",\"report_generation_nanos\":" + ps.reportGenerationNanos() + ",\"peak_memory_bytes\":" + session.estimatedBytes()
                + ",\"allocations\":" + ps.allocations() + ",\"dropped_timeline_events\":" + ps.droppedTimelineEvents()
                + ",\"dropped_sampled_traces\":" + ps.droppedSampledTraces() + "},\n  \"extensions\": {},\n  \"sampled_traces\": [");
            boolean firstTrace = true;
            for (ProfileSession.SampledTrace trace : session.getTraces()) {
                if (!firstTrace) out.write(','); firstTrace = false;
                out.write("{\"chunk_sequence\":" + trace.chunkSequence + ",\"worker_id\":\"" + esc(trace.workerId) + "\",\"entries\":[");
                boolean firstEntry = true;
                for (ProfileSession.TraceEntry entry : trace.entries) {
                    if (!firstEntry) out.write(','); firstEntry = false;
                    out.write("{\"stage\":\"" + entry.stage + "\",\"inclusive_nanos\":" + entry.inclusiveNanos
                        + ",\"exclusive_nanos\":" + entry.exclusiveNanos + "}");
                }
                out.write("]}");
            }
            out.write("]\n}\n");
        } finally { out.close(); session.getProfilerStatistics().json(System.nanoTime() - serializationStarted); }
    }
    private static double percent(long value, long total) { return total == 0 ? 0 : value * 100.0 / total; }
    private static String nullable(String value) { return value == null ? "null" : "\"" + esc(value) + "\""; }
    private static String finite(double value) { return Double.isInfinite(value) || Double.isNaN(value) ? "null" : Double.toString(value); }
    private static String strings(Iterable<String> values) {
        StringBuilder b = new StringBuilder("["); boolean first = true;
        for (String value : values) { if (!first) b.append(','); first = false; b.append('"').append(esc(value)).append('"'); }
        return b.append(']').toString();
    }
    private static String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"); }
}
