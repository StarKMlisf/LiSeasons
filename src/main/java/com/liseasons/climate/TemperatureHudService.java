package com.liseasons.climate;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import org.bukkit.entity.Player;

public final class TemperatureHudService {
    private final LISeasonsPlugin plugin;

    public TemperatureHudService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void render(Player player, TemperatureSnapshot snapshot) {
        if (!this.plugin.getLiConfig().temperatureConfig().actionBarEnabled()) {
            return;
        }
        player.sendActionBar(this.plugin.getMessageManager().rawMessage(
                "hud.temperature",
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot))
        ));
    }
}
