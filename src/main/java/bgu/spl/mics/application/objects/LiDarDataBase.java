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

    private static LiDarDataBase instance;
    private final List<StampedCloudPoints> cloudPoints;

    /**
     * Private constructor for singleton pattern.
     *
     * @param cloudPoints The list of stamped cloud points.
     */
    private LiDarDataBase(List<StampedCloudPoints> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static synchronized LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            try (FileReader reader = new FileReader(filePath)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
                List<StampedCloudPoints> data = gson.fromJson(reader, listType);
                instance = new LiDarDataBase(data);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load LiDAR data from file: " + filePath, e);
            }
        }
        return instance;
    }
    /**
     * @return The list of cloud points.
     */
    public List<StampedCloudPoints> getCloudPoints() {
        return new ArrayList<>(cloudPoints);
    }
}
