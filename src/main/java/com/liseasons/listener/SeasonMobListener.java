package com.liseasons.listener;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.config.SeasonMobConfig;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class SeasonMobListener implements Listener {
    private final LISeasonsPlugin plugin;

    public SeasonMobListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isNaturalLike(event.getSpawnReason())) {
            return;
        }

        World world = event.getLocation().getWorld();
        if (world == null) {
            return;
        }
        SeasonState state = this.plugin.getSeasonManager().getState(world);
        if (state == null) {
            return;
        }

        SeasonMobConfig config = this.plugin.getLiConfig().seasonWorldRulesConfig().mobConfig();
        if (!config.enabled()) {
            return;
        }

        if (state.season() == Season.SUMMER && event.getEntityType() == EntityType.ZOMBIE && isHotBiome(event.getLocation())) {
            tryReplaceZombie(event, config);
            return;
        }
        if (state.season() == Season.WINTER && event.getEntityType() == EntityType.SKELETON) {
            tryReplaceSkeleton(event, config);
        }
    }

    private void tryReplaceZombie(CreatureSpawnEvent event, SeasonMobConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) >= config.summerZombieToHuskChancePercent()) {
            return;
        }
        Zombie zombie = (Zombie) event.getEntity();
        Location location = zombie.getLocation();
        event.setCancelled(true);
        zombie.getWorld().spawn(location, Husk.class, husk -> husk.setBaby(zombie.isBaby()));
    }

    private void tryReplaceSkeleton(CreatureSpawnEvent event, SeasonMobConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) >= config.winterSkeletonToStrayChancePercent()) {
            return;
        }
        Skeleton skeleton = (Skeleton) event.getEntity();
        Location location = skeleton.getLocation();
        event.setCancelled(true);
        skeleton.getWorld().spawn(location, Stray.class, stray -> { });
    }

    private boolean isNaturalLike(CreatureSpawnEvent.SpawnReason reason) {
        return reason == CreatureSpawnEvent.SpawnReason.NATURAL
                || reason == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                || reason == CreatureSpawnEvent.SpawnReason.CHUNK_GEN;
    }

    private boolean isHotBiome(Location location) {
        String biomeKey = location.getBlock().getBiome().getKey().asString().toLowerCase(Locale.ROOT);
        for (String entry : this.plugin.getLiConfig().temperatureConfig().tropicalBiomes()) {
            String token = entry.toLowerCase(Locale.ROOT);
            if (!token.isBlank() && biomeKey.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
