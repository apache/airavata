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

import org.apache.airavata.orchestration.task.CompletingTask;
import org.apache.airavata.orchestration.task.JobVerificationTask;
import org.apache.airavata.orchestration.task.ParsingTriggeringTask;
import org.apache.airavata.task.AiravataTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlurmTaskFactory implements HelixTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmTaskFactory.class);

    private static final String ENV_SETUP_TASK_CLASS = "org.apache.airavata.compute.task.EnvSetupTask";
    private static final String INPUT_DATA_STAGING_TASK_CLASS = "org.apache.airavata.storage.task.InputDataStagingTask";
    private static final String OUTPUT_DATA_STAGING_TASK_CLASS =
            "org.apache.airavata.storage.task.OutputDataStagingTask";
    private static final String ARCHIVE_TASK_CLASS = "org.apache.airavata.storage.task.ArchiveTask";
    private static final String JOB_SUBMISSION_TASK_CLASS = "org.apache.airavata.compute.task.DefaultJobSubmissionTask";

    @Override
    public AiravataTask createEnvSetupTask(String processId) {
        LOGGER.info("Creating Slurm EnvSetupTask for process {}...", processId);
        return createTaskByReflection(ENV_SETUP_TASK_CLASS, "EnvSetupTask", "compute-service");
    }

    @Override
    public AiravataTask createInputDataStagingTask(String processId) {
        LOGGER.info("Creating Slurm InputDataStagingTask for process {}...", processId);
        return createTaskByReflection(INPUT_DATA_STAGING_TASK_CLASS, "InputDataStagingTask", "storage-service");
    }

    @Override
    public AiravataTask createJobSubmissionTask(String processId) {
        LOGGER.info("Creating Slurm DefaultJobSubmissionTask for process {}...", processId);
        return createTaskByReflection(JOB_SUBMISSION_TASK_CLASS, "DefaultJobSubmissionTask", "compute-service");
    }

    @Override
    public AiravataTask createOutputDataStagingTask(String processId) {
        LOGGER.info("Creating Slurm OutputDataStagingTask for process {}...", processId);
        return createTaskByReflection(OUTPUT_DATA_STAGING_TASK_CLASS, "OutputDataStagingTask", "storage-service");
    }

    @Override
    public AiravataTask createArchiveTask(String processId) {
        LOGGER.info("Creating Slurm ArchiveTask for process {}...", processId);
        return createTaskByReflection(ARCHIVE_TASK_CLASS, "ArchiveTask", "storage-service");
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

    private static AiravataTask createTaskByReflection(String className, String taskName, String moduleName) {
        try {
            return (AiravataTask)
                    Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + taskName + "; is " + moduleName + " on the classpath?", e);
        }
    }
}
