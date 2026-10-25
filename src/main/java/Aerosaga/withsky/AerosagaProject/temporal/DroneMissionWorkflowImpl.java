package Aerosaga.withsky.AerosagaProject.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow{
    private final DroneActivities activities= Workflow.newActivityStub(
            DroneActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(
                    Duration.ofSeconds(20)
            ).setRetryOptions(RetryOptions.newBuilder().setInitialInterval(
                    Duration.ofSeconds(1)
            ).setMaximumInterval(Duration.ofSeconds(5))
                    .setMaximumAttempts(3).build()
    ).build());

    private boolean cancelRequest;
    private boolean returnRequest;

    @Override
    public MissionResult execute(MissionInput input) {
        activities.updateMissionStatus(input.missionId(),"Validating","pre-fight check started");
        activities.validateAndPrepare(input);
        activities.updateMissionStatus(
                input.missionId(),"ready","pre-flight check passed"
        );
        activities.takeOff(input.missionId(), input.DroneId());
        activities.updateMissionStatus(input.missionId(), "Running","Drone Airborn");
        double simulatedSeconds=0;
        double battery=activities.currentBattery(input.DroneId());
        int scale=Math.max(1,input.timeScale());
        while(simulatedSeconds<45*60 && battery>10){
            if (cancelRequest||returnRequest||battery<=20){
                break;
            }
            for (MissionInput.WayPointInput waypoints:input.waypoint()){
                if(cancelRequest||returnRequest||battery<=25){
                    break;
                }
                activities.navigate(input.missionId() ,input.DroneId(),waypoints);
                Workflow.sleep(Duration.ofSeconds(Math.max(1,60/scale)));
                simulatedSeconds+=60;
                battery = activities.updateTelemetryAndBattery(input, simulatedSeconds);
                if (battery<=25){
                    break;
                }
            }
        }
        String finalstatus;
        if(cancelRequest){
            activities.updateMissionStatus(input.missionId(), "cancel request","cancel request; landing sequence initiated");
            activities.returnHome(input.missionId(), input.DroneId());
            activities.land(input.missionId(), input.DroneId());
            finalstatus="Cancelled";
        } else if (returnRequest||battery<=25||simulatedSeconds>=45*60) {
            activities.updateMissionStatus(input.missionId(), "return","return to home triggered");
            activities.returnHome(input.missionId(), input.DroneId());
            activities.land(input.missionId(), input.DroneId());
            finalstatus=battery<=10?"Failed":"Compiled";
        }else {
            activities.returnHome(input.missionId(), input.DroneId());
            activities.land(input.missionId(), input.DroneId());
            finalstatus="Compiled";
        }
        activities.updateMissionStatus(input.missionId(), finalstatus,"Mission work finished");
        return new MissionResult(input.missionId(), finalstatus,activities.currentBattery(input.DroneId()),"MIssion finished; check telemetry and mission events");
    }

    @Override
    public void requestCancel() {
        cancelRequest=true;
    }

    @Override
    public void requestReturnHome() {
        returnRequest=true;
    }
}
