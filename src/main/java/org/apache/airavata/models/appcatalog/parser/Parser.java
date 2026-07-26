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
package org.apache.airavata.models.appcatalog.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.parser.proto.Parser}.
 */
public record Parser(
        String id,
        String imageName,
        String outputDirPath,
        String inputDirPath,
        String executionCommand,
        List<ParserInput> inputFiles,
        List<ParserOutput> outputFiles,
        String gatewayId) {

    public Parser {
        inputFiles = inputFiles == null ? List.of() : List.copyOf(inputFiles);
        outputFiles = outputFiles == null ? List.of() : List.copyOf(outputFiles);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String imageName = "";
        private String outputDirPath = "";
        private String inputDirPath = "";
        private String executionCommand = "";
        private List<ParserInput> inputFiles = new ArrayList<>();
        private List<ParserOutput> outputFiles = new ArrayList<>();
        private String gatewayId = "";

        private Builder() {}

        private Builder(Parser source) {
            this.id = source.id;
            this.imageName = source.imageName;
            this.outputDirPath = source.outputDirPath;
            this.inputDirPath = source.inputDirPath;
            this.executionCommand = source.executionCommand;
            this.inputFiles = new ArrayList<>(source.inputFiles);
            this.outputFiles = new ArrayList<>(source.outputFiles);
            this.gatewayId = source.gatewayId;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setImageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder setOutputDirPath(String outputDirPath) {
            this.outputDirPath = outputDirPath;
            return this;
        }

        public Builder setInputDirPath(String inputDirPath) {
            this.inputDirPath = inputDirPath;
            return this;
        }

        public Builder setExecutionCommand(String executionCommand) {
            this.executionCommand = executionCommand;
            return this;
        }

        public Builder addInputFiles(ParserInput value) {
            this.inputFiles.add(value);
            return this;
        }

        public Builder addAllInputFiles(Iterable<ParserInput> values) {
            values.forEach(this.inputFiles::add);
            return this;
        }

        public Builder clearInputFiles() {
            this.inputFiles.clear();
            return this;
        }

        public Builder addOutputFiles(ParserOutput value) {
            this.outputFiles.add(value);
            return this;
        }

        public Builder addAllOutputFiles(Iterable<ParserOutput> values) {
            values.forEach(this.outputFiles::add);
            return this;
        }

        public Builder clearOutputFiles() {
            this.outputFiles.clear();
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Parser build() {
            return new Parser(
                    id, imageName, outputDirPath, inputDirPath, executionCommand, inputFiles, outputFiles,
                    gatewayId);
        }
    }
}
