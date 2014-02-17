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
//import org.apache.airavata.persistance.registry.jpa.model.Experiment_Input;
//import org.apache.airavata.persistance.registry.jpa.model.Experiment_Input_PK;
//import org.apache.airavata.persistance.registry.jpa.model.Experiment_Metadata;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//public class ExperimentInputResource extends AbstractResource {
//    private static final Logger logger = LoggerFactory.getLogger(ExperimentInputResource.class);
//
//    private ExperimentMetadataResource experimentMetadataResource;
//    private String experimentKey;
//    private String value;
//
//    public ExperimentMetadataResource getExperimentMetadataResource() {
//        return experimentMetadataResource;
//    }
//
//    public void setExperimentMetadataResource(ExperimentMetadataResource experimentMetadataResource) {
//        this.experimentMetadataResource = experimentMetadataResource;
//    }
//
//    public String getExperimentKey() {
//        return experimentKey;
//    }
//
//    public void setExperimentKey(String experimentKey) {
//        this.experimentKey = experimentKey;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public Resource create(ResourceType type) {
//        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public void remove(ResourceType type, Object name) {
//        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public Resource get(ResourceType type, Object name) {
//        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public List<Resource> get(ResourceType type) {
//        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//    }
//
//    public void save() {
//        EntityManager em = ResourceUtils.getEntityManager();
//        Experiment_Input existingInput = em.find(Experiment_Input.class, new Experiment_Input_PK(experimentMetadataResource.getExpID(), experimentKey));
//        em.close();
//
//        em = ResourceUtils.getEntityManager();
//        em.getTransaction().begin();
//        Experiment_Input exInput = new Experiment_Input();
//        exInput.setEx_key(experimentKey);
//        Experiment_Metadata metadata = em.find(Experiment_Metadata.class, experimentMetadataResource.getExpID());
//        exInput.setExperiment_metadata(metadata);
//        exInput.setExperiment_id(metadata.getExperiment_id());
//        exInput.setValue(value);
//
//        if (existingInput != null){
//            existingInput.setEx_key(experimentKey);
//            existingInput.setExperiment_metadata(metadata);
//            existingInput.setExperiment_id(metadata.getExperiment_id());
//            existingInput.setValue(value);
//            exInput = em.merge(existingInput);
//        }else {
//            em.persist(exInput);
//        }
//        em.getTransaction().commit();
//        em.close();
//    }
//}
