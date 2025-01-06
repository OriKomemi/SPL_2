package bgu.spl.mics.application.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class JsonParser{
    private Configurations config;
    private String dir;

    public JsonParser(String configPath) {
        this.dir = Paths.get(configPath).getParent().toString();
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configPath)) {
            this.config = gson.fromJson(reader, Configurations.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Static inner class for holding the singleton instance
    private static class Holder {
        private static JsonParser instance;

        // Initialize the instance with the configuration path
        private static void initialize(String configPath) {
            if (instance == null) {
                instance = new JsonParser(configPath);
            }
        }
    }

    // Public method to initialize the singleton instance
    public static void initialize(String configPath) {
        Holder.initialize(configPath);
    }

    // Public method to get the singleton instance
    public static JsonParser getInstance() {
        if (Holder.instance == null) {
            throw new IllegalStateException("JsonParser is not initialized. Call initialize() first.");
        }
        return Holder.instance;
    }

    public Configurations getConfig() {
        return config;
    }

    public List<Camera> getCameras() throws FileNotFoundException {
        List<Camera> cameras = new ArrayList<>();

        Gson gson = new Gson();
        Path fullPath = Paths.get(dir, config.getCameras().getCamera_datas_path());
        System.err.println(fullPath.normalize().toFile());
        FileReader reader = new FileReader(fullPath.normalize().toFile());
        // Define the type for a map of cameras to their detected objects
        Type mapType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();

        // Parse the JSON into the Map
        Map<String, List<StampedDetectedObjects>> cameraData = gson.fromJson(reader, mapType);

        // Iterate over the cameras
        for (Map.Entry<String, List<StampedDetectedObjects>> entry : cameraData.entrySet()) {
            String cameraName = entry.getKey();
            List<StampedDetectedObjects> detections = entry.getValue();
            CameraConfiguration cameraConf = config.getCameras().getCameraConfiguration(cameraName);
            cameras.add(new Camera(cameraConf.getId(), cameraConf.getFrequency(), detections, cameraName));
        }
        return cameras;
    }

    public List<LiDarWorkerTracker> getLidarWorkers() {
        List<LiDarWorkerTracker> workers = new ArrayList<>();

        for (LidarConfiguration conf : config.getLiDarWorkers().getLidarConfigurations()) {
            System.out.println("conf.getId()"+conf.getId());
            System.out.println("conf.getFrequency()"+conf.getFrequency());
            workers.add(new LiDarWorkerTracker(conf.getId(), conf.getFrequency()));
        }
        return workers;
    }

    public GPSIMU getGPSIMU() throws FileNotFoundException {
        List<Pose> poses;
        Gson gson = new Gson();
        Path fullPath = Paths.get(dir, config.getPoseJsonFile());
        FileReader reader = new FileReader(fullPath.normalize().toFile());
        Type type = new TypeToken<List<Pose>>() {}.getType();
        poses = gson.fromJson(reader, type);
        return new GPSIMU(poses);
    }

    public String getDir() {
        return dir;
    }

    public String getLidarDatabasePath() {
        Path fullPath = Paths.get(dir, config.getLiDarWorkers().getLidars_data_path());
        return fullPath.normalize().toString();
    }
}
