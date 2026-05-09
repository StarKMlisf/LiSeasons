package com.liseasons.event;

import com.liseasons.climate.TemperatureMonitorService;
import com.liseasons.climate.TemperatureService;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.config.ConfigManager;
import com.liseasons.season.SeasonManager;
import com.liseasons.season.SeasonState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 季节事件管理器
 * 负责季节事件的触发、冷却、效果执行
 */
public class SeasonEventManager {
    private final JavaPlugin plugin;
    private final SeasonManager seasonManager;
    private final TemperatureService temperatureService;
    private final ConfigManager configManager;
    
    // 事件配置缓存
    private final Map<String, SeasonEventConfig> eventConfigs = new ConcurrentHashMap<>();
    
    // 玩家事件冷却记录: playerId -> (eventId -> lastTriggerTime)
    private final Map<UUID, Map<String, Long>> playerEventCooldowns = new ConcurrentHashMap<>();
    
    // 全局事件冷却时间（毫秒）
    private long globalEventCooldown = 5000;
    
    // 玩家最后一次事件触发时间
    private final Map<UUID, Long> playerLastEventTime = new ConcurrentHashMap<>();
    
    // 效果执行器
    private final SeasonEventEffectExecutor effectExecutor;

    public SeasonEventManager(
            JavaPlugin plugin,
            SeasonManager seasonManager,
            TemperatureService temperatureService,
            ConfigManager configManager
    ) {
        this.plugin = plugin;
        this.seasonManager = seasonManager;
        this.temperatureService = temperatureService;
        this.configManager = configManager;
        this.effectExecutor = new SeasonEventEffectExecutor(plugin);
    }

    /**
     * 初始化事件配置
     */
    public void loadEventConfigs(List<SeasonEventConfig> configs) {
        eventConfigs.clear();
        for (SeasonEventConfig config : configs) {
            eventConfigs.put(config.eventId(), config);
        }
    }

    /**
     * 检查并触发玩家的季节事件
     */
    public void checkAndTriggerEvents(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 检查全局冷却
        long now = System.currentTimeMillis();
        Long lastEventTime = playerLastEventTime.get(playerId);
        if (lastEventTime != null && (now - lastEventTime) < globalEventCooldown) {
            return;
        }

        // 获取玩家所在世界的季节状态
        SeasonState seasonState = seasonManager.getState(player.getWorld());
        if (seasonState == null) {
            return;
        }

        // 获取玩家温度快照
        TemperatureSnapshot snapshot = temperatureService.snapshot(player);

        // 查找匹配的事件
        List<SeasonEventConfig> matchingEvents = findMatchingEvents(seasonState, snapshot);
        
        if (matchingEvents.isEmpty()) {
            return;
        }

        // 按优先级排序
        matchingEvents.sort(Comparator.comparingInt(SeasonEventConfig::priority).reversed());

        SeasonEventConfig eventConfig = null;
        for (SeasonEventConfig matchingEvent : matchingEvents) {
            if (!isEventCooldownExpired(playerId, matchingEvent.eventId(), now)) {
                continue;
            }
            if (!rollChance(matchingEvent)) {
                continue;
            }
            eventConfig = matchingEvent;
            break;
        }
        if (eventConfig == null) {
            return;
        }

        // 创建事件上下文
        SeasonEventContext context = new SeasonEventContext(
                player,
                seasonState,
                snapshot,
                now
        );

        // 执行事件效果
        triggerEvent(player, eventConfig, context);

        // 更新冷却时间
        updateEventCooldown(playerId, eventConfig.eventId(), now);
        playerLastEventTime.put(playerId, now);
    }

    /**
     * 管理员手动触发指定事件，用于测试配置效果。
     */
    public boolean triggerEventById(Player player, String eventId) {
        SeasonEventConfig eventConfig = eventConfigs.get(eventId);
        if (eventConfig == null || !eventConfig.enabled()) {
            return false;
        }
        SeasonState seasonState = seasonManager.getState(player.getWorld());
        if (seasonState == null) {
            return false;
        }
        TemperatureSnapshot snapshot = temperatureService.snapshot(player);
        SeasonEventContext context = new SeasonEventContext(
                player,
                seasonState,
                snapshot,
                System.currentTimeMillis()
        );
        triggerEvent(player, eventConfig, context);
        return true;
    }

    /**
     * 查找匹配的事件
     */
    private List<SeasonEventConfig> findMatchingEvents(SeasonState seasonState, TemperatureSnapshot snapshot) {
        List<SeasonEventConfig> matching = new ArrayList<>();

        for (SeasonEventConfig config : eventConfigs.values()) {
            // 检查是否启用
            if (!config.enabled()) {
                continue;
            }

            // 检查季节是否匹配
            if (!config.season().equals(seasonState.season())) {
                continue;
            }

            // 默认季节事件按当前季节、冷却与概率触发；温度模式仅用于兼容旧配置。
            if (config.triggersAt(snapshot.value())) {
                matching.add(config);
            }
        }

        return matching;
    }

    /**
     * 检查事件冷却是否已过期
     */
    private boolean isEventCooldownExpired(UUID playerId, String eventId, long now) {
        Map<String, Long> cooldowns = playerEventCooldowns.get(playerId);
        if (cooldowns == null) {
            return true;
        }

        Long lastTriggerTime = cooldowns.get(eventId);
        if (lastTriggerTime == null) {
            return true;
        }

        SeasonEventConfig config = eventConfigs.get(eventId);
        if (config == null) {
            return true;
        }

        return (now - lastTriggerTime) >= config.cooldownMillis();
    }

    /**
     * 更新事件冷却时间
     */
    private void updateEventCooldown(UUID playerId, String eventId, long now) {
        playerEventCooldowns
                .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(eventId, now);
    }

    private boolean rollChance(SeasonEventConfig eventConfig) {
        if (eventConfig.chancePercent() >= 100.0D) {
            return true;
        }
        if (eventConfig.chancePercent() <= 0.0D) {
            return false;
        }
        return java.util.concurrent.ThreadLocalRandom.current().nextDouble(100.0D) < eventConfig.chancePercent();
    }

    /**
     * 触发事件
     */
    private void triggerEvent(Player player, SeasonEventConfig eventConfig, SeasonEventContext context) {
        // 执行所有效果
        for (String effectId : eventConfig.effects()) {
            effectExecutor.executeEffect(player, effectId, context);
        }

        // 发送事件通知
        String message = String.format(
                "§e[%s] §f%s 事件触发！",
                context.getSeasonName(),
                eventConfig.getDisplayName()
        );
        player.sendMessage(message);
    }

    /**
     * 清理玩家数据（玩家离线时调用）
     */
    public void cleanupPlayer(UUID playerId) {
        playerEventCooldowns.remove(playerId);
        playerLastEventTime.remove(playerId);
    }

    /**
     * 设置全局事件冷却时间
     */
    public void setGlobalEventCooldown(long cooldownMillis) {
        this.globalEventCooldown = cooldownMillis;
    }

    /**
     * 获取事件配置
     */
    public SeasonEventConfig getEventConfig(String eventId) {
        return eventConfigs.get(eventId);
    }

    /**
     * 获取所有事件配置
     */
    public Collection<SeasonEventConfig> getAllEventConfigs() {
        return eventConfigs.values();
    }
}
