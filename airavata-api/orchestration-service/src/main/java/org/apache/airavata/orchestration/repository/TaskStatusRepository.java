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
package org.apache.airavata.orchestration.repository;

import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.springframework.stereotype.Component;

/**
 * Thin facade over ExecStatusRepository for backward compatibility.
 */
@Component
public class TaskStatusRepository {

    private final ExecStatusRepository delegate = new ExecStatusRepository();

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        return delegate.addTaskStatus(taskStatus, taskId);
    }

    public String updateTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        return delegate.updateTaskStatus(taskStatus, taskId);
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        return delegate.getTaskStatus(taskId);
    }
}
