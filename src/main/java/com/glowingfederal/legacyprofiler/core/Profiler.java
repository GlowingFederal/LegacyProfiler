package com.glowingfederal.legacyprofiler.core;

import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;

/** Hot-path facade. When inactive, enter/exit are a volatile read and branch with no allocation. */
public final class Profiler {
    private static volatile ProfileSession active;
    private static final ThreadLocal<TimingStack> STACK = new ThreadLocal<TimingStack>() {
        @Override protected TimingStack initialValue() { return new TimingStack(); }
    };
    private static final ThreadLocal<ProfileSession> STACK_SESSION = new ThreadLocal<ProfileSession>();
    private static final ThreadLocal<ProfileSession.SampledTrace> TRACE = new ThreadLocal<ProfileSession.SampledTrace>();

    private static volatile String recordingMode = "aggregate";
    private static volatile int sampleInterval = 100;
    private Profiler() { }
    public static ProfileSession beginSession() { return ProfilerManager.start(); }
    public static ProfileSession beginSession(ProfileSessionInfo info) { return ProfilerManager.start(info); }
    public static ProfileSession beginGlobalSession(ProfileSessionInfo info) { return ProfilerManager.startGlobal(info); }
    public static ProfileSession beginConsumerSession(String consumerId, ProfileSessionInfo info) { return ProfilerManager.startConsumer(consumerId, info); }
    public static java.io.File endSession() throws java.io.IOException { return ProfilerManager.stop(); }
    public static void configureSampling(String mode, int interval) { recordingMode=mode; sampleInterval=Math.max(1,interval); }
    static String getRecordingMode(){return recordingMode;} static int getSampleInterval(){return sampleInterval;}
    public static void recordCounter(Stage stage){ increment(stage); }
    public static void registerWriter(ReportWriter writer){ ProfileWriter.register(writer); }
    static void setActive(ProfileSession session) { active = session; }
    public static ProfileSession getActiveSession() { return active; }

    public static void enter(Stage stage) {
        long overheadStarted = System.nanoTime();
        ProfileSession session = active;
        if (session == null || !session.isRecording() || !session.accepts(stage)) return;
        if (STACK_SESSION.get() != session) { STACK.get().clear(); STACK_SESSION.set(session); }
        STACK.get().enter(stage, System.nanoTime()); session.timingEntered();
        session.getProfilerStatistics().enter(System.nanoTime() - overheadStarted);
    }
    public static void exit(Stage stage) {
        long overheadStarted = System.nanoTime();
        ProfileSession session = active;
        if (session == null || !session.isRecording() || !session.accepts(stage)) return;
        ProfileSession stackSession = STACK_SESSION.get();
        if (stackSession != null && stackSession != session) { STACK.get().clear(); STACK_SESSION.set(session); return; }
        if (stackSession == null) STACK_SESSION.set(session);
        TimingStack.Completed completed = STACK.get().exit(stage, System.nanoTime());
        if (completed == null) session.timingError(); else session.timingExited();
        if (completed != null && session == active) {
            session.record(completed);
            ProfileSession.SampledTrace trace = TRACE.get();
            if (trace != null) trace.entries.add(new ProfileSession.TraceEntry(completed));
        }
        session.getProfilerStatistics().exit(System.nanoTime() - overheadStarted);
    }
    public static void increment(Stage stage) {
        ProfileSession session = active;
        if (session != null && session.isRecording() && session.accepts(stage)) session.increment(stage);
    }
    public static void recordValue(Stage stage, long nanos) {
        ProfileSession session = active;
        if (session != null && session.isRecording() && session.accepts(stage)) session.recordValue(stage, nanos);
    }
    public static void recordChunkGenerated() {
        ProfileSession session = active;
        if (session != null && session.isRecording()) session.chunkGenerated();
    }
    public static StageMetadata registerStage(StageMetadata metadata) {
        ProfileSession session = active; long started = System.nanoTime();
        StageMetadata result = StageRegistry.register(metadata);
        if (session != null) session.getProfilerStatistics().lookup(System.nanoTime() - started);
        return result;
    }
    public static StageMetadata registerStage(String consumerId, StageMetadata metadata) {
        ProfileSession session = active; long started = System.nanoTime();
        StageMetadata result = StageRegistry.register(consumerId, metadata);
        if (session != null) session.getProfilerStatistics().lookup(System.nanoTime() - started);
        return result;
    }
    public static void recordEvent(String event) { recordEvent(event, Thread.currentThread().getName()); }
    public static void recordEvent(String event, String workerId) {
        ProfileSession session = active;
        if (session != null) session.recordEvent(event, workerId);
    }
    /** Explicit sample controls are provided for adapters whose chunk boundary is not vanilla's generator call. */
    public static void beginSample(long chunkSequence) {
        ProfileSession session = active;
        if (session != null && session.isRecording() && "sampled".equals(session.getMode())) TRACE.set(new ProfileSession.SampledTrace(chunkSequence));
    }
    public static void endSample() {
        ProfileSession session = active; ProfileSession.SampledTrace trace = TRACE.get();
        if (session != null && trace != null) session.addTrace(trace);
        TRACE.remove();
    }
    public static void serverTick() {
        ProfileSession session = active;
        if (session != null && session.isRecording()) session.tick(System.nanoTime());
    }
    static void clearCurrentThread() { STACK.get().clear(); STACK_SESSION.remove(); TRACE.remove(); }
}
