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

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "PARSER")
public class ParserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_ID")
    private String id;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Column(name = "OUTPUT_DIR_PATH")
    private String outputDirPath;

    @Column(name = "INPUT_DIR_PATH")
    private String inputDirPath;

    @Column(name = "EXECUTION_COMMAND")
    private String executionCommand;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @OneToMany(targetEntity = ParserInputEntity.class, cascade = CascadeType.ALL, orphanRemoval = true,
            mappedBy = "parser", fetch = FetchType.EAGER)
    private List<ParserInputEntity> inputFiles;

    @OneToMany(targetEntity = ParserOutputEntity.class, cascade = CascadeType.ALL, orphanRemoval = true,
            mappedBy = "parser", fetch = FetchType.EAGER)
    private List<ParserOutputEntity> outputFiles;

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

    public List<ParserInputEntity> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(List<ParserInputEntity> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public List<ParserOutputEntity> getOutputFiles() {
        return outputFiles;
    }

    public void setOutputFiles(List<ParserOutputEntity> outputFiles) {
        this.outputFiles = outputFiles;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }
}
