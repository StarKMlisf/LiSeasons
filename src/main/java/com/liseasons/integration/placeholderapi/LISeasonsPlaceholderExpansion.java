package com.liseasons.integration.placeholderapi;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.SeasonState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LISeasonsPlaceholderExpansion extends PlaceholderExpansion {
    private final LISeasonsPlugin plugin;

    public LISeasonsPlaceholderExpansion(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "liseasons";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kmian";
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!(offlinePlayer instanceof Player player) || !player.isOnline()) {
            return "";
        }
        SeasonState state = this.plugin.getSeasonManager().getState(player.getWorld());
        if (state == null) {
            return "";
        }

        String key = params.toLowerCase(java.util.Locale.ROOT);
        if (key.equals("temperature")
                || key.equals("temperature_status")
                || key.equals("air_temperature")
                || key.equals("wetness")) {
            PlaceholderTemperature temperature = temperature(player);
            return switch (key) {
                case "temperature" -> temperature.formatted();
                case "temperature_status" -> temperature.description();
                case "air_temperature" -> this.plugin.getTemperatureService().formatValue(temperature.airTemperature());
                case "wetness" -> Math.round(Math.max(0.0D, Math.min(1.0D, temperature.wetness())) * 100.0D) + "%";
                default -> "";
            };
        }

        return switch (key) {
            case "season" -> this.plugin.getMessageManager().seasonName(state.season().key());
            case "season_key" -> state.season().key();
            case "term" -> this.plugin.getMessageManager().solarTermName(state.solarTerm().key());
            case "term_key" -> state.solarTerm().key();
            case "world" -> player.getWorld().getName();
            case "mode" -> this.plugin.getSeasonManager().isAutomatic(player.getWorld()) ? "自动" : "手动";
            default -> null;
        };
    }

    private PlaceholderTemperature temperature(Player player) {
        TemperatureSnapshot snapshot = this.plugin.getTemperatureService().snapshot(player);
        return new PlaceholderTemperature(
                this.plugin.getTemperatureService().format(snapshot),
                snapshot.description(),
                snapshot.airTemperature(),
                snapshot.wetness()
        );
    }

    private record PlaceholderTemperature(String formatted, String description, double airTemperature, double wetness) {
    }
}
