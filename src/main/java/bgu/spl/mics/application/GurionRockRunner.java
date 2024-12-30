package bgu.spl.mics.application;

import java.io.FileNotFoundException;
import java.util.List;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.parser.Configurations;
import bgu.spl.mics.application.parser.JsonParser;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
          * @throws FileNotFoundException
          */
         public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            System.out.println("NO Config file :(");
            return;
        }

        String configPath = args[0];
        JsonParser parser = new JsonParser(configPath);
        Configurations config = parser.getConfig();

        System.err.println(config.getDuration());
        System.err.println(config.getTickTime());


        List<Camera> cameras = parser.getCameras();
        GPSIMU gpsimu = parser.getGPSIMU();
        List<LiDarWorkerTracker> lidarWorkers = parser.getLidarWorkers();

        // TODO: Initialize system components and services.
        // TODO: Start the simulation.
    }
}
