package com.liseasons.config;

public record TemperatureRangeConfig(
        double min,
        double max
) {
    public TemperatureRangeConfig {
        if (min > max) {
            double originalMin = min;
            min = max;
            max = originalMin;
        }
    }

    public double midpoint() {
        return (this.min + this.max) / 2.0D;
    }
}
