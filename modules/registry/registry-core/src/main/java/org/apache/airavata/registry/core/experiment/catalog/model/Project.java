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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "PROJECT")
public class Project {
    private final static Logger logger = LoggerFactory.getLogger(Project.class);
    private String gatewayId;
    private String userName;
    private String projectName;
    private String projectId;
    private String description;
    private Timestamp creationTime;
    private Collection<Experiment> experiments;
    private Gateway gateway;
    private Collection<ProjectUser> projectUsers;

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String ownerName) {
        this.userName = ownerName;
    }

    @Column(name = "PROJECT_NAME")
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Id
    @Column(name = "PROJECT_ID")
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Project project = (Project) o;
//
//        if (creationTime != null ? !creationTime.equals(project.creationTime) : project.creationTime != null)
//            return false;
//        if (description != null ? !description.equals(project.description) : project.description != null)
//            return false;
//        if (gatewayId != null ? !gatewayId.equals(project.gatewayId) : project.gatewayId != null) return false;
//        if (userName != null ? !userName.equals(project.userName) : project.userName != null) return false;
//        if (projectId != null ? !projectId.equals(project.projectId) : project.projectId != null) return false;
//        if (projectName != null ? !projectName.equals(project.projectName) : project.projectName != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = gatewayId != null ? gatewayId.hashCode() : 0;
//        result = 31 * result + (userName != null ? userName.hashCode() : 0);
//        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
//        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
//        result = 31 * result + (description != null ? description.hashCode() : 0);
//        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
//        return result;
//    }

    @OneToMany(mappedBy = "project")
    public Collection<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(Collection<Experiment> experimentByProjectId) {
        this.experiments = experimentByProjectId;
    }

    @ManyToOne
    @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID")
    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gatewayByGatewayId) {
        this.gateway = gatewayByGatewayId;
    }

    @OneToMany(mappedBy = "project")
    public Collection<ProjectUser> getProjectUsers() {
        return projectUsers;
    }

    public void setProjectUsers(Collection<ProjectUser> projectUsersByProjectId) {
        this.projectUsers = projectUsersByProjectId;
    }
}