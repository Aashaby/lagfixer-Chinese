package xyz.lychee.lagfixer.menu;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.objects.WorldsMonitor;
import xyz.lychee.lagfixer.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainMenu extends AbstractMenu {
    private final ItemBuilder i1;
    private final ItemBuilder i2;
    private final ItemBuilder i3;
    private final ItemBuilder i4;

    public MainMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, getLocalizedTitle(plugin), 1, true);
        this.i1 = createSkullItem("menu_main_item_config",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyZmYyNDRkZmM5ZGQzYTJjZWY2MzExMmU3NTAyZGM2MzY3YjBkMDIxMzI5NTAzNDdiMmI0NzlhNzIzNjZkZCJ9fX0=");
        this.i2 = createSkullItem("menu_main_item_server",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0=");
        this.i3 = createSkullItem("menu_main_item_worlds",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=");
        this.i4 = createSkullItem("menu_main_item_fork",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ5ZjE4YzlkODVmOTJmNzJmODY0ZDY3YzEzNjdlOWE0NWRjMTBmMzcxNTQ5YzQ2YTRkNGRkOWU0ZjEzZmY0In19fQ==");

        this.surroundInventory();
        this.fillButtons();
        this.fillInventory();
    }

    private static String getLocalizedTitle(LagFixer plugin) {
        return Language.getLocalized("menu_main_title",
                Placeholder.unparsed("version", plugin.getDescription().getVersion()));
    }

    private ItemBuilder createSkullItem(String nameKey, String texture) {
        return ItemBuilder.createSkull(texture)
                .setName(Language.getLocalized(nameKey))
                .setLore(" &8{*} " + Language.getLocalized("menu_hardware_lore_loading"));
    }

    private void fillButtons() {
        this.getInv().setItem(10, i1.build());
        this.getInv().setItem(12, i2.build());
        this.getInv().setItem(14, i3.build());
        this.getInv().setItem(16, i4.build());
    }

    @Override
    public void update() {
        SupportManager support = SupportManager.getInstance();
        ModuleManager moduleManager = ModuleManager.getInstance();
        ResourceMonitor rm = support.getResourceMonitor();
        WorldsMonitor wm = support.getWorldsMonitor();

        long loaded = moduleManager.getModules().values().stream().filter(AbstractModule::isLoaded).count();
        long total = moduleManager.getModules().size();

        // i1
        List<String> lore1 = new ArrayList<>();
        lore1.add(Language.getLocalized("menu_main_lore_loaded_modules",
                Placeholder.unparsed("count", String.valueOf(loaded)),
                Placeholder.unparsed("total", String.valueOf(total))));
        lore1.add(Language.getLocalized("menu_main_lore_version",
                Placeholder.unparsed("version", this.getPlugin().getDescription().getVersion())));
        lore1.add("");
        lore1.add(Language.getLocalized("menu_main_lore_click_config"));
        i1.setLore(lore1);

        // i2
        List<String> lore2 = new ArrayList<>();
        lore2.add(Language.getLocalized("menu_main_lore_tps",
                Placeholder.unparsed("tps", String.valueOf(rm.getTps()))));
        lore2.add(Language.getLocalized("menu_main_lore_mspt",
                Placeholder.unparsed("mspt", String.valueOf(rm.getMspt()))));
        lore2.add(Language.getLocalized("menu_main_lore_memory",
                Placeholder.unparsed("used", String.valueOf(rm.getRamUsed())),
                Placeholder.unparsed("total", String.valueOf(rm.getRamTotal())),
                Placeholder.unparsed("max", String.valueOf(rm.getRamMax()))));
        lore2.add(Language.getLocalized("menu_main_lore_cpu_process",
                Placeholder.unparsed("cpu", String.valueOf(rm.getCpuProcess()))));
        lore2.add(Language.getLocalized("menu_main_lore_cpu_system",
                Placeholder.unparsed("cpu", String.valueOf(rm.getCpuSystem()))));
        lore2.add("");
        lore2.add(Language.getLocalized("menu_main_lore_click_hardware"));
        i2.setLore(lore2);

        // i3
        List<String> lore3 = new ArrayList<>();
        lore3.add(Language.getLocalized("menu_main_lore_chunks",
                Placeholder.unparsed("chunks", String.valueOf(wm.getChunks()))));
        lore3.add(Language.getLocalized("menu_main_lore_entities",
                Placeholder.unparsed("entities", String.valueOf(wm.getEntities()))));
        lore3.add(Language.getLocalized("menu_main_lore_creatures",
                Placeholder.unparsed("creatures", String.valueOf(wm.getCreatures()))));
        lore3.add(Language.getLocalized("menu_main_lore_items",
                Placeholder.unparsed("items", String.valueOf(wm.getItems()))));
        lore3.add(Language.getLocalized("menu_main_lore_projectiles",
                Placeholder.unparsed("projectiles", String.valueOf(wm.getProjectiles()))));
        lore3.add(Language.getLocalized("menu_main_lore_vehicles",
                Placeholder.unparsed("vehicles", String.valueOf(wm.getVehicles()))));
        lore3.add(Language.getLocalized("menu_main_lore_tiles",
                Placeholder.unparsed("tiles", String.valueOf(wm.getTiles()))));
        lore3.add(Language.getLocalized("menu_main_lore_players",
                Placeholder.unparsed("online", String.valueOf(Bukkit.getOnlinePlayers().size())),
                Placeholder.unparsed("max", String.valueOf(Bukkit.getMaxPlayers()))));
        lore3.add("");
        lore3.add(Language.getLocalized("menu_main_lore_click_cleaner"));
        i3.setLore(lore3);

        // i4
        i4.setLore(Collections.singletonList(Language.getLocalized("menu_main_lore_click_fork")));

        this.fillButtons();
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        HumanEntity human = e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == 10) {
            human.openInventory(MenuCommand.getInstance().getModulesMenu().getInv());
        } else if (slot == 12) {
            HardwareMenu menu = MenuCommand.getInstance().getHardwareMenu();
            if (menu == null) {
                // 使用语言文件
                LagFixer.getInstance().getAudiences().sender(human)
                        .sendMessage(Language.getMainValue("menu_main_click_hardware_not_supported", true));
            } else {
                human.openInventory(menu.getInv());
            }
        } else {
            LagFixer.getInstance().getAudiences().sender(human)
                    .sendMessage(Language.getMainValue("menu_main_click_coming_soon", true));
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return null;
    }
}