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
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the project_user database table.
 */
@Entity
@Table(name="PROJECT_USER")
@IdClass(ProjectUserPK.class)
public class ProjectUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PROJECT_ID")
    private String projectID;

    @Id
    @Column(name = "USER_NAME")
    private String userName;

    public ProjectUserEntity() {
    }

    @ManyToOne(targetEntity = UserEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NAME", referencedColumnName = "USER_NAME")
    private UserEntity user;

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

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
