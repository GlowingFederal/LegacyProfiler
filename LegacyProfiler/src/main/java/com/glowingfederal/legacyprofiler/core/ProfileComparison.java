package com.glowingfederal.legacyprofiler.core;

/** Extension point for a future stable-identifier session comparison implementation. */
public interface ProfileComparison {
    ComparisonResult compare(ProfileSession baseline, ProfileSession candidate);
}
