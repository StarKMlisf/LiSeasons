package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public final class BlizzardVisualEffect implements SeasonVisualEffect {
    private final LISeasonsPlugin plugin;

    public BlizzardVisualEffect(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void render(Player player, SeasonState state, TemperatureSnapshot snapshot) {
        if (!this.plugin.getLiConfig().visualEffectsConfig().blizzardEnabled()) {
            return;
        }
        if (state.season() != Season.WINTER || !player.getWorld().hasStorm()) {
            return;
        }

        Location location = player.getLocation();
        player.spawnParticle(Particle.SNOWFLAKE, location.clone().add(0.0D, 1.2D, 0.0D), 26, 2.8D, 1.6D, 2.8D, 0.02D);
        player.spawnParticle(Particle.CLOUD, location.clone().add(0.0D, 1.0D, 0.0D), 8, 1.8D, 0.6D, 1.8D, 0.01D);

        if (ThreadLocalRandom.current().nextInt(100) < this.plugin.getLiConfig().visualEffectsConfig().blizzardSnowPlacementChance()) {
            placeSnowNear(player.getWorld(), location);
        }
    }

    private void placeSnowNear(World world, Location center) {
        int radius = this.plugin.getLiConfig().visualEffectsConfig().searchRadius();
        int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }

        int y = world.getHighestBlockYAt(x, z);
        Block block = world.getBlockAt(x, y, z);
        Block below = block.getRelative(BlockFace.DOWN);
        if (!block.getType().isAir() || !below.getType().isSolid()) {
            return;
        }

        BlockData snow = Material.SNOW.createBlockData();
        block.setBlockData(snow, false);
    }
}
