package com.glowingfederal.legacyprofiler.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Objective pre-write consistency checks; warnings never prevent salvageable reports. */
public final class ProfileValidator {
    private ProfileValidator() { }
    public static List<String> validate(ProfileSession session) {
        List<String> warnings = new ArrayList<String>();
        if (session.getTimingErrors() != 0) warnings.add(session.getTimingErrors() + " unbalanced or mismatched timing stack exits");
        if (session.getOpenTimings() != 0) warnings.add(session.getOpenTimings() + " timing stack entries remained open");
        Set<String> names = new HashSet<String>();
        for (StageMetadata stage : StageRegistry.snapshot()) names.add(stage.name);
        for (StageMetadata stage : StageRegistry.snapshot()) {
            if (stage.parent != null && !names.contains(stage.parent)) warnings.add("Missing parent " + stage.parent + " for " + stage.name);
            Set<String> visited = new HashSet<String>(); StageMetadata cursor = stage;
            while (cursor != null && cursor.parent != null) {
                if (!visited.add(cursor.name)) { warnings.add("Hierarchy cycle at " + stage.name); break; }
                cursor = StageRegistry.get(cursor.parent);
            }
        }
        for (StageStatistics value : session.getStatistics().values()) {
            StageStatistics.Snapshot s = value.snapshot();
            if (s.inclusiveNanos < 0 || s.exclusiveNanos < 0 || s.minimumNanos < 0) warnings.add("Negative timing aggregate detected");
            if (s.exclusiveNanos > s.inclusiveNanos) warnings.add("Exclusive timing exceeds inclusive timing");
            if (s.calls == 0 && (s.inclusiveNanos != 0 || s.exclusiveNanos != 0)) warnings.add("Non-zero total with zero calls");
        }
        return warnings;
    }
}
