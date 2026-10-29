package Aerosaga.withsky.AerosagaProject.repositry;

import Aerosaga.withsky.AerosagaProject.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    @Query("""
        SELECT DISTINCT m
        FROM Mission m
        LEFT JOIN FETCH m.drone
        LEFT JOIN FETCH m.waypoints
        """)
    List<Mission> findAllWithDetails();

    @Query("""
        SELECT DISTINCT m
        FROM Mission m
        LEFT JOIN FETCH m.drone
        LEFT JOIN FETCH m.waypoints
        WHERE m.id = :id
        """)
    Optional<Mission> findByIdWithDetails(@Param("id") Long id);

    List<Mission> findByDrone_Id(Long droneId);

    // Retained for compatibility with BatteryMonitorService.
    Mission[] findByDroneId(Long id);
}