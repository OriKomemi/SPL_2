import java.util.Arrays;
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
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.services.PoseService;

class PoseServiceTest {

    private PoseService poseService;
    private MessageBusImpl messageBus;

    @BeforeEach
    void setUp() {
        // Mock data for GPSIMU
        GPSIMU gpsimu = new GPSIMU(
            Arrays.asList(
                new Pose(1, 2.0f, 45.0f, 1),
                new Pose(2, 3.0f, 90.0f, 2),
                new Pose(3, 4.0f, 135.0f, 3)
            ),
            STATUS.UP
        );

        // Initialize PoseService
        poseService = new PoseService(gpsimu);

        // Initialize MessageBus
        messageBus = MessageBusImpl.getInstance();
    }

    @Test
    void testPoseServiceSendsPoseEvent() {
        AtomicBoolean eventSent = new AtomicBoolean(false);

        // Create a MicroService to handle PoseEvent
        MicroService mockService = new MicroService("MockService") {
            @Override
            protected void initialize() {
                subscribeEvent(PoseEvent.class, (PoseEvent event) -> {
                    eventSent.set(true);
                    Pose pose = event.getPose();
                    assertNotNull(pose, "Pose should not be null");
                    assertEquals(1.0f, pose.getX(), "Expected X coordinate");
                    assertEquals(2.0f, pose.getY(), "Expected Y coordinate");
                    assertEquals(45.0f, pose.getYaw(), "Expected yaw angle");
                });
            }
        };

        // Run PoseService and MockService in separate threads
        Thread poseServiceThread = new Thread(poseService);
        Thread mockServiceThread = new Thread(mockService);
        poseServiceThread.start();
        mockServiceThread.start();

        try {
            Thread.sleep(100); // Allow processing time

            // Send a TickBroadcast
            messageBus.sendBroadcast(new TickBroadcast(1)); // Simulate tick 1
            Thread.sleep(100); // Allow processing time

            assertTrue(eventSent.get(), "PoseEvent should be sent at tick 1");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        } finally {
            // Terminate services gracefully
            messageBus.sendBroadcast(new TerminatedBroadcast());

            try {
                poseServiceThread.join();
                mockServiceThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
