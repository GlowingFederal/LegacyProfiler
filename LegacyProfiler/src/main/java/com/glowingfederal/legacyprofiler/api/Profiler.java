package com.glowingfederal.legacyprofiler.api;
import com.glowingfederal.legacyprofiler.core.*; import com.glowingfederal.legacyprofiler.extension.Extensions; import java.io.*;
/** Frozen v1 facade; consumers require no access to profiler implementation details. */
public final class Profiler { static { Extensions.load(); } private Profiler(){}
 public static ProfileSession beginSession(){return com.glowingfederal.legacyprofiler.core.Profiler.beginSession();}
 public static ProfileSession beginSession(ProfileSessionInfo info){return com.glowingfederal.legacyprofiler.core.Profiler.beginSession(info);}
 public static File endSession()throws IOException{return com.glowingfederal.legacyprofiler.core.Profiler.endSession();}
 public static void enter(Stage s){com.glowingfederal.legacyprofiler.core.Profiler.enter(s);} public static void exit(Stage s){com.glowingfederal.legacyprofiler.core.Profiler.exit(s);}
 public static void recordCounter(Stage s){com.glowingfederal.legacyprofiler.core.Profiler.recordCounter(s);} public static void recordEvent(String e){com.glowingfederal.legacyprofiler.core.Profiler.recordEvent(e);}
 public static StageMetadata registerStage(StageMetadata s){return com.glowingfederal.legacyprofiler.core.Profiler.registerStage(s);} public static void registerWriter(ReportWriter w){com.glowingfederal.legacyprofiler.core.Profiler.registerWriter(w);}
 public static void configureSampling(String m,int n){com.glowingfederal.legacyprofiler.core.Profiler.configureSampling(m,n);} public static void recordChunkGenerated(){com.glowingfederal.legacyprofiler.core.Profiler.recordChunkGenerated();} public static void serverTick(){com.glowingfederal.legacyprofiler.core.Profiler.serverTick();}
}
