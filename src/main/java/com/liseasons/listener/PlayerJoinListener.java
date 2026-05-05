package com.liseasons.listener;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.season.SeasonState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {
    private final LISeasonsPlugin plugin;

    public PlayerJoinListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.plugin.getLiConfig().joinNotify()) {
            return;
        }

        Player player = event.getPlayer();
        SeasonState state = this.plugin.getSeasonManager().getState(player.getWorld());
        if (state == null) {
            return;
        }

        player.sendMessage(this.plugin.getMessageManager().message(
                "notify.join",
                MessageManager.Placeholder.of("season", this.plugin.getMessageManager().seasonName(state.season().key())),
                MessageManager.Placeholder.of("term", this.plugin.getMessageManager().solarTermName(state.solarTerm().key()))
        ));
    }
}
