package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class FallingLeavesVisualEffect implements SeasonVisualEffect {
    private final LISeasonsPlugin plugin;

    public FallingLeavesVisualEffect(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void render(Player player, SeasonState state, TemperatureSnapshot snapshot) {
        if (!this.plugin.getLiConfig().visualEffectsConfig().fallingLeavesEnabled()) {
            return;
        }
        if (state.season() == Season.WINTER) {
            return;
        }

        Block leafBlock = findNearbyLeaves(player.getWorld(), player.getLocation(), this.plugin.getLiConfig().visualEffectsConfig().searchRadius());
        if (leafBlock == null) {
            return;
        }

        Particle.DustOptions dust = switch (state.season()) {
            case SPRING -> new Particle.DustOptions(Color.fromRGB(135, 220, 120), 1.3F);
            case SUMMER -> new Particle.DustOptions(Color.fromRGB(88, 185, 96), 1.3F);
            case AUTUMN -> new Particle.DustOptions(Color.fromRGB(230, 162, 72), 1.4F);
            case WINTER -> new Particle.DustOptions(Color.fromRGB(210, 210, 210), 1.2F);
        };
        Location particleBase = leafBlock.getLocation().add(0.5D, 0.9D, 0.5D);
        player.spawnParticle(Particle.DUST, particleBase, 4, 0.4D, 0.5D, 0.4D, 0.01D, dust);
    }

    private Block findNearbyLeaves(World world, Location center, int radius) {
        for (int attempt = 0; attempt < 14; attempt++) {
            int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int y = center.getBlockY() + ThreadLocalRandom.current().nextInt(-2, 5);
            int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Block block = world.getBlockAt(x, y, z);
            if (Tag.LEAVES.isTagged(block.getType())) {
                return block;
            }
        }
        return null;
    }
}
