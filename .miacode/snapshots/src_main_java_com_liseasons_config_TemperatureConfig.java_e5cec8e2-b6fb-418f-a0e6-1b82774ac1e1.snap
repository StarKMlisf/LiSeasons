package com.liseasons.config;

import com.liseasons.season.Season;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record TemperatureConfig(
        Map<Season, Double> seasonBaseTemperatures,
        boolean dailyRandomBaseTemperature,
        Map<Season, TemperatureRangeConfig> seasonBaseRanges,
        boolean solarTermModifierEnabled,
        Map<String, Double> solarTermModifiers,
        double vanillaBiomeScale,
        Map<String, Double> biomeOverrides,
        List<String> tropicalBiomes,
        TemperatureConditionConfig conditionConfig,
        int displayDecimals,
        boolean actionBarEnabled,
        long updateIntervalTicks,
        boolean temperatureEventsEnabled,
        boolean highestTemperatureEventOnly,
        long temperatureEventCooldownMillis,
        double bodyTemperatureStep,
        double wetnessRecoveryPerStep,
        long wetnessRecoveryIntervalTicks,
        Map<String, Double> armorWarmth,
        double leatherDisabledAbove,
        Map<String, Double> heldItemTemperatureModifiers,
        List<TemperatureEventConfig> temperatureEvents
) {
    public TemperatureConfig {
        seasonBaseTemperatures = Collections.unmodifiableMap(new EnumMap<>(seasonBaseTemperatures));
        seasonBaseRanges = Collections.unmodifiableMap(new EnumMap<>(seasonBaseRanges));
        solarTermModifiers = Collections.unmodifiableMap(new LinkedHashMap<>(solarTermModifiers));
        biomeOverrides = Collections.unmodifiableMap(new LinkedHashMap<>(biomeOverrides));
        armorWarmth = Collections.unmodifiableMap(new LinkedHashMap<>(armorWarmth));
        heldItemTemperatureModifiers = Collections.unmodifiableMap(new LinkedHashMap<>(heldItemTemperatureModifiers));
        tropicalBiomes = List.copyOf(tropicalBiomes);
        displayDecimals = Math.max(0, Math.min(2, displayDecimals));
        updateIntervalTicks = Math.max(20L, updateIntervalTicks);
        vanillaBiomeScale = Math.max(0.0D, Math.min(16.0D, vanillaBiomeScale));
        temperatureEventCooldownMillis = Math.max(0L, temperatureEventCooldownMillis);
        bodyTemperatureStep = Math.max(0.1D, bodyTemperatureStep);
        wetnessRecoveryPerStep = Math.max(0.0D, wetnessRecoveryPerStep);
        wetnessRecoveryIntervalTicks = Math.max(20L, wetnessRecoveryIntervalTicks);
        temperatureEvents = List.copyOf(temperatureEvents);
    }
}
