package bgu.spl.mics.application.messages.events;

import java.util.List;

import  bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;

/**
 * DetectObjectsEvent is sent by CameraService to LiDAR workers to process detected objects.
 */
public class DetectObjectsEvent implements Event<List<DetectedObject>> {
    private final int time;
    private final int detectedTime;
    private final List<DetectedObject> detectedObjects;

    /**
     * Constructor for DetectObjectsEvent.
     *
     * @param detectedObjects The list of detected objects to be processed.
     */
    public DetectObjectsEvent(int time, int detectedTime, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedTime = detectedTime;
        this.detectedObjects = detectedObjects;
    }

    /**
     * @return The list of detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    public int getTime() {
        return time;
    }

    public int getDetectedTime() {
        return detectedTime;
    }
}
