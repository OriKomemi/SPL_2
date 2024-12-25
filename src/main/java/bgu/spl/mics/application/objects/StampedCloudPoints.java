package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
class StampedCloudPoints {

    private final String id;
    private final int time;
    private final List<List<Double>> cloudPoints;

    /**
     * Constructor for StampedCloudPoints.
     *
     * @param id          The ID of the object.
     * @param time        The time the object was tracked.
     * @param cloudPoints The list of cloud points.
     */
    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public List<List<Double>> getCloudPoints() {
        return cloudPoints;
    }
}