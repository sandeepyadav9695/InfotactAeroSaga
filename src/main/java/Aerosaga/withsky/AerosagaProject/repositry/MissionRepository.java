package Aerosaga.withsky.AerosagaProject.repositry;

import Aerosaga.withsky.AerosagaProject.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByDrone_Id(Long droneId);

    Mission[] findByDroneId(Long id);
}