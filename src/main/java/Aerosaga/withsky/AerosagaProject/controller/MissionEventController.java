package Aerosaga.withsky.AerosagaProject.controller;

import Aerosaga.withsky.AerosagaProject.entity.MissionEvent;
import Aerosaga.withsky.AerosagaProject.repositry.MissionEventRepository;
import Aerosaga.withsky.AerosagaProject.repositry.MissionRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/missions")
public class MissionEventController {

    private final MissionRepository missionRepository;
    private final MissionEventRepository missionEventRepository;

    public MissionEventController(MissionRepository missionRepository, MissionEventRepository missionEventRepository) {
        this.missionRepository = missionRepository;
        this.missionEventRepository = missionEventRepository;
    }
    @GetMapping("/{missionId}/events")
    public List<MissionEvent> getEvents(
            @PathVariable Long missionId) throws ChangeSetPersister.NotFoundException {

        if (!missionRepository.existsById(missionId)) {
            throw new ChangeSetPersister.NotFoundException(
            );
        }

        return missionEventRepository.findByMission_IdOrderByEventTimeAsc(missionId);
    }
}
