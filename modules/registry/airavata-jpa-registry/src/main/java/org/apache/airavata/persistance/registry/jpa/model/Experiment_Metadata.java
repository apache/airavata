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

package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name ="EXPERIMENT_METADATA")
public class Experiment_Metadata {
    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experiment_id;
    @Column(name = "EXPERIMENT_NAME")
    private String experiment_name;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SUBMITTED_DATE")
    private Timestamp submitted_date;
    @Column(name = "EXECUTION_USER" )
    private String execution_user;
    @Column(name = "GATEWAY_NAME")
    private String gateway_name;
    @Column(name = "PROJECT_NAME")
    private String project_name;


//    @ManyToOne(cascade= CascadeType.MERGE)
//    @JoinColumn(name = "EXECUTION_USER", referencedColumnName = "USER_NAME" )
//    private Users user;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_NAME")
    private Gateway gateway;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "PROJECT_NAME")
    private Project project;

    private boolean share_experiment;

    public String getExecution_user() {
        return execution_user;
    }

    public void setExecution_user(String execution_user) {
        this.execution_user = execution_user;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getExperiment_id() {
        return experiment_id;
    }

    public void setExperiment_id(String experiment_id) {
        this.experiment_id = experiment_id;
    }

    public String getExperiment_name() {
        return experiment_name;
    }

    public void setExperiment_name(String experiment_name) {
        this.experiment_name = experiment_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getSubmitted_date() {
        return submitted_date;
    }

    public void setSubmitted_date(Timestamp submitted_date) {
        this.submitted_date = submitted_date;
    }

//    public Users getUser() {
//        return user;
//    }
//
//    public void setUser(Users user) {
//        this.user = user;
//    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isShare_experiment() {
        return share_experiment;
    }

    public void setShare_experiment(boolean share_experiment) {
        this.share_experiment = share_experiment;
    }

}
