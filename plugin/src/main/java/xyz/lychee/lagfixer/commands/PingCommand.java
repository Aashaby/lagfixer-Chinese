package xyz.lychee.lagfixer.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ISupportNms;

public class PingCommand extends CommandManager.Subcommand {
    public PingCommand(CommandManager commandManager) {
        super(commandManager, "ping", "calculate average players ping");
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        ISupportNms nms = SupportManager.getInstance().getNms();
        if (args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Component notFound = Language.getMainValue("ping_player_not_found", true);
                if (notFound != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(notFound);
                return true;
            }
            Component msg = Language.getMainValue("ping_result", true,
                    Placeholder.unparsed("player", player.getDisplayName()),
                    Placeholder.unparsed("ping", String.valueOf(nms.getPlayerPing(player))));
            if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
            return true;
        }

        double averagePing = Bukkit.getOnlinePlayers()
                .stream()
                .mapToInt(nms::getPlayerPing)
                .average()
                .orElse(-1D);
        Component msg = Language.getMainValue("ping_average", true,
                Placeholder.unparsed("average", String.format("%.2f", averagePing)));
        if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
        return true;
    }
}