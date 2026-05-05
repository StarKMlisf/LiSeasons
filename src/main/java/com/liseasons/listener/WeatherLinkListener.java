package com.liseasons.listener;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.config.SeasonEffectConfig;
import com.liseasons.season.SeasonState;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public final class WeatherLinkListener implements Listener {
    private final LISeasonsPlugin plugin;

    public WeatherLinkListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!event.toWeatherState()) {
            return;
        }

        World world = event.getWorld();
        SeasonState state = this.plugin.getSeasonManager().getState(world);
        if (state == null) {
            return;
        }

        SeasonEffectConfig effectConfig = this.plugin.getLiConfig().seasonEffects().get(state.season());
        if (effectConfig == null) {
            return;
        }

        if (!roll(effectConfig.rainChance())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        if (!event.toThunderState()) {
            return;
        }

        World world = event.getWorld();
        SeasonState state = this.plugin.getSeasonManager().getState(world);
        if (state == null) {
            return;
        }

        SeasonEffectConfig effectConfig = this.plugin.getLiConfig().seasonEffects().get(state.season());
        if (effectConfig == null) {
            return;
        }

        if (!roll(effectConfig.thunderChance())) {
            event.setCancelled(true);
        }
    }

    private boolean roll(int chance) {
        if (chance <= 0) {
            return false;
        }
        if (chance >= 100) {
            return true;
        }
        return ThreadLocalRandom.current().nextInt(100) < chance;
    }
}
