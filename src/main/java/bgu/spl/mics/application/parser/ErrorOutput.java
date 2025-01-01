package bgu.spl.mics.application.parser;

import java.util.List;
import java.util.Map;

import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.TrackedObject;

public class ErrorOutput {
    String error;
    String faultySensor;
    Map<String, StampedDetectedObjects> lastCamerasFrame;
    Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    List<Pose> poses;
    Statistics statistics;

    public ErrorOutput(String error, String faultySensor, Map<String, StampedDetectedObjects> lastCamerasFrame,
            Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame, List<Pose> poses, Statistics statistics) {
        this.error = error;
        this.faultySensor = faultySensor;
        this.lastCamerasFrame = lastCamerasFrame;
        this.lastLiDarWorkerTrackersFrame = lastLiDarWorkerTrackersFrame;
        this.poses = poses;
        this.statistics = statistics;
    }
}