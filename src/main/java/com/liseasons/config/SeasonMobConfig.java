package com.liseasons.config;

public record SeasonMobConfig(
        boolean enabled,
        int summerZombieToHuskChancePercent,
        int winterSkeletonToStrayChancePercent
) {
    public SeasonMobConfig {
        summerZombieToHuskChancePercent = clampPercent(summerZombieToHuskChancePercent);
        winterSkeletonToStrayChancePercent = clampPercent(winterSkeletonToStrayChancePercent);
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
