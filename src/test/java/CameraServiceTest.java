
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        camera = new Camera(1, 2, STATUS.UP, detectedObjectsList);
    }

    @Test
    void testDetectObjectsEventSent() {
        AtomicBoolean eventSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(2); // Wait for both services to initialize

        // Create a MicroService to listen for DetectObjectsEvent
        MicroService mockService = new MicroService("MockService") {
            @Override
            protected void initialize() {
                subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
                    eventSent.set(true);
                    assertNotNull(event.getDetectedObjects());
                    assertFalse(event.getDetectedObjects().isEmpty());
                    System.out.println("DetectObjectsEvent received: " + event.getDetectedObjects());
                });

                // Subscribe to TerminatedBroadcast for graceful shutdown
                subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> {
                    System.out.println(getName() + " received TerminatedBroadcast. Exiting...");
                    terminate();
                });

                latch.countDown(); // Notify that initialization is complete
            }
        };

        // Extend CameraService to use latch
        CameraService cameraServiceWithLatch = new CameraService(camera) {
            @Override
            protected void initialize() {
                super.initialize();
                latch.countDown(); // Notify that initialization is complete
            }
        };

        // Start services in separate threads
        Thread cameraThread = new Thread(cameraServiceWithLatch);
        Thread mockServiceThread = new Thread(mockService);
        cameraThread.start();
        mockServiceThread.start();

        try {
            // Wait for both services to initialize
            if (!latch.await(2, TimeUnit.SECONDS)) {
                fail("Services did not initialize in time.");
            }

            // Simulate TickBroadcast messages
            messageBus.sendBroadcast(new TickBroadcast(2)); // Should trigger an event
            Thread.sleep(100); // Allow time for processing

            assertTrue(eventSent.get(), "DetectObjectsEvent should be sent at tick 2");

            eventSent.set(false); // Reset flag
            messageBus.sendBroadcast(new TickBroadcast(4)); // Should trigger another event
            Thread.sleep(100);

            assertTrue(eventSent.get(), "DetectObjectsEvent should be sent at tick 4");

            // Send TerminatedBroadcast to terminate services
            messageBus.sendBroadcast(new TerminatedBroadcast());
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        } finally {
            try {
                cameraThread.join(); // Wait for threads to finish
                mockServiceThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
