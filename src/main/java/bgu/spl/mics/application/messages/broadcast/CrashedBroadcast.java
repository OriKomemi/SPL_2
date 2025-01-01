package bgu.spl.mics.application.messages.broadcast;

import bgu.spl.mics.Broadcast;

/**
 * CrashedBroadcast is sent by sensors to notify other services of a crash.
 */
public class CrashedBroadcast implements Broadcast {

    private final String senderServiceName;
    private final String error;

    /**
     * Constructor for CrashedBroadcast.
     *
     * @param senderServiceName The name of the service that crashed.
     */
    public CrashedBroadcast(String senderServiceName, String error) {
        this.senderServiceName = senderServiceName;
        this.error = error;
    }

    /**
     * @return The name of the service that crashed.
     */
    public String getSenderServiceName() {
        return senderServiceName;
    }

    @Override
    public String toString() {
        return "CrashedBroadcast{" +
                "senderServiceName='" + senderServiceName + '\'' +
                '}';
    }

    public String getError() {
        return error;
    }
}
