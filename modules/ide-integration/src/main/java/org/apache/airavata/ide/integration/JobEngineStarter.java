package org.apache.airavata.ide.integration;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;

import java.util.ArrayList;

public class JobEngineStarter {

    public static void main(String args[]) throws Exception {

        ZkClient zkClient = new ZkClient(ServerSettings.getZookeeperConnection(), ZkClient.DEFAULT_SESSION_TIMEOUT,
                    ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
        ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

        zkHelixAdmin.addCluster(ServerSettings.getSetting("helix.cluster.name"), true);

        System.out.println("Starting Helix Controller .......");
        // Starting helix controller
        HelixController controller = new HelixController();
        controller.startServer();

        ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();

        for (String taskClassName : GlobalParticipant.TASK_CLASS_NAMES) {
            taskClasses.add(Class.forName(taskClassName).asSubclass(AbstractTask.class));
        }

        System.out.println("Starting Helix Participant .......");

        // Starting helix participant
        GlobalParticipant participant = new GlobalParticipant(taskClasses, null);
        participant.startServer();

        System.out.println("Starting Pre Workflow Manager .......");

        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.startServer();

        System.out.println("Starting Post Workflow Manager .......");

        PostWorkflowManager postWorkflowManager = new PostWorkflowManager();
        postWorkflowManager.startServer();
    }
}
