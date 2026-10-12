package Aerosa.withsky.AerosagaInfo.temporal;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneActivitiesImpl implements DroneActivities {

    private static final Logger log =
            LoggerFactory.getLogger(DroneActivitiesImpl.class);

    @Override
    public String takeOff(String droneId) {

        log.info(
                "Drone {}: performing pre-flight simulation",
                droneId
        );

        // Simulate an external operation, not real flight control.
        Activity.getExecutionContext()
                .heartbeat("Pre-flight simulation started");

        log.info(
                "Drone {}: takeoff simulation completed",
                droneId
        );

        return "Takeoff simulation successful for " + droneId;
    }
}
