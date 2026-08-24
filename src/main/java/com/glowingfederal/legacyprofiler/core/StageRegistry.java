package com.glowingfederal.legacyprofiler.core;
import java.util.*;
/**
 * Process-wide copy-on-write registry with no built-in domain stages.
 * Reads are thread-safe and registration is synchronized. Registration is permanent for the
 * process, rejects duplicate names, and should finish before any session begins because each
 * session snapshots the current stage set. Returned stage handles are immutable and cacheable.
 */
public final class StageRegistry {
 private static volatile Map<String,StageMetadata> metadata=Collections.emptyMap();
 private static volatile Map<String,Stage> stages=Collections.emptyMap();
 private StageRegistry() {}
 public static synchronized StageMetadata register(StageMetadata value) {
  if(metadata.containsKey(value.name)) throw new IllegalArgumentException("Stage already registered: "+value.name);
  Map<String,StageMetadata> m=new LinkedHashMap<String,StageMetadata>(metadata); m.put(value.name,value); metadata=Collections.unmodifiableMap(m);
  Map<String,Stage> s=new LinkedHashMap<String,Stage>(stages); s.put(value.name,new Stage(value.name)); stages=Collections.unmodifiableMap(s); return value;
 }
 public static StageMetadata get(String name){return metadata.get(name);}
 public static Stage stage(String name){Stage s=stages.get(name); if(s==null) throw new IllegalArgumentException("Unknown stage: "+name); return s;}
 public static List<StageMetadata> snapshot(){return new ArrayList<StageMetadata>(metadata.values());}
 public static List<Stage> stages(){return new ArrayList<Stage>(stages.values());}
}
