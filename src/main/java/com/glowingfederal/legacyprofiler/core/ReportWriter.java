package com.glowingfederal.legacyprofiler.core;
import java.io.File;
import java.io.IOException;
/**
 * Synchronous report extension. Implementations own their file content, must return a safe file
 * name, and should not mutate the supplied stopped session. An {@link IOException} aborts the
 * remaining end-session write operation and is propagated to the host.
 */
public interface ReportWriter {
    String fileName(ProfileSession session);
    void write(File file, ProfileSession session) throws IOException;
}
