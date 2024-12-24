package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {

    private final int time;
    private final List<DetectedObject> detectedObjects;

    /**
     * Constructor for StampedDetectedObjects.
     *
     * @param time            The time at which the objects were detected.
     * @param detectedObjects The list of detected objects.
     */
    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    /**
     * @return The time at which the objects were detected.
     */
    public int getTime() {
        return time;
    }

    /**
     * @return The list of detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}
