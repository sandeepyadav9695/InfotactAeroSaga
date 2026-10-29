package Aerosaga.withsky.AerosagaProject.service;

import Aerosaga.withsky.AerosagaProject.dto.CreateMissionRequest;
import Aerosaga.withsky.AerosagaProject.dto.WaypointRequest;
import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.entity.Mission;
import Aerosaga.withsky.AerosagaProject.entity.WayPoint;
import Aerosaga.withsky.AerosagaProject.repositry.DroneRepository;
import Aerosaga.withsky.AerosagaProject.repositry.MissionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;

    public MissionService(
            MissionRepository missionRepository,
            DroneRepository droneRepository) {
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
    }

    @Transactional
    public Mission create(CreateMissionRequest request) {

        Drone drone = droneRepository.findById(request.droneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Drone not found"));

        if (!"IDLE".equals(drone.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Drone is not IDLE");
        }

        if (drone.getBatteryLevel() < 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Drone battery must be at least 30% to create mission");
        }

        if (!"Connected".equalsIgnoreCase(drone.getGpsStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Drone GPS is disconnected");
        }

        List<WaypointRequest> requests = request.waypoints();

        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mission needs at least one waypoint");
        }

        Mission mission = new Mission();
        mission.setName(request.name());
        mission.setStatus("CREATED");
        mission.setDrone(drone);

        for (int i = 0; i < requests.size(); i++) {

            WaypointRequest point = requests.get(i);

            WayPoint wayPoint = new WayPoint();
            wayPoint.setLatitude(point.latitude());
            wayPoint.setLongitude(point.longitude());
            wayPoint.setSequenceNumber(i + 1);

            mission.addWaypoint(wayPoint);
        }

        return missionRepository.save(mission);
    }

    @Transactional(readOnly = true)
    public List<Mission> getAll() {
        return missionRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Mission getById(Long id) {
        return missionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mission not found: " + id));
    }
}
