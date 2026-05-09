package com.liseasons.climate;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.config.TemperatureConfig;
import com.liseasons.config.TemperatureEventConfig;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class TemperatureEventService {
    private final LISeasonsPlugin plugin;
    private final TemperatureEffectExecutor effectExecutor;
    private final Map<UUID, String> lastEventKey = new HashMap<>();
    private final Map<UUID, Long> lastTriggerTime = new HashMap<>();
    private final Map<UUID, Map<String, Long>> lastEventTimes = new HashMap<>();

    public TemperatureEventService(LISeasonsPlugin plugin) {
        this.plugin = plugin;
        this.effectExecutor = new TemperatureEffectExecutor();
    }

    public void apply(Player player, TemperatureSnapshot snapshot) {
        TemperatureConfig config = this.plugin.getLiConfig().temperatureConfig();
        if (!config.temperatureEventsEnabled()) {
            return;
        }

        List<TemperatureEventConfig> matchingEvents = config.temperatureEvents().stream()
                .filter(event -> event.triggersAt(snapshot.value()))
                .toList();

        if (matchingEvents.isEmpty()) {
            this.lastEventKey.remove(player.getUniqueId());
            return;
        }

        if (config.highestTemperatureEventOnly()) {
            matchingEvents = List.of(selectMostSevere(matchingEvents, snapshot.value()));
        }

        long now = System.currentTimeMillis();
        long lastTime = this.lastTriggerTime.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastTime < config.temperatureEventCooldownMillis()) {
            return;
        }

        Map<String, Long> eventTimes = this.lastEventTimes.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>());
        matchingEvents = matchingEvents.stream()
                .filter(event -> now - eventTimes.getOrDefault(event.key(), 0L) >= event.cooldownMillis())
                .toList();
        if (matchingEvents.isEmpty()) {
            return;
        }
        this.lastTriggerTime.put(player.getUniqueId(), now);

        String eventKey = matchingEvents.stream().map(TemperatureEventConfig::key).sorted().reduce((left, right) -> left + "|" + right).orElse("");
        String previous = this.lastEventKey.put(player.getUniqueId(), eventKey);

        for (TemperatureEventConfig event : matchingEvents) {
            eventTimes.put(event.key(), now);
            for (String effect : event.effects()) {
                this.effectExecutor.apply(player, effect);
            }
        }

        if (previous == null || !previous.equals(eventKey)) {
            player.sendMessage(this.plugin.getMessageManager().rawMessage(
                    "notify.temperature-event",
                    MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot)),
                    MessageManager.Placeholder.of("description", snapshot.description())
            ));
        }
    }

    private TemperatureEventConfig selectMostSevere(List<TemperatureEventConfig> events, double currentTemperature) {
        Comparator<TemperatureEventConfig> comparator = Comparator.comparingInt(TemperatureEventConfig::priority).reversed();
        comparator = comparator.thenComparing(currentTemperature < 0.0D
                ? Comparator.comparingDouble(TemperatureEventConfig::temperature)
                : Comparator.comparingDouble(TemperatureEventConfig::temperature).reversed());
        return events.stream().sorted(comparator).findFirst().orElse(events.getFirst());
    }
}
