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
package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.service.TaskService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@RestController
@RequestMapping(path="/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public long createTask(@RequestBody TaskResource resource) {
        return taskService.create(resource);
    }

    @PostMapping( path = "{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public long addStatus(@PathVariable("id") long id, @RequestBody TaskStatusResource resource) {
        return taskService.addTaskStatus(id, resource);
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResource findTaskById(@PathVariable("id") long id) {
        return this.taskService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Task with id " + id + " not found"));
    }

    @GetMapping(path = "status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskStatusResource findTaskStatusById(@PathVariable("id") long id) {
        return this.taskService.findTaskStatusById(id)
                .orElseThrow(() -> new ServerRuntimeException("Task status with id " + id + " not found"));
    }
}
