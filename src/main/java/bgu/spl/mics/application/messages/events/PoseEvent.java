package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseEvent is sent by PoseService to provide the robot's pose at a specific tick.
 */
public class PoseEvent implements Event<Pose> {

    private final Pose pose;

    /**
     * Constructor for PoseEvent.
     *
     * @param pose The robot's pose at a specific tick.
     */
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    /**
     * @return The robot's pose.
     */
    public Pose getPose() {
        return pose;
    }

    @Override
    public String toString() {
        return "PoseEvent{" +
                "pose=" + pose +
                '}';
    }
}
