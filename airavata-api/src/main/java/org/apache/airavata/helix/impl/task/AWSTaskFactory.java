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
package org.apache.airavata.helix.impl.task;

import org.apache.airavata.helix.impl.task.aws.AWSCompletingTask;
import org.apache.airavata.helix.impl.task.aws.AWSJobSubmissionTask;
import org.apache.airavata.helix.impl.task.aws.CreateEC2InstanceTask;
import org.apache.airavata.helix.impl.task.aws.NoOperationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSTaskFactory implements HelixTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSTaskFactory.class);

    @Override
    public AiravataTask createEnvSetupTask(String processId) {
        LOGGER.info("Creating AWS CreateEc2InstanceTask for process {}...", processId);
        return new CreateEC2InstanceTask();
    }

    @Override
    public AiravataTask createInputDataStagingTask(String processId) {
        return new NoOperationTask();
    }

    @Override
    public AiravataTask createJobSubmissionTask(String processId) {
        return new AWSJobSubmissionTask();
    }

    @Override
    public AiravataTask createOutputDataStagingTask(String processId) {
        return new NoOperationTask();
    }

    @Override
    public AiravataTask createArchiveTask(String processId) {
        return new NoOperationTask();
    }

    @Override
    public AiravataTask createJobVerificationTask(String processId) {
        return new NoOperationTask();
    }

    @Override
    public AiravataTask createCompletingTask(String processId) {
        return new AWSCompletingTask();
    }

    @Override
    public AiravataTask createParsingTriggeringTask(String processId) {
        return new NoOperationTask();
    }
}
