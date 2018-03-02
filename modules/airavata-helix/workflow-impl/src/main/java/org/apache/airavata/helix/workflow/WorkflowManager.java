package org.apache.airavata.helix.workflow;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.core.util.*;
import org.apache.airavata.helix.core.util.TaskUtil;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class WorkflowManager {

    private static final String WORKFLOW_PREFIX = "Workflow_of_process_";
    private TaskDriver taskDriver;

    public WorkflowManager(String helixClusterName, String instanceName, String zkConnectionString) throws Exception {

        HelixManager helixManager = HelixManagerFactory.getZKHelixManager(helixClusterName, instanceName,
                InstanceType.SPECTATOR, zkConnectionString);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        helixManager.disconnect();
                    }
                }
        );

        taskDriver = new TaskDriver(helixManager);
    }

    public void launchWorkflow(String processId, List<AbstractTask> tasks, boolean globalParticipant) throws Exception {

        Workflow.Builder workflowBuilder = new Workflow.Builder(WORKFLOW_PREFIX + processId).setExpiry(0);

        for (int i = 0; i < tasks.size(); i++) {
            AbstractTask data = tasks.get(i);
            String taskType = data.getClass().getAnnotation(TaskDef.class).name();
            TaskConfig.Builder taskBuilder = new TaskConfig.Builder().setTaskId("Task_" + data.getTaskId())
                    .setCommand(taskType);
            Map<String, String> paramMap = org.apache.airavata.helix.core.util.TaskUtil.serializeTaskData(data);
            paramMap.forEach(taskBuilder::addConfig);

            List<TaskConfig> taskBuilds = new ArrayList<>();
            taskBuilds.add(taskBuilder.build());

            JobConfig.Builder job = new JobConfig.Builder()
                    .addTaskConfigs(taskBuilds)
                    .setFailureThreshold(0)
                    .setMaxAttemptsPerTask(data.getRetryCount());

            if (!globalParticipant) {
                job.setInstanceGroupTag(taskType);
            }

            workflowBuilder.addJob((data.getTaskId()), job);

            List<OutPort> outPorts = TaskUtil.getOutPortsOfTask(data);
            outPorts.forEach(outPort -> {
                if (outPort != null) {
                    workflowBuilder.addParentChildDependency(data.getTaskId(), outPort.getNextJobId());
                }
            });
        }

        WorkflowConfig.Builder config = new WorkflowConfig.Builder().setFailureThreshold(0);
        workflowBuilder.setWorkflowConfig(config.build());
        Workflow workflow = workflowBuilder.build();

        taskDriver.start(workflow);

        //TODO : Do we need to monitor workflow status? If so how do we do it in a scalable manner? For example,
        // if the hfac that monitors a particular workflow, got killed due to some reason, who is taking the responsibility

        TaskState taskState = taskDriver.pollForWorkflowState(workflow.getName(),
                TaskState.COMPLETED, TaskState.FAILED, TaskState.STOPPED, TaskState.ABORTED);
        System.out.println("Workflow finished with state " + taskState.name());

    }
}