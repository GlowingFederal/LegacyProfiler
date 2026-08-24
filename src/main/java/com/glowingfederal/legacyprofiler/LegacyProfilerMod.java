package com.glowingfederal.legacyprofiler;

import com.glowingfederal.legacyprofiler.api.Profiler;
import com.glowingfederal.legacyprofiler.core.ProfilerManager;
import com.glowingfederal.legacyprofiler.forge.LegacyProfilerCommand;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/** Forge 1.7.10 container and lifecycle owner for the shared profiler runtime. */
@Mod(modid = LegacyProfilerMod.MOD_ID, name = LegacyProfilerMod.NAME,
    version = LegacyProfilerMod.VERSION, acceptedMinecraftVersions = "[1.7.10]")
public final class LegacyProfilerMod {
    public static final String MOD_ID = "legacyprofiler";
    public static final String NAME = "Legacy Profiler";
    public static final String VERSION = Profiler.VERSION;
    public static final String API_VERSION = Profiler.API_VERSION;

    private Logger logger;

    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        Profiler.initialize();
        ProfilerManager.setOutputRoot(new File(event.getModConfigurationDirectory().getParentFile(),
            "profiles/legacy-profiler"));
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        logger.info("Legacy Profiler {} (API {}) initialized", VERSION, API_VERSION);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new LegacyProfilerCommand());
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        if (!Profiler.isSessionActive()) return;
        try {
            File output = Profiler.endSession();
            logger.info("Finalized Legacy Profiler session during server shutdown: {}", output);
        } catch (IOException failure) {
            // ProfilerManager detaches the stopped session before export, so it cannot leak.
            logger.error("Could not export Legacy Profiler session during server shutdown", failure);
        }
    }
}
