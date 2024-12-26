
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.TerminatedBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.services.LiDarService;

class LiDarServiceTest {

    private LiDarService liDarService;
    private CountDownLatch latch;
    private MessageBusImpl messageBus;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(2); // Wait for both services to initialize
        LiDarDataBase liDARDataBase = LiDarDataBase.getInstance("example input/lidar_data.json");
        System.out.println(liDARDataBase.getCloudPoints().get(0).getCloudPoints());
        liDarService = new LiDarService(liDARDataBase) {
            @Override
            protected void initialize() {
                super.initialize();
                latch.countDown(); // Notify that initialization is complete
            }
        };
        messageBus = MessageBusImpl.getInstance();
    }

    @Test
    void testDetectObjectsEventProcessing() {

        AtomicBoolean eventSent = new AtomicBoolean(false);

        // Create a mock service to listen for TrackedObjectsEvent
        MicroService mockService = new MicroService("MockService") {
            @Override
            protected void initialize() {
                subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent event) -> {
                    eventSent.set(true);
                    List<TrackedObject> trackedObjects = event.getTrackedObjects();
                    assertNotNull(trackedObjects, "Tracked objects list should not be null");
                    assertEquals(1, trackedObjects.size(), "Expected one tracked object");
                    assertEquals("Wall_1", trackedObjects.get(0).getId(), "Tracked object ID should match");
                });

                // Terminate after processing
                subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> terminate());
                latch.countDown(); // Notify that initialization is complete

            }
        };

        // Run LiDarService and MockService in separate threads
        Thread liDarThread = new Thread(liDarService);
        Thread mockServiceThread = new Thread(mockService);
        liDarThread.start();
        mockServiceThread.start();

        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                fail("Services did not initialize in time.");
            }

            // Send a DetectObjectsEvent
            // create DetectObjectsEvent that in LidarDataBase
            messageBus.sendEvent(new DetectObjectsEvent(2, Arrays.asList(new DetectedObject("Wall_1", "test Object"))));
            Thread.sleep(100); // Allow processing time

            assertTrue(eventSent.get(), "TrackedObjectsEvent should have been sent");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        } finally {
            // Send termination signal
            messageBus.sendBroadcast(new TerminatedBroadcast());

            try {
                liDarThread.join();
                mockServiceThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
