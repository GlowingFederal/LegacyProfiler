package com.glowingfederal.legacyprofiler.api;

/** Immutable identity of the consumer that requested a profiling session. */
public final class ProfileSessionInfo {
    private static final ProfileSessionInfo UNKNOWN = builder().source("Unknown").build();

    private final String source;
    private final String displayName;
    private final String purpose;

    private ProfileSessionInfo(Builder builder) {
        source = required(builder.source, "source");
        displayName = optional(builder.displayName);
        purpose = optional(builder.purpose);
    }

    /** Deterministic attribution used by compatibility entry points that provide no consumer. */
    public static ProfileSessionInfo unknown() { return UNKNOWN; }
    public static Builder builder() { return new Builder(); }
    public String getSource() { return source; }
    public String getDisplayName() { return displayName; }
    public String getPurpose() { return purpose; }

    private static String required(String value, String name) {
        String normalized = optional(value);
        if (normalized == null) throw new IllegalArgumentException(name + " must not be null or blank");
        return normalized;
    }

    private static String optional(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.length() == 0 ? null : normalized;
    }

    public static final class Builder {
        private String source;
        private String displayName;
        private String purpose;
        private Builder() { }
        public Builder source(String value) { source = value; return this; }
        public Builder displayName(String value) { displayName = value; return this; }
        public Builder purpose(String value) { purpose = value; return this; }
        public ProfileSessionInfo build() { return new ProfileSessionInfo(this); }
    }
}
