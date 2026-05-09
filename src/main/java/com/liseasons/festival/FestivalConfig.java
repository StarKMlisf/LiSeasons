package com.liseasons.festival;

import java.time.MonthDay;

/**
 * 节日配置
 */
public record FestivalConfig(
        String festivalId,           // 节日唯一ID
        FestivalType type,           // 节日类型
        MonthDay date,               // 节日日期
        String displayName,          // 显示名称
        FestivalType.FestivalCategory category,  // 节日分类
        String description,           // 节日说明
        boolean enabled,             // 是否启用
        int priority                 // 优先级
) {
    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryName() {
        return category.getDisplayName();
    }
}
