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
package org.apache.airavata.agent.connection.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class JobBatchSpec {

    @JsonProperty("application_command")
    private String applicationCommand;

    @JsonProperty("parameter_grid")
    private Map<String, List<String>> parameterGrid;

    @JsonProperty("input_files")
    private List<String> inputFiles;

    public String getApplicationCommand() {
        return applicationCommand;
    }

    public void setApplicationCommand(String applicationCommand) {
        this.applicationCommand = applicationCommand;
    }

    public Map<String, List<String>> getParameterGrid() {
        return parameterGrid;
    }

    public void setParameterGrid(Map<String, List<String>> parameterGrid) {
        this.parameterGrid = parameterGrid;
    }

    public List<String> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(List<String> inputFiles) {
        this.inputFiles = inputFiles;
    }
}
