package com.liseasons.season;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.config.PluginConfig;
import com.liseasons.config.SeasonEffectConfig;
import com.liseasons.util.PlatformUtil;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.World;

public final class SeasonWeatherService {
    private final LISeasonsPlugin plugin;

    public SeasonWeatherService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void refresh(World world, SeasonState state, PluginConfig config) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        SeasonEffectConfig effectConfig = config.seasonEffects().get(state.season());
        if (effectConfig == null) {
            return;
        }

        Runnable updateTask = () -> applyWeather(world, state.season(), effectConfig);
        if (PlatformUtil.isFolia(this.plugin)) {
            int chunkX = world.getSpawnLocation().getBlockX() >> 4;
            int chunkZ = world.getSpawnLocation().getBlockZ() >> 4;
            this.plugin.getServer().getRegionScheduler().execute(this.plugin, world, chunkX, chunkZ, updateTask);
            return;
        }
        updateTask.run();
    }

    private void applyWeather(World world, Season season, SeasonEffectConfig effectConfig) {
        boolean shouldRain;
        boolean shouldThunder;

        if (season == Season.WINTER) {
            // Minecraft 的“下雪”依赖 storm=true；如果冬天关闭 storm，雨和雪都会消失。
            shouldRain = true;
            // 冬天默认关闭雷暴，避免暴雪天气里出现雷暴。
            shouldThunder = false;
        } else {
            shouldRain = rollChance(effectConfig.rainChance());
            shouldThunder = shouldRain && rollChance(effectConfig.thunderChance());
        }

        world.setStorm(shouldRain);
        world.setThundering(shouldThunder);
        world.setWeatherDuration(shouldRain ? randomTicks(6000, 18000) : randomTicks(12000, 24000));
        world.setThunderDuration(shouldThunder ? randomTicks(4000, 12000) : 0);
    }

    private boolean rollChance(int chance) {
        if (chance <= 0) {
            return false;
        }
        if (chance >= 100) {
            return true;
        }
        return ThreadLocalRandom.current().nextInt(100) < chance;
    }

    private int randomTicks(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
