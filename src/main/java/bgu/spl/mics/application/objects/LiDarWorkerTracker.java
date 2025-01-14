package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bgu.spl.mics.application.messages.events.DetectObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> trackedObjects;
    private List<TrackedObject> lastTrackedObjects;
    private final StatisticalFolder stats = StatisticalFolder.getInstance();

    /**
     * Constructor for LiDarWorkerTracker.
     *
     * @param id                 The ID of the LiDAR tracker.
     * @param frequency          The frequency of events from the LiDAR.
     * @param status             The status of the LiDAR tracker.
     * @param lastTrackedObjects The last objects tracked by the LiDAR.
     */
    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.trackedObjects = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public void setTrackedObjects(List<TrackedObject> val) {
        this.trackedObjects = val;
    }

    public void createTrackedObjects(List<StampedCloudPoints> cloudPoints, DetectObjectsEvent event) {
        List<TrackedObject> matchTrackedObjects = new ArrayList<>();
        for (DetectedObject obj : event.getDetectedObjects()) {
            cloudPoints.stream()
                .filter(cloudPoint -> cloudPoint.getId().equals(obj.getId()) && cloudPoint.getTime() == event.getDetectedTime())
                .forEach((stampedCloudPoints) -> {
                    if (stampedCloudPoints.getId().equals("ERROR"))
                        this.status = STATUS.ERROR;
                    matchTrackedObjects.add(
                        new TrackedObject(obj.getId(), event.getDetectedTime(), obj.getDescription(), stampedCloudPoints.getCloudPoints())
                    );
                    System.out.println("addTrackObj: " + obj.getId() + event.getDetectedTime() + obj.getDescription() + stampedCloudPoints.getCloudPoints().size());
                });
        }
        trackedObjects.addAll(matchTrackedObjects);
    }

    public List<TrackedObject> matchTrackedObjects(int currentTick) {
        List<TrackedObject> matchTrackedObjects =  trackedObjects.stream().filter(
            obj -> obj.getTime() == (currentTick - frequency)
            ).collect(Collectors.toList());

        if (!matchTrackedObjects.isEmpty()) {
            this.lastTrackedObjects = new ArrayList<>(matchTrackedObjects);
            this.trackedObjects.removeAll(lastTrackedObjects);
            stats.addTrackedObjects(matchTrackedObjects.size());
            return matchTrackedObjects;
        } else {
            return new ArrayList<>();
        }
    }

}
