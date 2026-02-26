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
package org.apache.airavata.status.service;

import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.model.TaskState;
import org.apache.airavata.status.model.ErrorModel;

/**
 * Process-level status service. All status events are scoped to a process.
 *
 * <p>Experiment state is a direct column on the experiment table, mutated by the
 * orchestration layer when process state changes cascade upward.
 */
public interface StatusService {

    String addProcessStatus(StatusModel<ProcessState> status, String processId) throws RegistryException;

    StatusModel<ProcessState> getLatestProcessStatus(String processId) throws RegistryException;

    String addTaskStatus(StatusModel<TaskState> status, String taskId) throws RegistryException;

    String addJobStatus(StatusModel<JobState> status, String jobId) throws RegistryException;

    String addProcessError(ErrorModel error, String processId) throws RegistryException;

    String addTaskError(ErrorModel error, String taskId) throws RegistryException;
}
