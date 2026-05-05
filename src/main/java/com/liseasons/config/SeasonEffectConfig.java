package com.liseasons.config;

public record SeasonEffectConfig(
        double uncoveredCropGrowthMultiplier,
        boolean uncoveredCropStop,
        int rainChance,
        int thunderChance
) {
    public SeasonEffectConfig {
        uncoveredCropGrowthMultiplier = Math.max(0.0D, uncoveredCropGrowthMultiplier);
        rainChance = clampChance(rainChance);
        thunderChance = clampChance(thunderChance);
    }

    private static int clampChance(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
