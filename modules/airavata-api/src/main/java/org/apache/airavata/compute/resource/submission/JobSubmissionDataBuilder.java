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
package org.apache.airavata.compute.resource.submission;

import org.apache.airavata.execution.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Builder that creates a {@link JobSubmissionData} from a {@link TaskContext}.
 * The resulting data object is passed into Groovy templates to generate job submission scripts.
 */
@Component
public class JobSubmissionDataBuilder {

    private static final Logger logger = LoggerFactory.getLogger(JobSubmissionDataBuilder.class);

    /**
     * Build a {@link JobSubmissionData} populated from the given {@link TaskContext}.
     *
     * @param taskContext the task execution context
     * @return populated JobSubmissionData
     * @throws Exception if required values cannot be resolved from the context
     */
    public JobSubmissionData build(TaskContext taskContext) throws Exception {
        var mapData = new JobSubmissionData();

        mapData.setProcessId(taskContext.getProcessId());
        mapData.setTaskId(taskContext.getTaskId());
        mapData.setGatewayId(taskContext.getGatewayId());

        // Working directory
        try {
            mapData.setWorkingDirectory(taskContext.getWorkingDir());
        } catch (Exception e) {
            logger.warn("Could not resolve working directory from task context", e);
        }

        // Stdout / stderr
        try {
            mapData.setStdoutFile(taskContext.getStdoutLocation());
        } catch (Exception e) {
            logger.warn("Could not resolve stdout location", e);
        }
        try {
            mapData.setStderrFile(taskContext.getStderrLocation());
        } catch (Exception e) {
            logger.warn("Could not resolve stderr location", e);
        }

        // Login username
        mapData.setUserName(taskContext.getComputeResourceLoginUserName());

        // Scheduling parameters from resource schedule map
        mapData.setQueueName(taskContext.getQueueName());
        mapData.setAccountString(taskContext.getAllocationProjectNumber());
        mapData.setReservationId(taskContext.getReservation());
        mapData.setQualityOfService(taskContext.getQualityOfService());

        // Scheduling integers from process model resource schedule map
        var processModel = taskContext.getProcessModel();
        if (processModel != null && processModel.getResourceSchedule() != null) {
            var schedule = processModel.getResourceSchedule();
            mapData.setNodeCount(toInt(schedule.get("nodeCount")));
            mapData.setCpuCount(toInt(schedule.get("totalCPUCount")));
            mapData.setWallTimeLimit(toInt(schedule.get("wallTimeLimit")));
            Object mem = schedule.get("totalPhysicalMemory");
            if (mem != null) {
                mapData.setTotalPhysicalMemory(mem.toString());
            }
        }

        // Application info
        var appModel = taskContext.getApplication();
        if (appModel != null) {
            mapData.setJobName("Airavata_" + appModel.getName() + "_" + taskContext.getProcessId());
        } else {
            mapData.setJobName("Airavata_" + taskContext.getProcessId());
        }

        logger.debug(
                "Built JobSubmissionData for process {} with working directory {}",
                taskContext.getProcessId(),
                mapData.getWorkingDirectory());
        return mapData;
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
