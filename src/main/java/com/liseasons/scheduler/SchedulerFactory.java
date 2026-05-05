package com.liseasons.scheduler;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.util.PlatformUtil;

public final class SchedulerFactory {
    private SchedulerFactory() {
    }

    public static SchedulerAdapter create(LISeasonsPlugin plugin) {
        if (PlatformUtil.isFolia(plugin)) {
            return new FoliaSchedulerAdapter(plugin);
        }
        return new BukkitSchedulerAdapter(plugin);
    }
}
