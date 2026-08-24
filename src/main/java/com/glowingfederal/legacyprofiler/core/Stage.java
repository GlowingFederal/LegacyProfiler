package com.glowingfederal.legacyprofiler.core;

/** Immutable stage identifier. Instances are created by the public registration API. */
public final class Stage implements Comparable<Stage> {
    private final String name;
    private final String consumerId;

    public Stage(String name) {
        this(name, null);
    }

    Stage(String name, String consumerId) {
        if (name == null || !name.matches("[a-z0-9][a-z0-9_-]*(\\.[a-z0-9][a-z0-9_-]*)*")) {
            throw new IllegalArgumentException("Invalid stage identifier: " + name);
        }
        this.name = name;
        this.consumerId = consumerId;
    }

    public String name() { return name; }

    /** Returns the explicit owner assigned at registration, or null for an unregistered handle. */
    public String consumerId() { return consumerId; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object value) {
        return value instanceof Stage && name.equals(((Stage) value).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public int compareTo(Stage other) {
        return name.compareTo(other.name);
    }
}
