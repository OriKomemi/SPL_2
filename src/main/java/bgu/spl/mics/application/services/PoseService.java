package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService sends PoseEvents based on the current robot pose provided by the GPSIMU.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();
            gpsimu.setCurrentTick(currentTick);
            Pose currentPose = gpsimu.getCurrentPose();
            if (currentPose != null) {
                sendEvent(new PoseEvent(currentPose));
            }
            if (gpsimu.getLastTick() < currentTick) {
                gpsimu.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(true));
                terminate();
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) -> {
            if (!terminated.isSensor()) {
                System.out.println(getName() + " received TerminatedBroadcast. Exiting...");
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (crashed) -> {
            System.out.println(getName() + " received CrashedBroadcast from: " + crashed.getFaultySensor());
            terminate();
        });
    }
}
