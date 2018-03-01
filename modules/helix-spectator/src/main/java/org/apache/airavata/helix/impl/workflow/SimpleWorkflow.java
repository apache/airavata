package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.EnvSetupTask;
import org.apache.airavata.helix.impl.task.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.OutputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.task.DefaultJobSubmissionTask;
import org.apache.airavata.helix.workflow.WorkflowManager;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SimpleWorkflow {

    public static void main(String[] args) throws Exception {

        String processId = "PROCESS_438a87cc-2dec-4edc-bfeb-31128df91bb6";
        AppCatalog appCatalog = RegistryFactory.getAppCatalog();
        ExperimentCatalog experimentCatalog = RegistryFactory.getDefaultExpCatalog();

        ProcessModel processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
        ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, processModel.getExperimentId());
        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasks();

        String[] taskIds = taskDag.split(",");
        final List<AiravataTask> allTasks = new ArrayList<>();

        boolean jobSubmissionFound = false;

        for (String taskId : taskIds) {
            Optional<TaskModel> model = taskList.stream().filter(taskModel -> taskModel.getTaskId().equals(taskId)).findFirst();

            if (model.isPresent()) {
                TaskModel taskModel = model.get();
                AiravataTask airavataTask = null;
                if (taskModel.getTaskType() == TaskTypes.ENV_SETUP) {
                    airavataTask = new EnvSetupTask();
                } else if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    airavataTask = new DefaultJobSubmissionTask();
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (jobSubmissionFound) {
                        airavataTask = new OutputDataStagingTask();
                    } else {
                        airavataTask = new InputDataStagingTask();
                    }
                }

                if (airavataTask != null) {
                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                    airavataTask.setProcessId(processModel.getProcessId());
                    airavataTask.setTaskId(taskModel.getTaskId());
                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() -1).setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
                    }
                    allTasks.add(airavataTask);
                }
            }
        }

/*        DefaultJobSubmissionTask defaultJobSubmissionTask = new DefaultJobSubmissionTask();
        defaultJobSubmissionTask.setGatewayId("default");
        defaultJobSubmissionTask.setExperimentId("Clone_of_Mothur-Test1_0c9f627e-2c32-403e-a28a-2a8b10c21c1a");
        defaultJobSubmissionTask.setProcessId("PROCESS_438a87cc-2dec-4edc-bfeb-31128df91bb6");
        defaultJobSubmissionTask.setTaskId("TASK_612844a4-aedb-41a5-824f-9b20c76867f7");

        List<AbstractTask> tasks = new ArrayList<>();
        tasks.add(defaultJobSubmissionTask);
*/
        WorkflowManager workflowManager = new WorkflowManager("AiravataDemoCluster", "wm-22", "localhost:2199");
        workflowManager.launchWorkflow(UUID.randomUUID().toString(), allTasks.stream().map(t -> (AiravataTask)t).collect(Collectors.toList()), true);
    }
}
