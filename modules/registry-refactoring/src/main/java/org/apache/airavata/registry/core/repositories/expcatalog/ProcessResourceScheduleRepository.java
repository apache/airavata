package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessResourceScheduleEntity;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessResourceScheduleRepository extends ExpCatAbstractRepository<ComputationalResourceSchedulingModel, ProcessResourceScheduleEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessResourceScheduleRepository.class);
    private ProcessRepository processRepository = new ProcessRepository();

    public ProcessResourceScheduleRepository() { super(ComputationalResourceSchedulingModel.class, ProcessResourceScheduleEntity.class); }

    public String addProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        ProcessModel processModel = processRepository.getProcess(processId);
        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel);
        processRepository.updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        return addProcessResourceSchedule(computationalResourceSchedulingModel, processId);
    }

    public ComputationalResourceSchedulingModel getProcessResourceSchedule(String processId) throws RegistryException {
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessResourceSchedule();
    }

    public boolean isProcessResourceScheduleExist(String processId) throws RegistryException {
        return isExists(processId);
    }

    public void removeProcessResourceSchedule(String processId) throws RegistryException {
        delete(processId);
    }

}