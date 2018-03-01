package org.apache.airavata.helix.impl.participant;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.core.support.TaskHelperImpl;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GlobalParticipant extends HelixParticipant {

    private String[] taskClasses = {
        "org.apache.airavata.helix.impl.task.EnvSetupTask",
        "org.apache.airavata.helix.impl.task.InputDataStagingTask",
        "org.apache.airavata.helix.impl.task.OutputDataStagingTask",
        "org.apache.airavata.helix.impl.task.submission.task.ForkJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.submission.task.DefaultJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.submission.task.LocalJobSubmissionTask"
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

    public GlobalParticipant(String propertyFile, Class taskClass, String taskTypeName) throws IOException {
        super(propertyFile, taskClass, taskTypeName);
    }

    public static void main(String args[]) throws IOException {
        GlobalParticipant participant = new GlobalParticipant("application.properties", null, null);
        Thread t = new Thread(participant);
        t.start();
    }

}
