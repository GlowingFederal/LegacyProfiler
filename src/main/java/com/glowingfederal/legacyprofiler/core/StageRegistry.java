package com.glowingfederal.legacyprofiler.core;
import java.util.*;
/**
 * Process-wide copy-on-write registry with no built-in domain stages.
 * Reads are thread-safe and registration is synchronized. Registration is permanent for the
 * process, rejects duplicate names, and should finish before any session begins because each
 * session snapshots the current stage set. Returned stage handles are immutable and cacheable.
 */
public final class StageRegistry {
 public static final String LEGACY_CONSUMER_ID="legacy";
 private static final String CONSUMER_PATTERN="[a-z0-9][a-z0-9_-]*";
 private static volatile Map<String,StageMetadata> metadata=Collections.emptyMap();
 private static volatile Map<String,Stage> stages=Collections.emptyMap();
 private StageRegistry() {}
 public static synchronized StageMetadata register(StageMetadata value) {
  return register(legacyConsumerId(value),value);
 }
 public static synchronized StageMetadata register(String consumerId,StageMetadata value) {
  consumerId=validateConsumerId(consumerId);
  if(value==null)throw new IllegalArgumentException("metadata must not be null");
  if(metadata.containsKey(value.name)) throw new IllegalArgumentException("Stage already registered: "+value.name);
  Map<String,StageMetadata> m=new LinkedHashMap<String,StageMetadata>(metadata); m.put(value.name,value); metadata=Collections.unmodifiableMap(m);
  Map<String,Stage> s=new LinkedHashMap<String,Stage>(stages); s.put(value.name,new Stage(value.name,consumerId)); stages=Collections.unmodifiableMap(s); return value;
 }
 public static String validateConsumerId(String value){if(value==null||!value.matches(CONSUMER_PATTERN))throw new IllegalArgumentException("Invalid consumer ID: "+value);return value;}
 private static String legacyConsumerId(StageMetadata value){
  if(value==null)throw new IllegalArgumentException("metadata must not be null");
  String candidate=value.source==null?null:value.source.trim().toLowerCase(Locale.ROOT);
  return candidate!=null&&candidate.matches(CONSUMER_PATTERN)?candidate:LEGACY_CONSUMER_ID;
 }
 public static StageMetadata get(String name){return metadata.get(name);}
 public static Stage stage(String name){Stage s=stages.get(name); if(s==null) throw new IllegalArgumentException("Unknown stage: "+name); return s;}
 public static List<StageMetadata> snapshot(){return new ArrayList<StageMetadata>(metadata.values());}
 public static List<Stage> stages(){return new ArrayList<Stage>(stages.values());}
}
