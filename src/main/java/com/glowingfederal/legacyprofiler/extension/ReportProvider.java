package com.glowingfederal.legacyprofiler.extension;
import com.glowingfederal.legacyprofiler.core.ReportWriter; import java.util.Collection;
/** Service-loaded provider of process-wide, synchronous report writers. */
public interface ReportProvider { Collection<ReportWriter> writers(); }
