package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single camera in the system.
 */
public class Camera {

    private final int id;
    private final String cameraKey;
    private final int frequency;
    private STATUS status;
    private final List<StampedDetectedObjects> detectedObjectsList;
    private int lastTick = 0;
    private final StatisticalFolder stats = StatisticalFolder.getInstance();
    private StampedDetectedObjects lastStampedDetectedObjects;
    private String errorMessgae;



    /**
     * Constructor for the Camera class.
     *
     * @param id                  The ID of the camera.
     * @param frequency           The time interval at which the camera sends events.
     * @param status              The current status of the camera.
     * @param detectedObjectsList The list of detected objects with timestamps.
     */
    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList, String cameraKey) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.cameraKey = cameraKey;
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

    public StampedDetectedObjects getLastStampedDetectedObjects() {
        return lastStampedDetectedObjects;
    }

    /**
     * @return The list of detected objects with timestamps.
     */
    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public String getErrorMessgae() {
        return errorMessgae;
    }

    public List<StampedDetectedObjects> getStampedDetectedObjects(int currentTick) {
        List<StampedDetectedObjects> objs = new ArrayList<>();
        detectedObjectsList.stream()
            .filter(stampedObject -> stampedObject.getTime() == (currentTick - frequency))
            .forEach(stampedObject -> {
                objs.add(stampedObject);
                for (DetectedObject detectedObject: stampedObject.getDetectedObjects()) {
                    if (detectedObject.getId().equals("ERROR")) {
                        this.status = STATUS.ERROR;
                        this.errorMessgae = detectedObject.getDescription();
                    }
                }
                if (this.status != STATUS.ERROR) {
                    this.lastStampedDetectedObjects = stampedObject;
                    stats.addDetectedObjects(stampedObject.getDetectedObjects().size());
                }
            });
        return objs;
    }

    public String getCameraKey() {
        return cameraKey;
    }

}
