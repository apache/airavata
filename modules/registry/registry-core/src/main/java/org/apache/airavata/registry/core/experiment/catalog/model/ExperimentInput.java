/**
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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "EXPERIMENT_INPUT")
@IdClass(ExperimentInputPK.class)
public class ExperimentInput {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentInput.class);
    private String experimentId;
    private String inputName;
    private String inputValue;
    private String dataType;
    private String applicationArgument;
    private boolean standardInput;
    private String userFriendlyDescription;
    private String metadata;
    private Integer inputOrder;
    private boolean isRequired;
    private boolean requiredToAddedToCmd;
    private boolean dataStaged;
    private String storageResourceId;
    private boolean isReadOnly;
    private Experiment experiment;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Id
    @Column(name = "INPUT_NAME")
    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    @Lob
    @Column(name = "INPUT_VALUE")
    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    @Column(name = "DATA_TYPE")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Column(name = "APPLICATION_ARGUMENT")
    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    @Column(name = "STANDARD_INPUT")
    public boolean getStandardInput() {
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

    @Column(name = "METADATA")
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Column(name = "INPUT_ORDER")
    public Integer getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(Integer inputOrder) {
        this.inputOrder = inputOrder;
    }

    @Column(name = "IS_REQUIRED")
    public boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Column(name = "REQUIRED_TO_ADDED_TO_CMD")
    public boolean getRequiredToAddedToCmd() {
        return requiredToAddedToCmd;
    }

    public void setRequiredToAddedToCmd(boolean requiredToAddedToCmd) {
        this.requiredToAddedToCmd = requiredToAddedToCmd;
    }

    @Column(name = "STORAGE_RESOURCE_ID")
    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @Column(name = "DATA_STAGED")
    public boolean getDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    @Column(name = "IS_READ_ONLY")
    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ExperimentInput that = (ExperimentInput) o;
//
//        if (applicationArgument != null ? !applicationArgument.equals(that.applicationArgument) : that.applicationArgument != null)
//            return false;
//        if (dataStaged != null ? !dataStaged.equals(that.dataStaged) : that.dataStaged != null) return false;
//        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) return false;
//        if (experimentId != null ? !experimentId.equals(that.experimentId) : that.experimentId != null) return false;
//        if (inputName != null ? !inputName.equals(that.inputName) : that.inputName != null) return false;
//        if (inputOrder != null ? !inputOrder.equals(that.inputOrder) : that.inputOrder != null) return false;
//        if (inputValue != null ? !inputValue.equals(that.inputValue) : that.inputValue != null) return false;
//        if (isRequired != null ? !isRequired.equals(that.isRequired) : that.isRequired != null) return false;
//        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
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
//        int result = experimentId != null ? experimentId.hashCode() : 0;
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
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experimentByExperimentId) {
        this.experiment = experimentByExperimentId;
    }
}