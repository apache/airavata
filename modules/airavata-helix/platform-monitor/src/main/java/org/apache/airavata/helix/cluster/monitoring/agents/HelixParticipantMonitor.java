package org.apache.airavata.helix.cluster.monitoring.agents;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.cluster.monitoring.ErrorNotifier;
import org.apache.airavata.helix.cluster.monitoring.PlatformMonitor;
import org.apache.airavata.helix.cluster.monitoring.PlatformMonitorError;
import org.apache.airavata.helix.impl.task.mock.MockTask;
import org.apache.airavata.helix.workflow.WorkflowOperator;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.task.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.UUID;

public class HelixParticipantMonitor implements PlatformMonitor {

    private final static Logger logger = LoggerFactory.getLogger(HelixParticipantMonitor.class);

    private String helixClusterName = ServerSettings.getSetting("helix.cluster.name");
    private String instanceName = ServerSettings.getSetting("helix.participant.name");
    private String zkConnectionString = ServerSettings.getZookeeperConnection();
    private WorkflowOperator operator;

    public HelixParticipantMonitor() throws Exception {
        operator = new WorkflowOperator(helixClusterName, "mock-wf-operator", zkConnectionString);
    }

    public void monitor(ErrorNotifier notifier) {

        logger.info("Monitoring Participant started");

        PlatformMonitorError monitorError = checkConnectivity();
        if (monitorError != null) notifier.sendNotification(monitorError);
        monitorError = checkMockWorkflow();
        if (monitorError != null) notifier.sendNotification(monitorError);

        logger.info("Monitoring Participant finished");

    }

    private PlatformMonitorError checkConnectivity() {
        ZkClient zkclient = null;
        try {
            zkclient = new ZkClient(zkConnectionString, ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
            ZKHelixAdmin admin = new ZKHelixAdmin(zkclient);

            InstanceConfig instanceConfig = admin.getInstanceConfig(helixClusterName, instanceName);

            String result = new String(instanceConfig.serialize(new ZNRecordSerializer()));

            int startPoint = result.indexOf("HELIX_ENABLED");
            int endPoint = result.indexOf("\n", startPoint);
            String enabledStr = result.substring(startPoint, endPoint);
            if (enabledStr.contains("false")) {
                PlatformMonitorError monitorError = new PlatformMonitorError();
                monitorError.setReason("Helix participant " + instanceName + " is not active");
                monitorError.setCategory("Participant");
                monitorError.setErrorCode("P001");
                return monitorError;
            }
        } catch (Exception e) {
            PlatformMonitorError monitorError = new PlatformMonitorError();
            monitorError.setError(e);
            monitorError.setReason("Failed to fetch Helix participant " + instanceName + " information");
            monitorError.setCategory("Participant");
            monitorError.setErrorCode("P002");
            return monitorError;
        } finally {
            if (zkclient != null) {
                zkclient.close();
            }
        }
        return null;
    }

    private PlatformMonitorError checkMockWorkflow() {
        MockTask mockTask  = new MockTask();
        mockTask.setTaskId("Mock-" + UUID.randomUUID().toString());
        try {
            String workflow = operator.launchWorkflow(UUID.randomUUID().toString(), Collections.singletonList(mockTask), true, false);
            /*TaskState state = operator.pollForWorkflowCompletion(workflow, Long.parseLong(ServerSettings.getSetting("platform_mock_workflow_timeout_ms")));
            if (state != TaskState.COMPLETED) {
                PlatformMonitorError monitorError = new PlatformMonitorError();
                monitorError.setReason("Mock workflow failed to execute with status " + state.name() + ". " +
                        "Check whether Helix cluster is working properly");
                monitorError.setCategory("Participant");
                monitorError.setErrorCode("P003");
                return monitorError;
            }*/
        } catch (Exception e) {
            PlatformMonitorError monitorError = new PlatformMonitorError();
            monitorError.setError(e);
            monitorError.setReason("Failed to launch mock workflow on helix cluster  " + helixClusterName + ". " +
                    "Check whether Helix cluster is working properly including the availability of Controller and Participant");
            monitorError.setCategory("Participant");
            monitorError.setErrorCode("P004");
            return monitorError;
        }
        return null;
    }
}