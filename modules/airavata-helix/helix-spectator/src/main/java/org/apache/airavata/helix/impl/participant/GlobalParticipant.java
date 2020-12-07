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
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.patform.monitoring.MonitoringServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GlobalParticipant extends HelixParticipant<AbstractTask> {

    private final static Logger logger = LoggerFactory.getLogger(GlobalParticipant.class);

    public final static String[] TASK_CLASS_NAMES = {
            "org.apache.airavata.helix.impl.task.env.EnvSetupTask",
            "org.apache.airavata.helix.impl.task.staging.InputDataStagingTask",
            "org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask",
            "org.apache.airavata.helix.impl.task.staging.JobVerificationTask",
            "org.apache.airavata.helix.impl.task.completing.CompletingTask",
            "org.apache.airavata.helix.impl.task.submission.ForkJobSubmissionTask",
            "org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask",
            "org.apache.airavata.helix.impl.task.submission.LocalJobSubmissionTask",
            "org.apache.airavata.helix.impl.task.staging.ArchiveTask",
            "org.apache.airavata.helix.impl.task.cancel.WorkflowCancellationTask",
            "org.apache.airavata.helix.impl.task.cancel.RemoteJobCancellationTask",
            "org.apache.airavata.helix.impl.task.cancel.CancelCompletingTask",
            "org.apache.airavata.helix.impl.task.parsing.DataParsingTask",
            "org.apache.airavata.helix.impl.task.parsing.ParsingTriggeringTask",
            "org.apache.airavata.helix.impl.task.mock.MockTask"
    };

    @SuppressWarnings("WeakerAccess")
    public GlobalParticipant(List<Class<? extends AbstractTask>> taskClasses, String taskTypeName) throws ApplicationSettingsException {
        super(taskClasses, taskTypeName);
    }

    public void startServer() {
        Thread t = new Thread(this);
        t.start();
    }

    public void stopServer() {

    }

    public static void main(String args[]) {
        logger.info("Starting global participant");

        try {
            ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();

            for (String taskClassName : TASK_CLASS_NAMES) {
                logger.debug("Adding task class: " + taskClassName + " to the global participant");
                taskClasses.add(Class.forName(taskClassName).asSubclass(AbstractTask.class));
            }

            if (ServerSettings.getBooleanSetting("participant.monitoring.enabled")) {
                MonitoringServer monitoringServer = new MonitoringServer(
                        ServerSettings.getSetting("participant.monitoring.host"),
                        ServerSettings.getIntSetting("participant.monitoring.port"));
                monitoringServer.start();

                Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
            }

            GlobalParticipant participant = new GlobalParticipant(taskClasses, null);
            participant.startServer();

        } catch (Exception e) {
            logger.error("Failed to start global participant", e);
        }
    }
}
