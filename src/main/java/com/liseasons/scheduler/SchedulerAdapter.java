package com.liseasons.scheduler;

public interface SchedulerAdapter {
    ScheduledHandle runAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks);
}
