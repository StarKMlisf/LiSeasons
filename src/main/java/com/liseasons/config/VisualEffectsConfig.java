package com.liseasons.config;

public record VisualEffectsConfig(
        long intervalTicks,
        int searchRadius,
        boolean auroraEnabled,
        int auroraChancePercent,
        boolean blizzardEnabled,
        int blizzardSnowPlacementChance,
        boolean fallingLeavesEnabled,
        boolean autumnLeafColorEnabled,
        int autumnLeafColorRadius,
        int autumnLeafColorBudget,
        boolean springCherryLeafEnabled,
        boolean treeSnowEnabled,
        boolean appleDropEnabled,
        int appleDropChancePercent
) {
    public VisualEffectsConfig {
        intervalTicks = Math.max(20L, intervalTicks);
        searchRadius = Math.max(4, Math.min(24, searchRadius));
        auroraChancePercent = clampPercent(auroraChancePercent);
        blizzardSnowPlacementChance = clampPercent(blizzardSnowPlacementChance);
        autumnLeafColorRadius = Math.max(4, Math.min(32, autumnLeafColorRadius));
        autumnLeafColorBudget = Math.max(1, Math.min(256, autumnLeafColorBudget));
        appleDropChancePercent = clampPercent(appleDropChancePercent);
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}