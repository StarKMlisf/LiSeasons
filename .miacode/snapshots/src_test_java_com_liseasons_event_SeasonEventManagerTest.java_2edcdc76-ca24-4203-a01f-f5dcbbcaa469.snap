package com.liseasons.event;

import com.liseasons.season.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 季节事件管理器单元测试
 */
public class SeasonEventManagerTest {

    @BeforeEach
    public void setUp() {
        // 初始化测试环境
    }

    @Test
    public void testSeasonEventTypeEnumeration() {
        // 验证所有16种事件类型都已定义
        SeasonEventType[] types = SeasonEventType.values();
        assertEquals(16, types.length, "应该有16种季节事件类型");
    }

    @Test
    public void testSeasonEventTypesBySeasonSpring() {
        // 验证春季事件
        List<SeasonEventType> springEvents = SeasonEventType.getBySeason(Season.SPRING);
        assertTrue(springEvents.size() > 0, "春季应该有事件");
        assertTrue(springEvents.stream().allMatch(e -> e.supportsSeason(Season.SPRING)), "所有春季事件都应该支持SPRING");
    }

    @Test
    public void testSeasonEventTypesBySeasonSummer() {
        // 验证夏季事件
        List<SeasonEventType> summerEvents = SeasonEventType.getBySeason(Season.SUMMER);
        assertTrue(summerEvents.size() > 0, "夏季应该有事件");
        assertTrue(summerEvents.stream().allMatch(e -> e.supportsSeason(Season.SUMMER)), "所有夏季事件都应该支持SUMMER");
    }

    @Test
    public void testSeasonEventTypesBySeasonAutumn() {
        // 验证秋季事件
        List<SeasonEventType> autumnEvents = SeasonEventType.getBySeason(Season.AUTUMN);
        assertTrue(autumnEvents.size() > 0, "秋季应该有事件");
        assertTrue(autumnEvents.stream().allMatch(e -> e.supportsSeason(Season.AUTUMN)), "所有秋季事件都应该支持AUTUMN");
    }

    @Test
    public void testSeasonEventTypesBySeasonWinter() {
        // 验证冬季事件
        List<SeasonEventType> winterEvents = SeasonEventType.getBySeason(Season.WINTER);
        assertTrue(winterEvents.size() > 0, "冬季应该有事件");
        assertTrue(winterEvents.stream().allMatch(e -> e.supportsSeason(Season.WINTER)), "所有冬季事件都应该支持WINTER");
    }

    @Test
    public void testSeasonEventConfigTriggerConditions() {
        // 测试事件触发条件
        SeasonEventConfig config = new SeasonEventConfig(
                "test-event",
                SeasonEventType.FAIR_WEATHER,
                Season.SPRING,
                15.0,
                "season",
                20.0,
                1,
                5000,
                100.0,
                "测试事件",
                List.of("PARTICLE:CHERRY_LEAVES:10:0.5:0.5:0.5"),
                true
        );

        // 默认 "season" 模式不按温度阈值触发
        assertTrue(config.triggersAt(-20.0), "season 模式不应因低温阻止触发");
        assertTrue(config.triggersAt(15.0), "season 模式应该按季节自然触发");
        assertTrue(config.triggersAt(40.0), "season 模式不应因高温阻止触发");
    }

    @Test
    public void testSeasonEventConfigTriggerConditionsAboveCompatibility() {
        // 测试旧配置 "above" 模式兼容
        SeasonEventConfig config = new SeasonEventConfig(
                "test-event",
                SeasonEventType.HOT_SUN,
                Season.SUMMER,
                25.0,
                "above",
                35.0,
                1,
                5000,
                100.0,
                "测试事件",
                List.of(),
                true
        );

        assertTrue(config.triggersAt(25.0), "温度25.0应该触发 (>= 25.0)");
        assertFalse(config.triggersAt(24.9), "温度24.9不应该触发 (< 25.0)");
    }

    @Test
    public void testSeasonEventConfigTriggerConditionsBelow() {
        // 测试 "below" 模式
        SeasonEventConfig config = new SeasonEventConfig(
                "test-event",
                SeasonEventType.HEAVY_SNOW,
                Season.WINTER,
                -5.0,
                "below",
                0.0,
                1,
                5000,
                100.0,
                "测试事件",
                List.of("POTION:SLOWNESS:1:100"),
                true
        );

        assertTrue(config.triggersAt(-5.0), "温度-5.0应该触发 (<= -5.0)");
        assertTrue(config.triggersAt(-10.0), "温度-10.0应该触发 (<= -5.0)");
        assertFalse(config.triggersAt(-4.9), "温度-4.9不应该触发 (> -5.0)");
    }

    @Test
    public void testSeasonEventConfigTriggerConditionsRange() {
        // 测试 "range" 模式
        SeasonEventConfig config = new SeasonEventConfig(
                "test-event",
                SeasonEventType.HOT_SUN,
                Season.SUMMER,
                25.0,
                "range",
                35.0,
                1,
                5000,
                100.0,
                "测试事件",
                List.of("MESSAGE:炎热高温"),
                true
        );

        assertTrue(config.triggersAt(25.0), "温度25.0应该触发 (在范围内)");
        assertTrue(config.triggersAt(30.0), "温度30.0应该触发 (在范围内)");
        assertTrue(config.triggersAt(35.0), "温度35.0应该触发 (在范围内)");
        assertFalse(config.triggersAt(24.9), "温度24.9不应该触发 (低于范围)");
        assertFalse(config.triggersAt(35.1), "温度35.1不应该触发 (高于范围)");
    }

    @Test
    public void testSeasonEventConfigDisplayName() {
        // 测试事件显示名称
        SeasonEventConfig config = new SeasonEventConfig(
                "fair-weather-spring",
                SeasonEventType.FAIR_WEATHER,
                Season.SPRING,
                15.0,
                "above",
                20.0,
                1,
                5000,
                100.0,
                "测试事件",
                List.of(),
                true
        );

        assertEquals("风和日丽", config.getDisplayName(), "显示名称应该是风和日丽");
    }
}
