package com.glowingfederal.legacyprofiler.api;

/** Identifies which registered stage owners a process-wide profiling session accepts. */
public enum ProfileScope {
    /** Records stages owned by every registered consumer. */
    GLOBAL,
    /** Records only stages owned by one specific consumer. */
    CONSUMER
}
