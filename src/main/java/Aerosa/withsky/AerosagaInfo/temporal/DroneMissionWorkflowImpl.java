package Aerosa.withsky.AerosagaInfo.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl
        implements DroneMissionWorkflow {

    private final DroneActivities activities =
            Workflow.newActivityStub(
                    DroneActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(30))
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setInitialInterval(
                                                    Duration.ofSeconds(1))
                                            .setMaximumAttempts(3)
                                            .build())
                            .build()
            );

    @Override
    public String executeMission(String droneId) {
        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Starting mission for drone {}", droneId);

        String result = activities.takeOff(droneId);

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Mission takeoff step completed");

        return result;
    }
}
