package com.liseasons.listener;

import com.liseasons.LISeasonsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家离线监听器
 * 负责清理玩家相关的事件和节日数据
 */
public final class PlayerQuitListener implements Listener {
    private final LISeasonsPlugin plugin;

    public PlayerQuitListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 清理季节事件数据
        if (this.plugin.getSeasonEventManager() != null) {
            this.plugin.getSeasonEventManager().cleanupPlayer(player.getUniqueId());
        }
        
        // 清理节日数据
        if (this.plugin.getFestivalManager() != null) {
            this.plugin.getFestivalManager().cleanupPlayer(player.getUniqueId());
        }
    }
}
