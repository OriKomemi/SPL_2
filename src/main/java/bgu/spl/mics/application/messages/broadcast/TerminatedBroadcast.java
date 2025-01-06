package bgu.spl.mics.application.messages.broadcast;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message indicating that the system is terminating.
 */
public class TerminatedBroadcast implements Broadcast {
    private final boolean isSensor;


    public TerminatedBroadcast(boolean isSensor) {
        this.isSensor = isSensor;
    }

    /**
     * @return The current tick.
     */
    public boolean isSensor() {
        return this.isSensor;
    }
}