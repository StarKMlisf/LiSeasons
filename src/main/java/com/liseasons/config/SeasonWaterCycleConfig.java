package com.liseasons.config;

import java.util.List;

public record SeasonWaterCycleConfig(
        boolean enabled,
        long intervalTicks,
        int searchRadius,
        int winterFreezeChancePercent,
        int springMeltChancePercent,
        int transitionMeltRadiusChunks,
        int transitionMeltBudgetPerTick,
        List<String> exemptBiomes
) {
    public SeasonWaterCycleConfig {
        intervalTicks = Math.max(20L, intervalTicks);
        searchRadius = Math.max(2, Math.min(12, searchRadius));
        winterFreezeChancePercent = clampPercent(winterFreezeChancePercent);
        springMeltChancePercent = clampPercent(springMeltChancePercent);
        transitionMeltRadiusChunks = Math.max(1, Math.min(12, transitionMeltRadiusChunks));
        transitionMeltBudgetPerTick = Math.max(16, Math.min(4096, transitionMeltBudgetPerTick));
        exemptBiomes = List.copyOf(exemptBiomes);
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
