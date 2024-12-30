package bgu.spl.mics.application.services;

import java.util.List;
import java.util.stream.Collectors;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedCloudPoints;
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
        //  TODO: add status handle.
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            List<TrackedObject> objectsToProcess = liDarWorkerTracker.getTrackedObjects();
            List<TrackedObject> matchTrackedObjects =  objectsToProcess.stream().filter(
                obj -> obj.getTime() == (currentTick - liDarWorkerTracker.getFrequency())
                ).collect(Collectors.toList());
            if (!matchTrackedObjects.isEmpty()) {
                sendEvent(new TrackedObjectsEvent(matchTrackedObjects));
                liDarWorkerTracker.setLastTrackedObjects(matchTrackedObjects);
            }
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
            List<StampedCloudPoints> cloudPoints = liDARDataBase.getCloudPoints();
            List<TrackedObject> trackedObjects = liDarWorkerTracker.getTrackedObjects();
            // Process detected objects to generate tracked objects
            for (DetectedObject obj : event.getDetectedObjects()) {
                cloudPoints.stream()
                    .filter(cloudPoint -> cloudPoint.getId().equals(obj.getId()) && cloudPoint.getTime() == event.getTime())
                    .forEach((stampedCloudPoints) -> {
                        trackedObjects.add(
                            new TrackedObject(obj.getId(), stampedCloudPoints.getTime(), obj.getDescription(), stampedCloudPoints.getCloudPoints())
                        );
                        liDarWorkerTracker.setTrackedObjects(trackedObjects);
                    });
            }
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> {
            System.out.println(getName() + " received TerminatedBroadcast. Exiting...");
            terminate();
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (crashed) -> {
            System.out.println(getName() + " received CrashedBroadcast from: " + crashed.getSenderServiceName());
            terminate();
        });
    }
}
