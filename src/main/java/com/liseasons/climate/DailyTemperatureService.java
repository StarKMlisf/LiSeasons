package com.liseasons.climate;

import com.liseasons.config.TemperatureConfig;
import com.liseasons.config.TemperatureRangeConfig;
import com.liseasons.season.Season;
import java.util.Map;
import java.util.Random;
import org.bukkit.World;

public final class DailyTemperatureService {
    public double baseTemperature(World world, Season season, TemperatureConfig config) {
        if (!config.dailyRandomBaseTemperature()) {
            return config.seasonBaseTemperatures().getOrDefault(season, 18.0D);
        }

        TemperatureRangeConfig range = config.seasonBaseRanges().get(season);
        if (range == null) {
            return config.seasonBaseTemperatures().getOrDefault(season, 18.0D);
        }

        long day = Math.floorDiv(world.getFullTime(), 24000L);
        long seed = world.getUID().getMostSignificantBits()
                ^ world.getUID().getLeastSignificantBits()
                ^ (day * 341873128712L)
                ^ (season.ordinal() * 132897987541L);
        Random random = new Random(seed);
        return range.min() + (range.max() - range.min()) * random.nextDouble();
    }

    public double solarTermModifier(String solarTermKey, TemperatureConfig config) {
        if (!config.solarTermModifierEnabled()) {
            return 0.0D;
        }
        Map<String, Double> modifiers = config.solarTermModifiers();
        return modifiers.getOrDefault(solarTermKey, 0.0D);
    }
}
