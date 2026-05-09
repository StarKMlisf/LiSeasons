package com.liseasons.event;

import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.SeasonState;
import org.bukkit.entity.Player;

/**
 * 季节事件上下文
 * 包含事件触发时的所有相关信息
 */
public record SeasonEventContext(
        Player player,
        SeasonState seasonState,
        TemperatureSnapshot temperatureSnapshot,
        long timestamp
) {
    /**
     * 获取事件发生的季节
     */
    public String getSeasonName() {
        return seasonState.season().key();
    }

    /**
     * 获取事件发生的节气
     */
    public String getSolarTermName() {
        return seasonState.solarTerm().key();
    }

    /**
     * 获取玩家体感温度
     */
    public double getBodyTemperature() {
        return temperatureSnapshot.value();
    }

    /**
     * 获取空气温度
     */
    public double getAirTemperature() {
        return temperatureSnapshot.airTemperature();
    }
}
