package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * GPSIMU represents the robot's GPS and IMU, providing pose data based on ticks.
 */
public class GPSIMU {

    private int currentTick;
    private STATUS status;
    private final List<Pose> poseList;

    /**
     * Constructor for GPSIMU.
     *
     * @param poseList The list of time-stamped poses.
     * @param status   The initial status of the GPSIMU.
     */
    public GPSIMU(List<Pose> poseList) {
        this.poseList = poseList;
        this.status = STATUS.UP;
        this.currentTick = 0;
    }

    /**
     * Sets the current tick for the GPSIMU.
     *
     * @param currentTick The current simulation tick.
     */
    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Retrieves the current pose based on the current tick.
     *
     * @return The Pose for the current tick, or null if not available.
     */
    public Pose getCurrentPose() {
        if (currentTick > 0 && currentTick <= poseList.size()) {
            return poseList.get(currentTick - 1); // Assuming ticks start at 1
        }
        return null;
    }

    /**
     * @return The current status of the GPSIMU.
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Sets the status of the GPSIMU.
     *
     * @param status The new status of the GPSIMU.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }
}
