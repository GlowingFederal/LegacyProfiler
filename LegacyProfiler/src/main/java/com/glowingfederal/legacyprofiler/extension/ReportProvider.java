package com.glowingfederal.legacyprofiler.extension;
import com.glowingfederal.legacyprofiler.core.ReportWriter; import java.util.Collection;
public interface ReportProvider { Collection<ReportWriter> writers(); }
