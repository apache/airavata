package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.resources.process.ProcessResource;
import org.apache.airavata.k8s.api.server.resources.task.TaskResource;
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

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResource findTaskById(@PathVariable("id") long id) {
        return this.taskService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Task with id " + id + " not found"));
    }
}
