package org.apache.airavata.k8s.gfac.core;

import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.gfac.messaging.KafkaSender;

import java.util.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ProcessLifeCycleManager {

    private List<TaskResource> taskDag;
    private Map<Long, Integer> taskPoint;
    private KafkaSender kafkaSender;

    public ProcessLifeCycleManager(List<TaskResource> tasks, KafkaSender kafkaSender) {
        this.taskDag = tasks;
        this.kafkaSender = kafkaSender;
    }

    public void init() {
        taskDag.sort(Comparator.comparing(TaskResource::getOrder));
        taskPoint = new HashMap<>();
        for (int i = 0; i < taskDag.size(); i++) {
            taskPoint.put(taskDag.get(i).getId(), i);
        }
    }

    public synchronized void onTaskStateChanged(long taskId, int state) {
        switch (state) {
            case TaskStatusResource.State.COMPLETED:
                Optional.ofNullable(this.taskPoint.get(taskId)).ifPresent(point -> {
                    if (point + 1 < taskDag.size()) {
                        TaskResource resource = taskDag.get(point + 1);
                        System.out.println("Submitting task " + taskId + " to queue");
                        submitTaskToQueue(resource);
                    }
                });
                break;
        }
    }

    public void submitTaskToQueue(TaskResource taskResource) {

        switch (taskResource.getTaskType()) {
            case TaskResource.TaskTypes.EGRESS_DATA_STAGING :
                this.kafkaSender.send("airavata-task-egress-staging", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.INGRESS_DATA_STAGING :
                this.kafkaSender.send("airavata-task-ingress-staging", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.ENV_SETUP :
                this.kafkaSender.send("airavata-task-env-setup", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.ENV_CLEANUP :
                this.kafkaSender.send("airavata-task-env-cleanup", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.JOB_SUBMISSION :
                this.kafkaSender.send("airavata-task-job-submission", taskResource.getId() + "");
                break;
        }
    }

}
