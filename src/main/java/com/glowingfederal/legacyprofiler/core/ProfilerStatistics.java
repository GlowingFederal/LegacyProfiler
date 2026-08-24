package com.glowingfederal.legacyprofiler.core;

import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe measurements of profiler cost and bounded-data loss. */
public final class ProfilerStatistics {
    private final AtomicLong enterNanos = new AtomicLong(), enters = new AtomicLong();
    private final AtomicLong exitNanos = new AtomicLong(), exits = new AtomicLong();
    private final AtomicLong lookupNanos = new AtomicLong(), lookups = new AtomicLong();
    private final AtomicLong timelineNanos = new AtomicLong(), jsonNanos = new AtomicLong(), reportNanos = new AtomicLong();
    private final AtomicLong allocations = new AtomicLong(), droppedEvents = new AtomicLong(), droppedTraces = new AtomicLong();

    void enter(long nanos) { enterNanos.addAndGet(nanos); enters.incrementAndGet(); allocations.incrementAndGet(); }
    void exit(long nanos) { exitNanos.addAndGet(nanos); exits.incrementAndGet(); }
    void lookup(long nanos) { lookupNanos.addAndGet(nanos); lookups.incrementAndGet(); }
    void timeline(long nanos) { timelineNanos.addAndGet(nanos); }
    void json(long nanos) { jsonNanos.set(nanos); }
    void report(long nanos) { reportNanos.set(nanos); }
    void droppedEvent() { droppedEvents.incrementAndGet(); }
    void droppedTrace() { droppedTraces.incrementAndGet(); }
    public long averageEnterNanos() { return average(enterNanos, enters); }
    public long averageExitNanos() { return average(exitNanos, exits); }
    public long averageLookupNanos() { return average(lookupNanos, lookups); }
    public long timelineWriteNanos() { return timelineNanos.get(); }
    public long jsonSerializationNanos() { return jsonNanos.get(); }
    public long reportGenerationNanos() { return reportNanos.get(); }
    public long allocations() { return allocations.get(); }
    public long droppedTimelineEvents() { return droppedEvents.get(); }
    public long droppedSampledTraces() { return droppedTraces.get(); }
    private static long average(AtomicLong total, AtomicLong count) { long n = count.get(); return n == 0 ? 0 : total.get() / n; }
}
