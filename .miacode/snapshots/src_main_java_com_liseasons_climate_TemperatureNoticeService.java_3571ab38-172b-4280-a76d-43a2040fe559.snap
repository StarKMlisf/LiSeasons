package com.liseasons.climate;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.config.TemperatureConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class TemperatureNoticeService {
    private final LISeasonsPlugin plugin;
    private final Map<UUID, String> lastDescriptions = new ConcurrentHashMap<>();
    private final Map<UUID, Double> lastNoticeTemperatures = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastNoticeTimes = new ConcurrentHashMap<>();

    public TemperatureNoticeService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyIfChanged(Player player, TemperatureSnapshot snapshot) {
        UUID playerId = player.getUniqueId();
        String previous = this.lastDescriptions.put(playerId, snapshot.description());
        if (previous == null || previous.equals(snapshot.description())) {
            return;
        }

        TemperatureConfig config = this.plugin.getLiConfig().temperatureConfig();
        if (!config.noticeEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastNoticeTime = this.lastNoticeTimes.getOrDefault(playerId, 0L);
        if (now - lastNoticeTime < config.noticeCooldownMillis()) {
            return;
        }

        Double lastNoticeTemperature = this.lastNoticeTemperatures.get(playerId);
        if (lastNoticeTemperature != null
                && Math.abs(snapshot.value() - lastNoticeTemperature) < config.noticeMinTemperatureDelta()) {
            return;
        }

        this.lastNoticeTimes.put(playerId, now);
        this.lastNoticeTemperatures.put(playerId, snapshot.value());
        player.sendMessage(this.plugin.getMessageManager().rawMessage(
                "notify.temperature-change",
                MessageManager.Placeholder.of("description", snapshot.description()),
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot))
        ));
    }

    public void clear(Player player) {
        UUID playerId = player.getUniqueId();
        this.lastDescriptions.remove(playerId);
        this.lastNoticeTemperatures.remove(playerId);
        this.lastNoticeTimes.remove(playerId);
    }
}
