package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.env.EnvSetupTask;
import org.apache.airavata.helix.impl.task.staging.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask;
import org.apache.airavata.helix.workflow.WorkflowManager;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PreWorkflowManager {

    private static final Logger logger = LogManager.getLogger(PreWorkflowManager.class);

    private final Subscriber subscriber;

    public PreWorkflowManager() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqProcessExchangeName());
        this.subscriber = MessagingFactory.getSubscriber(new ProcessLaunchMessageHandler(), routingKeys, Type.PROCESS_LAUNCH);
    }

    private String createAndLaunchPreWorkflow(String processId, String gateway) throws Exception {

        ExperimentCatalog experimentCatalog = RegistryFactory.getExperimentCatalog(gateway);

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
                    airavataTask.setRetryCount(1);
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (jobSubmissionFound) {
                        //airavataTask = new OutputDataStagingTask();
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

        WorkflowManager workflowManager = new WorkflowManager(
                ServerSettings.getSetting("helix.cluster.name"),
                ServerSettings.getSetting("post.workflow.manager.name"),
                ServerSettings.getZookeeperConnection());
        String workflowName = workflowManager.launchWorkflow(processId + "-PRE-" + UUID.randomUUID().toString(),
                allTasks.stream().map(t -> (AiravataTask) t).collect(Collectors.toList()), true, false);
        return workflowName;
    }

    public static void main(String[] args) throws Exception {
        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
    }

    private class ProcessLaunchMessageHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            logger.info(" Message Received with message id " + messageContext.getMessageId() + " and with message type: " + messageContext.getType());

            if (messageContext.getType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessSubmitEvent event = new ProcessSubmitEvent();
                TBase messageEvent = messageContext.getEvent();

                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                } catch (TException e) {
                    logger.error("Failed to fetch process submit event", e);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                }

                String processId = event.getProcessId();
                String gateway = event.getGatewayId();

                logger.info("Received process launch message for process " + processId + " in gateway " + gateway);

                try {
                    logger.info("Launching the pre workflow for process " + processId + " in gateway " + gateway );
                    String workflowName = createAndLaunchPreWorkflow(processId, gateway);
                    logger.info("Completed launching the pre workflow " + workflowName + " for process " + processId + " in gateway " + gateway );
                    subscriber.sendAck(messageContext.getDeliveryTag());
                } catch (Exception e) {
                    logger.error("Failed to launch the pre workflow for process " + processId + " in gateway " + gateway, e);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                }
            } else {
                logger.warn("Unknown message type");
                subscriber.sendAck(messageContext.getDeliveryTag());
            }
        }
    }
}
