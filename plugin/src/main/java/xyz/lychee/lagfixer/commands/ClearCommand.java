package xyz.lychee.lagfixer.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.stream.Streams;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearCommand extends CommandManager.Subcommand {
    public ClearCommand(CommandManager commandManager) {
        super(commandManager, "clear", "clear entities using rules in WorldCleaner");
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            Component usage = Language.getMainValue("clear_usage", true);
            if (usage != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(usage);
            return true;
        }

        WorldCleanerModule module = ModuleManager.getInstance().get(WorldCleanerModule.class);
        if (module == null || !module.isLoaded()) {
            Component disabled = Language.getMainValue("clear_disabled", true);
            if (disabled != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(disabled);
            return true;
        }

        AtomicInteger ai = new AtomicInteger();
        String type = args[0].toLowerCase();

        switch (type) {
            case "items" -> {
                module.getAllowedWorlds()
                        .stream()
                        .flatMap(w -> w.getEntitiesByClass(Item.class).stream())
                        .filter(module::clearItem)
                        .forEach(ent -> { ent.remove(); ai.incrementAndGet(); });
                Component msg = Language.getMainValue("clear_success_items", true,
                        Placeholder.unparsed("count", String.valueOf(ai.get())));
                if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
                return true;
            }
            case "creatures" -> {
                module.getAllowedWorlds()
                        .stream()
                        .flatMap(w -> w.getEntitiesByClass(Mob.class).stream())
                        .filter(module::clearCreature)
                        .forEach(ent -> { ent.remove(); ai.incrementAndGet(); });
                Component msg = Language.getMainValue("clear_success_creatures", true,
                        Placeholder.unparsed("count", String.valueOf(ai.get())));
                if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
                return true;
            }
            case "projectiles" -> {
                module.getAllowedWorlds()
                        .stream()
                        .flatMap(w -> w.getEntitiesByClass(Projectile.class).stream())
                        .filter(module::clearProjectile)
                        .forEach(ent -> { ent.remove(); ai.incrementAndGet(); });
                Component msg = Language.getMainValue("clear_success_projectiles", true,
                        Placeholder.unparsed("count", String.valueOf(ai.get())));
                if (msg != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(msg);
                return true;
            }
            default -> {
                Component invalid = Language.getMainValue("clear_invalid_type", true,
                        Placeholder.unparsed("type", type));
                if (invalid != null) LagFixer.getInstance().getAudiences().sender(sender).sendMessage(invalid);
                return true;
            }
        }
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return Streams.of("items", "creatures", "projectiles").filter(str -> str.startsWith(args[0])).toList();
        }
        return Collections.emptyList();
    }
}