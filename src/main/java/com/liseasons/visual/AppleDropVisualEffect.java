package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class AppleDropVisualEffect implements SeasonVisualEffect {
    private final LISeasonsPlugin plugin;

    public AppleDropVisualEffect(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void render(Player player, SeasonState state, TemperatureSnapshot snapshot) {
        if (!this.plugin.getLiConfig().visualEffectsConfig().appleDropEnabled()) {
            return;
        }
        if (state.season() != Season.SUMMER) {
            return;
        }
        if (ThreadLocalRandom.current().nextInt(100) >= this.plugin.getLiConfig().visualEffectsConfig().appleDropChancePercent()) {
            return;
        }

        Block oakLeaf = findNearbyOakLeaves(player.getWorld(), player.getLocation(), this.plugin.getLiConfig().visualEffectsConfig().searchRadius());
        if (oakLeaf == null) {
            return;
        }

        Item item = player.getWorld().dropItemNaturally(oakLeaf.getLocation().add(0.5D, 0.6D, 0.5D), new ItemStack(Material.APPLE));
        item.setVelocity(new Vector(
                ThreadLocalRandom.current().nextDouble(-0.08D, 0.08D),
                0.10D,
                ThreadLocalRandom.current().nextDouble(-0.08D, 0.08D)
        ));
        item.setPickupDelay(20);
    }

    private Block findNearbyOakLeaves(World world, Location center, int radius) {
        for (int attempt = 0; attempt < 16; attempt++) {
            int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int y = center.getBlockY() + ThreadLocalRandom.current().nextInt(-1, 7);
            int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() == Material.OAK_LEAVES || block.getType() == Material.AZALEA_LEAVES) {
                return block;
            }
        }
        return null;
    }
}
