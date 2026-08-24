package com.glowingfederal.legacyprofiler.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Takes bounded-rate aggregate snapshots; no background thread is created. */
public final class TimelineRecorder {
    private static final int MAX_ROWS = 86400;
    private final List<Sample> samples = new ArrayList<Sample>();
    private long lastSampleNanos;
    private long lastTickNanos;
    private double tickMillis;

    synchronized void serverTick(long now) {
        if (lastTickNanos != 0) tickMillis = (now - lastTickNanos) / 1_000_000.0;
        lastTickNanos = now;
    }

    synchronized void maybeSample(ProfileSession session, long now) {
        if (lastSampleNanos != 0 && now - lastSampleNanos < 1_000_000_000L) return;
        lastSampleNanos = now;
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        double tps = tickMillis <= 0 ? 20.0 : Math.min(20.0, 1000.0 / tickMillis);
        long started = System.nanoTime(); add(new Sample(session.elapsedNanos(now) / 1_000_000_000.0, session.getChunksGenerated(),
            tps, memory, tickMillis, session.stageTotals(), null, null));
        session.getProfilerStatistics().timeline(System.nanoTime() - started);
        session.observeMemory(memory);
    }

    synchronized void event(ProfileSession session, long now, String event, String workerId) {
        boolean dropped = samples.size() == MAX_ROWS;
        long started = System.nanoTime(); add(new Sample(session.elapsedNanos(now) / 1_000_000_000.0, session.getChunksGenerated(),
            Double.NaN, -1, Double.NaN, session.stageTotals(), event, workerId));
        session.getProfilerStatistics().timeline(System.nanoTime() - started);
        if (dropped) session.getProfilerStatistics().droppedEvent();
    }
    private void add(Sample sample) { if (samples.size() == MAX_ROWS) samples.remove(0); samples.add(sample); }

    synchronized List<Sample> snapshot() { return new ArrayList<Sample>(samples); }
    synchronized long estimatedBytes() { return 64L + samples.size() * (128L + StageRegistry.stages().size() * 16L); }

    public static final class Sample {
        public final double timestampSeconds;
        public final long chunks;
        public final double tps;
        public final long memoryBytes;
        public final double tickMillis;
        public final Map<Stage, Long> stageTotals;
        public final String event, workerId;
        Sample(double timestamp, long chunks, double tps, long memory, double tick,
               Map<Stage, Long> totals, String event, String workerId) {
            timestampSeconds = timestamp; this.chunks = chunks; this.tps = tps;
            memoryBytes = memory; tickMillis = tick; stageTotals = new java.util.LinkedHashMap<Stage, Long>(totals);
            this.event = event; this.workerId = workerId;
        }
    }
}
