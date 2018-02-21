package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.impl.task.EnvSetupTask;
import org.apache.airavata.helix.impl.task.submission.task.DefaultJobSubmissionTask;
import org.apache.airavata.helix.workflow.WorkflowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleWorkflow {

    public static void main(String[] args) throws Exception {

        EnvSetupTask envSetupTask = new EnvSetupTask();
        envSetupTask.setWorkingDirectory("/tmp/a");

        DefaultJobSubmissionTask defaultJobSubmissionTask = new DefaultJobSubmissionTask();
        defaultJobSubmissionTask.setGatewayId("default");
        defaultJobSubmissionTask.setExperimentId("Clone_of_Mothur-Test1_0c9f627e-2c32-403e-a28a-2a8b10c21c1a");
        defaultJobSubmissionTask.setProcessId("PROCESS_438a87cc-2dec-4edc-bfeb-31128df91bb6");
        defaultJobSubmissionTask.setTaskId(UUID.randomUUID().toString());

        List<AbstractTask> tasks = new ArrayList<>();
        tasks.add(defaultJobSubmissionTask);

        WorkflowManager workflowManager = new WorkflowManager("AiravataDemoCluster", "wm-22", "localhost:2199");
        workflowManager.launchWorkflow(UUID.randomUUID().toString(), tasks, true);
    }
}
