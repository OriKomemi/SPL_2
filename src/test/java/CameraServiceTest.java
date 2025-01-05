
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;

class CameraServiceTest {

    private Camera camera;
    private List<StampedDetectedObjects> stampedDetectedObjectsList;

    @BeforeEach
    void setUp() {
        // Pre-condition: Initialize Camera with detected objects
        stampedDetectedObjectsList = Arrays.asList(
            new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("1", "Tree"),
                new DetectedObject("ERROR", "Error Object")
            )),
            new StampedDetectedObjects(4, Arrays.asList(
                new DetectedObject("2", "Car"),
                new DetectedObject("3", "Building")
            ))
        );
        camera = new Camera(1, 2, stampedDetectedObjectsList);
    }

    @Test
    void testGetStampedDetectedObjects() {
        int currentTick = 6;
        List<StampedDetectedObjects> result = camera.getStampedDetectedObjects(currentTick);

        assertNotNull(result, "Result should not be null.");
        assertEquals(1, result.size(), "Expected one object.");

        StampedDetectedObjects expectedStampedObject = stampedDetectedObjectsList.stream()
            .filter(obj -> obj.getTime() == (currentTick - camera.getFrequency()))
            .findFirst()
            .orElse(null);

        StampedDetectedObjects stampedObject = result.get(0);
        assertEquals(expectedStampedObject.getTime(), stampedObject.getTime(), "Detected time should match currentTick - frequency.");

        List<DetectedObject> expectedObjects = expectedStampedObject.getDetectedObjects();
        List<DetectedObject> actualObjects = stampedObject.getDetectedObjects();
        
        for (int i = 0; i < expectedObjects.size(); i++) {
            DetectedObject expected = expectedObjects.get(i);
            DetectedObject actual = actualObjects.get(i);

            assertEquals(expected.getId(), actual.getId(), "Detected object ID should match.");
            assertEquals(expected.getDescription(), actual.getDescription(), "Detected object description should match.");
        }

    }
    
    @Test
    void testGetStampedDetectedObjectsWithError() {
        
        List<StampedDetectedObjects> detectedObjects = camera.getStampedDetectedObjects(4);

        
        detectedObjects.forEach(stampedObject -> stampedObject.getDetectedObjects().forEach(obj -> {
            if (obj.getId().equals("ERROR")) {
                camera.setStatus(STATUS.ERROR);
            }
        }));
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should change to ERROR at tick 4.");

    }

}
