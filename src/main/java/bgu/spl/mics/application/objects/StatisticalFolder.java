package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

public class StatisticalFolder {
    private static StatisticalFolder instance;

    private final AtomicInteger systemRuntime; // Total runtime in ticks
    private final AtomicInteger numDetectedObjects; // Total objects detected by cameras
    private final AtomicInteger numTrackedObjects; // Total objects tracked by LiDAR workers
    private final AtomicInteger numLandmarks; // Unique landmarks mapped

    // Private constructor to prevent direct instantiation
    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }

    // Static method to provide access to the singleton instance
    public static synchronized StatisticalFolder getInstance() {
        if (instance == null) {
            instance = new StatisticalFolder();
        }
        return instance;
    }

    // Increment system runtime
    public void incrementRuntime() {
        systemRuntime.incrementAndGet();
    }

    // Add to detected objects count
    public void addDetectedObjects(int count) {
        numDetectedObjects.addAndGet(count);
    }

    // Add to tracked objects count
    public void addTrackedObjects(int count) {
        numTrackedObjects.addAndGet(count);
    }

    // Add to landmarks count
    public synchronized void addLandmark() {
        numLandmarks.incrementAndGet();
    }

    // Getters
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }
}
