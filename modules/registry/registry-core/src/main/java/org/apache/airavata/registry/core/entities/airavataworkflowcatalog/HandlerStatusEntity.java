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
 */
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "HANDLER_STATUS")
@IdClass(HandlerStatusPK.class)
public class HandlerStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Id
    @Column(name = "HANDLER_ID")
    private String handlerId;

    @Column(name = "STATE")
    private String state;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @ManyToOne(targetEntity = WorkflowHandlerEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "HANDLER_ID", referencedColumnName = "ID", nullable = false, updatable = false),
            @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "WORKFLOW_ID", nullable = false, updatable = false)
    })
    private WorkflowHandlerEntity handler;

    public HandlerStatusEntity() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setHandler(WorkflowHandlerEntity handler) {
        this.handler = handler;
    }

    public String getId() {
        return id;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public String getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public WorkflowHandlerEntity getHandler() {
        return handler;
    }
}
