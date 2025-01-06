
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

class CameraTest {

    private Camera camera;
    private List<StampedDetectedObjects> stampedDetectedObjectsList;

    @BeforeEach
    void setUp() {
        // Pre-condition: Initialize Camera with detected objects
        stampedDetectedObjectsList = Arrays.asList(
            new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("2", "Car"),
                new DetectedObject("3", "Building")
            )),
            new StampedDetectedObjects(4, Arrays.asList(
                new DetectedObject("1", "Tree"),
                new DetectedObject("ERROR", "Error Object")
            ))
        );
        camera = new Camera(1, 2, stampedDetectedObjectsList);
    }

    @Test
    void testGetStampedDetectedObjects() {
        int currentTick = 4;
        List<StampedDetectedObjects> result = camera.getStampedDetectedObjects(currentTick);

        assertNotNull(result, "Result should not be null.");
        assertEquals(2, result.get(0).getDetectedObjects().size(), "Expected Two detected objects.");

        StampedDetectedObjects expectedStampedObjects = stampedDetectedObjectsList.stream()
            .filter(obj -> obj.getTime() == (currentTick - camera.getFrequency()))
            .findFirst()
            .orElse(null);

        StampedDetectedObjects stampedObjects = result.get(0);
        assertEquals(expectedStampedObjects.getTime(), stampedObjects.getTime(), "Detected time should match currentTick - frequency.");

        List<DetectedObject> expectedObjects = expectedStampedObjects.getDetectedObjects();
        List<DetectedObject> actualObjects = stampedObjects.getDetectedObjects();

        for (int i = 0; i < expectedObjects.size(); i++) {
            DetectedObject expected = expectedObjects.get(i);
            DetectedObject actual = actualObjects.get(i);

            assertEquals(expected.getId(), actual.getId(), "Detected object ID should match.");
            assertEquals(expected.getDescription(), actual.getDescription(), "Detected object description should match.");
        }

    }

    @Test
    void testGetStampedDetectedObjectsWithError() {
        int currentTick = 6;
        List<StampedDetectedObjects> detectedObjects = camera.getStampedDetectedObjects(currentTick);
        // after detecting an error, camera status should change to STATUS.ERROR
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should change to ERROR at tick 4.");

    }

}
