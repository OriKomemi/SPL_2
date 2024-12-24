package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message that signals the current tick of the simulation.
 */
public class TickBroadcast implements Broadcast {
    private final int tick;

    /**
     * Constructor for TickBroadcast.
     *
     * @param tick The current tick.
     */
    public TickBroadcast(int tick) {
        this.tick = tick;
    }

    /**
     * @return The current tick.
     */
    public int getTick() {
        return tick;
    }
}
