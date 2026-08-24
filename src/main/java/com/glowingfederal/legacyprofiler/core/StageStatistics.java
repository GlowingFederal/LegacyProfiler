package com.glowingfederal.legacyprofiler.core;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

/** Bounded, merge-safe aggregates. Percentiles use reservoir sampling to keep hour-long runs bounded. */
public final class StageStatistics {
    private static final int RESERVOIR_SIZE = 8192;
    private static final int LONGEST_SIZE = 25;
    private long calls, inclusiveTotal, exclusiveTotal, minimum = Long.MAX_VALUE, maximum;
    private double mean, m2;
    private final long[] reservoir = new long[RESERVOIR_SIZE];
    private int reservoirCount;
    private final PriorityQueue<Long> longest = new PriorityQueue<Long>(LONGEST_SIZE);
    private final Random random = new Random(0x4d4347L);

    synchronized void record(long inclusive, long exclusive) {
        calls++;
        inclusiveTotal += inclusive;
        exclusiveTotal += exclusive;
        minimum = Math.min(minimum, inclusive);
        maximum = Math.max(maximum, inclusive);
        double delta = inclusive - mean;
        mean += delta / calls;
        m2 += delta * (inclusive - mean);
        if (reservoirCount < reservoir.length) reservoir[reservoirCount++] = inclusive;
        else {
            long candidate = nextLong(calls);
            if (candidate < reservoir.length) reservoir[(int) candidate] = inclusive;
        }
        if (longest.size() < LONGEST_SIZE) longest.add(inclusive);
        else if (inclusive > longest.peek()) { longest.poll(); longest.add(inclusive); }
    }

    public synchronized Snapshot snapshot() {
        long[] samples = Arrays.copyOf(reservoir, reservoirCount);
        Arrays.sort(samples);
        Long[] top = longest.toArray(new Long[longest.size()]);
        Arrays.sort(top, java.util.Collections.reverseOrder());
        return new Snapshot(calls, inclusiveTotal, exclusiveTotal, calls == 0 ? 0 : minimum, maximum,
            mean, calls < 2 ? 0 : Math.sqrt(m2 / (calls - 1)), percentile(samples, .50),
            percentile(samples, .95), percentile(samples, .99), top, histogram(samples));
    }

    synchronized long estimatedBytes() { return 128L + reservoir.length * 8L + longest.size() * 24L; }

    private long nextLong(long bound) {
        long bits, value;
        do { bits = random.nextLong() & Long.MAX_VALUE; value = bits % bound; } while (bits - value + bound - 1 < 0L);
        return value;
    }

    private static long percentile(long[] values, double p) {
        if (values.length == 0) return 0;
        return values[(int) Math.ceil(p * values.length) - 1];
    }

    private static long[] histogram(long[] values) {
        long[] buckets = new long[6];
        for (long value : values) {
            if (value < 1000000L) buckets[0]++; else if (value < 2000000L) buckets[1]++;
            else if (value < 3000000L) buckets[2]++; else if (value < 5000000L) buckets[3]++;
            else if (value < 10000000L) buckets[4]++; else buckets[5]++;
        }
        return buckets;
    }

    public static final class Snapshot {
        public final long calls, inclusiveNanos, exclusiveNanos, minimumNanos, maximumNanos;
        public final double averageNanos, standardDeviationNanos;
        public final long medianNanos, p95Nanos, p99Nanos;
        public final Long[] longestNanos;
        public final long[] histogram;
        public final double coefficientOfVariation, spikeRatio;
        Snapshot(long calls, long inclusive, long exclusive, long minimum, long maximum, double average,
                 double deviation, long median, long p95, long p99, Long[] longest, long[] histogram) {
            this.calls = calls; this.inclusiveNanos = inclusive; this.exclusiveNanos = exclusive;
            this.minimumNanos = minimum; this.maximumNanos = maximum; this.averageNanos = average;
            this.standardDeviationNanos = deviation; this.medianNanos = median; this.p95Nanos = p95;
            this.p99Nanos = p99; this.longestNanos = longest; this.histogram = histogram;
            this.coefficientOfVariation = average == 0 ? 0 : deviation / average;
            this.spikeRatio = median == 0 ? (maximum == 0 ? 0 : Double.POSITIVE_INFINITY) : maximum / (double) median;
        }
    }
}
