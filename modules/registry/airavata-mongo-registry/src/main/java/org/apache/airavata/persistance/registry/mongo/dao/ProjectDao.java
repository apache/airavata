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
import org.apache.airavata.model.workspace.Project;
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

public class ProjectDao {
    private final static Logger logger = LoggerFactory.getLogger(ProjectDao.class);

    private static final String PROJECTS_COLLECTION_NAME = "projects";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String PROJECT_ID = "project_id";
    private static final String PROJECT_NAME = "name";
    private static final String PROJECT_DESCRIPTION = "description";
    private static final String PROJECT_OWNER = "owner";
    private static final String PROJECT_CREATION_TIME = "creation_time";

    public ProjectDao(){
        collection = MongoUtil.getAiravataRegistry().getCollection(PROJECTS_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes(){
        collection.createIndex(new BasicDBObject(PROJECT_ID, 1), new BasicDBObject("unique", true));
        collection.createIndex(new BasicDBObject(PROJECT_NAME, 1));
        collection.createIndex(new BasicDBObject(PROJECT_OWNER, 1));
        collection.createIndex(new BasicDBObject(PROJECT_DESCRIPTION, 1));
        collection.createIndex(new BasicDBObject(PROJECT_CREATION_TIME, 1));
    }

    public List<Project> getAllProjects() throws RegistryException{
        List<Project> projectList = new ArrayList();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
            try {
                projectList.add((Project) modelConversionHelper.deserializeObject(
                        Project.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return projectList;
    }

    public void createProject(Project project) throws RegistryException{
        try {
            WriteResult result = collection.insert((DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(project)));
            logger.debug("No of inserted results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    /**
     * The following operation replaces the document with item equal to
     * the given project id. The newly replaced document will only
     * contain the the _id field and the fields in the replacement document.
     * @param project
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public void updateProject(Project project) throws RegistryException{
        try {
            DBObject query = BasicDBObjectBuilder.start().add(
                    PROJECT_ID, project.getProjectId()).get();
            WriteResult result = collection.update(query, (DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(project)));
            logger.debug("No of updated results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    public void deleteProject(Project project) throws RegistryException{
        DBObject query = BasicDBObjectBuilder.start().add(
                PROJECT_ID, project.getProjectId()).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed experiments " + result.getN());
    }

    public Project getProject(String projectId) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(PROJECT_ID, projectId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (Project)modelConversionHelper.deserializeObject(
                        Project.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }

    public List<Project> searchProjects(Map<String, String> filters, int limit,
        int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException{
        List<Project> projectList = new ArrayList();
        BasicDBObjectBuilder queryBuilder = BasicDBObjectBuilder.start();
        for (String field : filters.keySet()) {
//            if (field.equals(Constants.FieldConstants.ProjectConstants.PROJECT_NAME)){
//                fil.put(AbstractResource.ProjectConstants.PROJECT_NAME, filters.get(field));
//            }else if (field.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
//                fil.put(AbstractResource.ProjectConstants.USERNAME, filters.get(field));
//            }else if (field.equals(Constants.FieldConstants.ProjectConstants.DESCRIPTION)){
//                fil.put(AbstractResource.ProjectConstants.DESCRIPTION, filters.get(field));
//            }else if (field.equals(Constants.FieldConstants.ProjectConstants.GATEWAY_ID)){
//                fil.put(AbstractResource.ProjectConstants.GATEWAY_ID, filters.get(field));
//            }
        }

        //handling pagination and ordering. ordering is allowed only on PROJECT_CREATION_TIME
        DBCursor cursor;
        if(limit > 0 && offset >= 0) {
            if(orderByIdentifier != null && orderByIdentifier.equals(
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME)){
                if(resultOrderType.equals(ResultOrderType.ASC)) {
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(PROJECT_CREATION_TIME, 1))
                            .skip(offset).limit(limit);
                }else{
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(PROJECT_CREATION_TIME, -1))
                            .skip(offset).limit(limit);
                }
            }else {
                cursor = collection.find(queryBuilder.get()).skip(offset).limit(limit);
            }
        }else{
            if(resultOrderType != null && resultOrderType.equals(
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME)){
                if(resultOrderType.equals(ResultOrderType.ASC)) {
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(PROJECT_CREATION_TIME, 1));
                }else{
                    cursor = collection.find(queryBuilder.get()).sort(new BasicDBObject(PROJECT_CREATION_TIME, -1));
                }
            }else {
                cursor = collection.find(queryBuilder.get());
            }
        }
        for(DBObject document: cursor){
            try {
                projectList.add((Project) modelConversionHelper.deserializeObject(
                        Project.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return projectList;
    }
}