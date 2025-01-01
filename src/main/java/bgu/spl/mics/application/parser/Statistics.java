package bgu.spl.mics.application.parser;

import java.util.Map;

import bgu.spl.mics.application.objects.Landmark;

class Statistics {
    int systemRuntime;
    int numDetectedObjects;
    int numTrackedObjects;
    int numLandmarks;
    Map<String, Landmark> landMarks;

    public Statistics(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks,
            Map<String, Landmark> landMarks) {
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = numDetectedObjects;
        this.numTrackedObjects = numTrackedObjects;
        this.numLandmarks = numLandmarks;
        this.landMarks = landMarks;
    }
}