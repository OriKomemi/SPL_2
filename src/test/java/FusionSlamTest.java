import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Landmark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    void setUp() {
        FusionSlam.initialize(2); // Initialize with 2 sensors
        fusionSlam = FusionSlam.getInstance();
    }

    @Test
    void testCreateLandmarkForNewObject() {
        // Preconditions: Setup test data
        Pose pose = new Pose(1,5.0, 10.0, 30.0); // Robot pose: x = 5, y = 10, yaw = 30 degrees
        TrackedObject trackedObject = new TrackedObject("1",1, "Test Object", Arrays.asList(new CloudPoint(2.0, 3.0)));

        // Invoke the method to create a landmark
        fusionSlam.createLandmark(trackedObject, pose);

        // Postconditions: Verify that a new landmark is created
        List<Landmark> landmarks = fusionSlam.getLandmarks();
        assertEquals(1, landmarks.size(), "A single landmark should be created.");

        Landmark createdLandmark = landmarks.get(0);
        assertEquals("1", createdLandmark.getId(), "The ID of the landmark should match the tracked object ID.");
        assertEquals("Test Object", createdLandmark.getDescription(), "The description should match the tracked object.");

        double yawRadians = Math.toRadians(30.0);
        double cosTheta = Math.cos(yawRadians);
        double sinTheta = Math.sin(yawRadians);

        // Verify the coordinates transformation
        CloudPoint expectedPoint = new CloudPoint(
            (cosTheta * 2.0) - (sinTheta * 3.0) + 5.0, // X
            (sinTheta * 2.0) + (cosTheta * 3.0) + 10.0  // Y
        );

        CloudPoint transformedPoint = createdLandmark.getCoordinates().get(0);

        assertEquals(expectedPoint.getX(), transformedPoint.getX(), 0.1, "X-coordinate should match expected value.");
        assertEquals(expectedPoint.getY(), transformedPoint.getY(), 0.1, "Y-coordinate should match expected value.");
    }

    @Test
    void testCreateLandmarkForExistingObject() {
        // Preconditions: Add an initial landmark
        Pose initialPose = new Pose(1, 5.0, 10.0, 30.0);
        TrackedObject initialTrackedObject = new TrackedObject("1", 1, "Test Object", Arrays.asList(new CloudPoint(2.0, 3.0)));
        fusionSlam.createLandmark(initialTrackedObject, initialPose);

        // Add an updated tracked object for the same landmark
        Pose updatedPose = new Pose(3,6.0, 12.0, 45.0);
        TrackedObject updatedTrackedObject = new TrackedObject("1", 3,"Updated Object", Arrays.asList(new CloudPoint(3.0, 4.0)));

        // Invoke the method to update the landmark
        fusionSlam.createLandmark(updatedTrackedObject, updatedPose);

        // Postconditions: Verify the landmark is updated
        List<Landmark> landmarks = fusionSlam.getLandmarks();
        assertEquals(1, landmarks.size(), "The existing landmark should be updated, not duplicated.");

        Landmark updatedLandmark = landmarks.get(0);
        assertEquals("1", updatedLandmark.getId(), "The ID should remain consistent.");

        // Verify the updated coordinates (average of old and new global coordinates)
        double yawRadians = Math.toRadians(30.0);
        double cosTheta = Math.cos(yawRadians);
        double sinTheta = Math.sin(yawRadians);
        CloudPoint initialGlobalPoint = new CloudPoint(
            (cosTheta * 2.0) - (sinTheta * 3.0) + 5.0, // X
            (sinTheta * 2.0) + (cosTheta * 3.0) + 10.0  // Y
        );

        yawRadians = Math.toRadians(45.0);
        cosTheta = Math.cos(yawRadians);
        sinTheta = Math.sin(yawRadians);
        CloudPoint updatedGlobalPoint = new CloudPoint(
            (cosTheta * 3.0) - (sinTheta * 4.0) + 6.0, // X
            (sinTheta * 3.0) + (cosTheta * 4.0) + 12.0 // Y
        );

        double averagedX = (initialGlobalPoint.getX() + updatedGlobalPoint.getX()) / 2.0;
        double averagedY = (initialGlobalPoint.getY() + updatedGlobalPoint.getY()) / 2.0;

        CloudPoint updatedLandmarkPoint = updatedLandmark.getCoordinates().get(0);

        assertEquals(averagedX, updatedLandmarkPoint.getX(), 0.1, "X-coordinate should match averaged value.");
        assertEquals(averagedY, updatedLandmarkPoint.getY(), 0.1, "Y-coordinate should match averaged value.");
    }
}