package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    // Access method for the singleton instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    private final List<Landmark> landmarks;
    private final List<Pose> poses;

    // Private constructor to prevent instantiation
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
    }

    /**
     * Adds a new landmark to the map.
     *
     * @param landmark The landmark to add.
     */
    public synchronized void addLandmark(Landmark landmark) {
        landmarks.add(landmark);
    }

    /**
     * Updates an existing landmark's coordinates by averaging them with new data.
     *
     * @param landmark The landmark to update.
     * @param newCoordinates The new coordinates to incorporate.
     */
    public synchronized void updateLandmark(Landmark landmark, List<CloudPoint> newCoordinates) {
        List<CloudPoint> updatedCoordinates = new ArrayList<>();

        List<CloudPoint> existingCoordinates = landmark.getCoordinates();
        for (int i = 0; i < existingCoordinates.size(); i++) {
            CloudPoint existing = existingCoordinates.get(i);
            CloudPoint newCoord = newCoordinates.get(i);
            updatedCoordinates.add(new CloudPoint(
                (existing.getX() + newCoord.getX()) / 2,
                (existing.getY() + newCoord.getY()) / 2
            ));
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
}