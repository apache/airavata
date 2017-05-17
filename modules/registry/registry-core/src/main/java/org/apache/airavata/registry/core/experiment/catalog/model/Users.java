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
@Table(name = "USERS")
@IdClass(UserPK.class)
public class Users {
    private final static Logger logger = LoggerFactory.getLogger(Users.class);
    private String airavataInternalUserId;
    private String userName;
    private String password;
    private String gatewayId;
//    private Collection<Experiment> experiments;
//    private Collection<GatewayWorker> gatewayWorkers;
//    private Collection<Project> projects;
//    private Collection<ProjectUser> projectUsers;
    private Gateway gateway;

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Id
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "AIRAVATA_INTERNAL_USER_ID")
    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID")
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST})
    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        User user = (User) o;
//
//        if (password != null ? !password.equals(user.password) : user.password != null) return false;
//        if (userName != null ? !userName.equals(user.userName) : user.userName != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = userName != null ? userName.hashCode() : 0;
//        result = 31 * result + (password != null ? password.hashCode() : 0);
//        return result;
//    }

//    @OneToMany(mappedBy = "userName")
//    public Collection<Experiment> getExperiments() {
//        return experiments;
//    }
//
//    public void setExperiments(Collection<Experiment> experimentByUserName) {
//        this.experiments = experimentByUserName;
//    }

//    @OneToMany(mappedBy = "userName")
//    public Collection<GatewayWorker> getGatewayWorkers() {
//        return gatewayWorkers;
//    }
//
//    public void setGatewayWorkers(Collection<GatewayWorker> gatewayWorkerByUserName) {
//        this.gatewayWorkers = gatewayWorkerByUserName;
//    }
//
//    @OneToMany(mappedBy = "userName")
//    public Collection<Project> getProjects() {
//        return projects;
//    }
//
//    public void setProjects(Collection<Project> projectByUserName) {
//        this.projects = projectByUserName;
//    }
//
//    @OneToMany(mappedBy = "userName")
//    public Collection<ProjectUser> getProjectUsers() {
//        return projectUsers;
//    }
//
//    public void setProjectUsers(Collection<ProjectUser> projectUserByUserName) {
//        this.projectUsers = projectUserByUserName;
//    }
}