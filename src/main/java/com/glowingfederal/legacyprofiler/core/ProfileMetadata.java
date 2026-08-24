package com.glowingfederal.legacyprofiler.core;
import java.util.*;
/** Runtime metadata supplied by the host through metadata providers. */
public final class ProfileMetadata {
 public final String minecraftVersion="unknown", forgeVersion="unknown";
 public final String javaVersion=System.getProperty("java.version","unknown");
 public final String os=System.getProperty("os.name","unknown")+" "+System.getProperty("os.version","");
 public final String cpu=System.getProperty("os.arch","unknown");
 public final long maximumHeap=Runtime.getRuntime().maxMemory(), allocatedHeap=Runtime.getRuntime().totalMemory();
 public final String profilerVersion="1.0.0", gitCommit="unavailable", configurationHash="unavailable", workerCount="unknown";
 public final List<String> loadedMods=Collections.emptyList();
 public final List<Integer> loadedDimensions=Collections.emptyList();
}
