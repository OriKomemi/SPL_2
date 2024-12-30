package bgu.spl.mics.application.parser;

import java.util.List;

public class LidarWorkers {
    private List<LidarConfiguration> LidarConfigurations;
    private String lidars_data_path;

    public List<LidarConfiguration> getLidarConfigurations() {
        return LidarConfigurations;
    }
    public String getLidars_data_path() {
        return lidars_data_path;
    }

}