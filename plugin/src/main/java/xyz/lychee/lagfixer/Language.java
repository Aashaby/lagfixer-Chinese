package xyz.lychee.lagfixer;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Language {
    private static final @Getter YamlConfiguration yaml;
    private static final @Getter LegacyComponentSerializer serializer;
    private static final @Getter Map<String, String> mainValues;

    static {
        yaml = new YamlConfiguration();
        mainValues = new HashMap<>();
        serializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexCharacter('#')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
    }

    private final AbstractModule module;
    private final Map<String, String> values = new HashMap<>();

    public Language(AbstractModule module) {
        this.module = module;
    }

    public static Component getMainValue(String key, boolean prefix, TagResolver.Single... placeholders) {
        if (!mainValues.containsKey(key)) {
            return null;
        }
        return createComponent(mainValues.get(key), prefix, placeholders);
    }

    public static Component createComponent(String message, boolean prefix, TagResolver.Single... placeholders) {
        Component component = MiniMessage.miniMessage().deserialize(message, placeholders);
        return prefix ? Component.empty().append(ConfigManager.getInstance().getPrefix()).append(component) : component;
    }

    public void loadMessages() {
        ConfigurationSection section = yaml.getConfigurationSection("messages." + this.module.getName());
        if (section == null) {
            return;
        }
        this.values.clear();
        section.getValues(true).forEach((key, value) -> {
            if (value instanceof String) {
                this.values.put(key, (String) value);
            }
        });
    }

    public boolean hasTranslation(String key) {
        return this.values.containsKey(key);
    }

    public Component getComponent(String key, boolean prefix, TagResolver.Single... placeholders) {
        if (!this.values.containsKey(key)) {
            return Component.text("Unknown value - " + key);
        }
        return createComponent(this.values.get(key), prefix, placeholders);
    }

    public String getString(String key, boolean prefix, TagResolver.Single... placeholders) {
        Component component = this.getComponent(key, prefix, placeholders);
        return component == null ? "Unknown value - " + key : MessageUtils.fixColors(null, Language.getSerializer().serialize(component));
    }

    /**
     * 快速获取本地化字符串（带占位符），自动转换为 Minecraft 颜色代码（§）
     * @param key 语言键，位于 messages.Main 下
     * @param placeholders MiniMessage 占位符
     * @return 已转换颜色代码的字符串，若键不存在则返回 "§f" + key
     */
    public static String getLocalized(String key, TagResolver.Single... placeholders) {
        Component comp = getMainValue(key, false, placeholders);
        if (comp == null) return "§f" + key; // 安全回退
        return MessageUtils.fixColors(null, getSerializer().serialize(comp));
    }

    /**
     * 快速获取本地化字符串并包装为单行 List（用于 Lore）
     */
    public static List<String> getLocalizedLore(String key, TagResolver.Single... placeholders) {
        return Collections.singletonList(getLocalized(key, placeholders));
    }
}
