package Aerosaga.withsky.AerosagaProject.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    public static final String TASK_QUEUE =
            "AEROSAGA_MISSION_QUEUE";

    @Bean
    WorkflowServiceStubs workflowServiceStubs(
            @Value("${temporal.target:localhost:7233}")
            String target) {

        return WorkflowServiceStubs.newInstance(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(target)
                        .build());
    }

    @Bean
    WorkflowClient workflowClient(
            WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    WorkerFactory workerFactory(
            WorkflowClient client,
            DroneActivities activities) {

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class);

        worker.registerActivitiesImplementations(activities);

        return factory;
    }
}
