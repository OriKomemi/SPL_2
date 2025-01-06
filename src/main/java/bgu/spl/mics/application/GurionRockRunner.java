package bgu.spl.mics.application;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.parser.Configurations;
import bgu.spl.mics.application.parser.JsonParser;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

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

        // Parse configuration
        String configPath = args[0];
        JsonParser.initialize(configPath);
        JsonParser parser = JsonParser.getInstance();
        Configurations config = parser.getConfig();

        List<Camera> cameras = parser.getCameras();
        GPSIMU gpsimu = parser.getGPSIMU();
        List<LiDarWorkerTracker> lidarWorkers = parser.getLidarWorkers();

        LiDarDataBase.initialize(parser.getLidarDatabasePath());
        LiDarDataBase.getInstance();

        int numOfSensors = cameras.size() + lidarWorkers.size() + 1; //cameras + lidarWorkers + gpsimu
        FusionSlam.initialize(numOfSensors);

        // Create and initialize services
        TimeService timeService = new TimeService(config.getTickTime(), config.getDuration());
        FusionSlamService fusionSlamService = new FusionSlamService();
        PoseService poseService = new PoseService(gpsimu);

        List<MicroService> lidarServices = lidarWorkers.stream()
        .map(LiDarService::new)
        .collect(Collectors.toList());

        List<MicroService> cameraServices = cameras.stream()
                .map(CameraService::new)
                .collect(Collectors.toList());



 // Combine all services except TimeService
        List<MicroService> allServices = new ArrayList<>();
        allServices.addAll(lidarServices);
        allServices.add(poseService);
        allServices.add(fusionSlamService);
        allServices.addAll(cameraServices);

        // Initialize thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(allServices.size() + 1);

        // Start all services except TimeService
        allServices.forEach(service -> executorService.execute(service));

        // Delay execution of TimeService by 100 milliseconds
        executorService.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100); // Delay before starting TimeService
                timeService.run();
            } catch (InterruptedException e) {
                System.err.println("TimeService startup interrupted: " + e.getMessage());
            }
        });

        // Shut down the executor after all services finish
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                System.err.println("Executor did not terminate within the expected time.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        System.out.println("Simulation completed. Results exported to output_file.json");
    }
}
