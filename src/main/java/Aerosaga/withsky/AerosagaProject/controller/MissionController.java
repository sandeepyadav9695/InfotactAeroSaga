//package Aerosaga.withsky.AerosagaProject;
//
//import Aerosaga.withsky.AerosagaProject.dto.CreateMissionRequest;
//import Aerosaga.withsky.AerosagaProject.entity.Mission;
//import Aerosaga.withsky.AerosagaProject.service.MissionService;
//import jakarta.validation.Valid;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/Missions")
//public class MissionController {
//    private final MissionService missionService;
//
//    public MissionController(MissionService missionService) {
//        this.missionService = missionService;
//    }
//    @PostMapping
//    public Mission create(@Valid @RequestBody CreateMissionRequest request){
//        return missionService.create(request);
//    }
//    @GetMapping
//    public List<Mission> getAll(){
//        return missionService.getAll();
//    }
//    @GetMapping("/{id}")
//    public Mission getById(@PathVariable Long id){
//        return missionService.getById(id);
//    }
//}

package Aerosaga.withsky.AerosagaProject.controller;

import Aerosaga.withsky.AerosagaProject.dto.CreateMissionRequest;
import Aerosaga.withsky.AerosagaProject.entity.Mission;
import Aerosaga.withsky.AerosagaProject.service.MissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }
    @PostMapping
    public Mission create(
            @Valid @RequestBody CreateMissionRequest request) {
        return missionService.create(request);
    }

    @GetMapping
    public List<Mission> getAll() {
        return missionService.getAll();
    }

    @GetMapping("/{id}")
    public Mission getById(@PathVariable Long id) {
        return missionService.getById(id);
    }
}