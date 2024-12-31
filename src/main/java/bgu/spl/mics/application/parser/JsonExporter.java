package bgu.spl.mics.application.parser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.application.objects.Landmark;
import bgu.spl.mics.application.objects.StatisticalFolder;

public class JsonExporter {
    public static void exportStatistics(List<Landmark> landmarks) {
        StatisticalFolder stats = StatisticalFolder.getInstance();

        // Prepare JSON data
        ExportData data = new ExportData(
            stats.getSystemRuntime(),
            stats.getNumDetectedObjects(),
            stats.getNumTrackedObjects(),
            stats.getNumLandmarks(),
            landmarks.stream().collect(Collectors.toMap(Landmark::getId, landmark -> landmark))
        );

        // Convert to JSON and write to file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path fullPath = Paths.get(JsonParser.getInstance().getDir(), "output_file.json");
        try (FileWriter writer = new FileWriter(fullPath.normalize().toFile())) {
            gson.toJson(data, writer);
            System.out.println("Statistics exported successfully to: " + fullPath.normalize().toString());
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
    }

    // Helper class to hold the export structure
    private static class ExportData {
        private final int systemRuntime;
        private final int numDetectedObjects;
        private final int numTrackedObjects;
        private final int numLandmarks;
        private final Map<String, Landmark> landMarks;

        public ExportData(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks, Map<String, Landmark> landMarks) {
            this.systemRuntime = systemRuntime;
            this.numDetectedObjects = numDetectedObjects;
            this.numTrackedObjects = numTrackedObjects;
            this.numLandmarks = numLandmarks;
            this.landMarks = landMarks;
        }
    }
}