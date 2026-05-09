package com.liseasons.climate;

import com.liseasons.LISeasonsPlugin;
import org.bukkit.entity.Player;

public final class TemperatureMonitorService {
    private final LISeasonsPlugin plugin;
    private final TemperatureEventService eventService;

    public TemperatureMonitorService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
        this.eventService = new TemperatureEventService(plugin);
    }

    public void tickPlayers() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline() || player.isDead()) {
                continue;
            }
            player.getScheduler().run(this.plugin, scheduledTask -> monitor(player), () -> { });
        }
    }

    private void monitor(Player player) {
        if (!player.isOnline() || player.isDead()) {
            return;
        }
        TemperatureSnapshot snapshot = this.plugin.getTemperatureService().snapshot(player);
        this.plugin.getTemperatureHudService().render(player, snapshot);
        this.plugin.getTemperatureNoticeService().notifyIfChanged(player, snapshot);
        this.eventService.apply(player, snapshot);
    }
}
