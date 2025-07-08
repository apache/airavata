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

import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.helix.impl.task.env.EnvSetupTask;
import org.apache.airavata.helix.impl.task.parsing.ParsingTriggeringTask;
import org.apache.airavata.helix.impl.task.staging.ArchiveTask;
import org.apache.airavata.helix.impl.task.staging.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.staging.JobVerificationTask;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlurmTaskFactory implements HelixTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmTaskFactory.class);

    @Override
    public AiravataTask createEnvSetupTask(String processId) {
        LOGGER.info("Creating Slurm EnvSetupTask for process {}...", processId);
        return new EnvSetupTask();
    }

    @Override
    public AiravataTask createInputDataStagingTask(String processId) {
        LOGGER.info("Creating Slurm InputDataStagingTask for process {}...", processId);
        return new InputDataStagingTask();
    }

    @Override
    public AiravataTask createJobSubmissionTask(String processId) {
        LOGGER.info("Creating Slurm DefaultJobSubmissionTask for process {}...", processId);
        return new DefaultJobSubmissionTask();
    }

    @Override
    public AiravataTask createOutputDataStagingTask(String processId) {
        LOGGER.info("Creating Slurm OutputDataStagingTask for process {}...", processId);
        return new OutputDataStagingTask();
    }

    @Override
    public AiravataTask createArchiveTask(String processId) {
        LOGGER.info("Creating Slurm ArchiveTask for process {}...", processId);
        return new ArchiveTask();
    }

    @Override
    public AiravataTask createJobVerificationTask(String processId) {
        LOGGER.info("Creating Slurm JobVerificationTask for process {}...", processId);
        return new JobVerificationTask();
    }

    @Override
    public AiravataTask createCompletingTask(String processId) {
        LOGGER.info("Creating Slurm CompletingTask for process {}...", processId);
        return new CompletingTask();
    }

    @Override
    public AiravataTask createParsingTriggeringTask(String processId) {
        LOGGER.info("Creating Slurm ParsingTriggeringTask for process {}...", processId);
        return new ParsingTriggeringTask();
    }
}
