package com.liseasons.listener;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.config.SeasonEffectConfig;
import com.liseasons.season.SeasonState;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public final class CropGrowListener implements Listener {
    private final LISeasonsPlugin plugin;

    public CropGrowListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCropGrow(BlockGrowEvent event) {
        World world = event.getBlock().getWorld();
        SeasonState state = this.plugin.getSeasonManager().getState(world);
        if (state == null) {
            return;
        }

        SeasonEffectConfig effectConfig = this.plugin.getLiConfig().seasonEffects().get(state.season());
        if (effectConfig == null) {
            return;
        }

        boolean uncovered = event.getBlock().getRelative(org.bukkit.block.BlockFace.UP).getType().isAir();
        if (uncovered && effectConfig.uncoveredCropStop()) {
            event.setCancelled(true);
            return;
        }

        double multiplier = uncovered ? effectConfig.uncoveredCropGrowthMultiplier() : 1.0D;
        if (multiplier <= 1.0D) {
            return;
        }

        BlockState newState = event.getNewState();
        if (!(newState.getBlockData() instanceof Ageable ageable)) {
            return;
        }

        int bonusStages = calculateBonusStages(multiplier);
        if (bonusStages <= 0) {
            return;
        }

        int boostedAge = Math.min(ageable.getMaximumAge(), ageable.getAge() + bonusStages);
        ageable.setAge(boostedAge);
        newState.setBlockData(ageable);
    }

    private int calculateBonusStages(double multiplier) {
        double extraGrowth = multiplier - 1.0D;
        int wholeStages = (int) Math.floor(extraGrowth);
        double fractionalStage = extraGrowth - wholeStages;

        if (fractionalStage > 0.0D && ThreadLocalRandom.current().nextDouble() < fractionalStage) {
            wholeStages++;
        }

        return Math.max(0, wholeStages);
    }
}
