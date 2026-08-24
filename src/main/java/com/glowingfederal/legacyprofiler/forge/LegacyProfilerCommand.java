package com.glowingfederal.legacyprofiler.forge;

import com.glowingfederal.legacyprofiler.api.ProfileSessionInfo;
import com.glowingfederal.legacyprofiler.api.Profiler;
import com.glowingfederal.legacyprofiler.core.ProfileSession;
import com.glowingfederal.legacyprofiler.core.ProfilerManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.io.IOException;

/** Small operator command for the lifecycle genuinely supported by the core. */
public final class LegacyProfilerCommand extends CommandBase {
    @Override public String getCommandName() { return "legacyprofiler"; }
    @Override public String getCommandUsage(ICommandSender sender) {
        return "/legacyprofiler <start|stop|status>";
    }
    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length != 1) throw new WrongUsageException(getCommandUsage(sender));
        if ("start".equalsIgnoreCase(arguments[0])) {
            if (Profiler.isSessionActive()) {
                reply(sender, "A profiling session is already active.");
                return;
            }
            Profiler.beginGlobalSession(ProfileSessionInfo.builder().source("legacyprofiler")
                .displayName("Legacy Profiler command").purpose("Operator-requested profile").build());
            reply(sender, "Profiling started.");
        } else if ("stop".equalsIgnoreCase(arguments[0])) {
            if (!Profiler.isSessionActive()) {
                reply(sender, "No profiling session is active.");
                return;
            }
            try {
                File output = Profiler.endSession();
                reply(sender, "Profiling stopped. Reports: " + output.getPath());
            } catch (IOException failure) {
                reply(sender, "Profiling stopped, but report export failed: " + failure.getMessage());
            }
        } else if ("status".equalsIgnoreCase(arguments[0])) {
            ProfileSession session = ProfilerManager.status();
            reply(sender, session == null ? "Profiler is idle." :
                "Profiler is " + session.getState().name().toLowerCase() + ".");
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private static void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("[Legacy Profiler] " + message));
    }
}
