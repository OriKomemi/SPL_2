package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single camera in the system.
 */
public class Camera {

    private final int id;
    private final int frequency;
    private STATUS status;
    private final List<StampedDetectedObjects> detectedObjectsList;
    private int lastTick = 0;
    private final StatisticalFolder stats = StatisticalFolder.getInstance();



    /**
     * Constructor for the Camera class.
     *
     * @param id                  The ID of the camera.
     * @param frequency           The time interval at which the camera sends events.
     * @param status              The current status of the camera.
     * @param detectedObjectsList The list of detected objects with timestamps.
     */
    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = detectedObjectsList;
        for (StampedDetectedObjects stampedObj : detectedObjectsList) {
            if (stampedObj.getTime() > lastTick) {
                lastTick = stampedObj.getTime();
            }
        }
    }

    /**
     * @return The ID of the camera.
     */
    public int getId() {
        return id;
    }

    /**
     * @return The frequency of the camera.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @return The current status of the camera.
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Sets the status of the camera.
     *
     * @param status The new status of the camera.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    public int getLastTick() {
        return lastTick;
    }

    /**
     * @return The list of detected objects with timestamps.
     */
    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public List<StampedDetectedObjects> getStampedDetectedObjects(int currentTick) {
        List<StampedDetectedObjects> objs = new ArrayList<>();
        detectedObjectsList.stream()
            .filter(obj -> obj.getTime() == (currentTick - frequency))
            .forEach(stampedObject -> {
                objs.add(stampedObject);
                stats.addDetectedObjects(stampedObject.getDetectedObjects().size());
            });
        return objs;
    }
}
