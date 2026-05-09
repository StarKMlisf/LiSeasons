package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class AuroraVisualEffect implements SeasonVisualEffect {
    private final LISeasonsPlugin plugin;

    public AuroraVisualEffect(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void render(Player player, SeasonState state, TemperatureSnapshot snapshot) {
        if (!this.plugin.getLiConfig().visualEffectsConfig().auroraEnabled()) {
            return;
        }
        if (state.season() != Season.WINTER) {
            return;
        }
        World world = player.getWorld();
        long time = world.getTime();
        if (time < 13000L || time > 22000L || world.hasStorm()) {
            return;
        }
        long day = world.getFullTime() / 24000L;
        if (Math.floorMod(day, 5) != 0) {
            return;
        }
        if (world.getHighestBlockYAt(player.getLocation()) > player.getLocation().getY()) {
            return;
        }

        Location base = player.getLocation().clone().add(0.0D, 18.0D, 0.0D);
        Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(97, 255, 163), 2.0F);
        Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(118, 196, 255), 2.0F);

        for (int index = -6; index <= 6; index++) {
            double x = index * 2.0D;
            double y = Math.sin((day + index) * 0.45D) * 2.5D;
            player.spawnParticle(Particle.DUST, base.clone().add(x, y, -10.0D), 1, 0.0D, 0.0D, 0.0D, green);
            player.spawnParticle(Particle.DUST, base.clone().add(x, y + 1.2D, -12.0D), 1, 0.0D, 0.0D, 0.0D, blue);
        }
    }
}
