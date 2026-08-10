package xyz.lychee.lagfixer.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.lang.management.ManagementFactory;

public class FreeCommand extends CommandManager.Subcommand {
    private boolean explicitGCDisabled = false;

    public FreeCommand(CommandManager commandManager) {
        super(commandManager, "free", "run garbage collector");
    }

    @Override
    public void load() {
        this.explicitGCDisabled = ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-XX:+DisableExplicitGC");
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (this.explicitGCDisabled) {
            Component msg = Language.getMainValue("free_disabled", true);
            if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
            return false;
        }

        SupportManager.getInstance().getFork().runNow(true, null, () -> {
            Runtime runtime = Runtime.getRuntime();

            long before = runtime.totalMemory() - runtime.freeMemory();

            try {
                runtime.gc();
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }

            long after = runtime.totalMemory() - runtime.freeMemory();

            long diff = before - after;
            if (diff <= 0) {
                Component none = Language.getMainValue("free_no_memory", true);
                if (none != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(none);
                return;
            }

            long freedMB = diff / (1024 * 1024);
            Component success = Language.getMainValue("free_success", true,
                    Placeholder.unparsed("mb", String.valueOf(freedMB)));
            if (success != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(success);
        });
        return true;
    }
}