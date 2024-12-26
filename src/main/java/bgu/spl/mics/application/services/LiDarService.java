package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
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
    public LiDarService(LiDarWorkerTracker liDarWorkerTracker, LiDarDataBase liDARDataBase) {
        super("LiDarService" + liDarWorkerTracker.getId());
        this.liDarWorkerTracker = liDarWorkerTracker;
        this.liDARDataBase = liDARDataBase;
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
            System.out.println(getName() + " received TickBroadcast: " + tick.getTick());
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
            List<TrackedObject> trackedObjects = new ArrayList<>();

            // Process detected objects to generate tracked objects
            for (DetectedObject obj : event.getDetectedObjects()) {
                List<StampedCloudPoints> cloudPoints = liDARDataBase.getCloudPoints();
                cloudPoints.stream()
                    .filter(cloudPoint -> cloudPoint.getId().equals(obj.getId()) && cloudPoint.getTime() == event.getTime())
                    .forEach((stampedCloudPoints) -> {
                        // Transform nested arrays into CloudPoint objects
                        List<CloudPoint> transformedData = new ArrayList<>();
                        for (List<Double> point : stampedCloudPoints.getCloudPoints()) {
                            transformedData.add(new CloudPoint(point.get(0), point.get(1), point.get(2)));
                        }
                        trackedObjects.add(
                            new TrackedObject(obj.getId(), stampedCloudPoints.getTime(), obj.getDescription(), transformedData)
                        );
                    });
            }

            // Send TrackedObjectsEvent
            if (!trackedObjects.isEmpty()) {
                liDarWorkerTracker.getLastTrackedObjects().addAll(trackedObjects);
                sendEvent(new TrackedObjectsEvent(trackedObjects));
                System.out.println(getName() + " sent TrackedObjectsEvent with " + trackedObjects.size() + " objects.");
            } else {
                System.out.println(getName() + " did not find any matching objects.");
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
