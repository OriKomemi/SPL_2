package bgu.spl.mics.application.parser;

import java.util.Map;

import bgu.spl.mics.application.objects.Landmark;

public class ValidOutput {
    private final int systemRuntime;
    private final int numDetectedObjects;
    private final int numTrackedObjects;
    private final int numLandmarks;
    private final Map<String, Landmark> landMarks;

    public ValidOutput(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks, Map<String, Landmark> landMarks) {
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = numDetectedObjects;
        this.numTrackedObjects = numTrackedObjects;
        this.numLandmarks = numLandmarks;
        this.landMarks = landMarks;
    }
}