package Aerosaga.withsky.AerosagaProject.repositry;

import Aerosaga.withsky.AerosagaProject.entity.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TelemetryRepository
        extends JpaRepository<Telemetry, Long> {

    List<Telemetry> findTop100ByMissionIdOrderByRecordedAtDesc(
            Long missionId);
}
