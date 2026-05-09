package com.liseasons.festival;

import java.time.MonthDay;

/**
 * 节日类型枚举。
 * 默认只记录服务器需要展示的节日，不再区分法定/公历/传统分类。
 */
public enum FestivalType {
    NEW_YEAR("元旦", MonthDay.of(1, 1), FestivalCategory.SERVER_CUSTOM),
    VALENTINES_DAY("情人节", MonthDay.of(2, 14), FestivalCategory.SERVER_CUSTOM),
    EASTER("复活节", MonthDay.of(4, 20), FestivalCategory.SERVER_CUSTOM),
    HALLOWEEN("万圣节", MonthDay.of(10, 31), FestivalCategory.SERVER_CUSTOM),
    CHRISTMAS("圣诞节", MonthDay.of(12, 25), FestivalCategory.SERVER_CUSTOM);

    private final String displayName;
    private final MonthDay date;
    private final FestivalCategory category;

    FestivalType(String displayName, MonthDay date, FestivalCategory category) {
        this.displayName = displayName;
        this.date = date;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MonthDay getDate() {
        return date;
    }

    public String getLunarDate() {
        return String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
    }

    public FestivalCategory getCategory() {
        return category;
    }

    /**
     * 获取指定分类的所有节日。
     */
    public static java.util.List<FestivalType> getByCategory(FestivalCategory category) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.category == category)
                .toList();
    }

    /**
     * 节日分类。
     * 保留旧分类枚举名用于兼容旧配置解析，但默认展示统一归类为“节日”。
     */
    public enum FestivalCategory {
        TRADITIONAL("节日"),
        LEGAL_HOLIDAY("节日"),
        GREGORIAN("节日"),
        SERVER_CUSTOM("节日");

        private final String displayName;

        FestivalCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
