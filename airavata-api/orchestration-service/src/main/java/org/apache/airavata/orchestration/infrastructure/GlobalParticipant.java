/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.server.IServer;
import org.apache.airavata.task.AbstractTask;
import org.apache.airavata.task.HelixParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalParticipant extends HelixParticipant<AbstractTask> implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(GlobalParticipant.class);

    private IServer.ServerStatus status = IServer.ServerStatus.STOPPED;

    public static final String[] TASK_CLASS_NAMES = {
        "org.apache.airavata.compute.task.EnvSetupTask",
        "org.apache.airavata.storage.task.InputDataStagingTask",
        "org.apache.airavata.storage.task.OutputDataStagingTask",
        "org.apache.airavata.orchestration.task.JobVerificationTask",
        "org.apache.airavata.orchestration.task.CompletingTask",
        "org.apache.airavata.compute.task.ForkJobSubmissionTask",
        "org.apache.airavata.compute.task.DefaultJobSubmissionTask",
        "org.apache.airavata.compute.task.LocalJobSubmissionTask",
        "org.apache.airavata.storage.task.ArchiveTask",
        "org.apache.airavata.orchestration.task.WorkflowCancellationTask",
        "org.apache.airavata.orchestration.task.RemoteJobCancellationTask",
        "org.apache.airavata.orchestration.task.CancelCompletingTask",
        "org.apache.airavata.orchestration.task.DataParsingTask",
        "org.apache.airavata.orchestration.task.ParsingTriggeringTask",
        "org.apache.airavata.orchestration.task.MockTask",
        "org.apache.airavata.compute.task.aws.CreateEC2InstanceTask",
        "org.apache.airavata.compute.task.aws.NoOperationTask",
        "org.apache.airavata.compute.task.aws.AWSJobSubmissionTask",
        "org.apache.airavata.compute.task.aws.AWSCompletingTask",
    };

    @SuppressWarnings("WeakerAccess")
    public GlobalParticipant(List<Class<? extends AbstractTask>> taskClasses, String taskTypeName)
            throws ApplicationSettingsException {
        super(taskClasses, taskTypeName);
    }

    @Override
    public void run() {
        status = ServerStatus.STARTED;
        super.run();
    }

    @Override
    public String getName() {
        return "helix_participant";
    }

    @Override
    public void stop() throws Exception {
        status = ServerStatus.STOPPING;
        status = ServerStatus.STOPPED;
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    public static void main(String args[]) {
        logger.info("Starting global participant");

        try {
            ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();

            for (String taskClassName : TASK_CLASS_NAMES) {
                logger.debug("Adding task class: " + taskClassName + " to the global participant");
                taskClasses.add(Class.forName(taskClassName).asSubclass(AbstractTask.class));
            }

            GlobalParticipant participant = new GlobalParticipant(taskClasses, null);
            participant.run();

        } catch (Exception e) {
            logger.error("Failed to start global participant", e);
        }
    }
}
