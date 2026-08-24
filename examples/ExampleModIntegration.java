// Documentation example only: this file is intentionally not part of Gradle's main source set.
package examplemod.integration;

import com.glowingfederal.legacyprofiler.api.Profiler;
import com.glowingfederal.legacyprofiler.core.Stage;
import com.glowingfederal.legacyprofiler.core.StageKind;
import com.glowingfederal.legacyprofiler.core.StageMetadata;

public final class ExampleModIntegration {
    private static Stage generation;
    private static Stage blocksPlaced;

    /** Call once from ExampleMod's deterministic initialization path, before profiling starts. */
    public static void registerStages() {
        Profiler.registerStage("examplemod", new StageMetadata(
            "examplemod.generation", null, StageKind.TIMING,
            "World generation", "ExampleMod generation work", true, "examplemod", 100));
        generation = Profiler.stage("examplemod.generation");

        Profiler.registerStage("examplemod", new StageMetadata(
            "examplemod.blocks_placed", null, StageKind.COUNTER,
            "World generation", "Blocks placed by ExampleMod", true, "examplemod", 110));
        blocksPlaced = Profiler.stage("examplemod.blocks_placed");
    }

    /** Consumer code records only; the installed Legacy Profiler mod owns the shared session. */
    public static void instrumentWork() {
        Profiler.enter(generation);
        try {
            generateAndCountBlocks();
        } finally {
            Profiler.exit(generation);
        }
    }

    private static void generateAndCountBlocks() {
        // Perform one unit of real generation work here.
        Profiler.recordCounter(blocksPlaced);
    }

    private ExampleModIntegration() { }
}
