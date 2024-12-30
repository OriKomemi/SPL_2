package bgu.spl.mics.application.objects;

import java.util.List;

public class StampedCloudPointsParse {

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
    public StampedCloudPointsParse(String id, int time, List<List<Double>> cloudPoints) {
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