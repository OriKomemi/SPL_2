package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
class Landmark {

    private final String id;
    private final String description;
    private final List<CloudPoint> coordinates;

    /**
     * Constructor for Landmark.
     *
     * @param id          The ID of the landmark.
     * @param description A description of the landmark.
     * @param coordinates The list of coordinates of the landmark.
     */
    public Landmark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }
}
