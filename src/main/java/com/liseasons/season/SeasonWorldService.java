package com.liseasons.season;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.config.SeasonWaterCycleConfig;
import com.liseasons.util.PlatformUtil;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Snowable;
import org.bukkit.entity.Player;

public final class SeasonWorldService {
    private final LISeasonsPlugin plugin;
    private final Deque<ChunkMeltTask> transitionMeltQueue = new ConcurrentLinkedDeque<>();
    private final Set<Long> queuedChunks = ConcurrentHashMap.newKeySet();
    private final Map<Long, Long> lastMeltScanTicks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerChunkQueueCursors = new ConcurrentHashMap<>();
    private final AtomicLong meltClockTicks = new AtomicLong(0L);

    public SeasonWorldService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void tickPlayers() {
        SeasonWaterCycleConfig config = this.plugin.getLiConfig().seasonWorldRulesConfig().waterCycleConfig();
        if (!config.enabled()) {
            return;
        }
        this.meltClockTicks.addAndGet(config.intervalTicks());
        processTransitionMeltQueue(config);
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline() || player.isDead()) {
                continue;
            }
            player.getScheduler().run(this.plugin, task -> processPlayer(player, config), () -> { });
        }
    }

    public void queueSpringMelt(World world) {
        SeasonWaterCycleConfig config = this.plugin.getLiConfig().seasonWorldRulesConfig().waterCycleConfig();
        if (!config.enabled() || world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        int queued = 0;
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            queued += queueChunk(world, chunk.getX(), chunk.getZ(), this.meltClockTicks.get(), 0L);
        }
        if (queued > 0) {
            this.plugin.getLogger().info("非冬季融雪任务已加入队列，待处理区块 " + queued + " 个。");
        }
    }

    private int queueChunk(World world, int chunkX, int chunkZ, long currentTick, long queueCooldownTicks) {
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            return 0;
        }
        long key = chunkKey(world, chunkX, chunkZ);
        long lastQueuedTick = this.lastMeltScanTicks.getOrDefault(key, Long.MIN_VALUE);
        if (lastQueuedTick != Long.MIN_VALUE && queueCooldownTicks > 0L && currentTick - lastQueuedTick < queueCooldownTicks) {
            return 0;
        }
        if (!this.queuedChunks.add(key)) {
            return 0;
        }
        this.lastMeltScanTicks.put(key, currentTick);
        this.transitionMeltQueue.addLast(new ChunkMeltTask(world, chunkX, chunkZ, key));
        return 1;
    }

    private void processPlayer(Player player, SeasonWaterCycleConfig config) {
        if (!player.isOnline() || player.isDead()) {
            return;
        }
        SeasonState state = this.plugin.getSeasonManager().getState(player.getWorld());
        if (state == null) {
            return;
        }
        if (state.season() != Season.WINTER) {
            meltVisibleSnowNearPlayer(player, config);
            queueNonWinterPlayerChunks(player, config);
        }
        processNearPlayer(player, state, config);
    }

    private void meltVisibleSnowNearPlayer(Player player, SeasonWaterCycleConfig config) {
        if (config.springMeltChancePercent() <= 0) {
            return;
        }

        World world = player.getWorld();
        Location origin = player.getLocation();
        int radius = config.searchRadius();
        int maxColumns = Math.max(24, Math.min(192, config.transitionMeltBudgetPerTick() / 2));
        int effectiveChance = Math.max(50, config.springMeltChancePercent());
        int columns = Math.max(8, maxColumns * effectiveChance / 100);
        int changedColumns = 0;

        for (int attempt = 0; attempt < columns; attempt++) {
            int x = origin.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int z = origin.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            if (meltSurfaceColumn(world, x, z, config, 48)) {
                changedColumns++;
            }
        }

        if (changedColumns > 0) {
            refreshPlayerChunk(world, origin);
        }
    }

    private void refreshPlayerChunk(World world, Location origin) {
        world.refreshChunk(origin.getBlockX() >> 4, origin.getBlockZ() >> 4);
    }

    private void queueNonWinterPlayerChunks(Player player, SeasonWaterCycleConfig config) {
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        // 非冬季只围绕玩家轮转入队，避免全量遍历已加载区块拖慢主线程。
        int radius = config.transitionMeltRadiusChunks();
        int side = radius * 2 + 1;
        int totalOffsets = side * side;
        int chunksPerRun = Math.max(2, Math.min((radius * 2 + 1) * 2, Math.max(1, config.transitionMeltBudgetPerTick() / 96)));
        int cursor = this.playerChunkQueueCursors.getOrDefault(player.getUniqueId(), 0);
        int centerChunkX = player.getLocation().getBlockX() >> 4;
        int centerChunkZ = player.getLocation().getBlockZ() >> 4;
        long currentTick = this.meltClockTicks.get();
        long queueCooldownTicks = Math.max(80L, config.intervalTicks() * 3L);

        int queued = queueChunk(world, centerChunkX, centerChunkZ, currentTick, queueCooldownTicks);
        int checked = 0;
        while (checked < totalOffsets && queued < chunksPerRun) {
            int offsetIndex = (cursor + checked) % totalOffsets;
            int dx = offsetIndex % side - radius;
            int dz = offsetIndex / side - radius;
            queued += queueChunk(world, centerChunkX + dx, centerChunkZ + dz, currentTick, queueCooldownTicks);
            checked++;
        }
        this.playerChunkQueueCursors.put(player.getUniqueId(), (cursor + Math.max(1, checked)) % totalOffsets);
    }

    private void processNearPlayer(Player player, SeasonState state, SeasonWaterCycleConfig config) {
        World world = player.getWorld();
        Location origin = player.getLocation();
        int radius = config.searchRadius();
        int x = origin.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int z = origin.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }

        int y = world.getHighestBlockYAt(x, z);
        Block top = world.getBlockAt(x, y, z);
        if (isExemptBiome(world.getBiome(x, y, z), config)) {
            return;
        }

        if (state.season() == Season.WINTER) {
            tryFreeze(top, config);
            tryPlaceSnow(world, x, z, config);
            return;
        }
        tryMeltNearSurface(world, x, z, config);
    }

    private void tryFreeze(Block top, SeasonWaterCycleConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) >= config.winterFreezeChancePercent()) {
            return;
        }
        if (top.getType() != Material.WATER) {
            return;
        }
        if (!(top.getBlockData() instanceof Levelled levelled) || levelled.getLevel() != 0) {
            return;
        }
        if (!top.getRelative(BlockFace.UP).getType().isAir()) {
            return;
        }
        top.setType(Material.ICE, true);
    }

    private void tryPlaceSnow(World world, int x, int z, SeasonWaterCycleConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) >= config.winterFreezeChancePercent()) {
            return;
        }

        int y = world.getHighestBlockYAt(x, z);
        Block ground = world.getBlockAt(x, y, z);
        Block place = ground.getRelative(BlockFace.UP);

        if (!place.getType().isAir()) {
            return;
        }

        Material groundType = ground.getType();
        if (!groundType.isSolid()) {
            return;
        }
        if (groundType == Material.WATER
                || groundType == Material.LAVA
                || groundType == Material.ICE
                || groundType == Material.FROSTED_ICE
                || groundType == Material.PACKED_ICE
                || groundType == Material.BLUE_ICE) {
            return;
        }
        if (groundType.name().endsWith("_LEAVES")) {
            return;
        }

        place.setType(Material.SNOW, true);
        if (ground.getBlockData() instanceof Snowable snowable) {
            snowable.setSnowy(true);
            ground.setBlockData(snowable, true);
        }
    }

    private void tryMeltNearSurface(World world, int x, int z, SeasonWaterCycleConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) >= config.springMeltChancePercent()) {
            return;
        }
        meltSurfaceColumn(world, x, z, config, 12);
    }

    private void processTransitionMeltQueue(SeasonWaterCycleConfig config) {
        if (PlatformUtil.isFolia(this.plugin)) {
            processTransitionMeltQueueFolia(config);
            return;
        }
        int budget = config.transitionMeltBudgetPerTick();
        while (budget > 0 && !this.transitionMeltQueue.isEmpty()) {
            ChunkMeltTask task = this.transitionMeltQueue.peekFirst();
            if (task == null) {
                return;
            }
            if (!task.world().isChunkLoaded(task.chunkX(), task.chunkZ())) {
                this.transitionMeltQueue.removeFirst();
                this.queuedChunks.remove(task.key());
                budget--;
                continue;
            }
            int spent = meltChunkSlice(task, config, budget);
            budget -= Math.max(1, spent);
            if (task.finished()) {
                if (task.changed()) {
                    task.world().refreshChunk(task.chunkX(), task.chunkZ());
                }
                this.transitionMeltQueue.removeFirst();
                this.queuedChunks.remove(task.key());
            }
        }
    }

    private void processTransitionMeltQueueFolia(SeasonWaterCycleConfig config) {
        int taskBudget = Math.max(64, config.transitionMeltBudgetPerTick());
        int tasks = Math.max(1, Math.min(4, taskBudget / 256));
        int perTaskBudget = Math.max(64, taskBudget / tasks);
        while (tasks > 0) {
            ChunkMeltTask task = this.transitionMeltQueue.pollFirst();
            if (task == null) {
                return;
            }
            if (!task.world().isChunkLoaded(task.chunkX(), task.chunkZ())) {
                this.queuedChunks.remove(task.key());
                tasks--;
                continue;
            }
            this.plugin.getServer().getRegionScheduler().execute(
                    this.plugin,
                    task.world(),
                    task.chunkX(),
                    task.chunkZ(),
                    () -> {
                        if (!task.world().isChunkLoaded(task.chunkX(), task.chunkZ())) {
                            this.queuedChunks.remove(task.key());
                            return;
                        }
                        meltChunkSlice(task, config, perTaskBudget);
                        if (task.finished()) {
                            if (task.changed()) {
                                task.world().refreshChunk(task.chunkX(), task.chunkZ());
                            }
                            this.queuedChunks.remove(task.key());
                            return;
                        }
                        this.transitionMeltQueue.addLast(task);
                    }
            );
            tasks--;
        }
    }

    private int meltChunkSlice(ChunkMeltTask task, SeasonWaterCycleConfig config, int budget) {
        World world = task.world();
        int baseX = task.chunkX() << 4;
        int baseZ = task.chunkZ() << 4;
        int spent = 0;
        // 预算按列扣减，空列也会消耗预算，避免无雪区块一次扫完整个队列。
        while (spent < budget && !task.finished()) {
            int x = baseX + task.localX();
            int z = baseZ + task.localZ();
            if (meltSurfaceColumn(world, x, z, config, 40)) {
                task.markChanged();
            }
            spent++;
            task.advanceColumn();
        }
        return spent;
    }

    private boolean meltSurfaceColumn(World world, int x, int z, SeasonWaterCycleConfig config, int depthBelowSurface) {
        int highestY = world.getHighestBlockYAt(x, z);
        if (isExemptBiome(world.getBiome(x, highestY, z), config)) {
            return false;
        }

        int maxY = Math.min(world.getMaxHeight() - 1, highestY + 3);
        int minY = Math.max(world.getMinHeight(), highestY - depthBelowSurface);
        boolean changed = false;
        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (meltBlock(block)) {
                changed = true;
                continue;
            }
            if (block.getBlockData() instanceof Snowable snowable && snowable.isSnowy()) {
                snowable.setSnowy(false);
                block.setBlockData(snowable, true);
                changed = true;
            }
        }
        return changed;
    }

    private boolean meltBlock(Block block) {
        Material type = block.getType();
        if (type == Material.ICE || type == Material.FROSTED_ICE) {
            if (block.getRelative(BlockFace.UP).getType().isAir()) {
                block.setType(Material.WATER, true);
                updateSnowyBelow(block);
                return true;
            }
            return false;
        }
        if (type == Material.SNOW || type == Material.SNOW_BLOCK || type == Material.POWDER_SNOW) {
            block.setType(Material.AIR, true);
            updateSnowyBelow(block);
            return true;
        }
        return false;
    }

    private void updateSnowyBelow(Block block) {
        Block below = block.getRelative(BlockFace.DOWN);
        if (below.getBlockData() instanceof Snowable snowable && snowable.isSnowy()) {
            snowable.setSnowy(false);
            below.setBlockData(snowable, true);
        }
    }

    private boolean isExemptBiome(Biome biome, SeasonWaterCycleConfig config) {
        String biomeName = biome.getKey().asString().toLowerCase(Locale.ROOT);
        for (String entry : config.exemptBiomes()) {
            String token = entry.toLowerCase(Locale.ROOT);
            if (!token.isBlank() && biomeName.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private long chunkKey(World world, int chunkX, int chunkZ) {
        long positionKey = (((long) chunkX) & 0xffffffffL) << 32 | (((long) chunkZ) & 0xffffffffL);
        long worldKey = world.getUID().getMostSignificantBits() ^ world.getUID().getLeastSignificantBits();
        return positionKey ^ worldKey;
    }

    private static final class ChunkMeltTask {
        private final World world;
        private final int chunkX;
        private final int chunkZ;
        private final long key;
        private int localX;
        private int localZ;
        private boolean changed;

        private ChunkMeltTask(World world, int chunkX, int chunkZ, long key) {
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.key = key;
        }

        private World world() {
            return this.world;
        }

        private int chunkX() {
            return this.chunkX;
        }

        private int chunkZ() {
            return this.chunkZ;
        }

        private long key() {
            return this.key;
        }

        private int localX() {
            return this.localX;
        }

        private int localZ() {
            return this.localZ;
        }

        private void advanceColumn() {
            this.localZ++;
            if (this.localZ >= 16) {
                this.localZ = 0;
                this.localX++;
            }
        }

        private boolean finished() {
            return this.localX >= 16;
        }

        private void markChanged() {
            this.changed = true;
        }

        private boolean changed() {
            return this.changed;
        }
    }
}
