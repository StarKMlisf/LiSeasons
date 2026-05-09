package com.liseasons.climate;

public record TemperatureSnapshot(
        double value,
        String description,
        double airTemperature,
        double wetness
) {
    public TemperatureSnapshot(double value, String description) {
        this(value, description, value, 0.0D);
    }
}
