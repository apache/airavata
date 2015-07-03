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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.*;

@Entity
@Table(name = "PROCESS_INPUT")
@IdClass(ProcessInputPK.class)
public class ProcessInput {
    private final static Logger logger = LoggerFactory.getLogger(ProcessInput.class);
    private String processId;
    private String inputName;
    private String inputValue;
    private String dataType;
    private String applicationArgument;
    private Boolean standardInput;
    private String userFriendlyDescription;
    private String metadata;
    private Integer inputOrder;
    private Boolean isRequired;
    private Boolean requiredToAddedToCmd;
    private Boolean dataStaged;
    private Process process;

    @Id
    @Column(name = "PROCESS_ID")
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Id
    @Column(name = "INPUT_NAME")
    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    @Basic
    @Lob
    @Column(name = "INPUT_VALUE")
    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    @Basic
    @Column(name = "DATA_TYPE")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Basic
    @Column(name = "APPLICATION_ARGUMENT")
    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    @Basic
    @Column(name = "STANDARD_INPUT")
    public Boolean getStandardInput() {
        return standardInput;
    }

    public void setStandardInput(Boolean standardInput) {
        this.standardInput = standardInput;
    }

    @Basic
    @Column(name = "USER_FRIENDLY_DESCRIPTION")
    public String getUserFriendlyDescription() {
        return userFriendlyDescription;
    }

    public void setUserFriendlyDescription(String userFriendlyDescription) {
        this.userFriendlyDescription = userFriendlyDescription;
    }

    @Basic
    @Column(name = "METADATA")
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Basic
    @Column(name = "INPUT_ORDER")
    public Integer getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(Integer inputOrder) {
        this.inputOrder = inputOrder;
    }

    @Basic
    @Column(name = "IS_REQUIRED")
    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Basic
    @Column(name = "REQUIRED_TO_ADDED_TO_CMD")
    public Boolean getRequiredToAddedToCmd() {
        return requiredToAddedToCmd;
    }

    public void setRequiredToAddedToCmd(Boolean requiredToAddedToCmd) {
        this.requiredToAddedToCmd = requiredToAddedToCmd;
    }

    @Basic
    @Column(name = "DATA_STAGED")
    public Boolean getDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(Boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ProcessInput that = (ProcessInput) o;
//
//        if (applicationArgument != null ? !applicationArgument.equals(that.applicationArgument) : that.applicationArgument != null)
//            return false;
//        if (dataStaged != null ? !dataStaged.equals(that.dataStaged) : that.dataStaged != null) return false;
//        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) return false;
//        if (inputName != null ? !inputName.equals(that.inputName) : that.inputName != null) return false;
//        if (inputOrder != null ? !inputOrder.equals(that.inputOrder) : that.inputOrder != null) return false;
//        if (inputValue != null ? !inputValue.equals(that.inputValue) : that.inputValue != null) return false;
//        if (isRequired != null ? !isRequired.equals(that.isRequired) : that.isRequired != null) return false;
//        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
//        if (processId != null ? !processId.equals(that.processId) : that.processId != null) return false;
//        if (requiredToAddedToCmd != null ? !requiredToAddedToCmd.equals(that.requiredToAddedToCmd) : that.requiredToAddedToCmd != null)
//            return false;
//        if (standardInput != null ? !standardInput.equals(that.standardInput) : that.standardInput != null)
//            return false;
//        if (userFriendlyDescription != null ? !userFriendlyDescription.equals(that.userFriendlyDescription) : that.userFriendlyDescription != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = processId != null ? processId.hashCode() : 0;
//        result = 31 * result + (inputName != null ? inputName.hashCode() : 0);
//        result = 31 * result + (inputValue != null ? inputValue.hashCode() : 0);
//        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
//        result = 31 * result + (applicationArgument != null ? applicationArgument.hashCode() : 0);
//        result = 31 * result + (standardInput != null ? standardInput.hashCode() : 0);
//        result = 31 * result + (userFriendlyDescription != null ? userFriendlyDescription.hashCode() : 0);
//        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
//        result = 31 * result + (inputOrder != null ? inputOrder.hashCode() : 0);
//        result = 31 * result + (isRequired != null ? isRequired.hashCode() : 0);
//        result = 31 * result + (requiredToAddedToCmd != null ? requiredToAddedToCmd.hashCode() : 0);
//        result = 31 * result + (dataStaged != null ? dataStaged.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process processByProcessId) {
        this.process = processByProcessId;
    }
}