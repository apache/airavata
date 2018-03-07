package org.apache.airavata.helix.impl.participant;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.core.support.TaskHelperImpl;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GlobalParticipant extends HelixParticipant {

    private static final Logger logger = LogManager.getLogger(GlobalParticipant.class);

    private String[] taskClasses = {
        "org.apache.airavata.helix.impl.task.env.EnvSetupTask",
        "org.apache.airavata.helix.impl.task.staging.InputDataStagingTask",
        "org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask",
        "org.apache.airavata.helix.impl.task.completing.CompletingTask",
        "org.apache.airavata.helix.impl.task.submission.ForkJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.submission.LocalJobSubmissionTask"
    };

    public Map<String, TaskFactory> getTaskFactory() {
        Map<String, TaskFactory> taskRegistry = new HashMap<>();

        for (String taskClass : taskClasses) {
            TaskFactory taskFac = context -> {
                try {
                    return AbstractTask.class.cast(Class.forName(taskClass).newInstance())
                            .setCallbackContext(context)
                            .setTaskHelper(new TaskHelperImpl());
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to initialize the task", e);
                    return null;
                } catch (ClassNotFoundException e) {
                    logger.error("Task class can not be found in the class path", e);
                    return null;
                }
            };

            TaskDef taskDef;
            try {
                taskDef = Class.forName(taskClass).getAnnotation(TaskDef.class);
                taskRegistry.put(taskDef.name(), taskFac);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return taskRegistry;
    }

    @SuppressWarnings("WeakerAccess")
    public GlobalParticipant(Class taskClass, String taskTypeName) throws ApplicationSettingsException {
        super(taskClass, taskTypeName);
    }

    public static void main(String args[]) {
        logger.info("Starting global participant");

        GlobalParticipant participant;
        try {
            participant = new GlobalParticipant(null, null);
            Thread t = new Thread(participant);
            t.start();
        } catch (Exception e) {
            logger.error("Failed to start global participant", e);
        }

    }

}
