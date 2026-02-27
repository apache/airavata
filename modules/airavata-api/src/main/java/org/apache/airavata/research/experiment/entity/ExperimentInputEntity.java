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
package org.apache.airavata.research.experiment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.storage.resource.model.DataType;

/**
 * Experiment input — either a parameter value or an artifact reference.
 *
 * <p>When {@code type} is {@code ARTIFACT}, the {@code artifactId} field references
 * a research artifact (dataset or repository). For all other types, {@code value}
 * holds the parameter value.
 */
@Entity
@Table(name = "experiment_input")
public class ExperimentInputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "input_id", nullable = false)
    private String inputId;

    @Column(name = "experiment_id", nullable = false, insertable = false, updatable = false)
    private String experimentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private DataType type;

    @Column(name = "artifact_id", length = 48)
    private String artifactId;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "command_line_arg")
    private String commandLineArg;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "add_to_command_line", nullable = false)
    private boolean addToCommandLine = true;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private ExperimentEntity experiment;

    public ExperimentInputEntity() {}

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
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

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCommandLineArg() {
        return commandLineArg;
    }

    public void setCommandLineArg(String commandLineArg) {
        this.commandLineArg = commandLineArg;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isAddToCommandLine() {
        return addToCommandLine;
    }

    public void setAddToCommandLine(boolean addToCommandLine) {
        this.addToCommandLine = addToCommandLine;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }
}
