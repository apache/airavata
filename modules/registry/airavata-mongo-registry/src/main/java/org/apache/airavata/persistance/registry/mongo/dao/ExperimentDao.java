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
package org.apache.airavata.persistance.registry.mongo.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.persistance.registry.mongo.conversion.ModelConversionHelper;
import org.apache.airavata.persistance.registry.mongo.utils.MongoUtil;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExperimentDao{
    private final static Logger logger = LoggerFactory.getLogger(ExperimentDao.class);

    private static final String EXPERIMENTS_COLLECTION_NAME = "experiments";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String EXPERIMENT_ID = "experiment_id";
    private static final String EXPERIMENT_NAME= "name";
    private static final String EXPERIMENT_DESCRIPTION = "description";
    private static final String USER_NAME = "user_name";
    private static final String GATEWAY = "gateway_execution_id";
    private static final String APPLICATION_ID = "application_id";
    private static final String EXPERIMENT_STATUS_STATE = "experiment_status.experiment_state";
    private static final String CREATION_TIME = "creation_time";

    //Todo Nested Indexes - Its good if we can get rid of them
    private static final String WORKFLOW_NODE_ID = "workflow_node_details_list.node_instance_id";
    private static final String TASK_ID = "workflow_node_details_list.task_details_list.task_id";


    public ExperimentDao(){
        collection = MongoUtil.getAiravataRegistry().getCollection(EXPERIMENTS_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes(){
        collection.createIndex(new BasicDBObject(EXPERIMENT_ID, 1), new BasicDBObject("unique", true));
        collection.createIndex(new BasicDBObject(WORKFLOW_NODE_ID, 1));
        collection.createIndex(new BasicDBObject(TASK_ID, 1));

//        //Defining a full-text index on experiment name and experiment description
//        BasicDBObject object = new BasicDBObject();
//        object.put(EXPERIMENT_NAME, "text");
//        object.put(EXPERIMENT_DESCRIPTION, "text");
//        collection.createIndex (object);
    }

    public List<Experiment> getAllExperiments() throws RegistryException{
        List<Experiment> experimentList = new ArrayList<Experiment>();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
            try {
                experimentList.add((Experiment) modelConversionHelper.deserializeObject(
                        Experiment.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return experimentList;
    }

    public void createExperiment(Experiment experiment) throws RegistryException{
        try {
            WriteResult result = collection.insert((DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(experiment)));
            logger.debug("No of inserted results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    /**
     * The following operation replaces the document with item equal to
     * the given experiment id. The newly replaced document will only
     * contain the the _id field and the fields in the replacement document.
     * @param experiment
     * @throws RegistryException
     */
    public void updateExperiment(Experiment experiment) throws RegistryException{
        try {
            DBObject query = BasicDBObjectBuilder.start().add(
                    EXPERIMENT_ID, experiment.getExperimentId()).get();
            WriteResult result = collection.update(query, (DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(experiment)));
            logger.debug("No of updated results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    public void deleteExperiment(Experiment experiment) throws RegistryException{
        DBObject query = BasicDBObjectBuilder.start().add(
                EXPERIMENT_ID, experiment.getExperimentId()).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed experiments " + result.getN());
    }


    public Experiment getExperiment(String experimentId) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(EXPERIMENT_ID, experimentId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (Experiment)modelConversionHelper.deserializeObject(
                        Experiment.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }

    public List<Experiment> searchExperiments(Map<String, String> filters, int limit,
        int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException{
        List<Experiment> experimentList = new ArrayList<Experiment>();
        BasicDBObjectBuilder queryBuilder = BasicDBObjectBuilder.start();
        for (String field : filters.keySet()) {
            if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                queryBuilder.add(EXPERIMENT_NAME, new BasicDBObject(
                        "$regex", ".*" + filters.get(field) + ".*"));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                queryBuilder.add(USER_NAME, filters.get(field));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
                queryBuilder.add(GATEWAY, filters.get(field));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
                queryBuilder.add(EXPERIMENT_DESCRIPTION, new BasicDBObject(
                        "$regex", ".*" + filters.get(field) + ".*"));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
                queryBuilder.add(APPLICATION_ID, filters.get(field));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
                queryBuilder.add(EXPERIMENT_STATUS_STATE, filters.get(field));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.FROM_DATE)) {
                queryBuilder.add(CREATION_TIME,new BasicDBObject("$gte",filters.get(field)));
            } else if (field.equals(Constants.FieldConstants.ExperimentConstants.TO_DATE)) {
                queryBuilder.add(CREATION_TIME,new BasicDBObject("$lte",filters.get(field)));
            }
        }

        //handling pagination and ordering. ordering is allowed only on CREATION_TIME
        DBCursor cursor;
        if(limit > 0 && offset >= 0) {
            if(orderByIdentifier != null && orderByIdentifier.equals(
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME)){
                if(resultOrderType.equals(ResultOrderType.ASC)) {
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(CREATION_TIME, 1))
                            .skip(offset).limit(limit);
                }else{
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(CREATION_TIME, -1))
                            .skip(offset).limit(limit);
                }
            }else {
                cursor = collection.find(queryBuilder.get()).skip(offset).limit(limit);
            }
        }else{
            if(resultOrderType != null && resultOrderType.equals(
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME)){
                if(resultOrderType.equals(ResultOrderType.ASC)) {
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(CREATION_TIME, 1));
                }else{
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(CREATION_TIME, -1));
                }
            }else {
                cursor = collection.find(queryBuilder.get());
            }
        }
        for(DBObject document: cursor){
            try {
                experimentList.add((Experiment) modelConversionHelper.deserializeObject(
                        Experiment.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return experimentList;
    }

    public void createWFNode(String experimentId, WorkflowNodeDetails workflowNodeDetail) throws RegistryException{
        Experiment experiment = getExperiment(experimentId);
        experiment.getWorkflowNodeDetailsList().add(workflowNodeDetail);
        updateExperiment(experiment);
    }

    public void updateWFNode(WorkflowNodeDetails workflowNodeDetail) throws RegistryException{
        Experiment experiment = getParentExperimentOfWFNode(workflowNodeDetail.getNodeInstanceId());
        for(WorkflowNodeDetails wfnd: experiment.getWorkflowNodeDetailsList()){
            if(wfnd.getNodeInstanceId().equals(workflowNodeDetail.getNodeInstanceId())){
                experiment.getWorkflowNodeDetailsList().remove(wfnd);
                experiment.getWorkflowNodeDetailsList().add(workflowNodeDetail);
                updateExperiment(experiment);
                return;
            }
        }
    }

    public void deleteWFNode(WorkflowNodeDetails workflowNodeDetail) throws RegistryException{
        Experiment experiment = getParentExperimentOfWFNode(workflowNodeDetail.getNodeInstanceId());
        for(WorkflowNodeDetails wfnd: experiment.getWorkflowNodeDetailsList()){
            if(wfnd.getNodeInstanceId().equals(workflowNodeDetail.getNodeInstanceId())){
                experiment.getWorkflowNodeDetailsList().remove(wfnd);
                updateExperiment(experiment);
                return;
            }
        }
    }

    public WorkflowNodeDetails getWFNode(String nodeId) throws RegistryException{
        Experiment experiment = getParentExperimentOfWFNode(nodeId);
        if(experiment != null) {
            for (WorkflowNodeDetails wfnd : experiment.getWorkflowNodeDetailsList()) {
                if (wfnd.getNodeInstanceId().equals(nodeId)) {
                    return wfnd;
                }
            }
        }
        return null;
    }

    public void createTaskDetail(String nodeId, TaskDetails taskDetail) throws RegistryException{
        Experiment experiment = getParentExperimentOfWFNode(nodeId);
        for(WorkflowNodeDetails wfnd: experiment.getWorkflowNodeDetailsList()){
            if(wfnd.getNodeInstanceId().equals(nodeId)){
                wfnd.getTaskDetailsList().add(taskDetail);
                updateExperiment(experiment);
                return;
            }
        }
    }

    public void updateTaskDetail(TaskDetails taskDetail) throws RegistryException{
        Experiment experiment = getParentExperimentOfTask(taskDetail.getTaskId());
        for(WorkflowNodeDetails wfnd: experiment.getWorkflowNodeDetailsList()){
            for(TaskDetails taskDetails: wfnd.getTaskDetailsList()){
                if(taskDetails.getTaskId().equals(taskDetail)){
                    wfnd.getTaskDetailsList().remove(taskDetail);
                    wfnd.getTaskDetailsList().add(taskDetail);
                    updateExperiment(experiment);
                    return;
                }
            }
        }
    }

    public void deleteTaskDetail(TaskDetails taskDetail) throws RegistryException{
        Experiment experiment = getParentExperimentOfTask(taskDetail.getTaskId());
        for(WorkflowNodeDetails wfnd: experiment.getWorkflowNodeDetailsList()){
            for(TaskDetails taskDetails: wfnd.getTaskDetailsList()){
                if(taskDetails.getTaskId().equals(taskDetail)){
                    wfnd.getTaskDetailsList().remove(taskDetail);
                    updateExperiment(experiment);
                    return;
                }
            }
        }
    }

    public TaskDetails getTaskDetail(String taskId) throws RegistryException{
        Experiment experiment = getParentExperimentOfTask(taskId);
        if(experiment != null) {
            for (WorkflowNodeDetails wfnd : experiment.getWorkflowNodeDetailsList()) {
                for (TaskDetails taskDetails : wfnd.getTaskDetailsList()) {
                    if (taskDetails.getTaskId().equals(taskId)) {
                        return taskDetails;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Method to get parent Experiment of the given workflow node instance id
     * @param nodeInstanceId
     * @return
     * @throws RegistryException
     */
    public Experiment getParentExperimentOfWFNode(String nodeInstanceId) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(WORKFLOW_NODE_ID, nodeInstanceId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (Experiment)modelConversionHelper.deserializeObject(
                        Experiment.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }

    /**
     * Method to get the parent experiment of the given task id
     * @param taskId
     * @return
     * @throws RegistryException
     */
    public Experiment getParentExperimentOfTask(String taskId) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(TASK_ID, taskId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (Experiment)modelConversionHelper.deserializeObject(
                        Experiment.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }

    /**
     * Method to get the parent workflow node of the given task id
     * @param taskId
     * @return
     * @throws RegistryException
     */
    public WorkflowNodeDetails getParentWFNodeOfTask(String taskId) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(TASK_ID, taskId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                Experiment experiment = (Experiment)modelConversionHelper.deserializeObject(
                        Experiment.class, json);
                for(WorkflowNodeDetails wfnd: experiment.getWorkflowNodeDetailsList()){
                    for(TaskDetails taskDetails: wfnd.getTaskDetailsList()){
                        if(taskDetails.getTaskId().equals(taskId)){
                            return wfnd;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }
}