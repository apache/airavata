package org.apache.airavata.k8s.orchestrator.service;

import org.apache.airavata.k8s.api.resources.application.ApplicationDeploymentResource;
import org.apache.airavata.k8s.api.resources.application.ApplicationIfaceResource;
import org.apache.airavata.k8s.api.resources.compute.ComputeResource;
import org.apache.airavata.k8s.api.resources.experiment.ExperimentInputResource;
import org.apache.airavata.k8s.api.resources.experiment.ExperimentOutputResource;
import org.apache.airavata.k8s.api.resources.experiment.ExperimentResource;
import org.apache.airavata.k8s.api.resources.process.ProcessResource;
import org.apache.airavata.k8s.api.resources.task.TaskParamResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.orchestrator.messaging.KafkaSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class ExperimentLaunchService {

    private final RestTemplate restTemplate;
    private final KafkaSender kafkaSender;

    @Value("${api.server.url}")
    private String apiServerUrl;

    @Value("${scheduler.topic.name}")
    private String schedulerTopic;

    public ExperimentLaunchService(RestTemplate restTemplate, KafkaSender kafkaSender) {
        this.restTemplate = restTemplate;
        this.kafkaSender = kafkaSender;
    }

    public void launch(long experimentId) {
        ExperimentResource experimentResource = this.restTemplate.getForObject(
                "http://" + this.apiServerUrl + "/experiment/{experimentId}",
                ExperimentResource.class,
                experimentId);

        ApplicationIfaceResource ifaceResource = this.restTemplate.getForObject(
                "http://" + this.apiServerUrl + "/appiface/{ifaceId}",
                ApplicationIfaceResource.class,
                experimentResource.getApplicationInterfaceId());

        ApplicationDeploymentResource deploymentResource = this.restTemplate.getForObject(
                "http://" + this.apiServerUrl + "/appdep/{depId}",
                ApplicationDeploymentResource.class,
                experimentResource.getApplicationDeploymentId());

        ComputeResource computeResource = this.restTemplate.getForObject(
                "http://" + this.apiServerUrl + "/compute/{computeId}",
                ComputeResource.class,
                deploymentResource.getComputeResourceId());

        ProcessResource processResource = new ProcessResource();
        processResource.setCreationTime(System.currentTimeMillis());
        processResource.setExperimentDataDir("/tmp/" + experimentId);
        processResource.setExperimentId(experimentId);

        List<TaskResource> taskDagResources = determineTaskDag(experimentResource, ifaceResource, deploymentResource, processResource, computeResource);
        processResource.setTasks(taskDagResources);

        Long processId = this.restTemplate.postForObject("http://" + this.apiServerUrl + "/process", processResource, Long.class);

        System.out.println("Iface " + ifaceResource.getName() + ", Dep " + deploymentResource.getId());
        kafkaSender.send(schedulerTopic, processId.toString());
    }

    private List<TaskResource> determineTaskDag(ExperimentResource exRes,
                                               ApplicationIfaceResource appIfRes,
                                               ApplicationDeploymentResource appDepRes,
                                               ProcessResource processResource,
                                               ComputeResource computeResource) {

        List<TaskResource> taskDag = new ArrayList<>();

        AtomicInteger dagOrder = new AtomicInteger(0);

        Optional.ofNullable(appDepRes.getPreJobCommand()).ifPresent(preJob -> {
            TaskResource resource = new TaskResource();
            resource.setTaskType(TaskResource.TaskTypes.ENV_SETUP);
            resource.setCreationTime(System.currentTimeMillis());
            resource.setTaskDetail("Pre-job command for experiment " + exRes.getId());
            resource.setTaskParams(Arrays.asList(
                    new TaskParamResource().setKey("command").setValue(preJob),
                    new TaskParamResource().setKey("compute-id").setValue(computeResource.getId() + ""),
                    new TaskParamResource().setKey("compute-name").setValue(computeResource.getName() + "")));
            resource.setOrder(dagOrder.incrementAndGet());
            taskDag.add(resource);
        });


        StringBuffer inputArgument = new StringBuffer();

        Optional.ofNullable(exRes.getExperimentInputs()).ifPresent(exInps -> exInps.forEach(expInp -> {

            switch (expInp.getType()) {
                case ExperimentInputResource.Types.URI:

                    TaskResource resource = new TaskResource();
                    resource.setTaskType(TaskResource.TaskTypes.INGRESS_DATA_STAGING);
                    resource.setCreationTime(System.currentTimeMillis());
                    resource.setTaskDetail("Ingress data staging for input " + expInp.getName());
                    String localPath = processResource.getExperimentDataDir() + "/inputs/" + expInp.getId();
                    resource.setTaskParams(Arrays.asList(
                            new TaskParamResource().setKey("source").setValue(expInp.getValue()),
                            new TaskParamResource().setKey("target").setValue(localPath),
                            new TaskParamResource().setKey("compute-id").setValue(computeResource.getId() + ""),
                            new TaskParamResource().setKey("compute-name").setValue(computeResource.getName() + "")));
                    resource.setOrder(dagOrder.incrementAndGet());

                    inputArgument.append(" ");
                    if (expInp.getArguments() != null && !expInp.getArguments().isEmpty()) {
                        inputArgument.append(expInp.getArguments());
                        inputArgument.append(" ");
                    }
                    inputArgument.append(localPath);
                    taskDag.add(resource);
                    break;

                case ExperimentInputResource.Types.FLOAT:
                case ExperimentInputResource.Types.STRING:
                case ExperimentInputResource.Types.INTEGER:
                    inputArgument.append(" ");
                    if (expInp.getArguments() != null && !expInp.getArguments().isEmpty()) {
                        inputArgument.append(expInp.getArguments());
                        inputArgument.append(" ");
                    }
                    inputArgument.append(expInp.getValue());
                    break;
            }

        }));

        Optional.ofNullable(appDepRes.getExecutablePath()).ifPresent(exPath -> {
            TaskResource resource = new TaskResource();
            resource.setTaskType(TaskResource.TaskTypes.JOB_SUBMISSION);
            resource.setCreationTime(System.currentTimeMillis());
            resource.setTaskDetail("Job submission command for experiment " + exRes.getId());

            resource.setTaskParams(Arrays.asList(
                    new TaskParamResource().setKey("command").setValue(exPath),
                    new TaskParamResource().setKey("arguments").setValue(inputArgument.toString()),
                    new TaskParamResource().setKey("compute-id").setValue(computeResource.getId() + ""),
                    new TaskParamResource().setKey("compute-name").setValue(computeResource.getName() + "")));
            resource.setOrder(dagOrder.incrementAndGet());
            taskDag.add(resource);
        });

        Optional.ofNullable(exRes.getExperimentOutputs()).ifPresent(exOps -> exOps.forEach(expOut -> {
            if (expOut.getType() == ExperimentOutputResource.Types.URI) {
                TaskResource resource = new TaskResource();
                resource.setTaskType(TaskResource.TaskTypes.EGRESS_DATA_STAGING);
                resource.setCreationTime(System.currentTimeMillis());
                resource.setTaskDetail("Egress data staging for output " + expOut.getName());
                resource.setTaskParams(Arrays.asList(
                        new TaskParamResource().setKey("source").setValue(expOut.getValue()),
                        new TaskParamResource().setKey("target").setValue("/tmp/" + exRes.getId() + "/outputs/" + expOut.getId()),
                        new TaskParamResource().setKey("compute-id").setValue(computeResource.getId() + ""),
                        new TaskParamResource().setKey("compute-name").setValue(computeResource.getName() + "")));
                resource.setOrder(dagOrder.incrementAndGet());
            }

            if (expOut.getType() == ExperimentOutputResource.Types.STDOUT) {
                // TODO
            }
        }));

        Optional.ofNullable(appDepRes.getPostJobCommand()).ifPresent(postJob -> {
            TaskResource resource = new TaskResource();
            resource.setTaskType(TaskResource.TaskTypes.ENV_CLEANUP);
            resource.setCreationTime(System.currentTimeMillis());
            resource.setTaskDetail("Post-job command for experiment " + exRes.getId());
            resource.setTaskParams(Arrays.asList(
                    new TaskParamResource().setKey("command").setValue(postJob),
                    new TaskParamResource().setKey("compute-id").setValue(computeResource.getId() + ""),
                    new TaskParamResource().setKey("compute-name").setValue(computeResource.getName() + "")));
            resource.setOrder(dagOrder.incrementAndGet());
            taskDag.add(resource);
        });

        return taskDag;
    }
}
