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
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import org.apache.airavata.model.application.io.DataType;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "HANDLER_OUTPUT")
@IdClass(HandlerOutputPK.class)
public class HandlerOutputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "HANDLER_ID")
    private String handlerId;

    @Id
    @Column(name = "NAME")
    private String name;

    @Lob
    @Column(name = "VALUE")
    private String value;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column(name = "APPLICATION_ARGUMENT")
    private String applicationArgument;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "REQUIRED_TO_ADDED_TO_COMMAND_LINE")
    private boolean requiredToAddedToCommandLine;

    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "SEARCH_QUERY")
    private String searchQuery;

    @Column(name = "OUTPUT_STREAMING")
    private boolean outputStreaming;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @ManyToOne(targetEntity = WorkflowHandlerEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "HANDLER_ID", referencedColumnName = "ID", nullable = false, updatable = false),
            @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "WORKFLOW_ID", nullable = false, updatable = false)
    })
    private WorkflowHandlerEntity handler;

    public HandlerOutputEntity() {
    }

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public void setHandler(WorkflowHandlerEntity handler) {
        this.handler = handler;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public DataType getType() {
        return type;
    }

    public String getApplicationArgument() {
        return applicationArgument;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public String getLocation() {
        return location;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public boolean isOutputStreaming() {
        return outputStreaming;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public WorkflowHandlerEntity getHandler() {
        return handler;
    }
}
