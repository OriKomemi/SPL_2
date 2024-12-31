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
    private final String lidarDataPath;
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
    public LiDarWorkerTracker(int id, int frequency, String lidarDataPath) {
        this.id = id;
        this.frequency = frequency;
        this.lidarDataPath = lidarDataPath;
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

    public void setLastTrackedObjects(List<TrackedObject> val) {
        this.lastTrackedObjects = val;
    }

    public void setTrackedObjects(List<TrackedObject> val) {
        this.trackedObjects = val;
    }

    public String getLidarDataPath() {
        return lidarDataPath;
    }
    public void createTrackedObjects(List<StampedCloudPoints> cloudPoints, DetectObjectsEvent event) {
        for (DetectedObject obj : event.getDetectedObjects()) {
            System.out.println(obj.getId());
            cloudPoints.stream()
                .filter(cloudPoint -> cloudPoint.getId().equals(obj.getId()) && cloudPoint.getTime() == event.getTime())
                .forEach((stampedCloudPoints) -> {
                    trackedObjects.add(
                        new TrackedObject(obj.getId(), stampedCloudPoints.getTime(), obj.getDescription(), stampedCloudPoints.getCloudPoints())
                    );
                    this.setTrackedObjects(trackedObjects);
                });
        }
    }

    public List<TrackedObject> matchTrackedObjects(int currentTick) {
        List<TrackedObject> matchTrackedObjects =  trackedObjects.stream().filter(
            obj -> obj.getTime() == (currentTick - frequency)
            ).collect(Collectors.toList());
        if (!matchTrackedObjects.isEmpty()) {
            lastTrackedObjects = matchTrackedObjects;
            stats.addTrackedObjects(matchTrackedObjects.size());
            return matchTrackedObjects;
        } else {
            return new ArrayList<>();
        }
    }

}
