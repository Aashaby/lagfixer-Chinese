package xyz.lychee.lagfixer.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class ReloadCommand extends CommandManager.Subcommand {
    private volatile boolean reload = false;

    public ReloadCommand(CommandManager commandManager) {
        super(commandManager, "reload", "reload all plugin configuration");
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.reload) {
            Component running = Language.getMainValue("reload_running", true);
            if (running != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(running);
            return true;
        }

        this.reload = true;
        Thread thread = new Thread(() -> {
            TimingUtil t = TimingUtil.startNew();

            LagFixer plugin = this.getCommandManager().getPlugin();
            plugin.getLogger().sendHeader(plugin.getDescription().getVersion());

            plugin.reloadConfig();
            try {
                ConfigManager.getInstance().load();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ModuleManager.getInstance().getModules().forEach((clazz, m) -> {
                boolean enabled = m.getConfig().getBoolean(m.getName() + ".enabled");

                try {
                    if (m.isLoaded()) {
                        m.disable();
                        m.setLoaded(false);
                    }

                    if (enabled) {
                        m.load();
                        m.setLoaded(true);
                        m.loadAllConfig();
                        plugin.getLogger().info("&rConfiguration for &e" + m.getName() + " &rsuccessfully reloaded!");
                    } else if (m.isLoaded()) {
                        plugin.getLogger().info("&rSuccessfully disabled module &e" + m.getName() + "&r!");
                    }

                    m.getMenu().updateAll();
                } catch (Exception ex) {
                    plugin.printError(ex);
                    plugin.getLogger().info("&rError reloading configuration for &c" + m.getName() + "&r!");
                }
            });

            Component done = Language.getMainValue("reload_done", true,
                    Placeholder.unparsed("time", t.stop().toString()));
            if (done != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(done);
            this.reload = false;
        });
        thread.setName("LagFixer Reload");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return true;
    }
}