package com.liseasons;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageManager {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([a-zA-Z0-9_-]+)>");

    private final LISeasonsPlugin plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration messages;

    public MessageManager(LISeasonsPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = loadMessages();
    }

    public void reload() {
        this.messages = loadMessages();
    }

    public Component message(String path, Placeholder... placeholders) {
        String prefix = this.messages.getString("prefix", "");
        String template = this.messages.getString(path, "<red>缺少消息: " + path + "</red>");
        return this.miniMessage.deserialize(applyPlaceholders(prefix + template, placeholders));
    }

    public Component rawMessage(String path, Placeholder... placeholders) {
        String template = this.messages.getString(path, "<red>缺少消息: " + path + "</red>");
        return this.miniMessage.deserialize(applyPlaceholders(template, placeholders));
    }

    public List<Component> messageList(String path, Placeholder... placeholders) {
        List<String> list = this.messages.getStringList(path);
        if (list.isEmpty()) {
            return Collections.singletonList(message(path, placeholders));
        }

        List<Component> components = new ArrayList<>();
        String prefix = this.messages.getString("prefix", "");
        for (String line : list) {
            components.add(this.miniMessage.deserialize(applyPlaceholders(prefix + line, placeholders)));
        }
        return components;
    }

    public List<Component> rawMessageList(String path, Placeholder... placeholders) {
        List<String> list = this.messages.getStringList(path);
        if (list.isEmpty()) {
            return Collections.singletonList(rawMessage(path, placeholders));
        }

        List<Component> components = new ArrayList<>();
        for (String line : list) {
            components.add(this.miniMessage.deserialize(applyPlaceholders(line, placeholders)));
        }
        return components;
    }

    public String seasonName(String key) {
        return this.messages.getString("season-names." + key, key);
    }

    public String solarTermName(String key) {
        return this.messages.getString("solar-term-names." + key, key);
    }

    private FileConfiguration loadMessages() {
        File file = new File(this.plugin.getDataFolder(), "messages.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    private String applyPlaceholders(String input, Placeholder... placeholders) {
        String result = input;
        if (placeholders != null) {
            for (Placeholder placeholder : placeholders) {
                if (placeholder == null) {
                    continue;
                }
                String token = "<" + placeholder.key() + ">";
                result = result.replace(token, Objects.toString(placeholder.value(), ""));
            }
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (isMiniMessageTag(key)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group()));
                continue;
            }
            matcher.appendReplacement(buffer, "");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private boolean isMiniMessageTag(String key) {
        return key.startsWith("/")
                || key.equals("green")
                || key.equals("aqua")
                || key.equals("white")
                || key.equals("gray")
                || key.equals("gold")
                || key.equals("red")
                || key.equals("yellow")
                || key.equals("gradient");
    }

    public record Placeholder(String key, String value) {
        public static Placeholder of(String key, String value) {
            return new Placeholder(key, value);
        }
    }
}
