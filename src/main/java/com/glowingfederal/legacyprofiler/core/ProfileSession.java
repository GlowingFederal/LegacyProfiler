package com.glowingfederal.legacyprofiler.core;


import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;
import com.glowingfederal.legacyprofiler.api.ProfileScope;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.List;

public final class ProfileSession {
    public enum State { RUNNING, PAUSED, STOPPED }
    private final Date startedAt = new Date();
    private final long startedNanos = System.nanoTime();
    private final File outputDirectory;
    private final ProfileSessionInfo sessionInfo;
    private final ProfileScope scope;
    private final String consumerId;
    private final ProfileMetadata metadata = new ProfileMetadata();
    private final Map<Stage, StageStatistics> statistics = new LinkedHashMap<Stage, StageStatistics>();
    private final TimelineRecorder timeline = new TimelineRecorder();
    private final ProfilerStatistics profilerStatistics = new ProfilerStatistics();
    private final AtomicLong chunks = new AtomicLong();
    private volatile State state = State.RUNNING;
    private volatile long pausedAt, pausedNanos, endedNanos;
    private volatile long peakMemory, memoryTotal, memorySamples;
    private final List<SampledTrace> traces = new ArrayList<SampledTrace>();
    private final AtomicLong timingErrors = new AtomicLong();
    private final AtomicLong openTimings = new AtomicLong();
    private static final int MAX_TRACES = 4096;

    ProfileSession(File outputDirectory, ProfileSessionInfo sessionInfo, ProfileScope scope, String consumerId) {
        this.outputDirectory = outputDirectory;
        if (sessionInfo == null) throw new IllegalArgumentException("sessionInfo must not be null");
        this.sessionInfo = sessionInfo;
        if (scope == null) throw new IllegalArgumentException("scope must not be null");
        if (scope == ProfileScope.CONSUMER) StageRegistry.validateConsumerId(consumerId);
        if (scope == ProfileScope.GLOBAL && consumerId != null) throw new IllegalArgumentException("global scope must not have a consumer ID");
        this.scope = scope;
        this.consumerId = consumerId;
        for (Stage stage : StageRegistry.stages()) if (accepts(stage)) statistics.put(stage, new StageStatistics());
    }

    void record(TimingStack.Completed completed) { statistics.get(completed.stage).record(completed.inclusiveNanos, completed.exclusiveNanos); }
    void increment(Stage stage) { statistics.get(stage).record(0, 0); }
    void recordValue(Stage stage, long nanos) { statistics.get(stage).record(Math.max(0, nanos), Math.max(0, nanos)); }
    void chunkGenerated() { long count = chunks.incrementAndGet(); if (count % 500 == 0) recordEvent("Generated " + count + " Chunks", null); }
    void tick(long now) { timeline.serverTick(now); timeline.maybeSample(this, now); }

    synchronized boolean pause(long now) {
        if (state != State.RUNNING) return false;
        state = State.PAUSED; pausedAt = now; return true;
    }
    synchronized boolean resume(long now) {
        if (state != State.PAUSED) return false;
        pausedNanos += now - pausedAt; pausedAt = 0; state = State.RUNNING; return true;
    }
    synchronized void stop(long now) {
        if (state == State.PAUSED) pausedNanos += now - pausedAt;
        endedNanos = now; state = State.STOPPED;
    }

    public boolean isRecording() { return state == State.RUNNING; }
    public State getState() { return state; }
    public Date getStartedAt() { return new Date(startedAt.getTime()); }
    public Date getEndedAt() { return new Date(startedAt.getTime() + elapsedNanos(System.nanoTime()) / 1_000_000L); }
    public File getOutputDirectory() { return outputDirectory; }
    public ProfileSessionInfo getSessionInfo() { return sessionInfo; }
    public ProfileScope getScope() { return scope; }
    public String getConsumerId() { return consumerId; }
    public boolean accepts(Stage stage) { return stage != null && (scope == ProfileScope.GLOBAL || consumerId.equals(stage.consumerId())); }
    public ProfileMetadata getMetadata() { return metadata; }
    public long getChunksGenerated() { return chunks.get(); }
    public TimelineRecorder getTimeline() { return timeline; }
    public ProfilerStatistics getProfilerStatistics() { return profilerStatistics; }
    public Map<Stage, StageStatistics> getStatistics() { return statistics; }
    public long elapsedNanos(long now) {
        long end = endedNanos != 0 ? endedNanos : (state == State.PAUSED ? pausedAt : now);
        return Math.max(0, end - startedNanos - pausedNanos);
    }
    public long estimatedBytes() {
        long bytes = 512 + timeline.estimatedBytes();
        for (StageStatistics value : statistics.values()) bytes += value.estimatedBytes();
        synchronized (this) { for (SampledTrace trace : traces) bytes += 64L + trace.entries.size() * 40L; }
        return bytes;
    }
    Map<Stage, Long> stageTotals() {
        Map<Stage, Long> totals = new LinkedHashMap<Stage, Long>();
        for (Map.Entry<Stage, StageStatistics> entry : statistics.entrySet())
            totals.put(entry.getKey(), entry.getValue().snapshot().inclusiveNanos);
        return totals;
    }
    synchronized void observeMemory(long bytes) { peakMemory = Math.max(peakMemory, bytes); memoryTotal += bytes; memorySamples++; }
    public long getPeakMemory() { return peakMemory; }
    public long getAverageMemory() { return memorySamples == 0 ? 0 : memoryTotal / memorySamples; }
    public String getMode() { return Profiler.getRecordingMode(); }
    boolean shouldSampleNextChunk() { return "sampled".equals(getMode()) && (chunks.get() + 1) % Math.max(1, Profiler.getSampleInterval()) == 0; }
    synchronized void addTrace(SampledTrace trace) { if (traces.size() == MAX_TRACES) { traces.remove(0); profilerStatistics.droppedTrace(); } traces.add(trace); }
    public synchronized List<SampledTrace> getTraces() { return new ArrayList<SampledTrace>(traces); }
    void timingError() { timingErrors.incrementAndGet(); }
    void timingEntered() { openTimings.incrementAndGet(); }
    void timingExited() { openTimings.decrementAndGet(); }
    public long getTimingErrors() { return timingErrors.get(); }
    public long getOpenTimings() { return openTimings.get(); }
    public void recordEvent(String event, String workerId) { if (event != null && event.length() > 0) timeline.event(this, System.nanoTime(), event, workerId); }
    public static final class SampledTrace {
        public final long chunkSequence;
        public final String workerId;
        public final List<TraceEntry> entries = new ArrayList<TraceEntry>();
        SampledTrace(long sequence) { chunkSequence = sequence; workerId = Thread.currentThread().getName(); }
    }
    public static final class TraceEntry {
        public final Stage stage; public final long inclusiveNanos, exclusiveNanos;
        TraceEntry(TimingStack.Completed value) { stage=value.stage; inclusiveNanos=value.inclusiveNanos; exclusiveNanos=value.exclusiveNanos; }
    }
}
