package com.glowingfederal.legacyprofiler.core;
import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;
import java.io.*; import java.text.*; import java.util.*;
/** Domain-neutral session lifecycle. Hosts may choose the output root. */
public final class ProfilerManager {
 private static ProfileSession session; private static File outputRoot=new File("logs/legacyprofiler"); private ProfilerManager(){}
 public static synchronized void setOutputRoot(File root){if(session!=null)throw new IllegalStateException("session active"); outputRoot=root;}
 public static synchronized ProfileSession start(){return start(ProfileSessionInfo.unknown());}
 public static synchronized ProfileSession start(ProfileSessionInfo info){return start(new File(outputRoot,"profile-"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",Locale.ROOT).format(new Date())),info);}
 public static synchronized ProfileSession start(File directory){return start(directory,ProfileSessionInfo.unknown());}
 public static synchronized ProfileSession start(File directory,ProfileSessionInfo info){if(directory==null)throw new IllegalArgumentException("directory must not be null");if(info==null)throw new IllegalArgumentException("sessionInfo must not be null");if(session!=null)Profiler.setActive(null); session=new ProfileSession(directory,info);Profiler.setActive(session);session.recordEvent("Profiler Started",null);return session;}
 public static synchronized File stop() throws IOException{if(session==null)return null;ProfileSession done=session;done.recordEvent("Profiler Stopped",null);Profiler.setActive(null);done.stop(System.nanoTime());new ProfileWriter().write(done);session=null;return done.getOutputDirectory();}
 public static synchronized boolean pause(){boolean c=session!=null&&session.pause(System.nanoTime());if(c)session.recordEvent("Profiler Paused",null);return c;}
 public static synchronized boolean resume(){boolean c=session!=null&&session.resume(System.nanoTime());if(c)session.recordEvent("Profiler Resumed",null);return c;}
 public static synchronized ProfileSession reset(){if(session==null)return null;File d=session.getOutputDirectory();ProfileSessionInfo info=session.getSessionInfo();Profiler.setActive(null);session=new ProfileSession(d,info);Profiler.setActive(session);Profiler.clearCurrentThread();return session;}
 public static synchronized ProfileSession status(){return session;}
}
