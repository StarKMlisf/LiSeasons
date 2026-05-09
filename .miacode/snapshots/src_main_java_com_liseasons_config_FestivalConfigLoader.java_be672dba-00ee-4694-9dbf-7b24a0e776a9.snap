package com.liseasons.config;

import com.liseasons.festival.FestivalConfig;
import com.liseasons.festival.FestivalType;
import org.bukkit.configuration.ConfigurationSection;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 节日配置加载器
 */
public class FestivalConfigLoader {
    
    /**
     * 从配置文件加载节日配置
     */
    public static List<FestivalConfig> loadFestivals(ConfigurationSection config) {
        List<FestivalConfig> festivals = new ArrayList<>();
        
        if (config == null) {
            return festivals;
        }

        // 加载服务器节日。旧版本的传统/法定/公历分组仍兼容读取，但默认配置只保留这一类。
        loadFestivalsByCategory(config, "server-custom-festivals", festivals);
        loadFestivalsByCategory(config, "traditional-festivals", festivals);
        loadFestivalsByCategory(config, "legal-holidays", festivals);
        loadFestivalsByCategory(config, "gregorian-festivals", festivals);

        return festivals;
    }

    /**
     * 加载指定分类的节日
     */
    private static void loadFestivalsByCategory(
            ConfigurationSection config,
            String categoryKey,
            List<FestivalConfig> festivals
    ) {
        ConfigurationSection categorySection = config.getConfigurationSection(categoryKey);
        if (categorySection == null) {
            return;
        }

        for (String festivalKey : categorySection.getKeys(false)) {
            ConfigurationSection festivalSection = categorySection.getConfigurationSection(festivalKey);
            if (festivalSection == null) {
                continue;
            }

            try {
                FestivalConfig festivalConfig = loadSingleFestival(festivalKey, festivalSection);
                if (festivalConfig != null) {
                    festivals.add(festivalConfig);
                }
            } catch (Exception e) {
                System.err.println("Failed to load festival: " + festivalKey + " - " + e.getMessage());
            }
        }
    }

    /**
     * 加载单个节日配置
     */
    private static FestivalConfig loadSingleFestival(String festivalId, ConfigurationSection section) {
        String typeStr = section.getString("type");
        if (typeStr == null) {
            throw new IllegalArgumentException("Festival type not specified");
        }

        FestivalType type = FestivalType.valueOf(typeStr.trim().toUpperCase(Locale.ROOT));
        
        String dateStr = section.getString("date");
        if (dateStr == null) {
            throw new IllegalArgumentException("Festival date not specified");
        }

        // 解析日期格式 "MM-dd"
        String[] dateParts = dateStr.split("-");
        if (dateParts.length != 2) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }

        int month = Integer.parseInt(dateParts[0]);
        int day = Integer.parseInt(dateParts[1]);
        MonthDay date = MonthDay.of(month, day);

        String displayName = section.getString("display-name", type.getDisplayName());
        String categoryStr = section.getString("category", type.getCategory().name());
        FestivalType.FestivalCategory category = FestivalType.FestivalCategory.valueOf(categoryStr.trim().toUpperCase(Locale.ROOT));
        String description = section.getString("description", "");
        
        int priority = section.getInt("priority", 0);
        boolean enabled = section.getBoolean("enabled", true);

        return new FestivalConfig(
                festivalId,
                type,
                date,
                displayName,
                category,
                description,
                enabled,
                priority
        );
    }
}
