package bgu.spl.mics.application.services;

import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 *
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private final LiDarWorkerTracker liDarWorkerTracker;
    private final LiDarDataBase liDARDataBase;

    /**
     * Constructor for LiDarService.
     *
     * @param liDarWorkerTracker A LiDAR Worker Tracker object that this service will use to manage tracking.
     * @param liDARDataBase      A LiDAR DataBase object that this service will use to retrieve cloud points.
     */
    public LiDarService(LiDarWorkerTracker liDarWorkerTracker) {
        super("LiDarService" + liDarWorkerTracker.getId());
        this.liDarWorkerTracker = liDarWorkerTracker;
        this.liDARDataBase = LiDarDataBase.getInstance(liDarWorkerTracker.getLidarDataPath());
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();
            List<TrackedObject> matchTrackedObjects = liDarWorkerTracker.matchTrackedObjects(currentTick);

            if (!matchTrackedObjects.isEmpty()) {
                sendEvent(new TrackedObjectsEvent(matchTrackedObjects));
            }

            if (currentTick - liDarWorkerTracker.getFrequency() > liDARDataBase.getLastTick()) {
                System.out.println(getName() + " shutting down.");
                liDarWorkerTracker.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(true));
                terminate();
            }
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
            List<TrackedObject> newTrackedObjects = liDarWorkerTracker.createTrackedObjects(liDARDataBase.getCloudPoints(), event);
            if (liDarWorkerTracker.getStatus() == STATUS.ERROR) {
                sendBroadcast(new CrashedBroadcast(this.getName(), ""));
            } else if (liDarWorkerTracker.getFrequency() == 0) {
                sendEvent(new TrackedObjectsEvent(newTrackedObjects));
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
            FusionSlam fusionSlam = FusionSlam.getInstance();
            fusionSlam.addLastLidarFrame(this.getName(), liDarWorkerTracker.getLastTrackedObjects());
            System.out.println(getName() + " received CrashedBroadcast from: " + crashed.getSenderServiceName());
            terminate();
        });
    }
}
