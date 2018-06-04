/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.participant;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.core.support.TaskHelperImpl;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GlobalParticipant extends HelixParticipant {

    private final static Logger logger = LoggerFactory.getLogger(GlobalParticipant.class);

    private String[] taskClasses = {
            "org.apache.airavata.helix.impl.task.env.EnvSetupTask",
            "org.apache.airavata.helix.impl.task.staging.InputDataStagingTask",
            "org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask",
            "org.apache.airavata.helix.impl.task.completing.CompletingTask",
            "org.apache.airavata.helix.impl.task.submission.ForkJobSubmissionTask",
            "org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask",
            "org.apache.airavata.helix.impl.task.submission.LocalJobSubmissionTask",
            "org.apache.airavata.helix.impl.task.staging.ArchiveTask",
            "org.apache.airavata.helix.impl.task.cancel.WorkflowCancellationTask",
            "org.apache.airavata.helix.impl.task.cancel.RemoteJobCancellationTask",
            "org.apache.airavata.helix.impl.task.cancel.CancelCompletingTask"
    };

    public Map<String, TaskFactory> getTaskFactory() {
        Map<String, TaskFactory> taskRegistry = new HashMap<>();

        for (String taskClass : taskClasses) {
            TaskFactory taskFac = context -> {
                try {
                    return AbstractTask.class.cast(Class.forName(taskClass).newInstance())
                            .setParticipant(GlobalParticipant.this)
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
