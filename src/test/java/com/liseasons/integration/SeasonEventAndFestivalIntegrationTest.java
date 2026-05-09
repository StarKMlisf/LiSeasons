package com.liseasons.integration;

import com.liseasons.event.SeasonEventConfig;
import com.liseasons.event.SeasonEventType;
import com.liseasons.festival.FestivalConfig;
import com.liseasons.festival.FestivalType;
import com.liseasons.festival.FestivalType.FestivalCategory;
import com.liseasons.season.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 季节事件与节日系统集成测试
 */
public class SeasonEventAndFestivalIntegrationTest {

    @Test
    public void testSeasonEventAndFestivalCoexistence() {
        SeasonEventConfig eventConfig = new SeasonEventConfig(
                "spring-fair-weather",
                SeasonEventType.FAIR_WEATHER,
                Season.SPRING,
                15.0,
                "above",
                20.0,
                1,
                5000,
                100.0,
                "春季晴朗测试事件",
                List.of("PARTICLE:HAPPY_VILLAGER:10:0.5:0.5:0.5"),
                true
        );

        FestivalConfig festivalConfig = new FestivalConfig(
                "new-year",
                FestivalType.NEW_YEAR,
                FestivalType.NEW_YEAR.getDate(),
                FestivalType.NEW_YEAR.getDisplayName(),
                FestivalCategory.SERVER_CUSTOM,
                "元旦默认测试配置",
                true,
                10
        );

        assertEquals(Season.SPRING, eventConfig.season());
        assertEquals(FestivalType.NEW_YEAR, festivalConfig.type());
    }

    @Test
    public void testSeasonEventTypeCoverage() {
        assertEquals(16, SeasonEventType.values().length, "应该有16种季节事件类型");
        assertEquals(4, SeasonEventType.getBySeason(Season.SPRING).size(), "春季应该有4种可用事件类型");
        assertEquals(4, SeasonEventType.getBySeason(Season.SUMMER).size(), "夏季应该有4种可用事件类型");
        assertEquals(4, SeasonEventType.getBySeason(Season.AUTUMN).size(), "秋季应该有4种可用事件类型");
        assertEquals(4, SeasonEventType.getBySeason(Season.WINTER).size(), "冬季应该有4种可用事件类型");
    }

    @Test
    public void testSharedSeasonEventTypes() {
        assertTrue(SeasonEventType.FAIR_WEATHER.supportsSeason(Season.SPRING));
        assertTrue(SeasonEventType.SUMMER_CLEAR_SKY.supportsSeason(Season.SUMMER));
        assertTrue(SeasonEventType.HARVEST_SEASON.supportsSeason(Season.AUTUMN));
        assertTrue(SeasonEventType.WINTER_CELEBRATION.supportsSeason(Season.WINTER));
    }

    @Test
    public void testFestivalCoverageByCategories() {
        assertEquals(0, FestivalType.getByCategory(FestivalCategory.TRADITIONAL).size());
        assertEquals(0, FestivalType.getByCategory(FestivalCategory.LEGAL_HOLIDAY).size());
        assertEquals(0, FestivalType.getByCategory(FestivalCategory.GREGORIAN).size());
        assertEquals(5, FestivalType.getByCategory(FestivalCategory.SERVER_CUSTOM).size());
        assertEquals(5, FestivalType.values().length, "应该只保留5个默认节日");
    }

    @Test
    public void testSeasonEventTriggerModes() {
        SeasonEventConfig rangeConfig = new SeasonEventConfig(
                "test-range",
                SeasonEventType.HOT_SUN,
                Season.SUMMER,
                25.0,
                "season",
                35.0,
                1,
                5000,
                100.0,
                "不按温度的季节自然触发测试事件",
                List.of(),
                true
        );

        assertTrue(rangeConfig.triggersAt(-10.0));
        assertTrue(rangeConfig.triggersAt(30.0));
        assertTrue(rangeConfig.triggersAt(45.0));
    }
}
