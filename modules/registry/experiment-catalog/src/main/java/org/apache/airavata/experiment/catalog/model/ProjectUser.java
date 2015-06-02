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

package org.apache.airavata.experiment.catalog.model;


import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;

@DataCache
@Entity
@IdClass(ProjectUser_PK.class)
@Table(name = "PROJECT_USER")
public class ProjectUser implements Serializable {
    @Id
    @Column(name = "PROJECT_ID")
    private String projectID;
    @Id
    @Column(name = "USER_NAME")
    private String userName;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "USER_NAME")
    private Users user;

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
