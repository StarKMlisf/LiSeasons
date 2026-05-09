package com.liseasons.visual;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.SeasonState;
import java.util.List;
import org.bukkit.entity.Player;

public final class SeasonVisualManager {
    private final LISeasonsPlugin plugin;
    private final List<SeasonVisualEffect> effects;

    public SeasonVisualManager(LISeasonsPlugin plugin) {
        this.plugin = plugin;
        this.effects = List.of(
                new AuroraVisualEffect(plugin),
                new BlizzardVisualEffect(plugin),
                new FallingLeavesVisualEffect(plugin),
                new AutumnLeafColorEffect(plugin),
                new TreeSnowVisualEffect(plugin),
                new AppleDropVisualEffect(plugin)
        );
    }

    public void tickPlayers() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline() || player.isDead()) {
                continue;
            }
            player.getScheduler().run(this.plugin, scheduledTask -> renderForPlayer(player), () -> { });
        }
    }

    private void renderForPlayer(Player player) {
        if (!player.isOnline() || player.isDead()) {
            return;
        }

        SeasonState state = this.plugin.getSeasonManager().getState(player.getWorld());
        if (state == null) {
            return;
        }
        TemperatureSnapshot snapshot = this.plugin.getTemperatureService().snapshot(player);
        for (SeasonVisualEffect effect : this.effects) {
            effect.render(player, state, snapshot);
        }
    }
}