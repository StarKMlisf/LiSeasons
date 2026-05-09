package com.liseasons.config;

import com.liseasons.event.SeasonEventConfig;
import com.liseasons.festival.FestivalConfig;
import com.liseasons.season.Season;
import com.liseasons.season.SolarTerm;
import com.liseasons.visual.SeasonBiomeColorConfig;
import java.time.MonthDay;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record PluginConfig(
        boolean useGregorianTrigger,
        long refreshIntervalTicks,
        boolean syncRealTime,
        long realTimeSyncIntervalTicks,
        Map<Season, Integer> seasonDurationDays,
        Map<Season, MonthDay> seasonStartDates,
        Map<SolarTerm, MonthDay> gregorianDates,
        Map<Season, SeasonEffectConfig> seasonEffects,
        SeasonWorldRulesConfig seasonWorldRulesConfig,
        CalendarConfig calendarConfig,
        TemperatureConfig temperatureConfig,
        VisualEffectsConfig visualEffectsConfig,
        SeasonBiomeColorConfig biomeColorConfig,
        boolean enableAllWorlds,
        List<String> allowedWorlds,
        List<String> deniedWorlds,
        boolean broadcastChange,
        boolean joinNotify,
        boolean changeParticleEnabled,
        String changeParticle,
        int changeParticleCount,
        boolean changeSoundEnabled,
        String changeSound,
        float changeSoundVolume,
        float changeSoundPitch,
        boolean customCropsEnabled,
        List<SeasonEventConfig> seasonEventConfigs,
        List<FestivalConfig> festivalConfigs
) {
    public PluginConfig {
        seasonDurationDays = Collections.unmodifiableMap(new EnumMap<>(seasonDurationDays));
        seasonStartDates = Collections.unmodifiableMap(new EnumMap<>(seasonStartDates));
        gregorianDates = Collections.unmodifiableMap(new EnumMap<>(gregorianDates));
        seasonEffects = Collections.unmodifiableMap(new EnumMap<>(seasonEffects));
        allowedWorlds = List.copyOf(allowedWorlds);
        deniedWorlds = List.copyOf(deniedWorlds);
        seasonEventConfigs = List.copyOf(seasonEventConfigs);
        festivalConfigs = List.copyOf(festivalConfigs);
    }
}
