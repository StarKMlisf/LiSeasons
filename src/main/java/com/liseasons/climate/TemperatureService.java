package com.liseasons.climate;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.config.TemperatureConfig;
import com.liseasons.config.TemperatureConditionConfig;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class TemperatureService {
    private final LISeasonsPlugin plugin;
    private final DailyTemperatureService dailyTemperatureService = new DailyTemperatureService();
    private final Map<UUID, PlayerTemperatureState> playerStates = new ConcurrentHashMap<>();

    public TemperatureService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public TemperatureSnapshot snapshot(Player player) {
        return snapshot(player.getWorld(), player.getLocation(), player);
    }

    public TemperatureSnapshot snapshot(World world, Location location) {
        return snapshot(world, location, null);
    }

    private TemperatureSnapshot snapshot(World world, Location location, Player player) {
        SeasonState state = this.plugin.getSeasonManager().getState(world);
        if (state == null) {
            return new TemperatureSnapshot(20.0D, "四季系统未启用");
        }

        TemperatureConfig config = this.plugin.getLiConfig().temperatureConfig();
        TemperatureProfile profile = profile(world, location, player, state, config);
        return new TemperatureSnapshot(profile.bodyTemperature(), describe(profile.bodyTemperature()), profile.airTemperature(), profile.wetness());
    }

    public String format(TemperatureSnapshot snapshot) {
        return format(snapshot.value());
    }

    public String formatValue(double value) {
        return format(value);
    }

    public String formatBaseTemperature(Season season) {
        TemperatureConfig config = this.plugin.getLiConfig().temperatureConfig();
        double value = config.dailyRandomBaseTemperature()
                ? config.seasonBaseRanges().getOrDefault(season, new com.liseasons.config.TemperatureRangeConfig(18.0D, 18.0D)).midpoint()
                : config.seasonBaseTemperatures().getOrDefault(season, 18.0D);
        return format(value);
    }

    private TemperatureProfile profile(World world, Location location, Player player, SeasonState state, TemperatureConfig config) {
        double airTemperature = this.dailyTemperatureService.baseTemperature(world, state.season(), config);
        airTemperature += this.dailyTemperatureService.solarTermModifier(state.solarTerm().key(), config);
        airTemperature += biomeModifier(world, location, config);
        airTemperature += airConditionModifier(world, location, state, config.conditionConfig(), config);

        if (player == null) {
            return new TemperatureProfile(airTemperature, airTemperature, 0.0D, java.util.List.of());
        }

        PlayerTemperatureState playerState = this.playerStates.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerTemperatureState());
        double playerModifier = playerConditionModifier(world, location, player, state, config.conditionConfig(), config, airTemperature, playerState);
        double targetBodyTemperature = airTemperature + playerModifier;
        double bodyTemperature = smoothBodyTemperature(playerState.bodyTemperature(), targetBodyTemperature, config.bodyTemperatureStep());
        playerState.setBodyTemperature(bodyTemperature);

        return new TemperatureProfile(airTemperature, bodyTemperature, playerState.wetness(), new ArrayList<>());
    }

    private String format(double value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        StringBuilder pattern = new StringBuilder("0");
        int decimals = this.plugin.getLiConfig().temperatureConfig().displayDecimals();
        if (decimals > 0) {
            pattern.append(".");
            pattern.append("0".repeat(decimals));
        }
        DecimalFormat format = new DecimalFormat(pattern.toString(), symbols);
        return format.format(value) + "℃";
    }

    private double biomeModifier(World world, Location location, TemperatureConfig config) {
        double vanillaTemperature = world.getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        double relative = (vanillaTemperature - 0.8D) * config.vanillaBiomeScale();
        String normalizedBiome = biomeKey(world.getBiome(location));

        for (Map.Entry<String, Double> entry : config.biomeOverrides().entrySet()) {
            if (normalizedBiome.contains(entry.getKey())) {
                relative += entry.getValue();
            }
        }
        return relative;
    }

    private String biomeKey(Biome biome) {
        if (!(biome instanceof Keyed keyed)) {
            return "";
        }
        return keyed.getKey().asString().toLowerCase(Locale.ROOT);
    }

    private double airConditionModifier(World world,
                                        Location location,
                                        SeasonState state,
                                        TemperatureConditionConfig config,
                                        TemperatureConfig temperatureConfig) {
        double modifier = 0.0D;
        long time = world.getTime();
        boolean daytime = time < 12300L || time > 23850L;
        boolean canSeeSky = world.getHighestBlockYAt(location) <= location.getBlockY();

        if (!canSeeSky && location.getBlock().getLightFromSky() <= 7) {
            modifier += config.shade();
        }
        modifier += dayNightModifier(world, config);
        if (world.isThundering() && canSeeSky) {
            modifier += config.thunder();
        } else if (world.hasStorm() && canSeeSky) {
            modifier += config.rain();
        }
        if (!canSeeSky && location.getY() < world.getHighestBlockYAt(location) - 6) {
            modifier += config.underground();
        }
        if (location.getY() >= config.highAltitudeStartY()) {
            double altitudeSteps = Math.max(1.0D, (location.getY() - config.highAltitudeStartY()) / 32.0D + 1.0D);
            modifier += config.highAltitude() * altitudeSteps;
        }
        if (state.season() == Season.SUMMER && daytime && matchesConfiguredBiome(world.getBiome(location), temperatureConfig.tropicalBiomes())) {
            modifier += config.tropicalSummerDay();
        }
        return modifier;
    }

    private double dayNightModifier(World world, TemperatureConditionConfig config) {
        double angle = (world.getTime() - 6000.0D) / 24000.0D * Math.PI * 2.0D;
        double curve = Math.cos(angle);
        if (curve >= 0.0D) {
            return curve * config.dayMax();
        }
        return -curve * config.nightMin();
    }

    private double playerConditionModifier(World world,
                                           Location location,
                                           Player player,
                                           SeasonState state,
                                           TemperatureConditionConfig config,
                                           TemperatureConfig temperatureConfig,
                                           double airTemperature,
                                           PlayerTemperatureState playerState) {
        double modifier = 0.0D;
        modifier += armorModifier(player, temperatureConfig, airTemperature);
        modifier += heldItemModifier(player, config);
        modifier += nearbyHeatModifier(location.getBlock(), config);

        if (player.isInWater()) {
            playerState.soak();
        } else {
            playerState.recoverWetness(
                    temperatureConfig.wetnessRecoveryPerStep(),
                    world.getFullTime(),
                    temperatureConfig.wetnessRecoveryIntervalTicks()
            );
        }

        double waterModifier = state.season() == Season.WINTER ? config.waterWinter() : config.waterDefault();
        modifier += waterModifier * playerState.wetness();
        return modifier;
    }

    private double smoothBodyTemperature(double current, double target, double maxStep) {
        if (Double.isNaN(current)) {
            return target;
        }
        double delta = target - current;
        if (Math.abs(delta) <= maxStep) {
            return target;
        }
        return current + Math.copySign(maxStep, delta);
    }

    private double heldItemModifier(Player player, TemperatureConditionConfig config) {
        Material material = player.getInventory().getItemInMainHand().getType();
        TemperatureConfig temperatureConfig = this.plugin.getLiConfig().temperatureConfig();
        Double customModifier = temperatureConfig.heldItemTemperatureModifiers().get(material.name().toLowerCase(Locale.ROOT));
        if (customModifier != null) {
            return customModifier;
        }
        if (material == Material.LAVA_BUCKET) {
            return config.heldLavaBucket();
        }
        if (material == Material.SOUL_TORCH) {
            return config.heldSoulTorch();
        }
        if (material == Material.TORCH) {
            return config.heldTorch();
        }
        return 0.0D;
    }

    private double nearbyHeatModifier(Block origin, TemperatureConditionConfig config) {
        int range = config.nearbyHeatRange();
        double modifier = 0.0D;
        for (int x = -range; x <= range; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -range; z <= range; z++) {
                    Block target = origin.getRelative(x, y, z);
                    Material material = target.getType();
                    if (material == Material.LAVA || material == Material.LAVA_CAULDRON) {
                        modifier = Math.max(modifier, config.nearbyLava());
                        continue;
                    }
                    if (material == Material.MAGMA_BLOCK) {
                        modifier = Math.max(modifier, config.nearbyLava());
                        continue;
                    }
                    if (material == Material.SOUL_CAMPFIRE) {
                        modifier = minCooling(modifier, config.nearbySoulCampfire());
                        continue;
                    }
                    if (material == Material.CAMPFIRE) {
                        modifier = Math.max(modifier, config.nearbyCampfire());
                        continue;
                    }
                    if (material == Material.SOUL_FIRE) {
                        modifier = minCooling(modifier, config.nearbySoulFire());
                        continue;
                    }
                    if (material == Material.FIRE) {
                        modifier = Math.max(modifier, config.nearbyFire());
                        continue;
                    }
                    if (material == Material.BLUE_ICE) {
                        modifier = minCooling(modifier, config.nearbyBlueIce());
                        continue;
                    }
                    if (material == Material.PACKED_ICE) {
                        modifier = minCooling(modifier, config.nearbyPackedIce());
                        continue;
                    }
                    if (material == Material.ICE || material == Material.FROSTED_ICE) {
                        modifier = minCooling(modifier, config.nearbyIce());
                    }
                }
            }
        }
        return modifier;
    }

    private double minCooling(double current, double candidate) {
        if (candidate >= 0.0D) {
            return current;
        }
        return Math.min(current, candidate);
    }

    private double armorModifier(Player player, TemperatureConfig config, double airTemperature) {
        double modifier = 0.0D;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            String key = armorKey(item.getType());
            if ("leather".equals(key) && airTemperature > config.leatherDisabledAbove()) {
                continue;
            }
            modifier += config.armorWarmth().getOrDefault(key, config.conditionConfig().armorPerPiece());
        }
        return modifier;
    }

    private String armorKey(Material material) {
        String name = material.name().toLowerCase(Locale.ROOT);
        if (name.startsWith("leather_")) {
            return "leather";
        }
        if (name.startsWith("chainmail_")) {
            return "chainmail";
        }
        if (name.startsWith("iron_")) {
            return "iron";
        }
        if (name.startsWith("golden_")) {
            return "golden";
        }
        if (name.startsWith("diamond_")) {
            return "diamond";
        }
        if (name.startsWith("netherite_")) {
            return "netherite";
        }
        return "default";
    }

    private boolean matchesConfiguredBiome(Biome biome, java.util.List<String> configuredKeys) {
        String biomeName = biomeKey(biome);
        for (String entry : configuredKeys) {
            String normalized = entry.toLowerCase(Locale.ROOT);
            if (!normalized.isBlank() && biomeName.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private String describe(double value) {
        if (value <= -30) {
            return "极寒";
        }
        if (value <= -20) {
            return "严寒";
        }
        if (value <= -10) {
            return "寒冷";
        }
        if (value <= 0) {
            return "阴冷";
        }
        if (value <= 10) {
            return "偏凉";
        }
        if (value <= 22) {
            return "舒适";
        }
        if (value <= 35) {
            return "偏热";
        }
        if (value <= 45) {
            return "炎热";
        }
        return "酷热";
    }
}
