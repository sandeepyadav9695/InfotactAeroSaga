
package Aerosaga.withsky.AerosagaProject.controller;

import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.service.DroneService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    // Activate an IDLE drone
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Drone> activateDrone(
            @PathVariable Long id) {

        Drone updatedDrone = droneService.activateDrone(id);

        return ResponseEntity.ok(updatedDrone);
    }
    // Deactivate drone
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Drone> deactivateDrone(
            @PathVariable Long id) {

        Drone updatedDrone = droneService.deactivateDrone(id);

        return ResponseEntity.ok(updatedDrone);
    }

    // Register a new drone
    @PostMapping
    public Drone register(@Valid @RequestBody Drone drone) {
        return droneService.register(drone);
    }

    // Get all drones
    @GetMapping
    public List<Drone> getAll() {
        return droneService.getAll();
    }

    // Get drone by ID
    @GetMapping("/{id}")
    public Drone getById(@PathVariable Long id) {
        return droneService.getById(id);
    }

    // Update drone battery
    @PatchMapping("/{id}/battery")
    public Drone updateBattery(
            @PathVariable Long id,
            @RequestParam double batteryLevel) {

        return droneService.updateBattery(id, batteryLevel);
    }
}
