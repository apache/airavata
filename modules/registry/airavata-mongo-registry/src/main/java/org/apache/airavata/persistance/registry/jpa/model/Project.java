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

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@DataCache
@Entity
@Table(name ="PROJECT")
public class Project implements Serializable {
    @Id
    @Column(name = "PROJECT_ID")
    private String project_id;

    @Column(name = "GATEWAY_ID")
    private String gateway_id;

    @Column(name = "PROJECT_NAME")
    private String project_name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "USER_NAME")
    private String user_name;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private Gateway gateway;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "USER_NAME")
    private Users users;


    public String getProject_name() {
        return project_name;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getProject_id() {
        return project_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getGateway_id() {
        return gateway_id;
    }

    public void setGateway_id(String gateway_id) {
        this.gateway_id = gateway_id;
    }
}

