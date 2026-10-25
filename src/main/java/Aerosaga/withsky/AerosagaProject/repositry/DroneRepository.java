package Aerosaga.withsky.AerosagaProject.repositry;


import Aerosaga.withsky.AerosagaProject.entity.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drone, Long> {
    Optional<Drone> findByName(String name);
}
