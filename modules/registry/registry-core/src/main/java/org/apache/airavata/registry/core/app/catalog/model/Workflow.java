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

package org.apache.airavata.registry.core.app.catalog.model;


import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "WORKFLOW")
public class Workflow implements Serializable {

    @Column(name = "WF_NAME")
    private String wfName;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Lob
    @Column(name = "GRAPH")
    private char[] graph;

    @Id
    @Column(name = "WF_TEMPLATE_ID")
    private String wfTemplateId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Lob
    @Column(name = "IMAGE")
    private byte[] image;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getWfName() {
        return wfName;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public char[] getGraph() {
        return graph;
    }

    public String getWfTemplateId() {
        return wfTemplateId;
    }

    public void setWfName(String wfName) {
        this.wfName=wfName;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser=createdUser;
    }

    public void setGraph(char[] graph) {
        this.graph=graph;
    }

    public void setWfTemplateId(String wfTemplateId) {
        this.wfTemplateId=wfTemplateId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}

