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

import org.apache.airavata.task.AiravataTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSTaskFactory implements HelixTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSTaskFactory.class);

    private static final String CREATE_EC2_INSTANCE_TASK_CLASS =
            "org.apache.airavata.compute.task.aws.CreateEC2InstanceTask";
    private static final String NO_OPERATION_TASK_CLASS = "org.apache.airavata.compute.task.aws.NoOperationTask";
    private static final String AWS_JOB_SUBMISSION_TASK_CLASS =
            "org.apache.airavata.compute.task.aws.AWSJobSubmissionTask";
    private static final String AWS_COMPLETING_TASK_CLASS = "org.apache.airavata.compute.task.aws.AWSCompletingTask";

    @Override
    public AiravataTask createEnvSetupTask(String processId) {
        LOGGER.info("Creating AWS CreateEc2InstanceTask for process {}...", processId);
        return createTaskByReflection(CREATE_EC2_INSTANCE_TASK_CLASS, "CreateEC2InstanceTask", "compute-service");
    }

    @Override
    public AiravataTask createInputDataStagingTask(String processId) {
        return createTaskByReflection(NO_OPERATION_TASK_CLASS, "NoOperationTask", "compute-service");
    }

    @Override
    public AiravataTask createJobSubmissionTask(String processId) {
        return createTaskByReflection(AWS_JOB_SUBMISSION_TASK_CLASS, "AWSJobSubmissionTask", "compute-service");
    }

    @Override
    public AiravataTask createOutputDataStagingTask(String processId) {
        return createTaskByReflection(NO_OPERATION_TASK_CLASS, "NoOperationTask", "compute-service");
    }

    @Override
    public AiravataTask createArchiveTask(String processId) {
        return createTaskByReflection(NO_OPERATION_TASK_CLASS, "NoOperationTask", "compute-service");
    }

    @Override
    public AiravataTask createJobVerificationTask(String processId) {
        return createTaskByReflection(NO_OPERATION_TASK_CLASS, "NoOperationTask", "compute-service");
    }

    @Override
    public AiravataTask createCompletingTask(String processId) {
        return createTaskByReflection(AWS_COMPLETING_TASK_CLASS, "AWSCompletingTask", "compute-service");
    }

    @Override
    public AiravataTask createParsingTriggeringTask(String processId) {
        return createTaskByReflection(NO_OPERATION_TASK_CLASS, "NoOperationTask", "compute-service");
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
