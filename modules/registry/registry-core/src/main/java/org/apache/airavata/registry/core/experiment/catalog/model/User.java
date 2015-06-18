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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "USER")
public class User {
    private final static Logger logger = LoggerFactory.getLogger(User.class);
    private String userName;
    private String password;
    private Collection<Experiment> experiments;
    private Collection<GatewayWorker> gatewayWorkers;
    private Collection<Project> projects;
    private Collection<ProjectUser> projectUsers;

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (userName != null ? !userName.equals(user.userName) : user.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "usersByUserName")
    public Collection<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(Collection<Experiment> experimentByUserName) {
        this.experiments = experimentByUserName;
    }

    @OneToMany(mappedBy = "users")
    public Collection<GatewayWorker> getGatewayWorkers() {
        return gatewayWorkers;
    }

    public void setGatewayWorkers(Collection<GatewayWorker> gatewayWorkerByUserName) {
        this.gatewayWorkers = gatewayWorkerByUserName;
    }

    @OneToMany(mappedBy = "user")
    public Collection<Project> getProjects() {
        return projects;
    }

    public void setProjects(Collection<Project> projectByUserName) {
        this.projects = projectByUserName;
    }

    @OneToMany(mappedBy = "user")
    public Collection<ProjectUser> getProjectUsers() {
        return projectUsers;
    }

    public void setProjectUsers(Collection<ProjectUser> projectUserByUserName) {
        this.projectUsers = projectUserByUserName;
    }
}