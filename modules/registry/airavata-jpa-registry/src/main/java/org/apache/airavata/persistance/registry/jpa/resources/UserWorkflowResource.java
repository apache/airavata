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
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import java.sql.Date;
import java.util.List;

public class UserWorkflowResource extends AbstractResource {
    private int projectID;
    private String userName;
    private String name;
    private Date lastUpdateDate;
    private String content;

    public UserWorkflowResource() {
    }

    public UserWorkflowResource(int projectID, String userName, String name) {
        this.projectID = projectID;
        this.userName = userName;
        this.name = name;
    }

    public int getProjectID() {
        return projectID;
    }

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public String getContent() {
        return content;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        User_Workflow userWorkflow = new User_Workflow();
        userWorkflow.setUser_workflow_name(name);
        userWorkflow.setLast_update_date(lastUpdateDate);
        userWorkflow.setWorkflow_content(content);
        Project project = new Project();
        project.setProject_ID(projectID);
        userWorkflow.setProject_ID(projectID);
        Users user = new Users();
        user.setUser_name(userName);
        userWorkflow.setUser_name(userName);
        em.persist(userWorkflow);
        end();
    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }
}
