package xyz.lychee.lagfixer.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;

public class MonitorCommand extends CommandManager.Subcommand {
    public MonitorCommand(CommandManager commandManager) {
        super(commandManager, "monitor", "check server load statistics", "tps", "mspt");
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        ResourceMonitor monitor = SupportManager.getInstance().getResourceMonitor();
        Component msg = Language.getMainValue("monitor_result", true,
                Placeholder.unparsed("tps", String.valueOf(monitor.getTps())),
                Placeholder.unparsed("mspt", String.valueOf(monitor.getMspt())),
                Placeholder.unparsed("ram_used", String.valueOf(monitor.getRamUsed())),
                Placeholder.unparsed("ram_total", String.valueOf(monitor.getRamTotal())),
                Placeholder.unparsed("ram_max", String.valueOf(monitor.getRamMax())),
                Placeholder.unparsed("cpu_process", String.valueOf(monitor.getCpuProcess())),
                Placeholder.unparsed("cpu_system", String.valueOf(monitor.getCpuSystem()))
        );
        if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
        return true;
    }
}