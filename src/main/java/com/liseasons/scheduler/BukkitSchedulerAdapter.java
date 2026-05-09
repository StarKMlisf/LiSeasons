package com.liseasons.scheduler;

import com.liseasons.LISeasonsPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitSchedulerAdapter implements SchedulerAdapter {
    private final LISeasonsPlugin plugin;

    public BukkitSchedulerAdapter(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledHandle runAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks) {
        BukkitTask bukkitTask = this.plugin.getServer().getScheduler()
                .runTaskTimer(this.plugin, task, initialDelayTicks, periodTicks);
        return bukkitTask::cancel;
    }
}
