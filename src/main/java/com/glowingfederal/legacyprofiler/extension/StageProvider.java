package com.glowingfederal.legacyprofiler.extension;
import com.glowingfederal.legacyprofiler.core.StageMetadata; import java.util.Collection;
/** Service-loaded provider whose non-null stages are registered once on first API use. */
public interface StageProvider { Collection<StageMetadata> stages(); }
