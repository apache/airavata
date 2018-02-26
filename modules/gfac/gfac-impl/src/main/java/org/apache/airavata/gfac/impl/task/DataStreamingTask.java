/**
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
package org.apache.airavata.gfac.impl.task;

import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.task.utils.StreamData;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Timer;

public class DataStreamingTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(DataStreamingTask.class);
    private String userName;
    private String hostName;
    private String inputPath;
    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {
        inputPath = propertyMap.get("inputPath");
        hostName = propertyMap.get("hostName");
        userName = propertyMap.get("userName");
    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        ProcessState processState = taskContext.getParentProcessContext().getProcessState();
        try {
            TaskStatus status = new TaskStatus(TaskState.EXECUTING);
            final DataStagingTaskModel subTaskModel = (DataStagingTaskModel) ThriftUtils.getSubTaskModel
                    (taskContext.getTaskModel());
            if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                OutputDataObjectType processOutput = taskContext.getProcessOutput();
                if (processOutput != null && processOutput.getValue() == null) {
                    log.error("expId: {}, processId:{}, taskId: {}:- Couldn't stage file {} , file name shouldn't be null",
                            taskContext.getExperimentId(), taskContext.getProcessId(), taskContext.getTaskId(),
                            processOutput.getName());
                    status = new TaskStatus(TaskState.FAILED);
                    if (processOutput.isIsRequired()) {
                        status.setReason("File name is null, but this output's isRequired bit is not set");
                    } else {
                        status.setReason("File name is null");
                    }
                    return status;
                }
                if (processOutput != null) {
                    if (processOutput.isOutputStreaming()) {
                        // stream output periodically
                        ComputationalResourceSchedulingModel resourceSchedule = taskContext.getParentProcessContext()
                                .getProcessModel().getProcessResourceSchedule();
                        int wallTimeLimit = resourceSchedule.getWallTimeLimit();
                        if (wallTimeLimit > 10) {
                            int period = wallTimeLimit / 10;
                            Timer timer = new Timer();
                            StreamData streamData = new StreamData(userName, hostName, inputPath, taskContext, subTaskModel);
                            timer.schedule(streamData, 0, 1000 * 60 * period);
                            status.setState(TaskState.COMPLETED);
                        }
                    }
                }

            }
            return null;
        } catch (TException e) {
            log.error("Error while creating data streaming task", e);
            return null;
        }
    }



        @Override
    public TaskStatus recover(TaskContext taskContext) {
        return null;
    }

    @Override
    public TaskTypes getType() {
        return null;
    }



}
