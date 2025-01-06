package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance;

        // Called from initialize() below
        private static synchronized void init(String filePath) {
            if (instance == null) {
                try (FileReader reader = new FileReader(filePath)) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<StampedCloudPointsParse>>() {}.getType();
                    List<StampedCloudPointsParse> data = gson.fromJson(reader, listType);
                    List<StampedCloudPoints> transformedData = new ArrayList<>();
                    for (StampedCloudPointsParse stampedCloudPoints : data) {
                        List<CloudPoint> points = new ArrayList<>();
                        for (List<Double> point : stampedCloudPoints.getCloudPoints()) {
                            points.add(new CloudPoint(point.get(0), point.get(1)));
                        }
                        transformedData.add(new StampedCloudPoints(
                                stampedCloudPoints.getId(),
                                stampedCloudPoints.getTime(),
                                points
                        ));
                    }

                    instance = new LiDarDataBase(transformedData);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to load LiDAR data from file: " + filePath, e);
                }
            }
        }
    }

    public static synchronized void initialize(String filePath) {
        // If re-initializing is not desired, you could remove the reset line.
        LiDarDataBaseHolder.instance = null;  // allow re-initialization if needed
        LiDarDataBaseHolder.init(filePath);
    }

    public static LiDarDataBase getInstance() {
        if (LiDarDataBaseHolder.instance == null) {
            return null;
        }
        return LiDarDataBaseHolder.instance;
    }

    private final List<StampedCloudPoints> cloudPoints;
    private int lastTick;

    private LiDarDataBase(List<StampedCloudPoints> cloudPoints) {
        this.cloudPoints = cloudPoints;
        this.lastTick = 0;
        for (StampedCloudPoints point : cloudPoints) {
            if (point.getTime() > this.lastTick) {
                this.lastTick = point.getTime();
            }
        }
    }

    public List<StampedCloudPoints> getCloudPoints() {
        return new ArrayList<>(cloudPoints);
    }

    public int getLastTick() {
        return lastTick;
    }
}
