package com.liseasons.config;

import java.util.List;

public record TemperatureEventConfig(
        String key,
        double temperature,
        String mode,
        int priority,
        long cooldownMillis,
        List<String> effects
) {
    public TemperatureEventConfig {
        key = key == null || key.isBlank() ? "unnamed" : key;
        mode = mode == null || mode.isBlank() ? defaultMode(temperature) : mode.toLowerCase(java.util.Locale.ROOT);
        priority = Math.max(0, priority);
        cooldownMillis = Math.max(0L, cooldownMillis);
        effects = List.copyOf(effects);
    }

    public boolean triggersAt(double currentTemperature) {
        if ("above".equals(this.mode)) {
            return currentTemperature >= this.temperature;
        }
        if ("below".equals(this.mode)) {
            return currentTemperature <= this.temperature;
        }
        return Math.abs(currentTemperature - this.temperature) < 0.0001D;
    }

    private static String defaultMode(double temperature) {
        return temperature >= 0.0D ? "above" : "below";
    }
}
