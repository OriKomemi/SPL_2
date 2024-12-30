package bgu.spl.mics.application.parser;

public class Configurations {
    private Cameras Cameras;
    private LidarWorkers LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;


    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public LidarWorkers getLiDarWorkers() {
        return LiDarWorkers;
    }

    public Cameras getCameras() {
        return Cameras;
    }

}
