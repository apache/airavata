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
package org.apache.airavata.registry.core.entities.workflowcatalog;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the edge database table.
 */
@Entity
public class EdgeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private EdgePK id;

    @Column(name = "COMPONENT_STATUS_ID")
    private String componentStatusId;

    @Column(name = "CREATED_TIME")
    private Timestamp createdTime;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TEMPLATE_ID")
    private String templateId;

    public EdgeEntity() {
    }

    public EdgePK getId() {
        return id;
    }

    public void setId(EdgePK id) {
        this.id = id;
    }

    public String getComponentStatusId() {
        return componentStatusId;
    }

    public void setComponentStatusId(String componentStatusId) {
        this.componentStatusId = componentStatusId;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}