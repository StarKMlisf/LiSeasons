package com.liseasons.season;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.config.PluginConfig;
import com.liseasons.util.PlatformUtil;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class SeasonManager {
    private final LISeasonsPlugin plugin;
    private final Map<String, WorldSeasonData> worldStates = new ConcurrentHashMap<>();
    private final SeasonWeatherService weatherService;
    private TimeCalculator timeCalculator;

    public SeasonManager(LISeasonsPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.weatherService = new SeasonWeatherService(plugin);
        this.timeCalculator = new TimeCalculator(
                config.useGregorianTrigger(),
                config.seasonDurationDays(),
                config.seasonStartDates(),
                config.gregorianDates()
        );
    }

    public void reload(PluginConfig config) {
        this.timeCalculator = new TimeCalculator(
                config.useGregorianTrigger(),
                config.seasonDurationDays(),
                config.seasonStartDates(),
                config.gregorianDates()
        );
        tickAllWorlds();
    }

    public void tickAllWorlds() {
        for (World world : this.plugin.getServer().getWorlds()) {
            runSeasonTick(world);
        }
    }

    public boolean isEnabled(World world) {
        PluginConfig config = this.plugin.getLiConfig();
        if (config.deniedWorlds().contains(world.getName())) {
            return false;
        }
        return config.enableAllWorlds() || config.allowedWorlds().contains(world.getName());
    }

    public SeasonState getState(World world) {
        if (!isEnabled(world)) {
            return null;
        }
        WorldSeasonData existing = this.worldStates.get(world.getName());
        if (existing != null) {
            return existing.state();
        }
        SeasonState calculated = this.timeCalculator.calculate(world.getFullTime(), LocalDate.now());
        this.worldStates.put(world.getName(), new WorldSeasonData(calculated, true));
        return calculated;
    }

    public boolean isAutomatic(World world) {
        WorldSeasonData data = this.worldStates.get(world.getName());
        return data == null || data.automatic();
    }

    public SeasonState setSeason(World world, Season season) {
        SolarTerm term = SolarTerm.firstOf(season);
        return setState(world, new SeasonState(season, term), false);
    }

    public SeasonState setSolarTerm(World world, SolarTerm term) {
        return setState(world, new SeasonState(term.season(), term), false);
    }

    public SeasonState advance(World world) {
        SeasonState current = getState(world);
        if (current == null) {
            return null;
        }
        return setSolarTerm(world, current.solarTerm().next());
    }

    public void setAutomatic(World world, boolean automatic) {
        SeasonState current = getState(world);
        if (current == null) {
            return;
        }
        this.worldStates.put(world.getName(), new WorldSeasonData(current, automatic));
        if (automatic) {
            tickAllWorlds();
        }
    }

    public Collection<String> loadedWorldNames() {
        return this.plugin.getServer().getWorlds().stream().map(World::getName).toList();
    }

    private SeasonState setState(World world, SeasonState newState, boolean automatic) {
        this.worldStates.put(world.getName(), new WorldSeasonData(newState, automatic));
        notifyWorldChanged(world, newState);
        return newState;
    }

    private void runSeasonTick(World world) {
        if (PlatformUtil.isFolia(this.plugin)) {
            int chunkX = world.getSpawnLocation().getBlockX() >> 4;
            int chunkZ = world.getSpawnLocation().getBlockZ() >> 4;
            this.plugin.getServer().getRegionScheduler().execute(this.plugin, world, chunkX, chunkZ, () -> tickWorld(world));
            return;
        }
        tickWorld(world);
    }

    private void tickWorld(World world) {
        if (!isEnabled(world)) {
            return;
        }
        WorldSeasonData previous = this.worldStates.get(world.getName());
        SeasonState calculated = this.timeCalculator.calculate(world.getFullTime(), LocalDate.now());
        if (previous == null) {
            this.worldStates.put(world.getName(), new WorldSeasonData(calculated, true));
            return;
        }
        if (!previous.automatic()) {
            return;
        }
        if (!previous.state().equals(calculated)) {
            previous.setState(calculated);
            notifyWorldChanged(world, calculated);
        }
    }

    private void notifyWorldChanged(World world, SeasonState state) {
        PluginConfig config = this.plugin.getLiConfig();
        MessageManager messages = this.plugin.getMessageManager();

        this.weatherService.refresh(world, state, config);
        if (this.plugin.getBiomeColorService() != null) {
            this.plugin.getBiomeColorService().refreshWorld(world);
        }
        if (state.season() != Season.WINTER && this.plugin.getSeasonWorldService() != null) {
            this.plugin.getSeasonWorldService().queueSpringMelt(world);
        }

        if (config.changeSoundEnabled()) {
            Sound sound = parseSound(config.changeSound());
            if (sound != null) {
                for (Player player : world.getPlayers()) {
                    player.getScheduler().run(this.plugin, task -> {
                        if (player.isOnline()) {
                            player.playSound(player.getLocation(), sound, config.changeSoundVolume(), config.changeSoundPitch());
                        }
                    }, () -> { });
                }
            }
        }

        if (config.changeParticleEnabled()) {
            Particle particle = parseParticle(config.changeParticle());
            if (particle != null) {
                for (Player player : world.getPlayers()) {
                    player.getScheduler().run(this.plugin, task -> {
                        if (player.isOnline()) {
                            player.spawnParticle(particle, player.getLocation().add(0.0D, 1.0D, 0.0D), config.changeParticleCount());
                        }
                    }, () -> { });
                }
            }
        }

        if (config.broadcastChange()) {
            Component component = messages.message(
                    "notify.changed",
                    MessageManager.Placeholder.of("world", world.getName()),
                    MessageManager.Placeholder.of("season", messages.seasonName(state.season().key())),
                    MessageManager.Placeholder.of("term", messages.solarTermName(state.solarTerm().key()))
            );
            this.plugin.getServer().broadcast(component);
        }
    }

    private Sound parseSound(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        NamespacedKey key = name.contains(":")
                ? NamespacedKey.fromString(name.toLowerCase(java.util.Locale.ROOT))
                : NamespacedKey.minecraft(name.toLowerCase(java.util.Locale.ROOT));
        return key == null ? null : Registry.SOUNDS.get(key);
    }

    private Particle parseParticle(String name) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
