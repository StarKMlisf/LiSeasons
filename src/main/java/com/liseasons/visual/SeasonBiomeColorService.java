package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.scheduler.ScheduledHandle;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import com.liseasons.util.PlatformUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class SeasonBiomeColorService {
    private final LISeasonsPlugin plugin;
    private final Map<Season, Biome> targetBiomes = new EnumMap<>(Season.class);
    private final AtomicLong paintedChunks = new AtomicLong(0);

    private ScheduledHandle taskHandle;
    private boolean enabled;

    public SeasonBiomeColorService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        SeasonBiomeColorConfig config = this.plugin.getLiConfig().biomeColorConfig();
        if (!config.enabled() || !config.biomeSpoofEnabled()) {
            this.enabled = false;
            return;
        }

        loadTargetBiomes(config);
        if (this.targetBiomes.isEmpty()) {
            this.plugin.getLogger().warning("季节色调未配置可用目标群系，功能不启动。");
            this.enabled = false;
            return;
        }

        this.paintedChunks.set(0);
        this.enabled = true;
        startTicker(config.intervalTicks());
        this.plugin.getLogger().info("季节色调服务已就绪 (SeasonCore-style Bukkit biome spoof)");
    }

    public void stop() {
        if (this.taskHandle != null) {
            this.taskHandle.cancel();
            this.taskHandle = null;
        }
        if (this.enabled) {
            this.plugin.getLogger().info("季节色调已停用，分批染色区块 " + this.paintedChunks.get() + " 个。");
        }
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void refreshWorld(World world) {
        if (!this.enabled || world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        this.plugin.getLogger().info("季节色调已切换，正在按玩家周围区块分批刷新草地和树叶颜色。");
        repaintWorldPlayers(world, true);
    }

    private void startTicker(long intervalTicks) {
        long period = Math.max(10L, intervalTicks);
        if (PlatformUtil.isFolia(this.plugin)) {
            ScheduledTask task = this.plugin.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(this.plugin, scheduled -> repaintOnlinePlayers(false), 40L, period);
            this.taskHandle = task::cancel;
            return;
        }
        BukkitTask task = this.plugin.getServer().getScheduler()
                .runTaskTimer(this.plugin, () -> repaintOnlinePlayers(false), 40L, period);
        this.taskHandle = task::cancel;
    }

    private void repaintOnlinePlayers(boolean transitionBoost) {
        for (World world : this.plugin.getServer().getWorlds()) {
            repaintWorldPlayers(world, transitionBoost);
        }
    }

    private void repaintWorldPlayers(World world, boolean transitionBoost) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        SeasonState state = this.plugin.getSeasonManager().getState(world);
        if (state == null) {
            return;
        }
        Biome target = this.targetBiomes.get(state.season());
        if (target == null) {
            return;
        }

        SeasonBiomeColorConfig config = this.plugin.getLiConfig().biomeColorConfig();
        int budget = config.spoofBudgetChunksPerTick() * (transitionBoost ? 4 : 1);
        int radius = config.spoofRadiusChunks();
        for (Player player : world.getPlayers()) {
            if (budget <= 0) {
                break;
            }
            if (PlatformUtil.isFolia(this.plugin)) {
                int playerBudget = budget;
                player.getScheduler().run(this.plugin, task -> repaintPlayerAreaFolia(player, target, radius, playerBudget, config), () -> { });
                budget = 0;
                continue;
            }
            budget = repaintPlayerArea(player, target, radius, budget, config);
        }
    }

    private void repaintPlayerAreaFolia(Player player, Biome target, int radius, int budget, SeasonBiomeColorConfig config) {
        if (!player.isOnline()) {
            return;
        }
        World world = player.getWorld();
        int centerX = player.getLocation().getBlockX() >> 4;
        int centerZ = player.getLocation().getBlockZ() >> 4;
        int scheduled = 0;
        for (ChunkOffset offset : sortedOffsets(player, radius)) {
            if (scheduled >= budget) {
                break;
            }
            int chunkX = centerX + offset.dx();
            int chunkZ = centerZ + offset.dz();
            this.plugin.getServer().getRegionScheduler().execute(this.plugin, world, chunkX, chunkZ, () -> {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    return;
                }
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                paintChunk(chunk, target, config);
            });
            scheduled++;
        }
    }

    private int repaintPlayerArea(Player player, Biome target, int radius, int budget, SeasonBiomeColorConfig config) {
        World world = player.getWorld();
        int centerX = player.getLocation().getBlockX() >> 4;
        int centerZ = player.getLocation().getBlockZ() >> 4;

        for (ChunkOffset offset : sortedOffsets(player, radius)) {
            if (budget <= 0) {
                break;
            }
            int chunkX = centerX + offset.dx();
            int chunkZ = centerZ + offset.dz();
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                continue;
            }
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            if (paintChunk(chunk, target, config)) {
                budget--;
            }
        }
        return budget;
    }

    private List<ChunkOffset> sortedOffsets(Player player, int radius) {
        Vector look = player.getLocation().getDirection().clone();
        look.setY(0.0D);
        if (look.lengthSquared() < 0.0001D) {
            look = new Vector(0, 0, 1);
        } else {
            look.normalize();
        }

        List<ChunkOffset> offsets = new ArrayList<>();
        offsets.add(new ChunkOffset(0, 0, 0, 1.0D));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                int distance = Math.max(Math.abs(dx), Math.abs(dz));
                Vector direction = new Vector(dx, 0, dz);
                double forward = direction.lengthSquared() < 0.0001D ? 0.0D : look.dot(direction.normalize());
                offsets.add(new ChunkOffset(dx, dz, distance, forward));
            }
        }
        offsets.sort(Comparator
                .comparingInt(ChunkOffset::distance)
                .thenComparingDouble(offset -> -offset.forwardScore()));
        return offsets;
    }

    private boolean paintChunk(Chunk chunk, Biome target, SeasonBiomeColorConfig config) {
        if (isChunkAtTarget(chunk, target, config)) {
            return false;
        }

        World world = chunk.getWorld();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        for (int x = 0; x < 16; x += config.spoofStepXz()) {
            for (int z = 0; z < 16; z += config.spoofStepXz()) {
                for (int y = minY; y < maxY; y += config.spoofStepY()) {
                    world.setBiome(baseX + x, y, baseZ + z, target);
                }
            }
        }

        world.refreshChunk(chunk.getX(), chunk.getZ());
        this.paintedChunks.incrementAndGet();
        return true;
    }

    private boolean isChunkAtTarget(Chunk chunk, Biome target, SeasonBiomeColorConfig config) {
        World world = chunk.getWorld();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        for (int x = 0; x < 16; x += Math.max(4, config.spoofStepXz())) {
            for (int z = 0; z < 16; z += Math.max(4, config.spoofStepXz())) {
                for (int y = minY; y < maxY; y += Math.max(16, config.spoofStepY())) {
                    if (world.getBiome(baseX + x, y, baseZ + z) != target) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void loadTargetBiomes(SeasonBiomeColorConfig config) {
        this.targetBiomes.clear();
        for (Map.Entry<Season, NamespacedKey> entry : config.spoofTargets().entrySet()) {
            Biome biome = Registry.BIOME.get(entry.getValue());
            if (biome == null) {
                this.plugin.getLogger().warning("季节色调目标群系无效: " + entry.getKey().key() + " -> " + entry.getValue());
                continue;
            }
            this.targetBiomes.put(entry.getKey(), biome);
        }
    }

    private record ChunkOffset(int dx, int dz, int distance, double forwardScore) {
    }
}
