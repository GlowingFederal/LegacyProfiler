// Documentation example only: this file is intentionally not part of Gradle's main source set.
package examplemod.integration;

import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;
import com.glowingfederal.legacyprofiler.api.Profiler;
import com.glowingfederal.legacyprofiler.core.Stage;
import com.glowingfederal.legacyprofiler.core.StageKind;
import com.glowingfederal.legacyprofiler.core.StageMetadata;

import java.io.IOException;

public final class ExampleModIntegration {
    private static Stage generation;
    private static Stage blocksPlaced;

    /** Call once from ExampleMod's deterministic initialization path, before profiling starts. */
    public static void registerStages() {
        Profiler.registerStage(new StageMetadata(
            "EXAMPLEMOD_GENERATION", null, StageKind.TIMING,
            "World generation", "ExampleMod generation work", true, "examplemod", 100));
        generation = Profiler.stage("EXAMPLEMOD_GENERATION");

        Profiler.registerStage(new StageMetadata(
            "EXAMPLEMOD_BLOCKS_PLACED", null, StageKind.COUNTER,
            "World generation", "Blocks placed by ExampleMod", true, "examplemod", 110));
        blocksPlaced = Profiler.stage("EXAMPLEMOD_BLOCKS_PLACED");
    }

    /** Example host-owned session. Real mods should surface the IOException to their users. */
    public static void profileWork() throws IOException {
        Profiler.beginSession(ProfileSessionInfo.builder()
            .source("examplemod")
            .displayName("Example Mod")
            .purpose("Measure generation work")
            .build());
        try {
            Profiler.enter(generation);
            try {
                generateAndCountBlocks();
            } finally {
                Profiler.exit(generation);
            }
        } finally {
            Profiler.endSession();
        }
    }

    private static void generateAndCountBlocks() {
        // Perform one unit of real generation work here.
        Profiler.recordCounter(blocksPlaced);
    }

    private ExampleModIntegration() { }
}
