
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;

class CameraServiceTest {

    private CameraService cameraService;
    private MessageBusImpl messageBus;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        List<DetectedObject> singleDetectedObject = Arrays.asList(new DetectedObject("1", "Wall"));
        List<DetectedObject> multipleDetectedObject = Arrays.asList(
            new DetectedObject("2", "Wall"),
            new DetectedObject("3", "Chair Base")
        );
        // Creating mock data for detected objects
        StampedDetectedObjects object1 = new StampedDetectedObjects(
            2,
            singleDetectedObject
        );

        StampedDetectedObjects object2 = new StampedDetectedObjects(
            4,
            multipleDetectedObject
        );

        List<StampedDetectedObjects> detectedObjectsList = Arrays.asList(object1, object2);

        // Initialize Camera and CameraService
        Camera camera = new Camera(1, 2, STATUS.UP, detectedObjectsList);
        cameraService = new CameraService(camera);
    }

    @Test
    void testDetectObjectsEventSent() {
        AtomicBoolean eventSent = new AtomicBoolean(false);

        // Create a MicroService to listen for DetectObjectsEvent
        MicroService mockService = new MicroService("MockService") {
            @Override
            protected void initialize() {
                subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
                    eventSent.set(true);
                    assertNotNull(event.getDetectedObjects());
                    assertFalse(event.getDetectedObjects().isEmpty(), "Expected a non-empty list of detected objects");
                    System.out.println("DetectObjectsEvent received: " + event.getDetectedObjects());
                });

                // Subscribe to TerminatedBroadcast for graceful shutdown
                subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> terminate());
            }
        };

        // // Ensure CameraService also subscribes to TerminatedBroadcast
        // cameraService.subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> cameraService.terminate());


        // Start CameraService and MockService in separate threads
        Thread cameraThread = new Thread(cameraService);
        Thread mockServiceThread = new Thread(mockService);
        cameraThread.start();
        mockServiceThread.start();

        try {
            Thread.sleep(100);
            System.out.println("Sending TickBroadcast for tick 2");
            messageBus.sendBroadcast(new TickBroadcast(2));
            Thread.sleep(100);

            System.out.println("Checking if eventSent is true :" + eventSent.get());
            assertTrue(eventSent.get(), "DetectObjectsEvent should be sent at tick 2");

            eventSent.set(false); // Reset
            System.out.println("Sending TickBroadcast for tick 4");
            messageBus.sendBroadcast(new TickBroadcast(4));
            Thread.sleep(100);

            System.out.println("Checking if eventSent is true");
            assertTrue(eventSent.get(), "DetectObjectsEvent should be sent at tick 4");
            // Send TerminatedBroadcast to terminate services
            messageBus.sendBroadcast(new TerminatedBroadcast());
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        } finally {
            try {
                cameraThread.join();
                mockServiceThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
