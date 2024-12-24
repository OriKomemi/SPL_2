import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.MicroService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeServiceTest {

    @Test
    void testTimeService() throws InterruptedException {
        int tickDuration = 100; // 100ms per tick
        int totalTicks = 5; // Run for 5 ticks

        // Create a TimeService
        TimeService timeService = new TimeService(tickDuration, totalTicks);

        // Custom TestService to Count Received Ticks
        class TestService extends MicroService {
            private int receivedTicks = 0;

            public TestService() {
                super("TestService");
            }

            @Override
            protected void initialize() {
                subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
                    receivedTicks++;
                    System.out.println("Received Tick: " + tick.getTick());
                });

                subscribeBroadcast(TerminatedBroadcast.class, (terminated) -> {
                    System.out.println("Received TerminatedBroadcast");
                    terminate();
                });
            }

            public int getReceivedTicks() {
                return receivedTicks;
            }
        }

        TestService testService = new TestService();

        // Run the TimeService and TestService in separate threads
        Thread timeServiceThread = new Thread(timeService);
        Thread testServiceThread = new Thread(testService);

        timeServiceThread.start();
        testServiceThread.start();

        // Wait for the threads to finish
        timeServiceThread.join();
        testServiceThread.join();

        // Assertions
        assertEquals(totalTicks, testService.getReceivedTicks(), "TestService should receive all tick broadcasts");
    }
}
