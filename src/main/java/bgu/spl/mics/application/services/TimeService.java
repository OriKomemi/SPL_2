package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int TickTime;
    private final int Duration;
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
    }

    @Override
    protected void initialize() {
        int[] currentTick = {1};

        subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> {
            terminate();
        });

        new Thread(() -> {
            try {
                while (currentTick[0] <= Duration) {
                    sendBroadcast(new TickBroadcast(currentTick[0]));
                    Thread.sleep(TickTime);
                    currentTick[0]++;
                }
                sendBroadcast(new TerminatedBroadcast());
                terminate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
