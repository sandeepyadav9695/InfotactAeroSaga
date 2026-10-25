package Aerosaga.withsky.AerosagaProject.temporal;

public record MissionResult(
        Long missionId,
        String finalStatus,
        double finalBatteryPercent,
        String message
) {
}