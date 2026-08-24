package com.glowingfederal.legacyprofiler.core;
import java.io.File;
import java.io.IOException;
public interface ReportWriter {
    String fileName(ProfileSession session);
    void write(File file, ProfileSession session) throws IOException;
}
