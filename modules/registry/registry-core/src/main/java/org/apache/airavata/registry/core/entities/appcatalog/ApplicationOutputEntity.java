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
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.airavata.model.application.io.DataType;
import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the application_output database table.
 */
@Entity
@Table(name = "APPLICATION_OUTPUT")
@IdClass(ApplicationOutputPK.class)
public class ApplicationOutputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="INTERFACE_ID")
    private String interfaceId;

    @Id
    @Column(name="OUTPUT_KEY")
    private String name;

    @Column(name = "APP_ARGUMENT")
    private String applicationArgument;

    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;

    @Column(name = "DATA_NAME_LOCATION")
    private String location;

    @Column(name = "DATA_TYPE")
    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "OUTPUT_STREAMING")
    private boolean outputStreaming;

    @Column(name = "OUTPUT_VALUE")
    private String value;

    @Column(name = "REQUIRED_TO_COMMANDLINE")
    private boolean requiredToAddedToCommandLine;

    @Column(name = "SEARCH_QUERY")
    private String searchQuery;

    @Column(name = "METADATA", length = 4096)
    private String metaData;

    @ManyToOne(targetEntity = ApplicationInterfaceEntity.class)
    @JoinColumn(name = "INTERFACE_ID", nullable = false, updatable = false)
    @ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
    private ApplicationInterfaceEntity applicationInterface;

    public ApplicationOutputEntity() {
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    public boolean getDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean getOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean getRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public ApplicationInterfaceEntity getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(ApplicationInterfaceEntity applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

}
