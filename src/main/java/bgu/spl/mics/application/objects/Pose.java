package bgu.spl.mics.application.objects;

public class Pose {
    private int time;
    private double x;
    private double y;
    private double yaw;

    public Pose(int time, double x, double y, double yaw) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.yaw = yaw;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return "Pose{" +
               "time=" + time +
               ", x=" + x +
               ", y=" + y +
               ", yaw=" + yaw +
               '}';
    }
}
