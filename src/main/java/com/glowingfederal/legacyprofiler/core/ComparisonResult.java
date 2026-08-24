package com.glowingfederal.legacyprofiler.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable container reserved for future comparison metrics. */
public final class ComparisonResult {
    private final Map<String, Object> extensions;
    public ComparisonResult(Map<String, Object> extensions) {
        this.extensions = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(extensions));
    }
    public Map<String, Object> getExtensions() { return extensions; }
}
