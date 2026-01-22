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
package org.apache.airavata.activities.shared;

import java.util.UUID;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.base.AbstractTask;
import org.apache.airavata.task.base.AiravataTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes AiravataTask instances from Dapr workflow activities.
 */
public final class TaskExecutorHelper {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorHelper.class);

    private TaskExecutorHelper() {}

    /**
     * Execute an AiravataTask with the given input.
     */
    public static String executeTask(AbstractTask task, BaseActivityInput input) throws Exception {
        if (task instanceof AiravataTask airavataTask) {
            airavataTask.setProcessId(input.processId());
            airavataTask.setExperimentId(input.experimentId());
            airavataTask.setGatewayId(input.gatewayId());
            airavataTask.setSkipAllStatusPublish(input.skipAllStatusPublish());
            airavataTask.setSkipProcessStatusPublish(input.skipProcessStatusPublish());
            airavataTask.setSkipExperimentStatusPublish(input.skipExperimentStatusPublish());
            airavataTask.setForceRunTask(input.forceRunTask());
        }

        if (task.getTaskId() == null) {
            task.setTaskId(UUID.randomUUID().toString());
        }

        task.init("dapr-workflow", "dapr-job", task.getTaskId());

        if (task instanceof AiravataTask airavataTask) {
            var loadContextMethod = AiravataTask.class.getDeclaredMethod("loadContext");
            loadContextMethod.setAccessible(true);
            loadContextMethod.invoke(airavataTask);
        }

        var taskHelper = WorkflowRuntimeHolder.getBean(TaskHelper.class);
        task.setTaskHelper(taskHelper);

        TaskResult result;
        if (task instanceof AiravataTask airavataTask) {
            var getTaskContextMethod = AiravataTask.class.getDeclaredMethod("getTaskContext");
            getTaskContextMethod.setAccessible(true);
            var taskContext = (org.apache.airavata.task.base.TaskContext) getTaskContextMethod.invoke(airavataTask);
            result = airavataTask.onRun(taskHelper, taskContext);
        } else {
            result = task.onRun(taskHelper);
        }

        if (result.getStatus() == TaskResult.Status.COMPLETED) {
            return result.getInfo() != null ? result.getInfo() : "Completed";
        } else {
            throw new RuntimeException("Task failed: " + (result.getInfo() != null ? result.getInfo() : "Unknown"));
        }
    }
}
