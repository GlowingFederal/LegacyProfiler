package com.glowingfederal.legacyprofiler.core;

import java.io.File;
import java.io.IOException;

/** Output extension point for comparison results; no built-in implementation exists in v1.0. */
public interface ComparisonWriter {
    void write(File file, ComparisonResult result) throws IOException;
}
