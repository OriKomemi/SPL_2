package bgu.spl.mics.application.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int TickTime; // Duration of each tick in milliseconds
    private final int Duration; // Total number of ticks before termination
    private final ScheduledExecutorService scheduler;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.TickTime = TickTime;
        this.Duration = Duration;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void initialize() {
        StatisticalFolder stats = StatisticalFolder.getInstance();
        // Schedule periodic tick broadcasts
        scheduler.scheduleAtFixedRate(new Runnable() {
            private int currentTick = 1;

            @Override
            public void run() {
                if (currentTick <= Duration) {
                    sendBroadcast(new TickBroadcast(currentTick));
                    stats.incrementRuntime();
                    currentTick++;
                } else {
                    // Send TerminatedBroadcast and shut down
                    sendBroadcast(new TerminatedBroadcast(false));
                    scheduler.shutdown();
                    terminate();
                }
            }
        }, 0, TickTime, TimeUnit.SECONDS);

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) -> {
            if (!terminated.getIsSensor()) {
                System.out.println(getName() + " received TerminatedBroadcast. Exiting...");
                terminate();
            }
        });
    }
}
