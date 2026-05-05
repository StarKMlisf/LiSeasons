package com.liseasons.visual;

import com.liseasons.season.Season;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.NamespacedKey;

public record SeasonBiomeColorConfig(
        boolean enabled,
        long intervalTicks,
        int radius,
        Map<Season, Map<NamespacedKey, NamespacedKey>> biomeMappings,
        Map<Season, Map<NamespacedKey, BiomeVisualColors>> biomeVisuals,
        boolean biomeSpoofEnabled,
        int spoofRadiusChunks,
        int spoofBudgetChunksPerTick,
        int spoofStepXz,
        int spoofStepY,
        Map<Season, NamespacedKey> spoofTargets
) {
    public SeasonBiomeColorConfig {
        intervalTicks = Math.max(20L, intervalTicks);
        radius = Math.max(4, Math.min(32, radius));
        Map<Season, Map<NamespacedKey, NamespacedKey>> mappingCopy = new EnumMap<>(Season.class);
        Map<Season, Map<NamespacedKey, BiomeVisualColors>> visualCopy = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            Map<NamespacedKey, NamespacedKey> mappings = biomeMappings.getOrDefault(season, Map.of());
            Map<NamespacedKey, BiomeVisualColors> visuals = biomeVisuals.getOrDefault(season, Map.of());
            mappingCopy.put(season, Collections.unmodifiableMap(mappings));
            visualCopy.put(season, Collections.unmodifiableMap(visuals));
        }
        biomeMappings = Collections.unmodifiableMap(mappingCopy);
        biomeVisuals = Collections.unmodifiableMap(visualCopy);
        spoofRadiusChunks = Math.max(1, Math.min(32, spoofRadiusChunks));
        spoofBudgetChunksPerTick = Math.max(1, Math.min(64, spoofBudgetChunksPerTick));
        spoofStepXz = Math.max(1, Math.min(16, spoofStepXz));
        spoofStepY = Math.max(1, Math.min(32, spoofStepY));
        Map<Season, NamespacedKey> targetCopy = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            NamespacedKey target = spoofTargets.get(season);
            if (target != null) {
                targetCopy.put(season, target);
            }
        }
        spoofTargets = Collections.unmodifiableMap(targetCopy);
    }
}
