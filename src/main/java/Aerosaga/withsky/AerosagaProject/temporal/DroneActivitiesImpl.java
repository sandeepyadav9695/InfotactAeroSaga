package Aerosaga.withsky.AerosagaProject.temporal;

import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.entity.Mission;
import Aerosaga.withsky.AerosagaProject.repositry.DroneRepository;
import Aerosaga.withsky.AerosagaProject.repositry.MissionRepository;

import io.temporal.failure.ApplicationFailure;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DroneActivitiesImpl implements DroneActivities  {

    private final DroneRepository drones;
    private final MissionRepository missions;

    public DroneActivitiesImpl(
            DroneRepository drones,
            MissionRepository missions) {
        this.drones = drones;
        this.missions = missions;
    }

    private Drone findDrone(Long id) {
        return drones.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Drone not found: " + id));
    }

    private Mission findMission(Long id) {
        return missions.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Mission not found: " + id));
    }

    @Override
    @Transactional
    public void validateAndPrepare(MissionInput input) {
        Drone drone = findDrone(input.DroneId());
        Mission mission = findMission(input.missionId());

        if (mission.getDrone() == null
                || !mission.getDrone().getId()
                .equals(drone.getId())) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "Mission assigned to a different drone",
                    "INVALID_ASSIGNMENT");
        }

        if (!"IDLE".equals(drone.getStatus())
                && !"READY".equals(drone.getStatus())) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "Drone is not available",
                    "DRONE_UNAVAILABLE");
        }

        if (drone.getBatteryLevel() < 30.0) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "Battery must be at least 30%",
                    "LOW_START_BATTERY");
        }

        if (!"CONNECTED".equals(drone.getGpsStatus())) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "GPS is not connected",
                    "GPS_UNAVAILABLE");
        }

        if (input.waypoint() == null
                || input.waypoint().isEmpty()) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "Add at least one waypoint",
                    "NO_WAYPOINTS");
        }
    }

    @Override
    @Transactional
    public void takeOff(Long missionId, Long droneId) {
        Drone drone = findDrone(droneId);
        Mission mission = findMission(missionId);

        drone.setStatus("IN_FLIGHT");
        mission.setStatus("RUNNING");

        drones.save(drone);
        missions.save(mission);
    }

    @Transactional
    @Override
    public double updateTelemetryAndBattery(
            MissionInput input,
            double simulatedSeconds) {

        Drone drone = findDrone(input.DroneId());

        double enduranceSeconds =
                propsEnduranceSeconds();

        double battery = Math.max(
                0.0,
                input.startTimeBattery()
                        - (100.0 * simulatedSeconds
                        / enduranceSeconds));

        drone.setBatteryLevel(
                Math.round(battery * 100.0) / 100.0);

        drones.save(drone);

        return drone.getBatteryLevel();
    }

    private double propsEnduranceSeconds() {
        // Default simulated endurance: 45 minutes.
        return 45.0 * 60.0;
    }

    @Override
    @Transactional
    public void navigate(
            Long missionId,
            Long droneId,
            MissionInput.WayPointInput waypoint) {

        Drone drone = findDrone(droneId);
        findMission(missionId);

        // Your current Drone entity has no GPS coordinate fields.
        // This method updates flight status only.
        drone.setStatus("IN_FLIGHT");
        drones.save(drone);
    }

    @Override
    @Transactional
    public void returnHome(Long missionId, Long droneId) {
        Drone drone = findDrone(droneId);
        findMission(missionId);

        drone.setStatus("RETURNING");
        drones.save(drone);
    }

    @Override
    @Transactional
    public void land(Long missionId, Long droneId) {
        Drone drone = findDrone(droneId);
        Mission mission = findMission(missionId);

        drone.setStatus("IDLE");
        mission.setStatus("COMPLETED");

        drones.save(drone);
        missions.save(mission);
    }

    @Override
    @Transactional
    public void updateMissionStatus(
            Long missionId,
            String status,
            String message) {

        Mission mission = findMission(missionId);
        mission.setStatus(status);
        missions.save(mission);
    }

    @Override
    public double currentBattery(Long droneId) {
        return findDrone(droneId).getBatteryLevel();
    }
}