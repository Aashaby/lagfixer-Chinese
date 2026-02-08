package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class ReloadCommand extends CommandManager.Subcommand {
    private volatile boolean reload = false;

    public ReloadCommand(CommandManager commandManager) {
        // 汉化文本reload all plugin configuration
        super(commandManager, "reload", "重载插件全部配置");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.reload) {
            // 汉化文本Reload is running, wait for results in console!
            return MessageUtils.sendMessage(true, sender, "&7正在重载，请在控制台等待结果！");
        }

        this.reload = true;
        Thread thread = new Thread(() -> {
            TimingUtil t = TimingUtil.startNew();

            LagFixer plugin = this.getCommandManager().getPlugin();
            plugin.sendHeader();

            plugin.reloadConfig();
            try {
                ConfigManager.getInstance().load();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ModuleManager.getInstance().getModules().forEach((clazz, m) -> {
                boolean enabled = m.getConfig().getBoolean(m.getName() + ".enabled");

                try {
                    if (enabled) {
                        if (!m.isLoaded()) {
                            m.load();
                            m.setLoaded(true);
                        }
                        m.loadAllConfig();
                        // 汉化文本Configuration for
                        plugin.getLogger().info("&r模块 &e" + m.getName() + " &r配置已成功重载！");
                    } else if (m.isLoaded()) {
                        m.disable();
                        m.setLoaded(false);
                        // 汉化文本Successfully disabled module
                        plugin.getLogger().info("&r已成功禁用模块 &e" + m.getName() + "&r！");
                    }
                    m.getMenu().updateAll();
                } catch (Exception ex) {
                    plugin.printError(ex);
                    // 汉化文本Error reloading configuration for
                    plugin.getLogger().info("&r重载模块 &c" + m.getName() + "&r 配置时出错！");
                }
            });

            // 汉化文本Reloaded modules configurations in
            // 汉化文本Working methods to apply all changes:
            // 汉化文本Server restart
            // 汉化文本All plugins reload, command:
            // 汉化文本Plugman reload, command:
            MessageUtils.sendMessage(true, sender, "&7已在 &f" + t.stop().getExecutingTime() + "&7ms 内重载模块配置。" +
                    "\n " +
                    "\n &7应用所有更改的方式：" +
                    "\n  &8{*} &7重启服务器（&f推荐&7）" +
                    "\n  &8{*} &7重载全部插件，命令：&f/reload confirm" +
                    "\n  &8{*} &7使用 Plugman 重载，命令：&f/plugman reload LagFixer");
            this.reload = false;
        });
        thread.setName("LagFixer Reload");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return true;
    }
}