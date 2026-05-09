package com.liseasons.event;

import com.liseasons.season.Season;
import java.util.List;

/**
 * 季节事件配置
 * 定义单个季节事件的触发条件和效果
 */
public record SeasonEventConfig(
        String eventId,                    // 事件唯一ID
        SeasonEventType type,              // 事件类型
        Season season,                     // 所属季节
        double triggerTemperature,         // 兼容旧配置的触发温度阈值
        String triggerMode,                // 触发模式: "season" | "always" | "above" | "below" | "range"
        double triggerTemperatureMax,      // 兼容旧配置的触发温度上限
        int priority,                      // 优先级（高优先级优先触发）
        long cooldownMillis,               // 事件冷却时间（毫秒）
        double chancePercent,              // 触发概率，取值 0-100
        String description,                 // 事件说明
        List<String> effects,              // 效果列表
        boolean enabled                    // 是否启用
) {
    /**
     * 检查事件是否满足触发条件。
     * 默认季节事件不按温度触发；above/below/range 仅作为旧配置兼容模式保留。
     */
    public boolean triggersAt(double temperature) {
        return switch (triggerMode) {
            case "season", "always" -> true;
            case "above" -> temperature >= triggerTemperature;
            case "below" -> temperature <= triggerTemperature;
            case "range" -> temperature >= triggerTemperature && temperature <= triggerTemperatureMax;
            default -> false;
        };
    }

    /**
     * 获取事件的显示名称
     */
    public String getDisplayName() {
        return type.getDisplayName();
    }
}
