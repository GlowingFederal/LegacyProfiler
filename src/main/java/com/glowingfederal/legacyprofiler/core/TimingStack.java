package com.glowingfederal.legacyprofiler.core;

import java.util.ArrayDeque;
import java.util.Deque;

/** Per-thread nested timing state. Child elapsed time is charged against its parent. */
final class TimingStack {
    private final Deque<Frame> frames = new ArrayDeque<Frame>();

    void enter(Stage stage, long now) { frames.push(new Frame(stage, now)); }

    Completed exit(Stage expected, long now) {
        if (frames.isEmpty()) return null;
        Frame frame = frames.pop();
        if (frame.stage != expected) {
            frames.clear(); // A broken hook must not poison every subsequent measurement on this thread.
            return null;
        }
        long inclusive = Math.max(0L, now - frame.started);
        if (!frames.isEmpty()) frames.peek().childNanos += inclusive;
        return new Completed(frame.stage, inclusive, Math.max(0L, inclusive - frame.childNanos));
    }

    void clear() { frames.clear(); }

    private static final class Frame {
        final Stage stage;
        final long started;
        long childNanos;
        Frame(Stage stage, long started) { this.stage = stage; this.started = started; }
    }

    static final class Completed {
        final Stage stage;
        final long inclusiveNanos;
        final long exclusiveNanos;
        Completed(Stage stage, long inclusiveNanos, long exclusiveNanos) {
            this.stage = stage; this.inclusiveNanos = inclusiveNanos; this.exclusiveNanos = exclusiveNanos;
        }
    }
}
