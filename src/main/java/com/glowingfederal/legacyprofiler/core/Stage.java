package com.glowingfederal.legacyprofiler.core;

/** Immutable stage identifier. Instances are created by the public registration API. */
public final class Stage implements Comparable<Stage> {
    private final String name;
    public Stage(String name) {
        if (name == null || !name.matches("[A-Z][A-Z0-9_]*")) throw new IllegalArgumentException("Invalid stage identifier: " + name);
        this.name = name;
    }
    public String name() { return name; }
    @Override public String toString() { return name; }
    @Override public boolean equals(Object value) { return value instanceof Stage && name.equals(((Stage) value).name); }
    @Override public int hashCode() { return name.hashCode(); }
    public int compareTo(Stage other) { return name.compareTo(other.name); }
}
