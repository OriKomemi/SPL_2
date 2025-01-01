package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam INSTANCE;

        private static void initialize(int numOfSensors) {
            if (INSTANCE == null) {
                INSTANCE = new FusionSlam(numOfSensors);
            }
        }
    }

    // Access method for the singleton instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Public method to initialize the singleton instance
    public static void initialize(int numOfSensors) {
        FusionSlamHolder.initialize(numOfSensors);
    }

    private final List<Landmark> landmarks;
    private final List<Pose> poses;
    private int numOfSensors;
    private int terminatedSensorsCounter;
    private final StatisticalFolder stats = StatisticalFolder.getInstance();
    private Map<String, TrackedObject> lastLiDarWorkerTrackersFrame = new HashMap<>();
    private Map<String, StampedDetectedObjects> lastCamerasFrame = new HashMap<>();


    // Private constructor to prevent instantiation
    private FusionSlam(int numOfSensors) {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.numOfSensors = numOfSensors;
        this.terminatedSensorsCounter = 0;

    }

    /**
     * Adds a new landmark to the map.
     *
     * @param landmark The landmark to add.
     */
    public synchronized void addLandmark(Landmark landmark) {
        System.out.println(landmark.getId());
        landmarks.add(landmark);
        stats.addLandmark();
    }

    public List<CloudPoint> transformToGlobalCoordinates(TrackedObject obj, Pose pose) {
        double cosTheta = Math.cos(Math.toRadians(pose.getYaw()));
        double sinTheta = Math.sin(Math.toRadians(pose.getYaw()));

        return obj.getCoordinates().stream().map(local -> new CloudPoint(
            cosTheta * local.getX() - sinTheta * local.getY() + pose.getX(),
            sinTheta * local.getX() + cosTheta * local.getY() + pose.getY()
        )).collect(Collectors.toList());
    }

    public void createLandmark(TrackedObject obj, Pose pose) {
        Landmark existingLandmark = findLandmarkById(obj.getId());
        if (existingLandmark == null) {
            // Transform object coordinates to global frame and add as a new landmark
            Landmark newLandmark = new Landmark(
                obj.getId(),
                obj.getDescription(),
                transformToGlobalCoordinates(obj, pose)
            );
            addLandmark(newLandmark);
        } else {
            // Update existing landmark with averaged coordinates
            updateLandmark(existingLandmark, transformToGlobalCoordinates(obj, pose));
        }
    }
    /**
     * Updates an existing landmark's coordinates by averaging them with new data.
     *
     * @param landmark The landmark to update.
     * @param newCoordinates The new coordinates to incorporate.
     */
    public synchronized void updateLandmark(Landmark landmark, List<CloudPoint> newCoordinates) {
        List<CloudPoint> existingCoordinates = landmark.getCoordinates();
        List<CloudPoint> updatedCoordinates = new ArrayList<>();
        int minSize = Math.min(existingCoordinates.size(), newCoordinates.size());

        for (int i = 0; i < minSize; i++) {
            CloudPoint existing = existingCoordinates.get(i);
            CloudPoint newCoord = newCoordinates.get(i);
            updatedCoordinates.add(new CloudPoint(
                (existing.getX() + newCoord.getX()) / 2,
                (existing.getY() + newCoord.getY()) / 2
            ));
        }

        if (existingCoordinates.size() > minSize) {
            updatedCoordinates.addAll(existingCoordinates.subList(minSize, existingCoordinates.size()));
        } else if (newCoordinates.size() > minSize) {
            updatedCoordinates.addAll(newCoordinates.subList(minSize, newCoordinates.size()));
        }

        landmark.setCoordinates(updatedCoordinates);
    }

    /**
     * Finds a landmark by its ID.
     *
     * @param id The ID of the landmark.
     * @return The landmark if found, or null otherwise.
     */
    public synchronized Landmark findLandmarkById(String id) {
        Optional<Landmark> landmark = landmarks.stream()
            .filter(l -> l.getId().equals(id))
            .findFirst();
        return landmark.orElse(null);
    }

    /**
     * Retrieves all landmarks.
     *
     * @return A list of all landmarks.
     */
    public synchronized List<Landmark> getLandmarks() {
        return new ArrayList<>(landmarks);
    }

    /**
     * Adds a new pose to the list of poses.
     *
     * @param pose The pose to add.
     */
    public synchronized void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * Retrieves the list of all poses.
     *
     * @return A list of all poses.
     */
    public synchronized List<Pose> getPoses() {
        return new ArrayList<>(poses);
    }

    public synchronized List<Pose> getPosesByTime(int time) {
        return new ArrayList<>(poses);
    }

    public int getNumOfSensors() {
        return numOfSensors;
    }

    public void setNumOfSensors(int numOfSensors) {
        this.numOfSensors = numOfSensors;
    }

    public int getTerminatedSensorsCounter() {
        return terminatedSensorsCounter;
    }

    public void increaseTerminatedSensorsCounter() {
        this.terminatedSensorsCounter += 1;
    }

}