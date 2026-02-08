package xyz.lychee.lagfixer;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lychee.lagfixer.managers.*;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class LagFixer extends JavaPlugin {
    private static @Getter LagFixer instance;
    private final ArrayList<AbstractManager> managers = new ArrayList<>();
    private final Logger logger = new ColoredLogger();
    private BukkitAudiences audiences;

    @Override
    public void onEnable() {
        instance = this;
        this.audiences = BukkitAudiences.create(this);
        this.managers.clear();
        this.sendHeader();
        this.loadManager(new ErrorsManager(this));
        this.loadManager(new ConfigManager(this));
        this.loadManager(new SupportManager(this));
        this.loadManager(new HookManager(this));
        this.loadManager(new MetricsManager(this));
        this.loadManager(new UpdaterManager(this));
        this.loadManager(new ModuleManager(this));
        this.loadManager(new CommandManager(this));
        // 汉化文本Remember to leave a rating!
        this.getLogger().info("&f记得给我们评分！&r &e&l★ ★ ★ ★ ★");
        // 汉化文本Support us
        this.getLogger().info("&c❤ &f支持我们 &e&nhttps://ko-fi.com/lajczik");
    }

    @Override
    public void onDisable() {
        Iterator<AbstractManager> it = this.managers.iterator();
        while (it.hasNext()) {
            AbstractManager manager = it.next();
            try {
                // 汉化文本Disabling manager...
                this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &f正在禁用管理器...");
                TimingUtil t = TimingUtil.startNew();
                manager.disable();
                // 汉化文本Disabled manager in
                this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &f已禁用管理器，耗时 &e" + t.stop().getExecutingTime() + "ms&f！");
            } catch (Exception ex) {
                this.printError(ex);
            }
            it.remove();
        }
        SupportManager.getInstance().getMonitor().stop();
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    public void sendHeader() {
        // 汉化文本Remember to leave a rating!
        // 汉化文本Best Performance Solution!
        this.getLogger().info("&f记得给我们评分！&r &e&l★ ★ ★ ★ ★" +
                "\n\n\n\n\n" +
                "\n&fLagFixer " + this.getDescription().getVersion() + " - 最佳性能解决方案！&r" +
                "\n░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░" +
                "\n░██╗░░░░░░█████╗░░██████╗░░░███████╗██╗██╗░░██╗███████╗██████╗░░" +
                "\n░██║░░░░░██╔══██╗██╔════╝░░░██╔════╝██║╚██╗██╔╝██╔════╝██╔══██╗░" +
                "\n░██║░░░░░███████║██║░░██╗░░░█████╗░░██║░╚███╔╝░█████╗░░██████╔╝░" +
                "\n░██║░░░░░██╔══██║██║░░╚██╗░░██╔══╝░░██║░██╔██╗░██╔══╝░░██╔══██╗░" +
                "\n░███████╗██║░░██║╚██████╔╝░░██║░░░░░██║██╔╝╚██╗███████╗██║░░██║░" +
                "\n░╚══════╝╚═╝░░╚═╝░╚═════╝░░░╚═╝░░░░░╚═╝╚═╝░░╚═╝╚══════╝╚═╝░░╚═╝░" +
                "\n░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░" +
                "\n\n\n\n\n");
    }

    public void loadManager(AbstractManager manager) {
        if (!manager.isEnabled()) {
            return;
        }
        this.managers.add(manager);
        try {
            // 汉化文本Enabling manager...
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &f正在启用管理器...");
            TimingUtil t = TimingUtil.startNew();
            manager.load();
            // 汉化文本Enabled manager in
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &f已启用管理器，耗时 &e" + t.stop().getExecutingTime() + "ms&f！");
        } catch (Exception ex) {
            this.printError(ex);
        }
    }

    public void printError(Throwable ex) {
        if (ErrorsManager.getInstance() != null && ErrorsManager.getInstance().checkError(ex)) {
            this.getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }
}

