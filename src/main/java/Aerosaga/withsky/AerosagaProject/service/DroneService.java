
package Aerosaga.withsky.AerosagaProject.service;

import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.repositry.DroneRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DroneService {

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    // Register a new drone
    public Drone register(Drone drone) {

        if (droneRepository.findByName(drone.getName()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Drone Name already Exists"
            );
        }

        drone.setBatteryLevel(100.0);
        drone.setStatus("IDLE");
        drone.setGpsStatus("Connected");

        return droneRepository.save(drone);
    }

    // Get all drones
    public List<Drone> getAll() {
        return droneRepository.findAll();
    }

    // Get drone by ID
    public Drone getById(Long id) {

        return droneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Drone not found: " + id
                ));
    }

    // Activate an IDLE drone
    public Drone activateDrone(Long id) {

        Drone drone = getById(id);

        if (!"IDLE".equalsIgnoreCase(drone.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only IDLE drones can be activated."
            );
        }

        if (drone.getBatteryLevel() <= 10) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Drone battery is too low to activate."
            );
        }

        drone.setStatus("ACTIVE");

        return droneRepository.save(drone);
    }
    // Deactivate an ACTIVE drone
    public Drone deactivateDrone(Long id) {

        Drone drone = getById(id);

        if (!"ACTIVE".equalsIgnoreCase(drone.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only ACTIVE drones can be deactivated."
            );
        }

        drone.setStatus("IDLE");

        return droneRepository.save(drone);
    }
    // Update drone battery
    public Drone updateBattery(Long id, double batteryLevel) {

        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Battery must be between 0 and 100"
            );
        }

        Drone drone = getById(id);

        drone.setBatteryLevel(batteryLevel);

        if (batteryLevel <= 10) {
            drone.setStatus("LOW_BATTERY");
        } else if ("LOW_BATTERY".equalsIgnoreCase(drone.getStatus())) {
            drone.setStatus("IDLE");
        }

        return droneRepository.save(drone);
    }
}
