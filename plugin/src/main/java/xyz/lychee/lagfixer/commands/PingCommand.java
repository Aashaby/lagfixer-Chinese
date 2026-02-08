package xyz.lychee.lagfixer.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class PingCommand extends CommandManager.Subcommand {
    public PingCommand(CommandManager commandManager) {
        // 汉化文本calculate average players ping
        super(commandManager, "ping", "计算玩家平均延迟");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        AbstractSupportNms nms = SupportManager.getInstance().getNms();
        if (args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                // 汉化文本Player not found on the server
                return MessageUtils.sendMessage(true, sender, "&7服务器上未找到该玩家");
            }

            // 汉化文本's ping is
            return MessageUtils.sendMessage(true, sender, "&7" + player.getDisplayName() + " 的延迟为 &e" + nms.getPlayerPing(player) + "&7ms");
        }

        double averagePing = Bukkit.getOnlinePlayers()
                .stream()
                .mapToInt(nms::getPlayerPing)
                .average()
                .orElse(-1D);
        // 汉化文本Average players ping:
        return MessageUtils.sendMessage(true, sender, "&7玩家平均延迟：&e" + averagePing);
    }
}