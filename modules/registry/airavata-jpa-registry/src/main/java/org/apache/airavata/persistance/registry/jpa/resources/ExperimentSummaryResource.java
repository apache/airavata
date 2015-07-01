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
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.MethodNotFoundException;
import java.sql.Timestamp;
import java.util.List;

public class ExperimentSummaryResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentSummaryResource.class);

    private String executionUser;
    private String expID;
    private String projectID;
    private Timestamp creationTime;
    private String expName;
    private String description;
    private String applicationId;

    private StatusResource status;

    @Override
    public ExperimentResource create(ResourceType type) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public void remove(ResourceType type, Object name) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public ExperimentResource get(ResourceType type, Object name) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public List<Resource> get(ResourceType type) throws RegistryException {
        throw new MethodNotFoundException();
    }


    @Override
    public void save() throws RegistryException {
        throw new MethodNotFoundException();
    }

    public String getExecutionUser() {
        return executionUser;
    }

    public void setExecutionUser(String executionUser) {
        this.executionUser = executionUser;
    }

    public String getExpID() {
        return expID;
    }

    public void setExpID(String expID) {
        this.expID = expID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getExpName() {
        return expName;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public StatusResource getStatus() {
        return status;
    }

    public void setStatus(StatusResource status) {
        this.status = status;
    }
}