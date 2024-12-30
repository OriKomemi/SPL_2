package bgu.spl.mics.application.parser;

import java.util.List;

public class Cameras {
    private List<CameraConfiguration> CamerasConfigurations;
    private String camera_datas_path;

    public List<CameraConfiguration> getCamerasConfigurations() {
        return CamerasConfigurations;
    }
    public CameraConfiguration getCameraConfiguration(String cameraKey) {
        for (CameraConfiguration conf : CamerasConfigurations) {
            if (conf.getCamera_key().equals(cameraKey)) {
                return conf;
            }
        }
        return null;
    }

    public String getCamera_datas_path() {
        return camera_datas_path;
    }

}