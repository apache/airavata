package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class ProcessRepository extends ExpCatAbstractRepository<ProcessModel, ProcessEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    public ProcessRepository() { super(ProcessModel.class, ProcessEntity.class); }

    protected String saveProcessModelData(ProcessModel processModel) throws RegistryException {
        ProcessEntity processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    protected ProcessEntity saveProcess(ProcessModel processModel) throws RegistryException {
        if (processModel.getProcessId() == null || processModel.getProcessId().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Process's ProcessId");
            processModel.setProcessId(ExpCatalogUtils.getID("PROCESS"));
        }

        String processId = processModel.getProcessId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProcessEntity processEntity = mapper.map(processModel, ProcessEntity.class);

        if (processEntity.getProcessResourceSchedule() != null) {
            logger.debug("Populating the Primary Key of ProcessResourceSchedule object for the Process");
            processEntity.getProcessResourceSchedule().setProcessId(processId);
        }

        if (processEntity.getProcessInputs() != null) {
            logger.debug("Populating the Primary Key of ProcessInput objects for the Process");
            processEntity.getProcessInputs().forEach(processInputEntity -> processInputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessOutputs() != null) {
            logger.debug("Populating the Primary Key of ProcessOutput objects for the Process");
            processEntity.getProcessOutputs().forEach(processOutputEntity -> processOutputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessStatuses() != null) {
            logger.debug("Populating the Primary Key of ProcessStatus objects for the Process");
            processEntity.getProcessStatuses().forEach(processStatusEntity -> processStatusEntity.setProcessId(processId));
        }

        if (processEntity.getProcessErrors() != null) {
            logger.debug("Populating the Primary Key of ProcessError objects for the Process");
            processEntity.getProcessErrors().forEach(processErrorEntity -> processErrorEntity.setProcessId(processId));
        }

        if (processEntity.getTasks() != null) {
            logger.debug("Populating the Process objects' Process ID for the Process");
            processEntity.getTasks().forEach(taskEntity -> taskEntity.setParentProcessId(processId));
        }

        if (!isProcessExist(processId)) {
            logger.debug("Checking if the Process already exists");
            processEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        processEntity.setLastUpdateTime(new Timestamp((System.currentTimeMillis())));
        return execute(entityManager -> entityManager.merge(processEntity));
    }

    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        process.setExperimentId(experimentId);
        String processId = saveProcessModelData(process);
        return processId;
    }

    public void updateProcess(ProcessModel updatedProcess, String processId) throws RegistryException {
        saveProcessModelData(updatedProcess);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.get(processId);
    }

    public String addProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel);
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        return addProcessResourceSchedule(computationalResourceSchedulingModel, processId);
    }

    public ComputationalResourceSchedulingModel getProcessResourceSchedule(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessResourceSchedule();
    }

    public String addProcessInputs(List<InputDataObjectType> processInputs, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<InputDataObjectType> inputDataObjectTypeList = processModel.getProcessInputs();

        for (InputDataObjectType input : processInputs) {

            if (inputDataObjectTypeList != null && !inputDataObjectTypeList.contains(input)) {
                logger.debug("Adding the ProcessInput to the list");
                inputDataObjectTypeList.add(input);
                processModel.setProcessInputs(inputDataObjectTypeList);
            }

        }

        updateProcess(processModel, processId);
        return processId;
    }

    public void updateProcessInputs(List<InputDataObjectType> processInputs, String processId) throws RegistryException {
        addProcessInputs(processInputs, processId);
    }

    public List<InputDataObjectType> getProcessInputs(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessInputs();
    }

    public String addProcessOutputs(List<OutputDataObjectType> processOutputs, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<OutputDataObjectType> outputDataObjectTypeList = processModel.getProcessOutputs();

        for (OutputDataObjectType output : processOutputs) {

            if (outputDataObjectTypeList != null && !outputDataObjectTypeList.contains(output)) {
                logger.debug("Adding the ProcessOutput to the list");
                outputDataObjectTypeList.add(output);
                processModel.setProcessOutputs(outputDataObjectTypeList);
            }

        }

        updateProcess(processModel, processId);
        return processId;
    }

    public void updateProcessOutputs(List<OutputDataObjectType> processOutputs, String processId) throws RegistryException {
        addProcessOutputs(processOutputs, processId);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessOutputs();
    }

    public String addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<ProcessStatus> processStatusList = processModel.getProcessStatuses();

        if (processStatusList.size() == 0 || !processStatusList.contains(processStatus)) {

            if (processStatus.getStatusId() == null) {
                logger.debug("Set ProcessStatus's StatusId");
                processStatus.setStatusId(ExpCatalogUtils.getID("STATUS"));
            }

            logger.debug("Adding the ProcessStatus to the list");
            processStatusList.add(processStatus);
        }

        processModel.setProcessStatuses(processStatusList);
        updateProcess(processModel, processId);
        return processStatus.getStatusId();
    }

    public String updateProcessStatus(ProcessStatus updatedProcessStatus, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<ProcessStatus> processStatusList = processModel.getProcessStatuses();

        for (ProcessStatus retrievedProcessStatus : processStatusList) {

            if (retrievedProcessStatus.getStatusId().equals(updatedProcessStatus.getStatusId())) {
                logger.debug("Updating the ProcessStatus");
                processStatusList.remove(retrievedProcessStatus);
                processStatusList.add(updatedProcessStatus);
            }

        }

        processModel.setProcessStatuses(processStatusList);
        updateProcess(processModel, processId);
        return updatedProcessStatus.getStatusId();
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<ProcessStatus> processStatusList = processModel.getProcessStatuses();

        if(processStatusList.size() == 0) {
            logger.debug("ProcessStatus list is empty");
            return null;
        }

        else {
            ProcessStatus latestProcessStatus = processStatusList.get(0);

            for(int i = 1; i < processStatusList.size(); i++){
                Timestamp timeOfStateChange = new Timestamp(processStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange != null) {

                    if (timeOfStateChange.after(new Timestamp(latestProcessStatus.getTimeOfStateChange()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange()) && processStatusList.get(i).getState().equals(ProcessState.COMPLETED.toString()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange()) && processStatusList.get(i).getState().equals(ProcessState.FAILED.toString()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange()) && processStatusList.get(i).getState().equals(ProcessState.CANCELED.toString()))) {
                        latestProcessStatus = processStatusList.get(i);
                    }

                }

            }
            return latestProcessStatus;
        }
    }

    public String addProcessError(ErrorModel processError, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        List<ErrorModel> errorModelList = processModel.getProcessErrors();

        if (errorModelList == null) {
            logger.debug("Adding the first ProcessError to the list");
            processModel.setProcessErrors(Arrays.asList(processError));
        }

        else if (!errorModelList.contains(processError)) {
            logger.debug("Adding the ProcessError to the list");
            errorModelList.add(processError);
            processModel.setProcessErrors(errorModelList);
        }

        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessError(ErrorModel processError, String processId) throws RegistryException {
        return addProcessError(processError, processId);
    }

    public List<ErrorModel> getProcessError(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessErrors();
    }

    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        List<ProcessModel> processModelList;

        if (fieldName.equals(DBConstants.Process.EXPERIMENT_ID)) {
            logger.debug("Search criteria is ExperimentId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Process.EXPERIMENT_ID, value);
            processModelList = processRepository.select(QueryConstants.GET_PROCESS_FOR_EXPERIMENT_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Process module.");
            throw new IllegalArgumentException("Unsupported field name for Process module.");
        }

        return processModelList;
    }

    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        List<String> processIds = new ArrayList<>();
        List<ProcessModel> processModelList = getProcessList(fieldName, value);
        for (ProcessModel processModel : processModelList) {
            processIds.add(processModel.getProcessId());
        }
        return processIds;
    }

    public boolean isProcessExist(String processId) throws RegistryException {
        return isExists(processId);
    }

    public void removeProcess(String processId) throws RegistryException {
        delete(processId);
    }

}
