package xyz.lychee.lagfixer.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class FreeCommand extends CommandManager.Subcommand {
    public FreeCommand(CommandManager commandManager) {
        // 汉化文本run garbage collector
        super(commandManager, "free", "运行垃圾回收（GC）");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        SupportManager.getInstance().getFork().runNow(true, null, () -> {
            Runtime runtime = Runtime.getRuntime();

            long before = runtime.totalMemory() - runtime.freeMemory();

            try {
                runtime.gc();
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}

            long after = runtime.totalMemory() - runtime.freeMemory();

            long diff = before - after;
            if (diff <= 0) {
                // 汉化文本Unable to free RAM, you need to remove jvm argument:
                MessageUtils.sendMessage(true, sender, "&7无法释放内存，你需要移除 JVM 参数：&e&n-XX:+DisableExplicitGC&7！");
            } else {
                long freedMB = diff / (1024 * 1024);
                // 汉化文本Successfully freed
                MessageUtils.sendMessage(true, sender, "&7已成功释放 &e" + freedMB + " &7MB 内存。");
            }
        });
        return true;
    }
}