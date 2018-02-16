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

@Entity
@Table(name = "EXPCAT_PROCESS_INPUT")
@IdClass(ProcessInputPK.class)
public class ProcessInputEntity {
    private String processId;
    public String name;
    public String value;
    public String type;
    public String applicationArgument;
    public boolean standardInput;
    public String userFriendlyDescription;
    public String metaData;
    public int inputOrder;
    public boolean isRequired;
    public boolean requiredToAddedToCommandLine;
    public boolean dataStaged;
    public String storageResourceId;

    private ProcessEntity process;

    @Id
    @Column(name = "PROCESS_ID")
    public String getProceesId() {
        return processId;
    }

    public void setProceseId(String processId) {
        this.processId = processId;
    }

    @Id
    @Column(name = "INPUT_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "INPUT_VALUE")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "INPUT_TYPE")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "APPLICATION_ARGUMENT")
    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    @Column(name = "STANDARD_INPUT")
    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    @Column(name = "USER_FRIENDLY_DESCRIPTION")
    public String getUserFriendlyDescription() {
        return userFriendlyDescription;
    }

    public void setUserFriendlyDescription(String userFriendlyDescription) {
        this.userFriendlyDescription = userFriendlyDescription;
    }

    @Lob
    @Column(name = "METADATA")
    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    @Column(name = "INPUT_ORDER")
    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    @Column(name = "REQUIRED")
    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Column(name = "REQUIRED_TO_ADDED_TO_COMMANDLINE")
    public boolean isRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    @Column(name = "DATA_STAGED")
    public boolean isDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    @Column(name = "STORAGE_RESOURCE_ID")
    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @ManyToOne(targetEntity = ProcessEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}