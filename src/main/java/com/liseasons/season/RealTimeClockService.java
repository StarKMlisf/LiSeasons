package com.liseasons.season;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.util.PlatformUtil;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.time.LocalTime;

public final class RealTimeClockService {
    private final LISeasonsPlugin plugin;

    public RealTimeClockService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void tickWorlds() {
        if (!this.plugin.getLiConfig().syncRealTime()) {
            restoreDaylightCycle();
            return;
        }
        syncAllWorlds();
    }

    private void syncAllWorlds() {
        for (World world : this.plugin.getServer().getWorlds()) {
            if (!this.plugin.getSeasonManager().isEnabled(world) || world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }
            syncWorld(world);
        }
    }

    private void syncWorld(World world) {
        runWorldTask(world, () -> {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(toMinecraftTime(LocalTime.now()));
        });
    }

    private void restoreDaylightCycle() {
        for (World world : this.plugin.getServer().getWorlds()) {
            if (!this.plugin.getSeasonManager().isEnabled(world) || world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }
            runWorldTask(world, () -> {
                Boolean current = world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
                if (Boolean.FALSE.equals(current)) {
                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                }
            });
        }
    }

    private void runWorldTask(World world, Runnable task) {
        if (PlatformUtil.isFolia(this.plugin)) {
            int chunkX = world.getSpawnLocation().getBlockX() >> 4;
            int chunkZ = world.getSpawnLocation().getBlockZ() >> 4;
            this.plugin.getServer().getRegionScheduler().execute(this.plugin, world, chunkX, chunkZ, task);
            return;
        }
        task.run();
    }

    private long toMinecraftTime(LocalTime localTime) {
        long seconds = localTime.toSecondOfDay();
        long minecraftTime = seconds * 24000L / 86400L;
        return (minecraftTime + 18000L) % 24000L;
    }
}
