package com.glowingfederal.legacyprofiler.core;
import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;
import com.glowingfederal.legacyprofiler.api.ProfileScope;
import java.io.*; import java.text.*; import java.util.*;
/** Domain-neutral session lifecycle. Hosts may choose the output root. */
public final class ProfilerManager {
 private static ProfileSession session; private static File outputRoot=new File("logs/legacyprofiler"); private ProfilerManager(){}
 public static synchronized void setOutputRoot(File root){if(root==null)throw new IllegalArgumentException("root must not be null");if(session!=null)throw new IllegalStateException("session active"); outputRoot=root;}
 public static synchronized ProfileSession start(){return startGlobal(ProfileSessionInfo.unknown());}
 public static synchronized ProfileSession start(ProfileSessionInfo info){return startGlobal(info);}
 public static synchronized ProfileSession startGlobal(ProfileSessionInfo info){return start(newDirectory(),info,ProfileScope.GLOBAL,null);}
 public static synchronized ProfileSession startConsumer(String consumerId,ProfileSessionInfo info){return start(newDirectory(),info,ProfileScope.CONSUMER,consumerId);}
 public static synchronized ProfileSession start(File directory){return start(directory,ProfileSessionInfo.unknown());}
 public static synchronized ProfileSession start(File directory,ProfileSessionInfo info){return start(directory,info,ProfileScope.GLOBAL,null);}
 private static File newDirectory(){return new File(outputRoot,"profile-"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",Locale.ROOT).format(new Date()));}
 private static ProfileSession start(File directory,ProfileSessionInfo info,ProfileScope scope,String consumerId){if(directory==null)throw new IllegalArgumentException("directory must not be null");if(info==null)throw new IllegalArgumentException("sessionInfo must not be null");if(session!=null)throw new IllegalStateException("A profiling session is already active");session=new ProfileSession(directory,info,scope,consumerId);Profiler.clearCurrentThread();Profiler.setActive(session);session.recordEvent("Profiler Started",null);return session;}
 public static synchronized File stop() throws IOException{if(session==null)return null;ProfileSession done=session;done.recordEvent("Profiler Stopped",null);Profiler.setActive(null);session=null;done.stop(System.nanoTime());new ProfileWriter().write(done);return done.getOutputDirectory();}
 public static synchronized boolean pause(){boolean c=session!=null&&session.pause(System.nanoTime());if(c)session.recordEvent("Profiler Paused",null);return c;}
 public static synchronized boolean resume(){boolean c=session!=null&&session.resume(System.nanoTime());if(c)session.recordEvent("Profiler Resumed",null);return c;}
 public static synchronized ProfileSession reset(){if(session==null)return null;File d=session.getOutputDirectory();ProfileSessionInfo info=session.getSessionInfo();ProfileScope scope=session.getScope();String consumerId=session.getConsumerId();Profiler.setActive(null);session=new ProfileSession(d,info,scope,consumerId);Profiler.setActive(session);Profiler.clearCurrentThread();return session;}
 public static synchronized ProfileSession status(){return session;}
}
