
package Aerosaga.withsky.AerosagaProject.repositry;

import Aerosaga.withsky.AerosagaProject.entity.MissionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionEventRepository
        extends JpaRepository<MissionEvent, Long> {

    List<MissionEvent> findByMission_IdOrderByEventTimeAsc(Long missionId);
}
