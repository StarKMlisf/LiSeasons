package com.liseasons.event;

import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import org.bukkit.entity.Player;

/**
 * 季节事件效果接口
 * 定义季节事件的效果执行方式
 */
public interface SeasonEventEffect {
    /**
     * 执行事件效果
     */
    void apply(Player player, SeasonEventContext context);

    /**
     * 获取效果的唯一标识
     */
    String getId();

    /**
     * 获取效果的显示名称
     */
    String getDisplayName();
}
