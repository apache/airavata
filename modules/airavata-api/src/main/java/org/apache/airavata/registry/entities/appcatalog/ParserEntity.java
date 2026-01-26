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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.IODirection;

/**
 * Parser entity representing a container-based output parser.
 *
 * <p>Uses unified {@link ParserIOEntity} for both inputs and outputs, distinguished
 * by the {@link IODirection} field.
 */
@Entity
@Table(name = "PARSER")
public class ParserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_ID")
    private String id;

    @Column(name = "IMAGE_NAME", nullable = false)
    private String imageName;

    @Column(name = "OUTPUT_DIR_PATH", nullable = false)
    private String outputDirPath;

    @Column(name = "INPUT_DIR_PATH", nullable = false)
    private String inputDirPath;

    @Column(name = "EXECUTION_COMMAND", nullable = false)
    private String executionCommand;

    @Column(name = "GATEWAY_ID", nullable = false)
    private String gatewayId;

    /**
     * All parser I/O entries (both inputs and outputs).
     */
    @OneToMany(
            targetEntity = ParserIOEntity.class,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "parser",
            fetch = FetchType.EAGER)
    private List<ParserIOEntity> ioEntries;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getOutputDirPath() {
        return outputDirPath;
    }

    public void setOutputDirPath(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }

    public String getInputDirPath() {
        return inputDirPath;
    }

    public void setInputDirPath(String inputDirPath) {
        this.inputDirPath = inputDirPath;
    }

    public String getExecutionCommand() {
        return executionCommand;
    }

    public void setExecutionCommand(String executionCommand) {
        this.executionCommand = executionCommand;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /**
     * Gets all I/O entries (both inputs and outputs).
     */
    public List<ParserIOEntity> getIoEntries() {
        return ioEntries;
    }

    public void setIoEntries(List<ParserIOEntity> ioEntries) {
        this.ioEntries = ioEntries;
    }

    /**
     * Gets only input entries.
     */
    @Transient
    public List<ParserIOEntity> getInputs() {
        if (ioEntries == null) {
            return List.of();
        }
        return ioEntries.stream()
                .filter(io -> io.getDirection() == IODirection.INPUT)
                .collect(Collectors.toList());
    }

    /**
     * Gets only output entries.
     */
    @Transient
    public List<ParserIOEntity> getOutputs() {
        if (ioEntries == null) {
            return List.of();
        }
        return ioEntries.stream()
                .filter(io -> io.getDirection() == IODirection.OUTPUT)
                .collect(Collectors.toList());
    }
}
