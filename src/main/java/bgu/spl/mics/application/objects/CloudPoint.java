package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
class CloudPoint {

    private final int x;
    private final int y;

    /**
     * Constructor for CloudPoint.
     *
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     */
    public CloudPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}