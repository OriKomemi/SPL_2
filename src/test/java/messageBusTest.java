import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

/**
 * Basic test suite for MessageBusImpl functionality.
 */
class MessageBusTest {

    private MessageBus messageBus;

    /**
     * Minimal mock Event that returns a String-type result.
     */
    static class TestEvent implements Event<String> { }

    /**
     * Minimal mock Broadcast.
     */
    static class TestBroadcast implements Broadcast { }

    /**
     * Minimal MicroService for testing. We override run() to continuously
     * try to take messages from the bus, but in unit tests, we may not
     * actually spin up a thread. We'll just call awaitMessage(...) directly.
     */
    static class MockMicroService extends MicroService {

        public MockMicroService(String name) {
            super(name);
        }

        @Override
        protected void initialize() {
            // In a real scenario, you'd subscribe to events/broadcasts here.
            // For unit testing, we'll just handle that in the test code.
        }
    }

    @BeforeEach
    void setUp() {
        // Get the singleton instance of MessageBusImpl
        messageBus = MessageBusImpl.getInstance();
    }

    /**
     * 1) Test register/unregister flow:
     *    - Register a microservice and ensure no exception is thrown.
     *    - Unregister the microservice and confirm it is effectively removed.
     */
    @Test
    void testRegisterAndUnregister() {
        MockMicroService mService = new MockMicroService("TestMS");
        // Register
        messageBus.register(mService);

        assertTrue(messageBus.isRegistered(mService), "MicroService should be registered.");

        // Now unregister
        messageBus.unregister(mService);

        // The microservice queue should no longer exist, so calling awaitMessage should fail
        assertThrows(IllegalStateException.class, () -> {
            messageBus.awaitMessage(mService);
        }, "Expect an exception since MicroService is unregistered.");
    }

    /**
     * 2) Test sending and receiving an Event:
     *    - Register a microservice, subscribe it to an event type.
     *    - Send the event, ensure it ends up in the microservice's queue.
     *    - Check that calling 'awaitMessage' returns that event.
     *    - Call 'complete' to resolve the Future, and verify it's resolved.
     */
    @Test
    void testSendEventAndComplete() throws InterruptedException {
        MockMicroService mService = new MockMicroService("EventReceiver");
        messageBus.register(mService);

        // Subscribe the microservice to TestEvent
        messageBus.subscribeEvent(TestEvent.class, mService);

        // Send an event
        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);
        assertNotNull(future, "Future should not be null because there's a subscriber.");

        // Microservice awaits the message
        Message msg = messageBus.awaitMessage(mService);
        assertTrue(msg instanceof TestEvent, "Expected the received message to be a TestEvent.");

        // Complete the event with some result
        messageBus.complete((Event<String>) msg, "TestResult");

        // Check the future is resolved
        String result = future.get(1, TimeUnit.SECONDS);
        assertEquals("TestResult", result, "Future should return the completed value.");
    }

    /**
     * 3) Test sending Broadcast:
     *    - Register two microservices and subscribe them to the same Broadcast.
     *    - Send the broadcast and ensure both receive it.
     */
    @Test
    void testBroadcast() throws InterruptedException {
        MockMicroService m1 = new MockMicroService("M1");
        MockMicroService m2 = new MockMicroService("M2");
        messageBus.register(m1);
        messageBus.register(m2);

        // Subscribe both to TestBroadcast
        messageBus.subscribeBroadcast(TestBroadcast.class, m1);
        messageBus.subscribeBroadcast(TestBroadcast.class, m2);

        // Send broadcast
        TestBroadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);

        // Each microservice should get the broadcast
        Message msg1 = messageBus.awaitMessage(m1);
        Message msg2 = messageBus.awaitMessage(m2);

        assertTrue(msg1 instanceof TestBroadcast, "M1 should receive a TestBroadcast.");
        assertTrue(msg2 instanceof TestBroadcast, "M2 should receive a TestBroadcast.");
    }
}
