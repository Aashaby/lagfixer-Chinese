package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.menu.ConfigMenu;
import xyz.lychee.lagfixer.modules.*;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.HashMap;

@Getter
public class ModuleManager extends AbstractManager {
    private static @Getter ModuleManager instance;
    private final HashMap<String, AbstractModule> modules = new HashMap<>();

    public ModuleManager(LagFixer plugin) {
        super(plugin);
        instance = this;
        this.addAll(
                new MobAiReducerModule(plugin, this),
                new LagShieldModule(plugin, this),
                new RedstoneLimiterModule(plugin, this),
                new EntityLimiterModule(plugin, this),
                new ConsoleFilterModule(plugin, this),
                new WorldCleanerModule(plugin, this),
                new VehicleMotionReducerModule(plugin, this),
                new InstantLeafDecayModule(plugin, this),
                new AbilityLimiterModule(plugin, this),
                new ExplosionOptimizerModule(plugin, this)
        );
    }

    public <T extends AbstractModule> T get(Class<T> clazz) {
        return clazz.cast(this.modules.get(clazz.getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractModule> T get(String name) {
        return this.modules.containsKey(name) ? (T) this.modules.get(name) : null;
    }

    private void addAll(AbstractModule... arrModules) {
        for (AbstractModule module : arrModules) {
            modules.put(module.getClass().getSimpleName(), module);
        }
    }

    @Override
    public void load() {
        //Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        for (AbstractModule module : this.modules.values()) {
            try {
                TimingUtil t = TimingUtil.startNew();
                boolean success = module.loadAllConfig();
                boolean enabled = module.getConfig().getBoolean(module.getName() + ".enabled");

                if (enabled) {
                    if (success) {
                        module.load();
                        // 汉化文本Successfully loaded module
                        this.getPlugin().getLogger().info(" &8• &r已成功加载模块 " + module.getName() + "，耗时 " + t.stop().getExecutingTime() + "ms。");
                    } else {
                        // 汉化文本Skipping unsupported module
                        this.getPlugin().getLogger().info(" &8• &r跳过不受支持的模块 " + module.getName() + "（当前版本：" + Bukkit.getServer().getBukkitVersion() + "）。");
                    }
                }

                module.setLoaded(success && enabled);
            } catch (Exception ex) {
                module.setLoaded(false);
                // 汉化文本Skipping module
                this.getPlugin().getLogger().info(" &8• &c跳过模块 " + module.getName() + "，原因：" + ex.getMessage());
                this.getPlugin().printError(ex);
            }

            ConfigMenu menu = module.getMenu();
            menu.load();
            menu.updateAll();
        }

        if (this.getPlugin().getConfig().isSet("modules")) {
            this.getPlugin().saveConfig();
        }
    }

    @Override
    public void disable() {
        //HandlerList.unregisterAll(this);

        for (AbstractModule module : this.modules.values()) {
            if (!module.isLoaded()) continue;

            try {
                TimingUtil t = TimingUtil.startNew();
                module.disable();
                // 汉化文本Successfully disabled module
                this.getPlugin().getLogger().info(" • 已成功禁用模块 " + module.getName() + "，耗时 " + t.stop().getExecutingTime() + "ms。");
            } catch (Exception ex) {
                // 汉化文本Error with disabling module
                this.getPlugin().getLogger().info(" • 禁用模块 " + module.getName() + " 时出错，原因：" + ex.getMessage());
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

