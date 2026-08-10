package xyz.lychee.lagfixer.menu;

import lombok.Data;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.io.File;
import java.util.*;

public class ConfigMenu extends AbstractMenu {
    private final Map<UUID, ConfigChange> playerChanges = new HashMap<>();
    private final AbstractModule module;
    private final File configFile;

    public ConfigMenu(LagFixer plugin, int size, AbstractModule module) {
        super(plugin, size, Language.getLocalized("config_title"), -1, true);
        this.module = module;
        this.configFile = new File(this.getPlugin().getDataFolder(), "modules/" + module.getName() + ".yml");

        this.itemClickEvent(size - 5, () -> {
            boolean loaded = this.module.isLoaded();
            String skull = loaded ?
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkwNzkzZjU2NjE2ZjEwMTUwMmRlMWQzNGViMjU0NGY2MDdkOTg5MDBlMzY5OTM2OTI5NTMxOWU2MzBkY2Y2ZCJ9fX0=" :
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRiZDlhNDViOTY4MWNlYTViMjhjNzBmNzVhNjk1NmIxZjU5NGZlYzg0MGI5NjA3Nzk4ZmIxZTcwNzc2NDQzMCJ9fX0=";
            String nameKey = loaded ? "config_enabled_name" : "config_disabled_name";
            String loreKey = loaded ? "config_enabled_lore" : "config_disabled_lore";
            return ItemBuilder.createSkull(skull)
                    .setName(Language.getLocalized(nameKey))
                    .setLore(Language.getLocalized(loreKey))
                    .build();
        }, e -> {
        });

        this.surroundInventory();
        ConfigurationSection base = this.module.getSection();
        ConfigurationSection defaults = base.getDefaultSection() != null ? base.getDefaultSection() : base;

        int slot = 0;
        for (Map.Entry<String, Object> entry : defaults.getValues(true).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) continue;

            String key = entry.getKey();
            this.itemClickEvent(slot++, () -> {
                Object val = base.get(key);
                List<String> lore = new ArrayList<>();
                lore.add(Language.getLocalized("config_lore_current"));

                if (val instanceof Collection<?> c) {
                    for (Object o : c) {
                        lore.add(Language.getLocalized("config_lore_value_item",
                                Placeholder.unparsed("value", String.valueOf(o))));
                    }
                } else {
                    lore.add(Language.getLocalized("config_lore_value_item",
                            Placeholder.unparsed("value", String.valueOf(val))));
                }

                lore.add("");
                lore.add(Language.getLocalized("config_lore_right_click"));
                lore.add(Language.getLocalized("config_lore_left_click"));

                return this.module.getBaseSkull().copy()
                        .setName(Language.getLocalized("config_item_key",
                                Placeholder.unparsed("key", key)))
                        .setLore(lore)
                        .build();
            }, e -> {
                HumanEntity human = e.getWhoClicked();
                if (e.isRightClick()) {
                    String defMsg = Language.getLocalized("config_default_value",
                            Placeholder.unparsed("key", key),
                            Placeholder.unparsed("value", String.valueOf(defaults.get(key))));
                    MessageUtils.sendMessage(true, human, defMsg);
                } else {
                    human.closeInventory();
                    playerChanges.put(human.getUniqueId(), new ConfigChange(this.module, key, this.module.getSection().get(key)));
                    MessageUtils.sendMessage(true, human, Language.getLocalized("config_edit_prompt"));
                    if (base.get(key) instanceof Collection) {
                        MessageUtils.sendMessage(false, human, Language.getLocalized("config_edit_collection_hint"));
                    }
                }
            });
        }
        this.fillInventory();
    }

    @Override
    public void update() {
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        ConfigChange change = this.playerChanges.remove(player.getUniqueId());
        if (change == null) return;

        e.setCancelled(true);

        if (e.getMessage().equalsIgnoreCase("-cancel") || e.getMessage().equalsIgnoreCase("cancel")) {
            openModuleMenu(player, change.getModule());
            MessageUtils.sendMessage(true, player, Language.getLocalized("config_edit_cancelled"));
            return;
        }

        try {
            updateConfigValue(change, e.getMessage());
            change.getModule().getConfig().save(configFile);
            change.getModule().loadAllConfig();

            Object newValue = change.getModule().getSection().get(change.getKey());
            MessageUtils.sendMessage(true, player,
                    Language.getLocalized("config_saved",
                            Placeholder.unparsed("old", String.valueOf(change.getValue())),
                            Placeholder.unparsed("new", String.valueOf(newValue)))
            );

            change.getModule().getMenu().updateAll();
            openModuleMenu(player, change.getModule());
        } catch (Exception ex) {
            MessageUtils.sendMessage(true, player, Language.getLocalized("config_error"));
            this.getPlugin().printError(ex);
        }
    }

    private void updateConfigValue(ConfigChange change, String message) {
        ConfigurationSection section = change.getModule().getSection();
        String key = change.getKey();

        if (section.isString(key)) {
            section.set(key, message);
        } else if (section.isInt(key)) {
            section.set(key, Integer.parseInt(message));
        } else if (section.isDouble(key)) {
            section.set(key, Double.parseDouble(message));
        } else if (section.isLong(key)) {
            section.set(key, Long.parseLong(message));
        } else if (section.isBoolean(key)) {
            section.set(key, Boolean.parseBoolean(message));
        } else if (section.isList(key)) {
            List<String> list = section.getStringList(key);
            if (list.contains(message)) {
                list.remove(message);
            } else {
                list.add(message);
            }
            section.set(key, list);
        }
    }

    private void openModuleMenu(Player player, AbstractModule module) {
        SupportManager.getInstance().getFork().runNow(false, player.getLocation(), () -> player.openInventory(module.getMenu().getInv()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerChanges.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
        // 处理启用/禁用按钮的点击（因为点击事件已在 itemClickEvent 中注册，但 handleClick 仍会被调用，此处不再重复处理）
        // 但为了区分，我们在这里处理 size-5 位置的点击（由 itemClickEvent 触发，但它的 Consumer 为空，所以这里补上逻辑）
        int slot = e.getSlot();
        int topSize = e.getView().getTopInventory().getSize();
        if (slot == topSize - 5) {
            try {
                boolean newState = !this.module.isLoaded();
                String path = this.module.getName() + ".enabled";

                if (newState) {
                    this.module.load();
                    this.module.loadAllConfig();
                    MessageUtils.sendMessage(true, e.getWhoClicked(),
                            Language.getLocalized("config_toggle_enabled",
                                    Placeholder.unparsed("module", this.module.getName())));
                } else {
                    this.module.disable();
                    MessageUtils.sendMessage(true, e.getWhoClicked(),
                            Language.getLocalized("config_toggle_disabled",
                                    Placeholder.unparsed("module", this.module.getName())));
                }

                this.module.setLoaded(newState);
                this.module.getConfig().set(path, newState);
                this.module.getConfig().save(this.configFile);

                this.updateAll();
            } catch (Exception ex) {
                MessageUtils.sendMessage(true, e.getWhoClicked(), Language.getLocalized("config_toggle_error"));
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getModulesMenu();
    }

    @Data
    public static class ConfigChange {
        private final AbstractModule module;
        private final String key;
        private final Object value;
    }
}