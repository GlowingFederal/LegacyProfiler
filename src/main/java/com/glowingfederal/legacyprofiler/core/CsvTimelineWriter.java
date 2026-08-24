package com.glowingfederal.legacyprofiler.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public final class CsvTimelineWriter implements ReportWriter {
    public String fileName(ProfileSession session) { return "timeline.csv"; }

    public void write(File file, ProfileSession session) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            out.write("timestamp_seconds,counter,tps,memory_bytes,tick_ms,event,worker_id,record_type,profile_source,profile_purpose,profile_scope,profile_consumer_id\n");
            out.write("0.000,0,,,,,,session_metadata," + csv(session.getSessionInfo().getSource()) + ","
                + csv(session.getSessionInfo().getPurpose()) + "," + csv(session.getScope().name()) + "," + csv(session.getConsumerId()) + "\n");
            for (TimelineRecorder.Sample sample : session.getTimeline().snapshot()) {
                String recordType = sample.event == null ? "sample" : "event";
                out.write(String.format(Locale.ROOT, "%.3f,%d,%s,%s,%s,%s,%s,%s,,,,%n",
                    sample.timestampSeconds, sample.chunks, number(sample.tps), number(sample.memoryBytes),
                    number(sample.tickMillis), csv(sample.event), csv(sample.workerId), recordType));
            }
        } finally { out.close(); }
    }

    private static String number(double value) { return Double.isNaN(value) ? "" : Double.toString(value); }
    private static String number(long value) { return value < 0 ? "" : Long.toString(value); }
    private static String csv(String value) {
        return value == null ? "" : "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
