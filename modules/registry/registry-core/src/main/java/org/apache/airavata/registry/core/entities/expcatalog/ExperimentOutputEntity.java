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

import org.apache.airavata.model.application.io.DataType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the experiment_output database table.
 */
@Entity
@Table(name = "EXPERIMENT_OUTPUT")
@IdClass(ExperimentOutputPK.class)
public class ExperimentOutputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Id
    @Column(name = "OUTPUT_NAME")
    private String name;

    @Lob
    @Column(name = "OUTPUT_VALUE")
    private String value;

    @Column(name = "DATA_TYPE")
    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column(name = "APPLICATION_ARGUMENT")
    private String applicationArgument;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "REQUIRED_TO_ADDED_TO_CMD")
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

    @Column(name = "METADATA", length = 4096)
    private String metaData;

    @ManyToOne(targetEntity = ExperimentEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    private ExperimentEntity experiment;

    public ExperimentOutputEntity() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }


    public boolean isRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    public boolean isDataMovement() {
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

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public boolean isOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }
}
