package com.liseasons.festival;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节日管理器
 * 负责节日的识别、触发和事件管理
 */
public class FestivalManager {
    private final JavaPlugin plugin;
    
    // 节日配置缓存
    private final Map<String, FestivalConfig> festivalConfigs = new ConcurrentHashMap<>();
    
    // 玩家节日冷却记录: playerId -> (festivalId -> lastTriggerTime)
    private final Map<UUID, Map<String, Long>> playerFestivalCooldowns = new ConcurrentHashMap<>();
    
    // 全局节日冷却时间（毫秒）
    private long globalFestivalCooldown = 5000;
    
    // 玩家最后一次节日事件触发时间
    private final Map<UUID, Long> playerLastFestivalTime = new ConcurrentHashMap<>();

    public FestivalManager(JavaPlugin plugin) {
        this(plugin, List.of());
    }

    public FestivalManager(JavaPlugin plugin, List<FestivalConfig> configs) {
        this.plugin = plugin;
        loadFestivalConfigs(configs);
    }

    /**
     * 初始化所有节日配置
     */
    private void initializeFestivals() {
        for (FestivalType type : FestivalType.values()) {
            FestivalConfig config = new FestivalConfig(
                    type.name().toLowerCase(),
                    type,
                    type.getDate(),
                    type.getDisplayName(),
                    type.getCategory(),
                    type.getDisplayName() + "默认节日配置",
                    true,
                    type.getCategory().ordinal()
            );
            festivalConfigs.put(config.festivalId(), config);
        }
    }

    /**
     * 从配置文件刷新节日配置。
     *
     * <p>配置文件是节日列表的唯一来源；只有配置缺失或为空时，才回退到内置节日，
     * 避免插件自动补回未在 festivals.yml 中声明的其他节日。</p>
     */
    public void loadFestivalConfigs(List<FestivalConfig> configs) {
        festivalConfigs.clear();
        if (configs == null || configs.isEmpty()) {
            initializeFestivals();
            return;
        }
        for (FestivalConfig config : configs) {
            festivalConfigs.put(config.festivalId(), config);
        }
    }

    /**
     * 检查并触发玩家的节日事件
     */
    public void checkAndTriggerFestival(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 检查全局冷却
        long now = System.currentTimeMillis();
        Long lastFestivalTime = playerLastFestivalTime.get(playerId);
        if (lastFestivalTime != null && (now - lastFestivalTime) < globalFestivalCooldown) {
            return;
        }

        // 获取当前日期
        LocalDate today = LocalDate.now();
        MonthDay currentDate = MonthDay.from(today);

        // 查找匹配的节日
        FestivalConfig festival = findMatchingFestival(currentDate);
        if (festival == null || !festival.enabled()) {
            return;
        }

        // 检查单个节日冷却
        if (!isFestivalCooldownExpired(playerId, festival.festivalId(), now)) {
            return;
        }

        // 触发节日事件
        triggerFestival(player, festival);

        // 更新冷却时间
        updateFestivalCooldown(playerId, festival.festivalId(), now);
        playerLastFestivalTime.put(playerId, now);
    }

    /**
     * 查找匹配的节日
     */
    private FestivalConfig findMatchingFestival(MonthDay date) {
        List<FestivalConfig> matching = new ArrayList<>();

        for (FestivalConfig config : festivalConfigs.values()) {
            if (!config.enabled()) {
                continue;
            }

            if (config.date().equals(date)) {
                matching.add(config);
            }
        }

        if (matching.isEmpty()) {
            return null;
        }

        // 按优先级排序，返回最高优先级的节日
        matching.sort(Comparator.comparingInt(FestivalConfig::priority).reversed());
        return matching.get(0);
    }

    /**
     * 检查节日冷却是否已过期
     */
    private boolean isFestivalCooldownExpired(UUID playerId, String festivalId, long now) {
        Map<String, Long> cooldowns = playerFestivalCooldowns.get(playerId);
        if (cooldowns == null) {
            return true;
        }

        Long lastTriggerTime = cooldowns.get(festivalId);
        if (lastTriggerTime == null) {
            return true;
        }

        // 节日冷却为 24 小时
        return (now - lastTriggerTime) >= 86400000L;
    }

    /**
     * 更新节日冷却时间
     */
    private void updateFestivalCooldown(UUID playerId, String festivalId, long now) {
        playerFestivalCooldowns
                .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(festivalId, now);
    }

    /**
     * 触发节日事件
     */
    private void triggerFestival(Player player, FestivalConfig festival) {
        String message = String.format(
                "§6[%s] §f%s 节日到来！",
                festival.getCategoryName(),
                festival.getDisplayName()
        );
        player.sendMessage(message);

        // 发送全服通知
        plugin.getServer().broadcastMessage(String.format(
                "§e✨ §f%s 来临！§e✨",
                festival.getDisplayName()
        ));
    }

    /**
     * 获取指定日期的节日
     */
    public FestivalConfig getFestivalByDate(MonthDay date) {
        return findMatchingFestival(date);
    }

    /**
     * 获取指定日期的所有节日
     */
    public List<FestivalConfig> getAllFestivalsByDate(MonthDay date) {
        List<FestivalConfig> result = new ArrayList<>();
        for (FestivalConfig config : festivalConfigs.values()) {
            if (config.enabled() && config.date().equals(date)) {
                result.add(config);
            }
        }
        result.sort(Comparator.comparingInt(FestivalConfig::priority).reversed());
        return result;
    }

    /**
     * 获取指定分类的所有节日
     */
    public List<FestivalConfig> getFestivalsByCategory(FestivalType.FestivalCategory category) {
        List<FestivalConfig> result = new ArrayList<>();
        for (FestivalConfig config : festivalConfigs.values()) {
            if (config.enabled() && config.category() == category) {
                result.add(config);
            }
        }
        result.sort(Comparator.comparingInt(FestivalConfig::priority).reversed());
        return result;
    }

    /**
     * 清理玩家数据（玩家离线时调用）
     */
    public void cleanupPlayer(UUID playerId) {
        playerFestivalCooldowns.remove(playerId);
        playerLastFestivalTime.remove(playerId);
    }

    /**
     * 设置全局节日冷却时间
     */
    public void setGlobalFestivalCooldown(long cooldownMillis) {
        this.globalFestivalCooldown = cooldownMillis;
    }

    /**
     * 获取节日配置
     */
    public FestivalConfig getFestivalConfig(String festivalId) {
        return festivalConfigs.get(festivalId);
    }

    /**
     * 获取所有节日配置
     */
    public Collection<FestivalConfig> getAllFestivalConfigs() {
        return festivalConfigs.values();
    }

    /**
     * 获取今天的节日
     */
    public FestivalConfig getTodayFestival() {
        LocalDate today = LocalDate.now();
        MonthDay currentDate = MonthDay.from(today);
        return findMatchingFestival(currentDate);
    }

    /**
     * 获取本月的所有节日
     */
    public List<FestivalConfig> getMonthFestivals(int month) {
        List<FestivalConfig> result = new ArrayList<>();
        for (FestivalConfig config : festivalConfigs.values()) {
            if (config.enabled() && config.date().getMonthValue() == month) {
                result.add(config);
            }
        }
        result.sort(Comparator.comparingInt(c -> c.date().getDayOfMonth()));
        return result;
    }

    /**
     * 获取全年的所有节日
     */
    public List<FestivalConfig> getAllYearFestivals() {
        List<FestivalConfig> result = new ArrayList<>(festivalConfigs.values());
        result.sort(Comparator
                .comparingInt((FestivalConfig c) -> c.date().getMonthValue())
                .thenComparingInt(c -> c.date().getDayOfMonth()));
        return result;
    }
}
