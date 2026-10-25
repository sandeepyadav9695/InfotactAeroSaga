package Aerosaga.withsky.AerosagaProject.temporal;
import java.util.List;
public record MissionInput(
        Long missionId,
        Long DroneId,
        int timeScale,
        double startTimeBattery,
        List<WayPointInput> waypoint
) {
    public record WayPointInput(
            Long id,
            int sequence,
            double latitude,
            double altitude
    ){}
}