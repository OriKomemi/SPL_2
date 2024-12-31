package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.parser.JsonExporter;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 *
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    private final FusionSlam fusionSlam = FusionSlam.getInstance();
    private Pose currentPose;
    private final List<TrackedObject> unhandledTrackedObjects;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService() {
        super("FusionSlamService");
        this.unhandledTrackedObjects = new ArrayList<>();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, (PoseEvent event) -> {
            currentPose = event.getPose();
            System.out.println("currentPose: " + currentPose);
            fusionSlam.addPose(currentPose);
        });

        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent event) -> {

            if (currentPose == null) {
                System.out.println(getName() + " - Cannot process tracked objects without a valid pose.");
                return;
            }

            List<TrackedObject> trackedObjects = event.getTrackedObjects();
            for (TrackedObject obj : trackedObjects) {
                if (obj.getTime() == currentPose.getTime()){
                    fusionSlam.createLandmark(obj, currentPose);
                } else {
                    unhandledTrackedObjects.add(obj);
                }
            }
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            for (int i = unhandledTrackedObjects.size() -1; i >= 0; i--) {
                TrackedObject obj = unhandledTrackedObjects.get(i);
                List<Pose> poses = fusionSlam.getPoses().stream().filter(p -> obj.getTime() == p.getTime()).collect(Collectors.toList());
                if (!poses.isEmpty()) {
                    fusionSlam.createLandmark(obj, poses.get(0)); //assuming only one pose per tick
                    unhandledTrackedObjects.remove(i);
                }
            }
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) -> {
            if (terminated.getIsSensor()) {
                fusionSlam.increaseTerminatedSensorsCounter();
                if (fusionSlam.getNumOfSensors() == fusionSlam.getTerminatedSensorsCounter()) {
                    JsonExporter.exportStatistics(fusionSlam.getLandmarks());
                    sendBroadcast(new TerminatedBroadcast(false));
                    terminate();
                }
            } else {
                System.out.println(getName() + " received TerminatedBroadcast. Exiting...");
                JsonExporter.exportStatistics(fusionSlam.getLandmarks());
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
            System.out.println(getName() + " received CrashedBroadcast from: " + crashed.getSenderServiceName());
            terminate();
        });
    }
}
