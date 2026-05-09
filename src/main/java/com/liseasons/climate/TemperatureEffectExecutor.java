package com.liseasons.climate;

import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class TemperatureEffectExecutor {
    public void apply(Player player, String rawEffect) {
        if (rawEffect == null || rawEffect.isBlank()) {
            return;
        }

        String effect = rawEffect.trim();
        String[] split = effect.split(" ", 2);
        String command = split[0];
        String target = split.length > 1 ? split[1].trim() : "";
        String[] parts = command.split(":");
        String type = parts[0].toUpperCase(Locale.ROOT);

        switch (type) {
            case "SCREEN_FREEZE" -> applyScreenFreeze(player, parts);
            case "PLAY_SOUND" -> applySound(player, parts);
            case "PARTICLE" -> applyParticle(player, parts, target);
            case "POTION" -> applyPotion(player, parts);
            case "BURN" -> applyBurn(player, parts);
            case "ENTITY_EFFECT" -> applyEntityEffect(player, parts);
            default -> {
            }
        }
    }

    private void applyScreenFreeze(Player player, String[] parts) {
        if (parts.length < 2) {
            return;
        }
        int ticks = parseInt(parts[1], 0);
        player.setFreezeTicks(Math.max(player.getFreezeTicks(), ticks));
    }

    private void applySound(Player player, String[] parts) {
        if (parts.length < 2) {
            return;
        }
        Sound sound = parseSound(parts[1]);
        if (sound == null) {
            return;
        }
        float volume = parts.length >= 3 ? (float) parseDouble(parts[2], 1.0D) : 1.0F;
        float pitch = parts.length >= 4 ? (float) parseDouble(parts[3], 1.0D) : 1.0F;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private Sound parseSound(String value) {
        NamespacedKey key = parseMinecraftKey(value);
        return key == null ? null : Registry.SOUNDS.get(key);
    }

    private NamespacedKey parseMinecraftKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.contains(":")
                ? NamespacedKey.fromString(value.toLowerCase(Locale.ROOT))
                : NamespacedKey.minecraft(value.toLowerCase(Locale.ROOT));
    }

    private void applyParticle(Player player, String[] parts, String target) {
        if (parts.length < 4) {
            return;
        }
        try {
            Particle particle = Particle.valueOf(parts[1].toUpperCase(Locale.ROOT));
            int amount = parseInt(parts[2], 1);
            double offset = parseDouble(parts[3], 0.0D);
            Location location = "@EyeHeight".equalsIgnoreCase(target)
                    ? player.getEyeLocation()
                    : player.getLocation().add(0.0D, 1.0D, 0.0D);
            player.spawnParticle(particle, location, amount, offset, offset, offset, 0.01D);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void applyPotion(Player player, String[] parts) {
        if (parts.length < 4) {
            return;
        }
        NamespacedKey key = parseMinecraftKey(parts[1]);
        PotionEffectType type = key == null ? null : Registry.POTION_EFFECT_TYPE.get(key);
        if (type == null) {
            return;
        }
        int amplifier = parseInt(parts[2], 0);
        int ticks = parseInt(parts[3], 20);
        player.addPotionEffect(new PotionEffect(type, ticks, amplifier, true, false, false));
    }

    private void applyBurn(Player player, String[] parts) {
        if (parts.length < 2) {
            return;
        }
        player.setFireTicks(Math.max(player.getFireTicks(), parseInt(parts[1], 0) * 20));
    }

    private void applyEntityEffect(Player player, String[] parts) {
        if (parts.length < 2) {
            return;
        }
        try {
            player.playEffect(EntityEffect.valueOf(parts[1].toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
