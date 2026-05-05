package com.liseasons.config;

public record TemperatureConditionConfig(
        double shade,
        double night,
        double nightMin,
        double dayMax,
        double rain,
        double thunder,
        double underground,
        double highAltitudeStartY,
        double highAltitude,
        double armorPerPiece,
        double heldTorch,
        double heldSoulTorch,
        double heldLavaBucket,
        double nearbyFire,
        double nearbySoulFire,
        double nearbyCampfire,
        double nearbySoulCampfire,
        double nearbyLava,
        double nearbyIce,
        double nearbyPackedIce,
        double nearbyBlueIce,
        int nearbyHeatRange,
        double waterDefault,
        double waterWinter,
        double tropicalSummerDay
) {
}
