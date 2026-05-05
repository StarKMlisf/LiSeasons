package com.liseasons.visual;

public record BiomeVisualColors(
        Integer skyColor,
        Integer waterColor,
        Integer waterFogColor,
        Integer grassColor,
        Integer foliageColor,
        Integer fogColor
) {
    public boolean hasAnyColor() {
        return this.skyColor != null
                || this.waterColor != null
                || this.waterFogColor != null
                || this.grassColor != null
                || this.foliageColor != null
                || this.fogColor != null;
    }
}
