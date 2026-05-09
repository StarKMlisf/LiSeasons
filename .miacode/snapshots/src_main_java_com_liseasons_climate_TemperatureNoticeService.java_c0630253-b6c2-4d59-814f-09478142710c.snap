package com.liseasons.climate;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class TemperatureNoticeService {
    private final LISeasonsPlugin plugin;
    private final Map<UUID, String> lastDescriptions = new ConcurrentHashMap<>();

    public TemperatureNoticeService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyIfChanged(Player player, TemperatureSnapshot snapshot) {
        String previous = this.lastDescriptions.put(player.getUniqueId(), snapshot.description());
        if (previous == null || previous.equals(snapshot.description())) {
            return;
        }
        player.sendMessage(this.plugin.getMessageManager().rawMessage(
                "notify.temperature-change",
                MessageManager.Placeholder.of("description", snapshot.description()),
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot))
        ));
    }

    public void clear(Player player) {
        this.lastDescriptions.remove(player.getUniqueId());
    }
}
