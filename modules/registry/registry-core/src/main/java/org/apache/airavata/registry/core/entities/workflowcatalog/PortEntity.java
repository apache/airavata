/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.core.entities.workflowcatalog;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.model.ComponentStatus;

/**
 * The persistent class for the port database table.
 */
@Entity
@Table(name = "PORT")
@IdClass(PortPK.class)
public class PortEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PORT_ID")
    private String portId;

    @Id
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "COMPONENT_STATUS_ID")
    private ComponentStatus status;

    @Column(name = "CREATED_TIME")
    private Timestamp createdTime;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME")
    private String name;

    public PortEntity() {}

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public ComponentStatus getComponentStatusId() {
        return status;
    }

    public void setComponentStatusId(ComponentStatus status) {
        this.status = status;
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
