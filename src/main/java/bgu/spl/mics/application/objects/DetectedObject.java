package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
/**
 * Represents a single detected object.
 */
public class DetectedObject {

    private final String id;
    private final String description;

    /**
     * Constructor for DetectedObject.
     *
     * @param id          The ID of the object.
     * @param description A description of the object.
     */
    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * @return The ID of the object.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The description of the object.
     */
    public String getDescription() {
        return description;
    }
}