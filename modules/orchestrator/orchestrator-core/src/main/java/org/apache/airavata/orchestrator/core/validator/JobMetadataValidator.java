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
package org.apache.airavata.orchestrator.core.validator;

import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.task.TaskModel;

/**
 * This is the interface to implement a validation logic, users can implement their validation
 * logic in validate mthod and if its failed, they can wrap-up an error and return the validation-Results object
 * as the return value
 */
public interface JobMetadataValidator {

    /**
     * Validation logic can be implemented, more importantsly no exceptions should be thrown,
     * if there are internal exceptions, errors can be encapsulate to the ValidationResults object
     * and set the results as failed (false) and return in, orchestrator will wrap them to an Exception and
     * thrown to the client side
     * @param experiment
     * @param processModel
     * @return
     */
    ValidationResults validate(ExperimentModel experiment, ProcessModel processModel);
}
