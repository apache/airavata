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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.sql.Date;

@Entity
public class Experiment {
    @Id
    private String experiment_ID;
    private Date submitted_date;

    @ManyToOne
    @JoinColumn(name = "user_name")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "project_ID")
    private Project project;

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public Date getSubmitted_date() {
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

    public void setSubmitted_date(Date submitted_date) {
        this.submitted_date = submitted_date;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
