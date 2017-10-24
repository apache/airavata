package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.task.TaskModel;
import org.apache.airavata.k8s.api.server.repository.ProcessRepository;
import org.apache.airavata.k8s.api.server.repository.TaskRepository;
import org.apache.airavata.k8s.api.server.resources.task.TaskResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class TaskService {

    private ProcessRepository processRepository;
    private TaskRepository taskRepository;

    public TaskService(ProcessRepository processRepository, TaskRepository taskRepository) {
        this.processRepository = processRepository;
        this.taskRepository = taskRepository;
    }

    public long create(TaskResource resource) {
        TaskModel taskModel = new TaskModel();
        taskModel.setCreationTime(resource.getCreationTime());
        taskModel.setLastUpdateTime(resource.getLastUpdateTime());
        taskModel.setParentProcess(processRepository.findById(resource.getParentProcessId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find process with id " +
                        resource.getParentProcessId())));
        taskModel.setTaskType(TaskModel.TaskTypes.valueOf(resource.getTaskType()));
        TaskModel saved = taskRepository.save(taskModel);
        return saved.getId();
    }

    public Optional<TaskResource> findById(long id) {
        return ToResourceUtil.toResource(taskRepository.findById(id).get());
    }

}
