///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.persistance.registry.jpa.resources;
//
//import org.apache.airavata.persistance.registry.jpa.Resource;
//import org.apache.airavata.persistance.registry.jpa.ResourceType;
//import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
//import org.apache.airavata.persistance.registry.jpa.model.Experiment_Metadata;
//import org.apache.airavata.persistance.registry.jpa.model.Experiment_Summary;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.persistence.EntityManager;
//import java.sql.Timestamp;
//import java.util.List;
//
//public class ExperimentSummaryResource extends AbstractResource {
//    private static final Logger logger = LoggerFactory.getLogger(ExperimentSummaryResource.class);
//    private ExperimentMetadataResource experimentMetadataResource;
//    private String status;
//    private Timestamp lastUpdateTime;
//
//    public ExperimentMetadataResource getExperimentMetadataResource() {
//        return experimentMetadataResource;
//    }
//
//    public void setExperimentMetadataResource(ExperimentMetadataResource experimentMetadataResource) {
//        this.experimentMetadataResource = experimentMetadataResource;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public Timestamp getLastUpdateTime() {
//        return lastUpdateTime;
//    }
//
//    public void setLastUpdateTime(Timestamp lastUpdateTime) {
//        this.lastUpdateTime = lastUpdateTime;
//    }
//
//    public Resource create(ResourceType type) {
//        logger.error("Unsupported resource type for experiment summary data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public void remove(ResourceType type, Object name) {
//        logger.error("Unsupported resource type for experiment summary data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public Resource get(ResourceType type, Object name) {
//        logger.error("Unsupported resource type for experiment summary data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public List<Resource> get(ResourceType type) {
//        logger.error("Unsupported resource type for experiment summary data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public void save() {
//        EntityManager em = ResourceUtils.getEntityManager();
//        Experiment_Summary existingExSummary = em.find(Experiment_Summary.class, experimentMetadataResource.getExpID());
//        em.close();
//
//        em = ResourceUtils.getEntityManager();
//        em.getTransaction().begin();
//        Experiment_Summary exSummary = new Experiment_Summary();
//        exSummary.setLast_update_time(lastUpdateTime);
//        exSummary.setStatus(status);
//        Experiment_Metadata metadata = em.find(Experiment_Metadata.class, experimentMetadataResource.getExpID());
//        exSummary.setExperiment_metadata(metadata);
//        exSummary.setExperimentID(metadata.getExperiment_id());
//
//        if (existingExSummary != null){
//            existingExSummary.setLast_update_time(lastUpdateTime);
//            existingExSummary.setStatus(status);
//            existingExSummary.setExperiment_metadata(metadata);
//            existingExSummary.setExperimentID(metadata.getExperiment_id());
//            exSummary = em.merge(existingExSummary);
//        }  else {
//            em.persist(exSummary);
//        }
//        em.getTransaction().commit();
//        em.close();
//    }
//}
