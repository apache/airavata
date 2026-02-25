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
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.execution.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Decision task: succeeds if any process output has data movement enabled, fails otherwise.
 *
 * <p>Used in post-execution DAGs to conditionally skip output data staging
 * when no outputs require remote file transfer.
 */
@Component("checkDataMovementTask")
public class CheckDataMovementTask implements DagTask {

    private static final Logger logger = LoggerFactory.getLogger(CheckDataMovementTask.class);

    @Override
    public DagTaskResult execute(TaskContext context) {
        var processModel = context.getProcessModel();
        var outputs = processModel.getProcessOutputs();
        if (outputs == null || outputs.isEmpty()) {
            return new DagTaskResult.Failure("No outputs defined");
        }
        boolean hasDataMovement = outputs.stream().anyMatch(ApplicationOutput::getDataMovement);
        if (hasDataMovement) {
            return new DagTaskResult.Success("Data movement outputs found");
        }
        return new DagTaskResult.Failure("No data movement outputs");
    }
}
