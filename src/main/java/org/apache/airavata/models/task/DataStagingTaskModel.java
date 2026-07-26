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
package org.apache.airavata.models.task;

import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.task.proto.DataStagingTaskModel}.
 */
public record DataStagingTaskModel(
        String source,
        String destination,
        DataStageType type,
        long transferStartTime,
        long transferEndTime,
        String transferRate,
        InputDataObjectType processInput,
        OutputDataObjectType processOutput) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String source = "";
        private String destination = "";
        private DataStageType type = DataStageType.DATA_STAGE_TYPE_UNKNOWN;
        private long transferStartTime;
        private long transferEndTime;
        private String transferRate = "";
        private InputDataObjectType processInput;
        private OutputDataObjectType processOutput;

        private Builder() {}

        private Builder(DataStagingTaskModel source) {
            this.source = source.source;
            this.destination = source.destination;
            this.type = source.type;
            this.transferStartTime = source.transferStartTime;
            this.transferEndTime = source.transferEndTime;
            this.transferRate = source.transferRate;
            this.processInput = source.processInput;
            this.processOutput = source.processOutput;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder setType(DataStageType type) {
            this.type = type;
            return this;
        }

        public Builder setTransferStartTime(long transferStartTime) {
            this.transferStartTime = transferStartTime;
            return this;
        }

        public Builder setTransferEndTime(long transferEndTime) {
            this.transferEndTime = transferEndTime;
            return this;
        }

        public Builder setTransferRate(String transferRate) {
            this.transferRate = transferRate;
            return this;
        }

        public Builder setProcessInput(InputDataObjectType processInput) {
            this.processInput = processInput;
            return this;
        }

        public Builder setProcessOutput(OutputDataObjectType processOutput) {
            this.processOutput = processOutput;
            return this;
        }

        public DataStagingTaskModel build() {
            return new DataStagingTaskModel(
                    source, destination, type, transferStartTime, transferEndTime, transferRate, processInput,
                    processOutput);
        }
    }
}
