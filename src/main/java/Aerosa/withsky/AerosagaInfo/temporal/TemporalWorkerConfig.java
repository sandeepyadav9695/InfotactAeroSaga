package Aerosa.withsky.AerosagaInfo.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalWorkerConfig {

    @Bean
    public WorkflowServiceStubs workflowServiceStubs(
            @Value("${aerosaga.temporal.target}") String target) {

        WorkflowServiceStubsOptions options =
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(target)
                        .build();

        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public WorkflowClient workflowClient(
            WorkflowServiceStubs serviceStubs) {

        return WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder().build()
        );
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            @Value("${aerosaga.temporal.task-queue}")
            String taskQueue) {

        WorkerFactory factory =
                WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(taskQueue);

        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                new DroneActivitiesImpl()
        );

        return factory;
    }
}
