package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessWorkflowEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessWorkflowPK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ProcessWorkflowRepository extends ExpCatAbstractRepository<ProcessWorkflow, ProcessWorkflowEntity, ProcessWorkflowPK> {

    private final static Logger logger = LoggerFactory.getLogger(ProcessInputRepository.class);

    public ProcessWorkflowRepository() {
        super(ProcessWorkflow.class, ProcessWorkflowEntity.class);
    }

    protected void saveProcessWorkflow(List<ProcessWorkflow> processWorkflows, String processId) throws RegistryException {

        for (ProcessWorkflow processWorkflow : processWorkflows) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ProcessWorkflowEntity processWorkflowEntity = mapper.map(processWorkflow, ProcessWorkflowEntity.class);

            if (processWorkflowEntity.getProcessId() == null) {
                logger.debug("Setting the ProcessWorkflowEntity's ProcessId");
                processWorkflowEntity.setProcessId(processId);
            }
            execute(entityManager -> entityManager.merge(processWorkflowEntity));
        }
    }

    public String addProcessWorkflow(ProcessWorkflow processWorkflow, String processId) throws RegistryException {
        saveProcessWorkflow(Collections.singletonList(processWorkflow), processId);
        return processId;
    }

    public void addProcessWorkflows(List<ProcessWorkflow> processWorkflows, String processId) throws RegistryException {
        saveProcessWorkflow(processWorkflows, processId);
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessWorkflows();
    }
}
