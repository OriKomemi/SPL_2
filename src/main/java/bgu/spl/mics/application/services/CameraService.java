package bgu.spl.mics.application.services;

import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 *
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService" + camera.getId());
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast

    //  TODO: add status handle.
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();
            List<StampedDetectedObjects> objs = camera.getStampedDetectedObjects(currentTick);
            for (StampedDetectedObjects stampedObject : objs) {
                System.out.println("DetectObjectsEvent out");
                sendEvent(new DetectObjectsEvent(stampedObject.getTime(), stampedObject.getDetectedObjects()));
            }
            if (currentTick - camera.getFrequency() > camera.getLastTick()) {
                camera.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(true));
                terminate();
            }

        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) -> {
            if (!terminated.getIsSensor()) {
                System.out.println(getName() + " received TerminatedBroadcast. Exiting...");
                terminate();
            }
        });
        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (crashed) -> {
            System.out.println(getName() + " received CrashedBroadcast from: " + crashed.getSenderServiceName());
            terminate();
        });
    }
}
