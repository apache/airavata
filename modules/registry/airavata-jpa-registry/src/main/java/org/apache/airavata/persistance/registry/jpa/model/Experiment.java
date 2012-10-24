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
public class Experiment {
    @Id
    private String experiment_ID;
    private Timestamp submitted_date;
    private String user_name;
    private String gateway_name;
    private String project_name;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "user_name")
    private Users user;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "gateway_name")
    private Gateway gateway;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "project_name")
    private Project project;

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public Timestamp getSubmitted_date() {
        return submitted_date;
    }

    public Users getUser() {
        return user;
    }

    public Project getProject() {
        return project;
    }

    public void setExperiment_ID(String experiment_ID) {
        this.experiment_ID = experiment_ID;
    }

    public void setSubmitted_date(Timestamp submitted_date) {
        this.submitted_date = submitted_date;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }
}
