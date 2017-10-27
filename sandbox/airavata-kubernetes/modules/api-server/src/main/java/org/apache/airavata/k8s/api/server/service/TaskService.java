package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.task.TaskModel;
import org.apache.airavata.k8s.api.server.model.task.TaskParam;
import org.apache.airavata.k8s.api.server.model.task.TaskStatus;
import org.apache.airavata.k8s.api.server.repository.ProcessRepository;
import org.apache.airavata.k8s.api.server.repository.TaskParamRepository;
import org.apache.airavata.k8s.api.server.repository.TaskRepository;
import org.apache.airavata.k8s.api.server.repository.TaskStatusRepository;
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
    private TaskParamRepository taskParamRepository;
    private TaskStatusRepository taskStatusRepository;

    public TaskService(ProcessRepository processRepository,
                       TaskRepository taskRepository,
                       TaskParamRepository taskParamRepository,
                       TaskStatusRepository taskStatusRepository) {

        this.processRepository = processRepository;
        this.taskRepository = taskRepository;
        this.taskParamRepository = taskParamRepository;
        this.taskStatusRepository = taskStatusRepository;
    }

    public long create(TaskResource resource) {
        TaskModel taskModel = new TaskModel();
        taskModel.setCreationTime(resource.getCreationTime());
        taskModel.setLastUpdateTime(resource.getLastUpdateTime());
        taskModel.setOrderIndex(resource.getOrder());
        taskModel.setTaskDetail(resource.getTaskDetail());
        taskModel.setParentProcess(processRepository.findById(resource.getParentProcessId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find process with id " +
                        resource.getParentProcessId())));
        taskModel.setTaskType(TaskModel.TaskTypes.valueOf(resource.getTaskType()));

        TaskModel savedTask = taskRepository.save(taskModel);

        Optional.ofNullable(resource.getTaskParams()).ifPresent(params -> params.forEach(param -> {
            TaskParam taskParam = new TaskParam();
            taskParam.setKey(param.getKey());
            taskParam.setValue(param.getValue());
            taskParam.setTaskModel(savedTask);
            taskParamRepository.save(taskParam);

        }));
        return savedTask.getId();
    }

    public long addTaskStatus(long taskId, TaskStatusResource resource) {

        TaskModel taskModel = taskRepository.findById(taskId)
                .orElseThrow(() -> new ServerRuntimeException("Task with id " + taskId + " can not be found"));

        TaskStatus status = new TaskStatus();
        status.setReason(resource.getReason());
        status.setState(TaskStatus.TaskState.valueOf(resource.getState()));
        status.setTimeOfStateChange(resource.getTimeOfStateChange());
        status.setTaskModel(taskModel);
        TaskStatus savedStatus = taskStatusRepository.save(status);

        return savedStatus.getId();
    }

    public Optional<TaskStatusResource> findTaskStatusById(long id) {
        return ToResourceUtil.toResource(taskStatusRepository.findById(id).get());
    }

    public Optional<TaskResource> findById(long id) {
        return ToResourceUtil.toResource(taskRepository.findById(id).get());
    }

}
