package Aerosaga.withsky.AerosagaProject.temporal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.SignalMethod;

@WorkflowInterface
public interface DroneMissionWorkflow {

    @WorkflowMethod
    MissionResult execute(MissionInput input);

    @SignalMethod
    void requestCancel();

    @SignalMethod
    void requestReturnHome();
}
