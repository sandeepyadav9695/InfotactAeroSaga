package Aerosaga.withsky.AerosagaProject.dto;
import java.util.List;
public record DashboardResponse(
    long totalDrones,
    long idleDrones,
    long activeDrones,
    long lowBatteryDrone,
    long totalMission,
    long activeMission,
    long completeMission,
    List<DroneStatus> drones,
    List<MissionStatus>mission,
    List<BatteryAlert> alerts
){
    public record DroneStatus(
            long id,
            String name,
            Double batteryPercent,
            String gpsStatus,
            Double latitude,
            Double longitude

    ){}
    public record MissionStatus(
            Long id,
            String name,
            String status,
            Long droneId,
            String droneName,
            String workflowId
    ){}
    public record BatteryAlert(
            Long droneId,
            String droneName,
            double batteryPercent,
            String severity,
            String message
    ) {}
}
