package com.liseasons.scheduler;

import com.liseasons.LISeasonsPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public final class FoliaSchedulerAdapter implements SchedulerAdapter {
    private final LISeasonsPlugin plugin;

    public FoliaSchedulerAdapter(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledHandle runAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks) {
        ScheduledTask scheduledTask = this.plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(this.plugin, scheduled -> task.run(), initialDelayTicks, periodTicks);
        return scheduledTask::cancel;
    }
}
