package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.resources.process.ProcessStatusResource;
import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.process.ProcessModel;
import org.apache.airavata.k8s.api.server.model.process.ProcessStatus;
import org.apache.airavata.k8s.api.server.model.task.TaskModel;
import org.apache.airavata.k8s.api.server.repository.ProcessRepository;
import org.apache.airavata.k8s.api.resources.process.ProcessResource;
import org.apache.airavata.k8s.api.server.repository.ProcessStatusRepository;
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
public class ProcessService {

    private ProcessRepository processRepository;
    private ProcessStatusRepository processStatusRepository;

    private ExperimentService experimentService;
    private TaskService taskService;

    public ProcessService(ProcessRepository processRepository,
                          ProcessStatusRepository processStatusRepository,
                          ExperimentService experimentService,
                          TaskService taskService) {

        this.processRepository = processRepository;
        this.processStatusRepository = processStatusRepository;
        this.experimentService = experimentService;
        this.taskService = taskService;
    }

    public long create(ProcessResource resource) {

        ProcessModel processModel = new ProcessModel();
        processModel.setId(resource.getId());
        processModel.setCreationTime(resource.getCreationTime());
        processModel.setLastUpdateTime(resource.getLastUpdateTime());
        processModel.setExperiment(experimentService.findEntityById(resource.getExperimentId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find experiment with id " +
                        resource.getExperimentId())));
        processModel.setExperimentDataDir(resource.getExperimentDataDir());

        ProcessModel saved = processRepository.save(processModel);

        Optional.ofNullable(resource.getTasks()).ifPresent(taskResources -> taskResources.forEach(taskRes -> {
            TaskModel taskModel = new TaskModel();
            taskRes.setParentProcessId(saved.getId());
            taskModel.setId(taskService.create(taskRes));
        }));

        return saved.getId();
    }

    public long addProcessStatus(long processId, ProcessStatusResource resource) {
        ProcessModel processModel = processRepository.findById(processId)
                .orElseThrow(() -> new ServerRuntimeException("Process with id " + processId + " can not be found"));

        ProcessStatus status = new ProcessStatus();
        status.setReason(resource.getReason());
        status.setState(ProcessStatus.ProcessState.valueOf(resource.getState()));
        status.setTimeOfStateChange(resource.getTimeOfStateChange());
        status.setProcessModel(processModel);
        ProcessStatus savedStatus = processStatusRepository.save(status);
        return savedStatus.getId();
    }

    public Optional<ProcessResource> findById(long id) {
        return ToResourceUtil.toResource(processRepository.findById(id).get());
    }
}
