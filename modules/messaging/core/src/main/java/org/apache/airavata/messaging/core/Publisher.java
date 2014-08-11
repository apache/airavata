package org.apache.airavata.messaging.core;

import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;

public interface Publisher {
    public void publish(ExperimentStatusChangeEvent event);
    public void publish(WorkflowNodeStatusChangeEvent event);
    public void publish(TaskStatusChangeEvent event);
    public void publish(JobStatusChangeEvent event);
}
