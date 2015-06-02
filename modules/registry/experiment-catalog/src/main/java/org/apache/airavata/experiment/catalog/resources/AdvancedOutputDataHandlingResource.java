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

package org.apache.airavata.experiment.catalog.resources;

import org.apache.airavata.experiment.catalog.Resource;
import org.apache.airavata.experiment.catalog.ResourceType;
import org.apache.airavata.experiment.catalog.ResourceUtils;
import org.apache.airavata.experiment.catalog.model.AdvancedOutputDataHandling;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class AdvancedOutputDataHandlingResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedOutputDataHandlingResource.class);
    private int outputDataHandlingId = 0;
    private  String outputDataDir;
    private String dataRegUrl;
    private boolean persistOutputData;
    private String experimentId;
    private String taskId;

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getOutputDataHandlingId() {
        return outputDataHandlingId;
    }

    public void setOutputDataHandlingId(int outputDataHandlingId) {
        this.outputDataHandlingId = outputDataHandlingId;
    }

    public String getOutputDataDir() {
        return outputDataDir;
    }

    public void setOutputDataDir(String outputDataDir) {
        this.outputDataDir = outputDataDir;
    }

    public String getDataRegUrl() {
        return dataRegUrl;
    }

    public void setDataRegUrl(String dataRegUrl) {
        this.dataRegUrl = dataRegUrl;
    }

    public boolean isPersistOutputData() {
        return persistOutputData;
    }

    public void setPersistOutputData(boolean persistOutputData) {
        this.persistOutputData = persistOutputData;
    }


    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for output data handling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }


    public void remove(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for output data handling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }


    public Resource get(ResourceType type, Object name) throws RegistryException  {
        logger.error("Unsupported resource type for output data handling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }


    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for output data handling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }


    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            AdvancedOutputDataHandling dataHandling;
            if (outputDataHandlingId != 0 ){
                dataHandling = em.find(AdvancedOutputDataHandling.class, outputDataHandlingId);
                dataHandling.setOutputDataHandlingId(outputDataHandlingId);
            }else {
                dataHandling = new AdvancedOutputDataHandling();
            }
            dataHandling.setDataRegUrl(dataRegUrl);
            dataHandling.setOutputDataDir(outputDataDir);
            dataHandling.setPersistOutputData(persistOutputData);
            dataHandling.setExpId(experimentId);
            dataHandling.setTaskId(taskId);
            em.persist(dataHandling);
            outputDataHandlingId = dataHandling.getOutputDataHandlingId();
            em.getTransaction().commit();
            em.close();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        }finally {
            if (em != null && em.isOpen()){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
