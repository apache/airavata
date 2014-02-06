/*
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
 *
 */

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Configuration_Data;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class ExperimentConfigDataResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentConfigDataResource.class);
    private ExperimentMetadataResource exMetadata;
    private String resourceHostID;
    private int cpuCount;
    private int nodeCount;
    private int numberOfThreads;
    private String queueName;
    private int wallTimeLimit;
    private Timestamp jobStartTime;
    private int physicalMemory;
    private String projectAccount;
    private boolean airavataAutoSchedule;
    private boolean overrideManualSchedule;
    private String workingDir;
    private boolean stageInputsToWDir;
    private String outputDataDir;
    private String dataRegURL;
    private boolean persistOutputData;
    private boolean cleanAfterJob;
    private String applicationID;
    private String applicationVersion;
    private String workflowTemplateId;
    private String workflowTemplateVersion;
    private String workingDirParent;
    private String startExecutionAt;
    private String executeBefore;
    private int numberOfRetries;

    private byte[] request;

    public ExperimentMetadataResource getExMetadata() {
        return exMetadata;
    }

    public void setExMetadata(ExperimentMetadataResource exMetadata) {
        this.exMetadata = exMetadata;
    }

    public String getResourceHostID() {
        return resourceHostID;
    }

    public void setResourceHostID(String resourceHostID) {
        this.resourceHostID = resourceHostID;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public Timestamp getJobStartTime() {
        return jobStartTime;
    }

    public void setJobStartTime(Timestamp jobStartTime) {
        this.jobStartTime = jobStartTime;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(int physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public String getProjectAccount() {
        return projectAccount;
    }

    public void setProjectAccount(String projectAccount) {
        this.projectAccount = projectAccount;
    }

    public boolean isAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean isOverrideManualSchedule() {
        return overrideManualSchedule;
    }

    public void setOverrideManualSchedule(boolean overrideManualSchedule) {
        this.overrideManualSchedule = overrideManualSchedule;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isStageInputsToWDir() {
        return stageInputsToWDir;
    }

    public void setStageInputsToWDir(boolean stageInputsToWDir) {
        this.stageInputsToWDir = stageInputsToWDir;
    }

    public String getOutputDataDir() {
        return outputDataDir;
    }

    public void setOutputDataDir(String outputDataDir) {
        this.outputDataDir = outputDataDir;
    }

    public String getDataRegURL() {
        return dataRegURL;
    }

    public void setDataRegURL(String dataRegURL) {
        this.dataRegURL = dataRegURL;
    }

    public boolean isPersistOutputData() {
        return persistOutputData;
    }

    public void setPersistOutputData(boolean persistOutputData) {
        this.persistOutputData = persistOutputData;
    }

    public boolean isCleanAfterJob() {
        return cleanAfterJob;
    }

    public void setCleanAfterJob(boolean cleanAfterJob) {
        this.cleanAfterJob = cleanAfterJob;
    }

    public byte[] getRequest() {
        return request;
    }

    public void setRequest(byte[] request) {
        this.request = request;
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getWorkflowTemplateId() {
        return workflowTemplateId;
    }

    public void setWorkflowTemplateId(String workflowTemplateId) {
        this.workflowTemplateId = workflowTemplateId;
    }

    public String getWorkflowTemplateVersion() {
        return workflowTemplateVersion;
    }

    public void setWorkflowTemplateVersion(String workflowTemplateVersion) {
        this.workflowTemplateVersion = workflowTemplateVersion;
    }

    public String getWorkingDirParent() {
        return workingDirParent;
    }

    public void setWorkingDirParent(String workingDirParent) {
        this.workingDirParent = workingDirParent;
    }

    public String getStartExecutionAt() {
        return startExecutionAt;
    }

    public void setStartExecutionAt(String startExecutionAt) {
        this.startExecutionAt = startExecutionAt;
    }

    public String getExecuteBefore() {
        return executeBefore;
    }

    public void setExecuteBefore(String executeBefore) {
        this.executeBefore = executeBefore;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for experiment config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for experiment config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();    }

    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for experiment config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();    }

    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for experiment config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();    }

    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Experiment_Configuration_Data existingConfig = em.find(Experiment_Configuration_Data.class, exMetadata.getExpID());
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Experiment_Configuration_Data exconfig = new Experiment_Configuration_Data();
        exconfig.setAiravata_auto_schedule(isAiravataAutoSchedule());
        exconfig.setClean_after_job(cleanAfterJob);
        exconfig.setComputational_project_account(projectAccount);
        exconfig.setData_reg_url(dataRegURL);
        exconfig.setExperiment_config_data(request);
        Experiment_Metadata metadata = em.find(Experiment_Metadata.class, exMetadata.getExpID());
        exconfig.setExperiment_metadata(metadata);
        exconfig.setJob_start_time(jobStartTime);
        exconfig.setNode_count(nodeCount);
        exconfig.setNumber_of_threads(numberOfThreads);
        exconfig.setOutput_data_dir(outputDataDir);
        exconfig.setOverride_manual_schedule(overrideManualSchedule);
        exconfig.setPersist_output_data(persistOutputData);
        exconfig.setQueue_name(queueName);
        exconfig.setResource_host_id(resourceHostID);
        exconfig.setStage_input_files_to_working_dir(stageInputsToWDir);
        exconfig.setTotal_cpu_count(cpuCount);
        exconfig.setTotal_physical_memory(physicalMemory);
        exconfig.setWalltime_limit(wallTimeLimit);
        exconfig.setUnique_working_dir(workingDir);
        exconfig.setWorking_dir_parent(workingDirParent);
        exconfig.setApplication_id(applicationID);
        exconfig.setApplication_version(applicationVersion);
        exconfig.setWorkflow_template_id(workflowTemplateId);
        exconfig.setWorkflow_template_version(workflowTemplateVersion);
        exconfig.setStart_execution_at(startExecutionAt);
        exconfig.setExecute_before(executeBefore);
        exconfig.setNumber_of_retries(numberOfRetries);

        if (existingConfig != null){
            existingConfig.setAiravata_auto_schedule(isAiravataAutoSchedule());
            existingConfig.setClean_after_job(cleanAfterJob);
            existingConfig.setComputational_project_account(projectAccount);
            existingConfig.setData_reg_url(dataRegURL);
            existingConfig.setExperiment_config_data(request);
            existingConfig.setExperiment_metadata(metadata);
            existingConfig.setJob_start_time(jobStartTime);
            existingConfig.setNode_count(nodeCount);
            existingConfig.setNumber_of_threads(numberOfThreads);
            existingConfig.setOutput_data_dir(outputDataDir);
            existingConfig.setOverride_manual_schedule(overrideManualSchedule);
            existingConfig.setPersist_output_data(persistOutputData);
            existingConfig.setQueue_name(queueName);
            existingConfig.setResource_host_id(resourceHostID);
            existingConfig.setStage_input_files_to_working_dir(stageInputsToWDir);
            existingConfig.setTotal_cpu_count(cpuCount);
            existingConfig.setTotal_physical_memory(physicalMemory);
            existingConfig.setWalltime_limit(wallTimeLimit);
            existingConfig.setUnique_working_dir(workingDir);
            existingConfig.setUnique_working_dir(workingDir);
            existingConfig.setWorking_dir_parent(workingDirParent);
            existingConfig.setApplication_id(applicationID);
            existingConfig.setApplication_version(applicationVersion);
            existingConfig.setWorkflow_template_id(workflowTemplateId);
            existingConfig.setWorkflow_template_version(workflowTemplateVersion);
            existingConfig.setStart_execution_at(startExecutionAt);
            existingConfig.setExecute_before(executeBefore);
            existingConfig.setNumber_of_retries(numberOfRetries);
            exconfig = em.merge(existingConfig);
        }
        else {
           em.persist(exconfig);
        }
        em.getTransaction().commit();
        em.close();
    }
}
