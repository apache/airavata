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
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.Process;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProcessResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(ProcessResource.class);
    private String processId;
    private String experimentId;
    private Timestamp creationTime;
    private Timestamp lastUpdateTime;
    private String processDetail;
    private String applicationInterfaceId;
    private String taskDag;
    private String applicationDeploymentId;
    private String computeResourceId;
    private String gatewayExecutionId;
    private boolean enableEmailNotification;
    private String emailAddresses;
    private String storageResourceId;
    private String userDn;
    private String userName;
    private boolean generateCert;
    private String experimentDataDir;
    private boolean useUserCRPref;
    private int processTypeValue;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getProcessDetail() {
        return processDetail;
    }

    public void setProcessDetail(String processDetail) {
        this.processDetail = processDetail;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getTaskDag() {
        return taskDag;
    }

    public void setTaskDag(String taskDag) {
        this.taskDag = taskDag;
    }

    public String getApplicationDeploymentId() {
        return applicationDeploymentId;
    }

    public void setApplicationDeploymentId(String applicationDeploymentId) {
        this.applicationDeploymentId = applicationDeploymentId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    public boolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isUseUserCRPref() {
        return useUserCRPref;
    }

    public void setUseUserCRPref(boolean useUserCRPref) {
        this.useUserCRPref = useUserCRPref;
    }

    public int getProcessTypeValue() {
        return processTypeValue;
    }

    public void setProcessTypeValue(int processTypeValue) {
        this.processTypeValue = processTypeValue;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException{
       switch (type){
           case PROCESS_ERROR:
               ProcessErrorResource errorResource = new ProcessErrorResource();
               errorResource.setProcessId(processId);
               return errorResource;
           case PROCESS_STATUS:
               ProcessStatusResource statusResource = new ProcessStatusResource();
               statusResource.setProcessId(processId);
               return statusResource;
           case PROCESS_INPUT:
               ProcessInputResource processInputResource = new ProcessInputResource();
               processInputResource.setProcessId(processId);
               return processInputResource;
           case PROCESS_OUTPUT:
               ProcessOutputResource processOutputResource = new ProcessOutputResource();
               processOutputResource.setProcessId(processId);
               return processOutputResource;
           case PROCESS_RESOURCE_SCHEDULE:
               ProcessResourceScheduleResource processResourceScheduleResource = new ProcessResourceScheduleResource();
               processResourceScheduleResource.setProcessId(processId);
               return processResourceScheduleResource;
           case TASK:
               TaskResource taskResource = new TaskResource();
               taskResource.setParentProcessId(processId);
               return taskResource;
	       case JOB:
		       JobResource jobResource = new JobResource();
		       jobResource.setProcessId(processId);
		       return jobResource;
           default:
               logger.error("Unsupported resource type for process resource.", new IllegalArgumentException());
               throw new IllegalArgumentException("Unsupported resource type for process resource.");
       }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case PROCESS_ERROR:
                    generator = new QueryGenerator(PROCESS_ERROR);
                    generator.setParameter(ProcessErrorConstants.ERROR_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case PROCESS_STATUS:
                    generator = new QueryGenerator(PROCESS_STATUS);
                    generator.setParameter(ProcessStatusConstants.STATUS_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case PROCESS_INPUT:
                    generator = new QueryGenerator(PROCESS_INPUT);
                    generator.setParameter(ProcessInputConstants.INPUT_NAME, name);
                    generator.setParameter(ProcessInputConstants.PROCESS_ID, processId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case PROCESS_OUTPUT:
                    generator = new QueryGenerator(PROCESS_OUTPUT);
                    generator.setParameter(ProcessOutputConstants.OUTPUT_NAME, name);
                    generator.setParameter(ProcessOutputConstants.PROCESS_ID, processId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case PROCESS_RESOURCE_SCHEDULE:
                    generator = new QueryGenerator(PROCESS_RESOURCE_SCHEDULE);
                    generator.setParameter(ProcessResourceScheduleConstants.PROCESS_ID, processId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case TASK:
                    generator = new QueryGenerator(TASK);
                    generator.setParameter(TaskConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
	            case JOB:
		            generator = new QueryGenerator(JOB);
		            generator.setParameter(JobConstants.JOB_ID, name);
		            generator.setParameter(JobConstants.PROCESS_ID, processId);
		            q = generator.deleteQuery(em);
		            q.executeUpdate();
		            break;
                default:
                    logger.error("Unsupported resource type for process detail resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case PROCESS_STATUS:
                    generator = new QueryGenerator(PROCESS_STATUS);
                    generator.setParameter(ProcessStatusConstants.STATUS_ID, name);
                    q = generator.selectQuery(em);
                    ProcessStatus status = (ProcessStatus) q.getSingleResult();
                    ProcessStatusResource statusResource = (ProcessStatusResource) Utils.getResource(ResourceType.PROCESS_STATUS, status);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return statusResource;
                case PROCESS_ERROR:
                    generator = new QueryGenerator(PROCESS_ERROR);
                    generator.setParameter(ProcessErrorConstants.ERROR_ID, name);
                    q = generator.selectQuery(em);
                    ProcessError processError = (ProcessError) q.getSingleResult();
                    ProcessErrorResource processErrorResource = (ProcessErrorResource) Utils.getResource(ResourceType.PROCESS_ERROR, processError);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return processErrorResource;
                case PROCESS_INPUT:
                    generator = new QueryGenerator(PROCESS_INPUT);
                    generator.setParameter(ProcessInputConstants.INPUT_NAME, name);
                    generator.setParameter(ProcessInputConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    ProcessInput processInput = (ProcessInput) q.getSingleResult();
                    ProcessInputResource processInputResource = (ProcessInputResource) Utils.getResource(ResourceType.PROCESS_INPUT, processInput);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return processInputResource;
                case PROCESS_OUTPUT:
                    generator = new QueryGenerator(PROCESS_OUTPUT);
                    generator.setParameter(ProcessOutputConstants.OUTPUT_NAME, name);
                    generator.setParameter(ProcessInputConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    ProcessOutput processOutput = (ProcessOutput) q.getSingleResult();
                    ProcessOutputResource outputResource = (ProcessOutputResource) Utils.getResource(ResourceType.PROCESS_OUTPUT, processOutput);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return outputResource;
                case PROCESS_RESOURCE_SCHEDULE:
                    generator = new QueryGenerator(PROCESS_RESOURCE_SCHEDULE);
                    generator.setParameter(ProcessResourceScheduleConstants.PROCESS_ID, name);
                    q = generator.selectQuery(em);
                    ProcessResourceSchedule processResourceSchedule = (ProcessResourceSchedule) q.getSingleResult();
                    ProcessResourceScheduleResource processResourceScheduleResource = (ProcessResourceScheduleResource)
                            Utils.getResource(ResourceType.PROCESS_RESOURCE_SCHEDULE, processResourceSchedule);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return processResourceScheduleResource;
                case TASK:
                    generator = new QueryGenerator(TASK);
                    generator.setParameter(TaskConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    Task task = (Task) q.getSingleResult();
                    TaskResource taskResource = (TaskResource) Utils.getResource(ResourceType.TASK, task);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return taskResource;
	            case JOB:
		            generator = new QueryGenerator(JOB);
		            generator.setParameter(JobConstants.JOB_ID, name);
		            generator.setParameter(JobConstants.PROCESS_ID, processId);
		            q = generator.selectQuery(em);
		            Job job = (Job) q.getSingleResult();
		            JobResource jobResource = (JobResource) Utils.getResource(ResourceType.JOB, job);
		            em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
		            return jobResource;
	            default:
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    logger.error("Unsupported resource type for process resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for process resource.");
            }
        } catch (Exception e) {
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        List<ExperimentCatResource> resourceList = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case PROCESS_INPUT:
                    generator = new QueryGenerator(PROCESS_INPUT);
                    generator.setParameter(ProcessInputConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ProcessInput processInput = (ProcessInput) result;
                            ProcessInputResource processInputResource =
                                    (ProcessInputResource) Utils.getResource(ResourceType.PROCESS_INPUT, processInput);
                            resourceList.add(processInputResource);
                        }
                    }
                    break;
                case PROCESS_OUTPUT:
                    generator = new QueryGenerator(PROCESS_OUTPUT);
                    generator.setParameter(ProcessOutputConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ProcessOutput processOutput = (ProcessOutput) result;
                            ProcessOutputResource processOutputResource
                                    = (ProcessOutputResource) Utils.getResource(ResourceType.PROCESS_OUTPUT, processOutput);
                            resourceList.add(processOutputResource);
                        }
                    }
                    break;
                case TASK:
                    generator = new QueryGenerator(TASK);
                    generator.setParameter(TaskConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Task task = (Task) result;
                            TaskResource taskResource =
                                    (TaskResource) Utils.getResource(ResourceType.TASK, task);
                            resourceList.add(taskResource);
                        }
                    }
                    break;
                case PROCESS_ERROR:
                    generator = new QueryGenerator(PROCESS_ERROR);
                    generator.setParameter(ProcessErrorConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ProcessError processError = (ProcessError) result;
                            ProcessErrorResource processErrorResource =
                                    (ProcessErrorResource) Utils.getResource(ResourceType.PROCESS_ERROR, processError);
                            resourceList.add(processErrorResource);
                        }
                    }
                    break;
                case PROCESS_STATUS:
                    generator = new QueryGenerator(PROCESS_STATUS);
                    generator.setParameter(ProcessStatusConstants.PROCESS_ID, processId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ProcessStatus processStatus = (ProcessStatus) result;
                            ProcessStatusResource processStatusResource =
                                    (ProcessStatusResource) Utils.getResource(ResourceType.PROCESS_STATUS, processStatus);
                            resourceList.add(processStatusResource);
                        }
                    }
                    break;
	            case JOB:
		            generator = new QueryGenerator(JOB);
		            generator.setParameter(JobConstants.PROCESS_ID, processId);
		            q = generator.selectQuery(em);
		            results = q.getResultList();
		            if (results.size() != 0) {
			            for (Object result : results) {
				            Job job = (Job) result;
				            JobResource jobResource =
						            (JobResource) Utils.getResource(ResourceType.JOB, job);
				            resourceList.add(jobResource);
			            }
		            }
		            break;
                default:
                    logger.error("Unsupported resource type for task resource.", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            Process existingProcess = em.find(Process.class, processId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            Process process;
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingProcess == null) {
            	process = new Process();
            }else {
                process = existingProcess;
            }
            process.setProcessId(processId);
            process.setExperimentId(experimentId);
            process.setCreationTime(creationTime);
            process.setLastUpdateTime(lastUpdateTime);
            process.setProcessDetail(processDetail);
            process.setTaskDag(taskDag);
            process.setComputeResourceId(computeResourceId);
            process.setApplicationInterfaceId(applicationInterfaceId);
            process.setApplicationDeploymentId(applicationDeploymentId);
            process.setGatewayExecutionId(gatewayExecutionId);
            process.setEnableEmailNotification(enableEmailNotification);
            process.setEmailAddresses(emailAddresses);
            process.setStorageId(storageResourceId);
            process.setUserDn(userDn);
            process.setGenerateCert(generateCert);
            process.setExperimentDataDir(experimentDataDir);
            process.setUserName(userName);
            process.setUseUserCRPref(useUserCRPref);
            process.setProcessTypeValue(processTypeValue);

            if (existingProcess == null){
                em.persist(process);
            }else {
                em.merge(process);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<ProcessInputResource> getProcessInputs() throws RegistryException{
        List<ProcessInputResource> processInputResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.PROCESS_INPUT);
        for (ExperimentCatResource resource : resources) {
            ProcessInputResource inputResource = (ProcessInputResource) resource;
            processInputResources.add(inputResource);
        }
        return processInputResources;
    }

    public List<ProcessOutputResource> getProcessOutputs() throws RegistryException{
        List<ProcessOutputResource> outputResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.PROCESS_OUTPUT);
        for (ExperimentCatResource resource : resources) {
            ProcessOutputResource outputResource = (ProcessOutputResource) resource;
            outputResources.add(outputResource);
        }
        return outputResources;
    }

    public List<ProcessStatusResource> getProcessStatuses() throws RegistryException{
        List<ProcessStatusResource> processStatusResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.PROCESS_STATUS);
        for (ExperimentCatResource resource : resources) {
            ProcessStatusResource statusResource = (ProcessStatusResource) resource;
            processStatusResources.add(statusResource);
        }
        return processStatusResources;
    }

    public ProcessStatusResource getProcessStatus() throws RegistryException{
        List<ProcessStatusResource> processStatusResources = getProcessStatuses();
        if(processStatusResources.size() == 0){
            return null;
        }else{
            ProcessStatusResource max = processStatusResources.get(0);
            for(int i=1; i<processStatusResources.size();i++){
                Timestamp timeOfStateChange = processStatusResources.get(i).getTimeOfStateChange();
                if (timeOfStateChange != null) {
                    if (timeOfStateChange.after(max.getTimeOfStateChange())
                      || (timeOfStateChange.equals(max.getTimeOfStateChange()) && processStatusResources.get(i).getState().equals(ProcessState.COMPLETED.toString()))
                      || (timeOfStateChange.equals(max.getTimeOfStateChange()) && processStatusResources.get(i).getState().equals(ProcessState.FAILED.toString()))
                      || (timeOfStateChange.equals(max.getTimeOfStateChange()) && processStatusResources.get(i).getState().equals(ProcessState.CANCELED.toString()))) {
                        max = processStatusResources.get(i);
                    }
                }
            }
            return max;
        }
    }

    public List<ProcessErrorResource> getProcessErrors() throws RegistryException{
        List<ProcessErrorResource> processErrorResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.PROCESS_ERROR);
        for (ExperimentCatResource resource : resources) {
            ProcessErrorResource errorResource = (ProcessErrorResource) resource;
            processErrorResources.add(errorResource);
        }
        return processErrorResources;
    }

    public ProcessErrorResource getProcessError() throws RegistryException{
        List<ProcessErrorResource> processErrorResources = getProcessErrors();
        if(processErrorResources.size() == 0){
            return null;
        }else{
            ProcessErrorResource max = processErrorResources.get(0);
            for(int i=1; i<processErrorResources.size();i++){
                if(processErrorResources.get(i).getCreationTime().after(max.getCreationTime())){
                    max = processErrorResources.get(i);
                }
            }
            return max;
        }
    }

    public ProcessResourceScheduleResource getProcessResourceSchedule() throws RegistryException{
        ExperimentCatResource resource = get(ResourceType.PROCESS_RESOURCE_SCHEDULE, processId);
        return (ProcessResourceScheduleResource)resource;
    }

    public List<TaskResource> getTaskList() throws RegistryException{
        List<TaskResource> taskResources = new ArrayList<TaskResource>();
        List<ExperimentCatResource> resources = get(ResourceType.TASK);
        for (ExperimentCatResource resource : resources) {
            TaskResource taskResource = (TaskResource) resource;
            taskResources.add(taskResource);
        }
        return taskResources;
    }

    public TaskResource getTask(String taskId) throws RegistryException {
        ExperimentCatResource resource = get(ResourceType.TASK, taskId);
        return (TaskResource)resource;
    }

	public JobResource getJob(String jobId) throws RegistryException {
		return (JobResource) get(ResourceType.JOB, jobId);
	}

	public List<JobResource> getJobList() throws RegistryException {
		List<JobResource> jobResources = new ArrayList();
		List<ExperimentCatResource> resources = get(ResourceType.JOB);
		for (ExperimentCatResource resource : resources) {
			JobResource jobResource = (JobResource) resource;
			jobResources.add(jobResource);
		}
		return jobResources;
	}
}
