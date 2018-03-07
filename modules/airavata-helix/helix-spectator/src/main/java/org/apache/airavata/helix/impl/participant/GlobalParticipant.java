package org.apache.airavata.helix.impl.participant;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.core.support.TaskHelperImpl;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
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
        Map<String, TaskFactory> taskRegistry = new HashMap<String, TaskFactory>();

        for (String taskClass : taskClasses) {
            TaskFactory taskFac = new TaskFactory() {
                public Task createNewTask(TaskCallbackContext context) {
                    try {
                        return AbstractTask.class.cast(Class.forName(taskClass).newInstance())
                                .setCallbackContext(context)
                                .setTaskHelper(new TaskHelperImpl());
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };

            TaskDef taskDef = null;
            try {
                taskDef = Class.forName(taskClass).getAnnotation(TaskDef.class);
                taskRegistry.put(taskDef.name(), taskFac);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return taskRegistry;
    }

    public GlobalParticipant(String propertyFile, Class taskClass, String taskTypeName, boolean readPropertyFromFile) throws IOException {
        super(propertyFile, taskClass, taskTypeName, readPropertyFromFile);
    }

    public static void main(String args[]) throws IOException {

        String confDir = null;
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("--confDir=")) {
                    confDir = arg.substring("--confDir=".length());
                }
            }
        }

        String propertiesFile = "application.properties";
        boolean readPropertyFromFile = false;

        if (confDir != null && !confDir.isEmpty()) {
            propertiesFile = confDir.endsWith(File.separator)? confDir + propertiesFile : confDir + File.separator + propertiesFile;
            readPropertyFromFile = true;
        }

        logger.info("Using configuration file " + propertiesFile);

        GlobalParticipant participant = new GlobalParticipant(propertiesFile, null, null, readPropertyFromFile);
        Thread t = new Thread(participant);
        t.start();
    }

}
