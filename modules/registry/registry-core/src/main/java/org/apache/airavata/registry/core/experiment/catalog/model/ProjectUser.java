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

@Entity
@Table(name = "PROJECT_USER")
@IdClass(ProjectUserPK.class)
public class ProjectUser {
    private final static Logger logger = LoggerFactory.getLogger(ProjectUser.class);
    private String projectId;
    private String userName;
    private Users user;
    private Project project;

    @Id
    @Column(name = "PROJECT_ID")
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ProjectUser that = (ProjectUser) o;
//
//        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
//        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = projectId != null ? projectId.hashCode() : 0;
//        result = 31 * result + (userName != null ? userName.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "USER_NAME", referencedColumnName = "USER_NAME", nullable = false)
    public Users getUser() {
        return user;
    }

    public void setUser(Users userByUserName) {
        this.user = userByUserName;
    }

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "PROJECT_ID", nullable = false)
    public Project getProject() {
        return project;
    }

    public void setProject(Project projectByProjectId) {
        this.project = projectByProjectId;
    }
}