package com.liseasons.climate;

import java.util.List;

public record TemperatureProfile(
        double airTemperature,
        double bodyTemperature,
        double wetness,
        List<TemperatureModifier> modifiers
) {
    public TemperatureProfile {
        modifiers = List.copyOf(modifiers);
    }
}
