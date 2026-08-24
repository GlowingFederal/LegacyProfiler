package com.glowingfederal.legacyprofiler.core;

/**
 * Immutable, data-driven description of a profiling stage.
 * Names must also satisfy {@link Stage}'s uppercase identifier contract. A parent is either
 * {@code null} or another registered stage name. Consumers should use a stable, namespaced name
 * and a non-null source (normally their mod ID); a null source falls back to {@code Adapter}.
 */
public final class StageMetadata {
    public final String name, parent, category, description, source;
    public final StageKind kind;
    public final boolean enabled;
    public final int displayOrder;

    public StageMetadata(String name, String parent, StageKind kind, String category, String description,
                         boolean enabled, String source, int displayOrder) {
        if (name == null || name.trim().length() == 0) throw new IllegalArgumentException("Stage name is required");
        if (kind == null) throw new IllegalArgumentException("Stage kind is required");
        this.name = name; this.parent = parent; this.kind = kind; this.category = category == null ? "Other" : category;
        this.description = description == null ? "" : description; this.enabled = enabled;
        this.source = source == null ? "Adapter" : source; this.displayOrder = displayOrder;
    }
}
