
package Aerosa.withsky.AerosagaInfo.controller;

import Aerosa.withsky.AerosagaInfo.temporal.DroneMissionWorkflow;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    public MissionController(
            WorkflowClient workflowClient,
            @Value("${aerosaga.temporal.task-queue}")
            String taskQueue) {

        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startMission(
            @RequestBody StartMissionRequest request) {

        if (request == null
                || request.droneId() == null
                || request.droneId().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "droneId is required")
            );
        }

        String workflowId =
                "drone-mission-" + request.droneId()
                        + "-" + UUID.randomUUID();

        DroneMissionWorkflow workflow =
                workflowClient.newWorkflowStub(
                        DroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(taskQueue)
                                .build()
                );

        WorkflowExecution execution =
                WorkflowClient.start(
                        workflow::executeMission,
                        request.droneId()
                );

        return ResponseEntity.accepted().body(
                Map.of(
                        "message", "Drone mission started",
                        "droneId", request.droneId(),
                        "workflowId", execution.getWorkflowId(),
                        "runId", execution.getRunId(),
                        "status", "STARTED"
                )
        );
    }

    public record StartMissionRequest(String droneId) {
    }
}
