package com.glowingfederal.legacyprofiler.api;

import com.glowingfederal.legacyprofiler.core.ProfileSession;
import com.glowingfederal.legacyprofiler.core.ReportWriter;
import com.glowingfederal.legacyprofiler.core.Stage;
import com.glowingfederal.legacyprofiler.core.StageMetadata;
import com.glowingfederal.legacyprofiler.core.StageRegistry;
import com.glowingfederal.legacyprofiler.extension.Extensions;

import java.io.File;
import java.io.IOException;

/**
 * Stable v1 entry point for integrations.
 *
 * <p>Register stages before starting a session and cache their immutable {@link Stage} handles.
 * Recording methods are safe for concurrent producer threads. When no session is recording,
 * timing and counter calls return without recording data; inactive timing calls allocate no
 * objects. Session lifecycle should be controlled by one host thread.</p>
 */
public final class Profiler {
    /** Library implementation version represented by this source release. */
    public static final String VERSION = "1.0.0";
    /** Public facade contract version. Compatible additions retain the same major version. */
    public static final String API_VERSION = "1.0";

    static { Extensions.load(); }

    private Profiler() { }

    /** Starts a session attributed to {@code Unknown}. Prefer the attributed overload. */
    public static ProfileSession beginSession() {
        return com.glowingfederal.legacyprofiler.core.Profiler.beginSession();
    }

    /**
     * Starts a session. A currently active session is replaced without writing its report.
     * @param info non-null consumer attribution
     */
    public static ProfileSession beginSession(ProfileSessionInfo info) {
        return com.glowingfederal.legacyprofiler.core.Profiler.beginSession(info);
    }

    /**
     * Stops and writes the active session.
     * @return its output directory, or {@code null} if no session was active
     * @throws IOException if an output writer fails
     */
    public static File endSession() throws IOException {
        return com.glowingfederal.legacyprofiler.core.Profiler.endSession();
    }

    /** Enters a timing stage on the calling thread. Calls are ignored while inactive. */
    public static void enter(Stage stage) {
        com.glowingfederal.legacyprofiler.core.Profiler.enter(stage);
    }

    /**
     * Exits the expected stage on the calling thread. A mismatch discards that thread's timing
     * stack and is reported as a validation warning rather than throwing.
     */
    public static void exit(Stage stage) {
        com.glowingfederal.legacyprofiler.core.Profiler.exit(stage);
    }

    /** Increments a registered counter stage once. Calls are ignored while inactive. */
    public static void recordCounter(Stage stage) {
        com.glowingfederal.legacyprofiler.core.Profiler.recordCounter(stage);
    }

    /** Records a non-null, non-empty event with the current thread name as worker identity. */
    public static void recordEvent(String event) {
        com.glowingfederal.legacyprofiler.core.Profiler.recordEvent(event);
    }

    /**
     * Registers immutable metadata exactly once for a globally unique stage name.
     * Registration is synchronized and duplicate names throw {@link IllegalArgumentException}.
     * Register all stages before a session starts; sessions snapshot the registered stage set.
     */
    public static StageMetadata registerStage(StageMetadata metadata) {
        if (metadata == null) throw new IllegalArgumentException("metadata must not be null");
        return com.glowingfederal.legacyprofiler.core.Profiler.registerStage(metadata);
    }

    /**
     * Returns the immutable handle for a registered stage, suitable for permanent caching.
     * @throws IllegalArgumentException when the name has not been registered
     */
    public static Stage stage(String name) {
        return StageRegistry.stage(name);
    }

    /** Registers a process-wide writer. Writers run synchronously when a session ends. */
    public static void registerWriter(ReportWriter writer) {
        if (writer == null) throw new IllegalArgumentException("writer must not be null");
        com.glowingfederal.legacyprofiler.core.Profiler.registerWriter(writer);
    }

    /**
     * Selects {@code aggregate} or {@code sampled} recording for subsequently observed work.
     * The interval is clamped to at least one; a host still owns sample boundaries.
     */
    public static void configureSampling(String mode, int interval) {
        com.glowingfederal.legacyprofiler.core.Profiler.configureSampling(mode, interval);
    }

    /** Host-adapter progress hook; not a general-purpose consumer counter. */
    public static void recordChunkGenerated() {
        com.glowingfederal.legacyprofiler.core.Profiler.recordChunkGenerated();
    }

    /** Host-adapter server tick hook used for timeline sampling. */
    public static void serverTick() {
        com.glowingfederal.legacyprofiler.core.Profiler.serverTick();
    }
}
