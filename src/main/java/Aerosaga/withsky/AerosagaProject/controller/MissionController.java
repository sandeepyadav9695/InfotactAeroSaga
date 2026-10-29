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
import Aerosaga.withsky.AerosagaProject.entity.WayPoint;
import Aerosaga.withsky.AerosagaProject.service.MissionService;
import Aerosaga.withsky.AerosagaProject.temporal.DroneMissionWorkflow;
import Aerosaga.withsky.AerosagaProject.temporal.MissionInput;
import Aerosaga.withsky.AerosagaProject.temporal.TemporalConfig;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;
    private final WorkflowClient workflowClient;

    public MissionController(
            MissionService missionService,
            WorkflowClient workflowClient) {
        this.missionService = missionService;
        this.workflowClient = workflowClient;
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

    @PostMapping("/{missionId}/start")
    public ResponseEntity<?> startMission(
            @PathVariable Long missionId,
            @RequestBody(required = false) MissionActionRequest request) {

        Mission mission = missionService.getById(missionId);

        int timeScale = request != null && request.getTimeScale() != null
                ? request.getTimeScale()
                : 60;

        if (timeScale < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "timeScale must be at least 1");
        }

        String workflowId = workflowId(missionId);
        MissionInput input = buildMissionInput(mission, timeScale);

        try {
            DroneMissionWorkflow workflow =
                    workflowClient.newWorkflowStub(
                            DroneMissionWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setWorkflowId(workflowId)
                                    .setTaskQueue(TemporalConfig.TASK_QUEUE)
                                    .build());

            WorkflowClient.start(workflow::execute, input);

            return ResponseEntity.accepted().body(
                    new ActionResponse(
                            missionId,
                            workflowId,
                            "Mission workflow started"));

        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A workflow already exists for mission " + missionId);
        }
    }

    @PostMapping("/{missionId}/return-home")
    public ResponseEntity<?> returnHome(
            @PathVariable Long missionId) {

        missionService.getById(missionId);

        DroneMissionWorkflow workflow = runningWorkflow(missionId);
        workflow.requestReturnHome();

        return ResponseEntity.accepted().body(
                new ActionResponse(
                        missionId,
                        workflowId(missionId),
                        "Return-home signal sent"));
    }

    @PostMapping("/{missionId}/cancel")
    public ResponseEntity<?> cancelMission(
            @PathVariable Long missionId) {

        missionService.getById(missionId);

        DroneMissionWorkflow workflow = runningWorkflow(missionId);
        workflow.requestCancel();

        return ResponseEntity.accepted().body(
                new ActionResponse(
                        missionId,
                        workflowId(missionId),
                        "Cancellation signal sent"));
    }

    private DroneMissionWorkflow runningWorkflow(Long missionId) {
        return workflowClient.newWorkflowStub(
                DroneMissionWorkflow.class,
                workflowId(missionId));
    }

    private MissionInput buildMissionInput(
            Mission mission,
            int timeScale) {

        if (mission.getDrone() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mission has no assigned drone");
        }

        List<WayPoint> waypoints = mission.getWaypoints();

        if (waypoints == null || waypoints.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mission has no waypoints");
        }

        List<MissionInput.WayPointInput> waypointInputs =
                waypoints.stream()
                        .map(point -> new MissionInput.WayPointInput(
                                point.getId(),
                                point.getSequenceNumber(),
                                point.getLatitude(),
                                30.0
                        ))
                        .toList();

        return new MissionInput(
                mission.getId(),
                mission.getDrone().getId(),
                timeScale,
                mission.getDrone().getBatteryLevel(),
                waypointInputs
        );
    }

    private String workflowId(Long missionId) {
        return "mission-" + missionId;
    }

    public static class MissionActionRequest {

        private Integer timeScale;

        public Integer getTimeScale() {
            return timeScale;
        }

        public void setTimeScale(Integer timeScale) {
            this.timeScale = timeScale;
        }
    }

    public record ActionResponse(
            Long missionId,
            String workflowId,
            String message) {
    }
}
