package com.glowingfederal.legacyprofiler.core;

import java.util.Locale;

final class Formatters {
    private Formatters() { }
    static String nanos(double value) {
        if (value < 1000) return number(value) + " ns";
        if (value < 1000000) return number(value / 1000) + " us";
        if (value < 1000000000) return number(value / 1000000) + " ms";
        return number(value / 1000000000) + " s";
    }
    static String bytes(long value) {
        if (value < 1024) return value + " bytes";
        if (value < 1048576) return String.format(Locale.ROOT, "%.2f KiB", value / 1024.0);
        if (value < 1073741824L) return String.format(Locale.ROOT, "%.2f MiB", value / 1048576.0);
        return String.format(Locale.ROOT, "%.2f GiB", value / 1073741824.0);
    }
    static String percent(long value, long total) { return percent(total == 0 ? 0 : value * 100.0 / total); }
    static String percent(double value) {
        if (value != 0 && (value < .01 || 100 - value < .01)) return String.format(Locale.ROOT, "%.5f%%", value);
        if (value < 1) return String.format(Locale.ROOT, "%.3f%%", value);
        return String.format(Locale.ROOT, "%.2f%%", value);
    }
    private static String number(double value) {
        double magnitude = Math.abs(value);
        int decimals = magnitude >= 100 ? 0 : magnitude >= 10 ? 1 : 2;
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}
