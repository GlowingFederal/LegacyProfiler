package com.glowingfederal.legacyprofiler.extension;
import com.glowingfederal.legacyprofiler.core.*; import java.util.*;
/** Loads optional providers using Java's standard service-provider mechanism. */
public final class Extensions { private static boolean loaded; private Extensions(){}
 public static synchronized void load(){if(loaded)return;loaded=true;
  for(ProfilerPlugin p:ServiceLoader.load(ProfilerPlugin.class))p.initialize();
  for(StageProvider p:ServiceLoader.load(StageProvider.class))for(StageMetadata s:p.stages())Profiler.registerStage(s);
  for(ReportProvider p:ServiceLoader.load(ReportProvider.class))for(ReportWriter w:p.writers())Profiler.registerWriter(w);
 }
}
