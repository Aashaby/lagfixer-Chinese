package xyz.lychee.lagfixer.menu;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModulesMenu extends AbstractMenu {
    public ModulesMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, Language.getLocalized("menu_modules_title",
                Placeholder.unparsed("version", plugin.getDescription().getVersion())), 20, true);

        this.surroundInventory();
        this.fillInventory();

        List<AbstractModule> modules = new ArrayList<>(ModuleManager.getInstance().getModules().values());
        modules.sort(Comparator.comparing(AbstractModule::getName));

        int index = 0;
        for (AbstractModule module : modules) {
            int i = index++;
            int slot = i + 10 + i / 7 * 2;

            ItemBuilder ib = module.getBaseSkull().copy();
            this.itemClickEvent(slot, () -> {
                List<String> lore = new ArrayList<>();

                String statusKey = module.isLoaded() ? "menu_modules_lore_status_enabled" : "menu_modules_lore_status_disabled";
                String status = Language.getLocalized(statusKey);
                lore.add(Language.getLocalized("menu_modules_lore_status",
                        Placeholder.unparsed("status", status)));

                lore.add(Language.getLocalized("menu_modules_lore_values",
                        Placeholder.unparsed("count", String.valueOf(
                                module.getSection().getValues(true).values().stream()
                                        .filter(obj -> !(obj instanceof ConfigurationSection)).count()
                        ))));

                lore.add(Language.getLocalized("menu_modules_lore_performance",
                        Placeholder.unparsed("performance",
                                Language.getSerializer().serialize(module.getImpact().getComponent()))));
                lore.add("");
                lore.add(Language.getLocalized("menu_modules_lore_click"));
                lore.add("");
                lore.add(Language.getLocalized("menu_modules_lore_description"));

                for (String line : module.getDescription()) {
                    StringBuilder lineBuilder = new StringBuilder(" &8{*} &7");
                    int wordCount = 0;
                    for (String word : line.split("\\s+")) {
                        if (wordCount < 5 && lineBuilder.length() < 40) {
                            lineBuilder.append(word).append(' ');
                            ++wordCount;
                            continue;
                        }
                        lore.add(lineBuilder.toString().trim());
                        lineBuilder = new StringBuilder("&7").append(word).append(' ');
                        wordCount = 1;
                    }
                    if (lineBuilder.isEmpty()) continue;
                    lore.add(lineBuilder.toString().trim());
                }

                return ib.setName("&6&l⭐ &f&lModule: &e&l" + module.getName())
                        .setLore(lore)
                        .build();
            }, e -> e.getWhoClicked().openInventory(module.getMenu().getInv()));
        }
    }

    @Override
    public void update() {
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
    }

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getMainMenu();
    }
}