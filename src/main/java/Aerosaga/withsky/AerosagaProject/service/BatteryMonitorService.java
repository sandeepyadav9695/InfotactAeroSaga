package Aerosaga.withsky.AerosagaProject.service;


import Aerosaga.withsky.AerosagaProject.config.AeroSagaProperties;
import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.entity.Mission;
import Aerosaga.withsky.AerosagaProject.repositry.DroneRepository;
import Aerosaga.withsky.AerosagaProject.repositry.MissionRepository;
import Aerosaga.withsky.AerosagaProject.temporal.DroneMissionWorkflow;
import io.temporal.client.WorkflowClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BatteryMonitorService {

    private static final Logger log =
            LoggerFactory.getLogger(BatteryMonitorService.class);

    private static final Set<String> ACTIVE_MISSION_STATUSES =
            Set.of("VALIDATING", "READY", "RUNNING", "RETURNING");

    private final DroneRepository drones;
    private final MissionRepository missions;
    private final WorkflowClient temporal;
    private final AeroSagaProperties properties;

    public BatteryMonitorService(
            DroneRepository drones,
            MissionRepository missions,
            WorkflowClient temporal,
            AeroSagaProperties properties) {
        this.drones = drones;
        this.missions = missions;
        this.temporal = temporal;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString =
                    "${aerosaga.monitoring.interval-ms:10000}")
    public void checkBatteryLevels() {
        double threshold = properties.getDrone()
                .getReturnHomeBatteryPercent();

        for (Drone drone : drones.findAll()) {
            Double battery = drone.getBatteryLevel();

            if (battery == null || battery > threshold) {
                continue;
            }

            for (Mission mission :
                    missions.findByDroneId(drone.getId())) {

                if (!ACTIVE_MISSION_STATUSES.contains(
                        mission.getStatus())) {
                    continue;
                }

                String workflowId = mission.setWorkflowId(mission.getWorkflowId());

                if (workflowId == null || workflowId.isBlank()) {
                    continue;
                }

                try {
                    DroneMissionWorkflow workflow =
                            temporal.newWorkflowStub(
                                    DroneMissionWorkflow.class,
                                    workflowId);

                    workflow.requestReturnHome();

                    log.warn(
                            "Return-home signal sent: drone={}, battery={}%, mission={}",
                            drone.getName(),
                            battery,
                            mission.getId());

                } catch (RuntimeException ex) {
                    log.warn(
                            "Could not signal workflow {}: {}",
                            workflowId,
                            ex.getMessage());
                }
                break;
            }
        }
    }
}
