
package Aerosaga.withsky.AerosagaProject.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DroneActivities {

    @ActivityMethod
    void validateAndPrepare(MissionInput input);

    @ActivityMethod
    void takeOff(Long missionId, Long droneId);

    @ActivityMethod
    double updateTelemetryAndBattery(
            MissionInput input,
            double simulatedSeconds);

    @ActivityMethod
    void navigate(
            Long missionId,
            Long droneId,
            MissionInput.WayPointInput waypoint);

    @ActivityMethod
    void returnHome(Long missionId, Long droneId);

    @ActivityMethod
    void land(Long missionId, Long droneId);

    @ActivityMethod
    void updateMissionStatus(
            Long missionId,
            String status,
            String message);

    @ActivityMethod
    double currentBattery(Long droneId);
}
