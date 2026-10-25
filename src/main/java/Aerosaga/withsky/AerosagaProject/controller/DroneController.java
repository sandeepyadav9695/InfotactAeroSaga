package Aerosaga.withsky.AerosagaProject.controller;

import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.service.DroneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @PostMapping
    public Drone register(@Valid @RequestBody Drone drone) {
        return droneService.register(drone);
    }

    @GetMapping
    public List<Drone> getAll() {
        return droneService.getAll();
    }

    @GetMapping("/{id}")
    public Drone getById(@PathVariable Long id) {
        return droneService.getById(id);
    }

    @PatchMapping("/{id}/battery")
    public Drone updateBattery(
            @PathVariable Long id,
            @RequestParam double batteryLevel) {
        return droneService.updateBattery(id, batteryLevel);
    }
}
