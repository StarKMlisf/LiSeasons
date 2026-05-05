package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class TreeSnowVisualEffect implements SeasonVisualEffect {
    private final LISeasonsPlugin plugin;

    public TreeSnowVisualEffect(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void render(Player player, SeasonState state, TemperatureSnapshot snapshot) {
        if (!this.plugin.getLiConfig().visualEffectsConfig().treeSnowEnabled()) {
            return;
        }
        if (state.season() != Season.WINTER) {
            return;
        }

        Block leafBlock = findNearbyLeaves(player.getWorld(), player.getLocation(), this.plugin.getLiConfig().visualEffectsConfig().searchRadius());
        if (leafBlock == null) {
            return;
        }

        Location location = leafBlock.getLocation().add(0.5D, 0.8D, 0.5D);
        player.spawnParticle(Particle.SNOWFLAKE, location, 6, 0.35D, 0.45D, 0.35D, 0.01D);
    }

    private Block findNearbyLeaves(World world, Location center, int radius) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int y = center.getBlockY() + ThreadLocalRandom.current().nextInt(-1, 6);
            int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Block block = world.getBlockAt(x, y, z);
            if (Tag.LEAVES.isTagged(block.getType())) {
                return block;
            }
        }
        return null;
    }
}
