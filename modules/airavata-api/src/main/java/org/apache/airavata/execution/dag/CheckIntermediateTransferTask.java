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
package org.apache.airavata.execution.dag;

import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.model.TaskTypes;
import org.apache.airavata.execution.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Decision task: succeeds if the process has intermediate output-fetching tasks, fails otherwise.
 *
 * <p>Used in pre-execution DAGs to conditionally deprovision after job submission
 * when intermediate file transfer is required.
 */
@Component("checkIntermediateTransferTask")
public class CheckIntermediateTransferTask implements DagTask {

    private static final Logger logger = LoggerFactory.getLogger(CheckIntermediateTransferTask.class);

    @Override
    public DagTaskResult execute(TaskContext context) {
        var processModel = context.getProcessModel();
        boolean hasIntermediate = processModel.getTasks().stream()
                .anyMatch(task -> task.getTaskType() == TaskTypes.OUTPUT_FETCHING);
        if (hasIntermediate) {
            return new DagTaskResult.Success("Intermediate transfer tasks found");
        }
        return new DagTaskResult.Failure("No intermediate transfer tasks");
    }
}
