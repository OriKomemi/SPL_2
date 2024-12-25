package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
class StatisticalFolder {

    private final AtomicInteger systemRuntime = new AtomicInteger(0);
    private final AtomicInteger numDetectedObjects = new AtomicInteger(0);
    private final AtomicInteger numTrackedObjects = new AtomicInteger(0);
    private final AtomicInteger numLandmarks = new AtomicInteger(0);

    /**
     * Updates the runtime of the system.
     */
    public void incrementRuntime() {
        systemRuntime.incrementAndGet();
    }

    /**
     * @return The total runtime of the system.
     */
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    /**
     * Increments the count of detected objects.
     */
    public void incrementDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }

    /**
     * @return The cumulative count of detected objects.
     */
    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    /**
     * Increments the count of tracked objects.
     */
    public void incrementTrackedObjects() {
        numTrackedObjects.incrementAndGet();
    }

    /**
     * @return The cumulative count of tracked objects.
     */
    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    /**
     * Increments the count of landmarks.
     */
    public void incrementLandmarks() {
        numLandmarks.incrementAndGet();
    }

    /**
     * @return The total number of landmarks.
     */
    public int getNumLandmarks() {
        return numLandmarks.get();
    }
}
