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
    private StampedDetectedObjects lastDetectedObjects;  
    private String errorMessgae;



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

    public StampedDetectedObjects getLastDetectedObjects() {
        return lastDetectedObjects;
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
            .filter(obj -> obj.getTime() == (currentTick - frequency))
            .forEach(stampedObject -> {
                objs.add(stampedObject);
                for (DetectedObject obj: stampedObject.getDetectedObjects()) {
                    if (obj.getId().equals("ERROR")) {
                        this.status = STATUS.ERROR;
                        this.errorMessgae = obj.getDescription();
                    } else {
                        lastDetectedObjects = stampedObject;
                    }
                        
                }
                stats.addDetectedObjects(stampedObject.getDetectedObjects().size());
            });
        return objs;
    }
}
