package org.apache.airavata.ide.integration;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;

import java.util.ArrayList;

public class JobEngineStarter {

    public static void main(String[] args) throws Exception {

        System.out.println("Starting Helix Controller .......");
        HelixController controller = new HelixController();
        controller.startServer();
        Thread.sleep(5000);

        System.out.println("Starting Helix Participant .......");
        ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();
        for (String taskClassName : GlobalParticipant.TASK_CLASS_NAMES) {
            taskClasses.add(Class.forName(taskClassName).asSubclass(AbstractTask.class));
        }
        GlobalParticipant participant = new GlobalParticipant(taskClasses, null);
        participant.startServer();
        Thread.sleep(5000);

        System.out.println("Starting Pre Workflow Manager .......");
        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.startServer();
        Thread.sleep(5000);

        System.out.println("Starting Post Workflow Manager .......");
        PostWorkflowManager postWorkflowManager = new PostWorkflowManager();
        postWorkflowManager.startServer();
    }
}
