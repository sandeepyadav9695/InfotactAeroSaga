
package Aerosaga.withsky.AerosagaProject.service;

import Aerosaga.withsky.AerosagaProject.config.AeroSagaProperties;
import Aerosaga.withsky.AerosagaProject.dto.DashboardResponse;
import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.entity.Mission;
import Aerosaga.withsky.AerosagaProject.repositry.DroneRepository;
import Aerosaga.withsky.AerosagaProject.repositry.MissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "Validating", "Ready", "Running", "Returning"
    );

    private final DroneRepository drones;
    private final MissionRepository missions;
    private final AeroSagaProperties properties;

    public DashboardService(
            DroneRepository drones,
            MissionRepository missions,
            AeroSagaProperties properties) {
        this.drones = drones;
        this.missions = missions;
        this.properties = properties;
    }

    @Transactional
    public DashboardResponse getDashboard() {

        List<Drone> allDrones = drones.findAll();
        List<Mission> allMissions = missions.findAll();

        double lowThreshold =
                properties.getDrone().getReturnHomeBatteryPercent();

        List<DashboardResponse.DroneStatus> droneRows =
                allDrones.stream()
                        .map(d -> new DashboardResponse.DroneStatus(
                                d.getId(),
                                d.getName(),
                                d.getBatteryLevel(),
                                d.getGpsStatus(),
                                null,null
                        ))
                        .toList();

        List<DashboardResponse.MissionStatus> missionRows =
                allMissions.stream()
                        .map(m -> new DashboardResponse.MissionStatus(
                                m.getId(),
                                m.getName(),
                                m.getStatus(),
                                m.getDrone() == null
                                        ? null : m.getDrone().getId(),
                                m.getDrone() == null
                                        ? null : m.getDrone().getName(),
                                null
                        ))
                        .toList();

        List<DashboardResponse.BatteryAlert> alerts =
                allDrones.stream()
                        .filter(d -> d.getBatteryLevel() <= lowThreshold)
                        .map(d -> {
                            double battery = d.getBatteryLevel();

                            boolean critical = battery <= properties
                                    .getDrone()
                                    .getEmergencyBatteryPercent();

                            return new DashboardResponse.BatteryAlert(
                                    d.getId(),
                                    d.getName(),
                                    battery,
                                    critical ? "CRITICAL" : "WARNING",
                                    critical
                                            ? "Critical battery: immediate safe-response review required"
                                            : "Battery low: return-home threshold reached"
                            );
                        })
                        .toList();

        return new DashboardResponse(
                allDrones.size(),
                allDrones.stream()
                        .filter(d -> "IDLE".equals(d.getStatus()))
                        .count(),
                allDrones.stream()
                        .filter(d -> ACTIVE_STATUSES.contains(d.getStatus()))
                        .count(),
                allDrones.stream()
                        .filter(d -> false)
                        .count(),
                allMissions.size(),
                allMissions.stream()
                        .filter(m -> ACTIVE_STATUSES.contains(m.getStatus()))
                        .count(),
                allMissions.stream()
                        .filter(m -> "COMPLETED".equals(m.getStatus()))
                        .count(),
                droneRows,
                missionRows,
                alerts
        );
    }
}
