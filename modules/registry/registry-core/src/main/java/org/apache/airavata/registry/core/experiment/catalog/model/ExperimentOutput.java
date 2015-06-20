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

@Entity
@Table(name = "EXPERIMENT_OUTPUT")
@IdClass(ExperimentOutputPK.class)
public class ExperimentOutput {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentOutput.class);
    private String experimentId;
    private String outputName;
    private String outputValue;
    private String dataType;
    private String applicationArgument;
    private Boolean isRequired;
    private Boolean requiredToAddedToCmd;
    private Boolean dataMovement;
    private String location;
    private String searchQuery;
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
    @Column(name = "OUTPUT_NAME")
    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    @Basic
    @Column(name = "OUTPUT_VALUE")
    public String getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
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
    @Column(name = "DATA_MOVEMENT")
    public Boolean getDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(Boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    @Basic
    @Column(name = "LOCATION")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Basic
    @Column(name = "SEARCH_QUERY")
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentOutput that = (ExperimentOutput) o;
        if (outputName != null ? !outputName.equals(that.outputName) : that.outputName != null)
            return false;
        if (outputValue != null ? !outputValue.equals(that.outputValue) : that.outputValue != null)
            return false;
        if (applicationArgument != null ? !applicationArgument.equals(that.applicationArgument) : that.applicationArgument != null)
            return false;
        if (dataMovement != null ? !dataMovement.equals(that.dataMovement) : that.dataMovement != null) return false;
        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) return false;
        if (experimentId != null ? !experimentId.equals(that.experimentId) : that.experimentId != null) return false;
        if (isRequired != null ? !isRequired.equals(that.isRequired) : that.isRequired != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (requiredToAddedToCmd != null ? !requiredToAddedToCmd.equals(that.requiredToAddedToCmd) : that.requiredToAddedToCmd != null)
            return false;
        if (searchQuery != null ? !searchQuery.equals(that.searchQuery) : that.searchQuery != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = experimentId != null ? experimentId.hashCode() : 0;
        result = 31 * result + (outputName != null ? outputName.hashCode() : 0);
        result = 31 * result + (outputValue != null ? outputValue.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (applicationArgument != null ? applicationArgument.hashCode() : 0);
        result = 31 * result + (isRequired != null ? isRequired.hashCode() : 0);
        result = 31 * result + (requiredToAddedToCmd != null ? requiredToAddedToCmd.hashCode() : 0);
        result = 31 * result + (dataMovement != null ? dataMovement.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (searchQuery != null ? searchQuery.hashCode() : 0);
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experimentByExperimentId) {
        this.experiment = experimentByExperimentId;
    }
}