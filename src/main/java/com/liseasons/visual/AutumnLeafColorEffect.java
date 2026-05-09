package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.config.VisualEffectsConfig;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;

public final class AutumnLeafColorEffect implements SeasonVisualEffect {
    private static final Set<Material> TARGET_LEAVES = Set.of(
            Material.BIRCH_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.CHERRY_LEAVES
    );
    private static final Material AUTUMN_LEAVES = Material.ACACIA_LEAVES;

    private final LISeasonsPlugin plugin;
    private final Map<BlockKey, BlockData> backups = new ConcurrentHashMap<>();

    public AutumnLeafColorEffect(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void render(Player player, SeasonState state, TemperatureSnapshot snapshot) {
        VisualEffectsConfig config = this.plugin.getLiConfig().visualEffectsConfig();
        if (!config.autumnLeafColorEnabled()) {
            return;
        }

        if (state.season() == Season.AUTUMN) {
            paintNearbyLeaves(player, config);
            return;
        }

        restoreSomeLeaves(player.getWorld(), Math.max(4, config.autumnLeafColorBudget() / 2));
    }

    private void paintNearbyLeaves(Player player, VisualEffectsConfig config) {
        World world = player.getWorld();
        int radius = config.autumnLeafColorRadius();
        int budget = config.autumnLeafColorBudget();
        int centerX = player.getLocation().getBlockX();
        int centerY = player.getLocation().getBlockY();
        int centerZ = player.getLocation().getBlockZ();
        Set<ChunkKey> touchedChunks = new HashSet<>();

        for (int y = Math.min(world.getMaxHeight() - 1, centerY + 10); y >= Math.max(world.getMinHeight(), centerY - 4) && budget > 0; y--) {
            for (int x = centerX - radius; x <= centerX + radius && budget > 0; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius && budget > 0; z++) {
                    if ((Math.abs(x - centerX) + Math.abs(z - centerZ)) > radius * 2) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (paintLeaf(block)) {
                        touchedChunks.add(ChunkKey.of(block));
                        budget--;
                    }
                }
            }
        }
        refreshChunks(world, touchedChunks);
    }

    private boolean paintLeaf(Block block) {
        Material type = block.getType();
        if (!TARGET_LEAVES.contains(type)) {
            return false;
        }
        BlockKey key = BlockKey.of(block);
        if (this.backups.containsKey(key)) {
            return false;
        }

        BlockData original = block.getBlockData().clone();
        BlockData replacement = createAutumnLeafData(original);
        this.backups.put(key, original);
        block.setBlockData(replacement, false);
        return true;
    }

    private BlockData createAutumnLeafData(BlockData original) {
        BlockData replacement = AUTUMN_LEAVES.createBlockData();
        if (original instanceof Leaves source && replacement instanceof Leaves target) {
            target.setPersistent(source.isPersistent());
            target.setDistance(source.getDistance());
        }
        return replacement;
    }

    private void restoreSomeLeaves(World world, int budget) {
        Iterator<Map.Entry<BlockKey, BlockData>> iterator = this.backups.entrySet().iterator();
        Set<ChunkKey> touchedChunks = new HashSet<>();
        while (iterator.hasNext() && budget > 0) {
            Map.Entry<BlockKey, BlockData> entry = iterator.next();
            BlockKey key = entry.getKey();
            if (!key.worldId().equals(world.getUID())) {
                continue;
            }

            Block block = world.getBlockAt(key.x(), key.y(), key.z());
            if (block.getType() == AUTUMN_LEAVES || Tag.LEAVES.isTagged(block.getType())) {
                block.setBlockData(entry.getValue(), false);
                touchedChunks.add(ChunkKey.of(block));
            }
            iterator.remove();
            budget--;
        }
        refreshChunks(world, touchedChunks);
    }

    private void refreshChunks(World world, Set<ChunkKey> touchedChunks) {
        for (ChunkKey chunk : touchedChunks) {
            world.refreshChunk(chunk.x(), chunk.z());
        }
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {
        private static BlockKey of(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }

    private record ChunkKey(int x, int z) {
        private static ChunkKey of(Block block) {
            Chunk chunk = block.getChunk();
            return new ChunkKey(chunk.getX(), chunk.getZ());
        }
    }
}
