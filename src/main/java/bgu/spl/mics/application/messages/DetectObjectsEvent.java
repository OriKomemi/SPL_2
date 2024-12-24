package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

import  bgu.spl.mics.application.objects.DetectedObject;
import java.util.List;

/**
 * DetectObjectsEvent is sent by CameraService to LiDAR workers to process detected objects.
 */
public class DetectObjectsEvent implements Event<List<DetectedObject>> {

    private final List<DetectedObject> detectedObjects;

    /**
     * Constructor for DetectObjectsEvent.
     *
     * @param detectedObjects The list of detected objects to be processed.
     */
    public DetectObjectsEvent(List<DetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    /**
     * @return The list of detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}
