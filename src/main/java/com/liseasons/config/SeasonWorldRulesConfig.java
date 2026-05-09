package com.liseasons.config;

public record SeasonWorldRulesConfig(
        SeasonWaterCycleConfig waterCycleConfig,
        SeasonMobConfig mobConfig
) {
}
