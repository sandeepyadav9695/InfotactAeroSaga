package Aerosaga.withsky.AerosagaProject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aerosaga")
public class AeroSagaProperties {

    private Drone drone = new Drone();

    public Drone getDrone() {
        return drone;
    }

    public void setDrone(Drone drone) {
        this.drone = drone;
    }

    public static class Drone {

        private double returnHomeBatteryPercent = 25.0;
        private double emergencyBatteryPercent = 10.0;

        public double getReturnHomeBatteryPercent() {
            return returnHomeBatteryPercent;
        }

        public void setReturnHomeBatteryPercent(
                double returnHomeBatteryPercent) {
            this.returnHomeBatteryPercent = returnHomeBatteryPercent;
        }

        public double getEmergencyBatteryPercent() {
            return emergencyBatteryPercent;
        }

        public void setEmergencyBatteryPercent(
                double emergencyBatteryPercent) {
            this.emergencyBatteryPercent = emergencyBatteryPercent;
        }
    }
}
