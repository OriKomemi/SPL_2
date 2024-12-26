package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final int id;
    private final int frequency;
    private STATUS status;
    private final List<TrackedObject> lastTrackedObjects;

    /**
     * Constructor for LiDarWorkerTracker.
     *
     * @param id                 The ID of the LiDAR tracker.
     * @param frequency          The frequency of events from the LiDAR.
     * @param status             The status of the LiDAR tracker.
     * @param lastTrackedObjects The last objects tracked by the LiDAR.
     */
    public LiDarWorkerTracker(int id, int frequency, STATUS status, List<TrackedObject> lastTrackedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.lastTrackedObjects = lastTrackedObjects;
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
}
