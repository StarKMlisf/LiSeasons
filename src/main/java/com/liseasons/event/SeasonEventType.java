package com.liseasons.event;

import com.liseasons.season.Season;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 季节事件类型枚举。
 * 每个季节默认提供 4 个不依赖温度阈值的主题事件。
 */
public enum SeasonEventType {
    // 春季事件
    FAIR_WEATHER("风和日丽", Season.SPRING),
    SPRING_BREEZE("微风习习", Season.SPRING),
    SPRING_COLD("春寒料峭", Season.SPRING),
    SPRING_RAIN("春雨绵绵", Season.SPRING),

    // 夏季事件
    HOT_SUN("烈日炎炎", Season.SUMMER),
    SUMMER_CLEAR_SKY("盛夏晴空", Season.SUMMER),
    METEOR_NIGHT("流星之夜", Season.SUMMER),
    THUNDERSTORM_SEASON("雷暴季", Season.SUMMER),

    // 秋季事件
    HARVEST_SEASON("丰收时节", Season.AUTUMN),
    FALLING_LEAVES("落叶纷飞", Season.AUTUMN),
    AUTUMN_RAIN("秋雨连绵", Season.AUTUMN),
    PUMPKIN_FESTIVAL("南瓜庆典", Season.AUTUMN),

    // 冬季事件
    HEAVY_SNOW("大雪纷飞", Season.WINTER),
    EXTREME_COLD_NIGHT("极寒之夜", Season.WINTER),
    FROST_MORNING_FOG("冰霜晨雾", Season.WINTER),
    WINTER_CELEBRATION("冬日庆典", Season.WINTER);

    private final String displayName;
    private final Season primarySeason;
    private final Set<Season> seasons;

    SeasonEventType(String displayName, Season primarySeason, Season... extraSeasons) {
        this.displayName = displayName;
        this.primarySeason = primarySeason;
        this.seasons = EnumSet.of(primarySeason, extraSeasons);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Season getSeason() {
        return primarySeason;
    }

    public Set<Season> getSeasons() {
        return Set.copyOf(seasons);
    }

    public boolean supportsSeason(Season season) {
        return seasons.contains(season);
    }

    /**
     * 获取指定季节的所有事件类型。
     */
    public static List<SeasonEventType> getBySeason(Season season) {
        return Arrays.stream(values())
                .filter(type -> type.supportsSeason(season))
                .toList();
    }
}
