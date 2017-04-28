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

import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.model.Process;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ExperimentResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentResource.class);
    private String experimentId;
    private String projectId;
    private String gatewayId;
    private String experimentType;
    private String userName;
    private String experimentName;
    private Timestamp creationTime;
    private String description;
    private String executionId;
    private String gatewayExecutionId;
    private String gatewayInstanceId;
    private boolean enableEmailNotification;
    private String emailAddresses;

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getGatewayInstanceId() {
        return gatewayInstanceId;
    }

    public void setGatewayInstanceId(String gatewayInstanceId) {
        this.gatewayInstanceId = gatewayInstanceId;
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

    public ExperimentCatResource create(ResourceType type) throws RegistryException{
       switch (type){
           case EXPERIMENT_ERROR:
               ExperimentErrorResource errorResource = new ExperimentErrorResource();
               errorResource.setExperimentId(experimentId);
               return errorResource;
           case EXPERIMENT_STATUS:
               ExperimentStatusResource statusResource = new ExperimentStatusResource();
               statusResource.setExperimentId(experimentId);
               return statusResource;
           case EXPERIMENT_INPUT:
               ExperimentInputResource experimentInputResource = new ExperimentInputResource();
               experimentInputResource.setExperimentId(experimentId);
               return experimentInputResource;
           case EXPERIMENT_OUTPUT:
               ExperimentOutputResource outputResource = new ExperimentOutputResource();
               outputResource.setExperimentId(experimentId);
               return outputResource;
           case USER_CONFIGURATION_DATA:
               UserConfigurationDataResource configurationDataResource = new UserConfigurationDataResource();
               configurationDataResource.setExperimentId(experimentId);
               return configurationDataResource;
           case PROCESS:
               ProcessResource processResource = new ProcessResource();
               processResource.setExperimentId(experimentId);
               return processResource;
           default:
               logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
               throw new IllegalArgumentException("Unsupported resource type for experiment resource.");
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
                case EXPERIMENT_ERROR:
                    generator = new QueryGenerator(EXPERIMENT_ERROR);
                    generator.setParameter(ExperimentErrorConstants.ERROR_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case EXPERIMENT_STATUS:
                    generator = new QueryGenerator(EXPERIMENT_STATUS);
                    generator.setParameter(ExperimentStatusConstants.EXPERIMENT_ID, experimentId);
                    generator.setParameter(ExperimentStatusConstants.STATUS_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case EXPERIMENT_INPUT:
                    generator = new QueryGenerator(EXPERIMENT_INPUT);
                    generator.setParameter(ExperimentInputConstants.INPUT_NAME, name);
                    generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, experimentId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case EXPERIMENT_OUTPUT:
                    generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                    generator.setParameter(ExperimentOutputConstants.OUTPUT_NAME, name);
                    generator.setParameter(ExperimentOutputConstants.EXPERIMENT_ID, experimentId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case USER_CONFIGURATION_DATA:
                    generator = new QueryGenerator(USER_CONFIGURATION_DATA);
                    generator.setParameter(UserConfigurationDataConstants.EXPERIMENT_ID, experimentId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case PROCESS:
                    generator = new QueryGenerator(PROCESS);
                    generator.setParameter(ProcessConstants.PROCESS_ID, name);
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
                case EXPERIMENT_STATUS:
                    generator = new QueryGenerator(EXPERIMENT_STATUS);
                    generator.setParameter(ExperimentStatusConstants.STATUS_ID, name);
                    q = generator.selectQuery(em);
                    ExperimentStatus status = (ExperimentStatus) q.getSingleResult();
                    ExperimentStatusResource statusResource = (ExperimentStatusResource) Utils.getResource(ResourceType.EXPERIMENT_STATUS, status);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return statusResource;
                case EXPERIMENT_ERROR:
                    generator = new QueryGenerator(EXPERIMENT_ERROR);
                    generator.setParameter(ExperimentErrorConstants.ERROR_ID, name);
                    q = generator.selectQuery(em);
                    ExperimentError experimentError = (ExperimentError) q.getSingleResult();
                    ExperimentErrorResource processErrorResource = (ExperimentErrorResource) Utils.getResource(ResourceType.EXPERIMENT_ERROR, experimentError);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return processErrorResource;
                case EXPERIMENT_INPUT:
                    generator = new QueryGenerator(EXPERIMENT_INPUT);
                    generator.setParameter(ExperimentInputConstants.INPUT_NAME, name);
                    generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    ExperimentInput experimentInput = (ExperimentInput) q.getSingleResult();
                    ExperimentInputResource experimentInputResource = (ExperimentInputResource) Utils.getResource(ResourceType.EXPERIMENT_INPUT, experimentInput);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return experimentInputResource;
                case EXPERIMENT_OUTPUT:
                    generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                    generator.setParameter(ExperimentOutputConstants.OUTPUT_NAME, name);
                    generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    ExperimentOutput experimentOutput = (ExperimentOutput) q.getSingleResult();
                    ExperimentOutputResource outputResource = (ExperimentOutputResource) Utils.getResource(ResourceType.EXPERIMENT_OUTPUT, experimentOutput);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return outputResource;
                case USER_CONFIGURATION_DATA:
                    generator = new QueryGenerator(USER_CONFIGURATION_DATA);
                    generator.setParameter(UserConfigurationDataConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    UserConfigurationData configurationData = (UserConfigurationData) q.getSingleResult();
                    UserConfigurationDataResource configurationDataResource = (UserConfigurationDataResource)
                            Utils.getResource(ResourceType.USER_CONFIGURATION_DATA, configurationData);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return configurationDataResource;
                case PROCESS:
                    generator = new QueryGenerator(PROCESS);
                    generator.setParameter(ProcessConstants.PROCESS_ID, name);
                    q = generator.selectQuery(em);
                    Process process = (Process) q.getSingleResult();
                    ProcessResource processResource = (ProcessResource) Utils.getResource(ResourceType.PROCESS, process);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return processResource;
                default:
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for experiment resource.");
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
                case EXPERIMENT_INPUT:
                    generator = new QueryGenerator(EXPERIMENT_INPUT);
                    generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ExperimentInput experimentInput = (ExperimentInput) result;
                            ExperimentInputResource experimentInputResource =
                                    (ExperimentInputResource) Utils.getResource(ResourceType.EXPERIMENT_INPUT, experimentInput);
                            resourceList.add(experimentInputResource);
                        }
                    }
                    break;
                case EXPERIMENT_OUTPUT:
                    generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                    generator.setParameter(ExperimentOutputConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ExperimentOutput experimentOutput = (ExperimentOutput) result;
                            ExperimentOutputResource experimentOutputResource
                                    = (ExperimentOutputResource) Utils.getResource(ResourceType.EXPERIMENT_OUTPUT, experimentOutput);
                            resourceList.add(experimentOutputResource);
                        }
                    }
                    break;
                case PROCESS:
                    generator = new QueryGenerator(PROCESS);
                    generator.setParameter(ProcessConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Process process = (Process) result;
                            ProcessResource processResource =
                                    (ProcessResource) Utils.getResource(ResourceType.PROCESS, process);
                            resourceList.add(processResource);
                        }
                    }
                    break;
                case EXPERIMENT_ERROR:
                    generator = new QueryGenerator(EXPERIMENT_ERROR);
                    generator.setParameter(ExperimentErrorConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ExperimentError experimentError = (ExperimentError) result;
                            ExperimentErrorResource experimentErrorResource =
                                    (ExperimentErrorResource) Utils.getResource(ResourceType.EXPERIMENT_ERROR, experimentError);
                            resourceList.add(experimentErrorResource);
                        }
                    }
                    break;
                case EXPERIMENT_STATUS:
                    generator = new QueryGenerator(EXPERIMENT_STATUS);
                    generator.setParameter(ExperimentStatusConstants.EXPERIMENT_ID, experimentId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ExperimentStatus experimentStatus = (ExperimentStatus) result;
                            ExperimentStatusResource experimentStatusResource =
                                    (ExperimentStatusResource) Utils.getResource(ResourceType.EXPERIMENT_STATUS, experimentStatus);
                            resourceList.add(experimentStatusResource);
                        }
                    }
                    break;
                default:
                    logger.error("Unsupported resource type for experiment resource.", new UnsupportedOperationException());
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
            Experiment existingExp = em.find(Experiment.class, experimentId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            Experiment experiment;
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingExp == null) {
            	experiment = new Experiment();
            }else {
                experiment = existingExp;
            }
            experiment.setExperimentId(experimentId);
            experiment.setProjectId(projectId);
            experiment.setGatewayId(gatewayId);
            experiment.setExperimentType(experimentType);
            experiment.setUserName(userName);
            experiment.setExperimentName(experimentName);
            experiment.setCreationTime(creationTime);
            experiment.setDescription(description);
            experiment.setExecutionId(executionId);
            experiment.setGatewayInstanceId(gatewayInstanceId);
            experiment.setGatewayExecutionId(gatewayExecutionId);
            experiment.setEnableEmailNotification(enableEmailNotification);
            experiment.setEmailAddresses(emailAddresses);
            if (existingExp == null){
                em.persist(experiment);
            }else {
                em.merge(experiment);
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

    public List<ExperimentInputResource> getExperimentInputs() throws RegistryException{
        List<ExperimentInputResource> experimentInputResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.EXPERIMENT_INPUT);
        for (ExperimentCatResource resource : resources) {
            ExperimentInputResource inputResource = (ExperimentInputResource) resource;
            experimentInputResources.add(inputResource);
        }
        return experimentInputResources;
    }

    public List<ExperimentOutputResource> getExperimentOutputs() throws RegistryException{
        List<ExperimentOutputResource> outputResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.EXPERIMENT_OUTPUT);
        for (ExperimentCatResource resource : resources) {
            ExperimentOutputResource outputResource = (ExperimentOutputResource) resource;
            outputResources.add(outputResource);
        }
        return outputResources;
    }

    public List<ExperimentStatusResource> getExperimentStatuses() throws RegistryException{
        List<ExperimentStatusResource> experimentStatusResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.EXPERIMENT_STATUS);
        for (ExperimentCatResource resource : resources) {
            ExperimentStatusResource statusResource = (ExperimentStatusResource) resource;
            experimentStatusResources.add(statusResource);
        }
        return experimentStatusResources;
    }

    public ExperimentStatusResource getExperimentStatus() throws RegistryException{
        List<ExperimentStatusResource> experimentStatusResources = getExperimentStatuses();
        if(experimentStatusResources.size() == 0){
            return null;
        }else{
            ExperimentStatusResource max = experimentStatusResources.get(0);
            for(int i=1; i<experimentStatusResources.size();i++){
                Timestamp timeOfStateChange = experimentStatusResources.get(i).getTimeOfStateChange();
                if (timeOfStateChange != null) {
                    if (timeOfStateChange.after(max.getTimeOfStateChange())
                            || (timeOfStateChange.equals(max.getTimeOfStateChange()) && experimentStatusResources.get(i).getState().equals(ExperimentState.COMPLETED.toString()))
                            || (timeOfStateChange.equals(max.getTimeOfStateChange()) && experimentStatusResources.get(i).getState().equals(ExperimentState.FAILED.toString()))
                            || (timeOfStateChange.equals(max.getTimeOfStateChange()) && experimentStatusResources.get(i).getState().equals(ExperimentState.CANCELED.toString()))){
                        max = experimentStatusResources.get(i);
                    }
                }
            }
            return max;
        }
    }

    public List<ExperimentErrorResource> getExperimentErrors() throws RegistryException{
        List<ExperimentErrorResource> experimentErrorResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.EXPERIMENT_ERROR);
        for (ExperimentCatResource resource : resources) {
            ExperimentErrorResource errorResource = (ExperimentErrorResource) resource;
            experimentErrorResources.add(errorResource);
        }
        return experimentErrorResources;
    }

    public ExperimentErrorResource getExperimentError() throws RegistryException{
        List<ExperimentErrorResource> experimentErrorResources = getExperimentErrors();
        if(experimentErrorResources.size() == 0){
            return null;
        }else{
            ExperimentErrorResource max = experimentErrorResources.get(0);
            for(int i=1; i<experimentErrorResources.size();i++){
                if(experimentErrorResources.get(i).getCreationTime().after(max.getCreationTime())){
                    max = experimentErrorResources.get(i);
                }
            }
            return max;
        }
    }

    public UserConfigurationDataResource getUserConfigurationDataResource() throws RegistryException{
        ExperimentCatResource resource = get(ResourceType.USER_CONFIGURATION_DATA, experimentId);
        return (UserConfigurationDataResource)resource;
    }

    public List<ProcessResource> getProcessList() throws RegistryException{
        List<ProcessResource> processResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.PROCESS);
        for (ExperimentCatResource resource : resources) {
            ProcessResource processResource = (ProcessResource) resource;
            processResources.add(processResource);
        }
        return processResources;
    }

    public ProcessResource getProcess(String processID) throws RegistryException {
        ExperimentCatResource resource = get(ResourceType.PROCESS, processID);
        return (ProcessResource)resource;
    }
}
