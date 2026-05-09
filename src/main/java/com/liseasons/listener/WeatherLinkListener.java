package com.liseasons.listener;

import com.liseasons.LISeasonsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public final class WeatherLinkListener implements Listener {
    @SuppressWarnings("unused")
    private final LISeasonsPlugin plugin;

    public WeatherLinkListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        // 不拦截天气变化，避免影响管理员执行 /weather。
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        // 不拦截雷暴变化，避免影响管理员执行 /weather thunder。
    }
}
