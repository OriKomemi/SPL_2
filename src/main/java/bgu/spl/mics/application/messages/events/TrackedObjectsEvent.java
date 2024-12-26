package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;

/**
 * TrackedObjectsEvent is sent by LiDarService to provide information about tracked objects.
 */
public class TrackedObjectsEvent implements Event<List<TrackedObject>> {

    private final List<TrackedObject> trackedObjects;

    /**
     * Constructor for TrackedObjectsEvent.
     *
     * @param trackedObjects The list of tracked objects.
     */
    public TrackedObjectsEvent(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    /**
     * @return The list of tracked objects.
     */
    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public String toString() {
        return "TrackedObjectsEvent{" +
                "trackedObjects=" + trackedObjects +
                '}';
    }
}
