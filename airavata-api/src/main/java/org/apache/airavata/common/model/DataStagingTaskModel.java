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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: DataStagingTaskModel
 */
public class DataStagingTaskModel {
    private String source;
    private String destination;
    private DataStageType type;
    private long transferStartTime;
    private long transferEndTime;
    private String transferRate;
    private InputDataObjectType processInput;
    private OutputDataObjectType processOutput;

    public DataStagingTaskModel() {}

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public DataStageType getType() {
        return type;
    }

    public void setType(DataStageType type) {
        this.type = type;
    }

    public long getTransferStartTime() {
        return transferStartTime;
    }

    public void setTransferStartTime(long transferStartTime) {
        this.transferStartTime = transferStartTime;
    }

    public long getTransferEndTime() {
        return transferEndTime;
    }

    public void setTransferEndTime(long transferEndTime) {
        this.transferEndTime = transferEndTime;
    }

    public String getTransferRate() {
        return transferRate;
    }

    public void setTransferRate(String transferRate) {
        this.transferRate = transferRate;
    }

    public InputDataObjectType getProcessInput() {
        return processInput;
    }

    public void setProcessInput(InputDataObjectType processInput) {
        this.processInput = processInput;
    }

    public OutputDataObjectType getProcessOutput() {
        return processOutput;
    }

    public void setProcessOutput(OutputDataObjectType processOutput) {
        this.processOutput = processOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataStagingTaskModel that = (DataStagingTaskModel) o;
        return Objects.equals(source, that.source)
                && Objects.equals(destination, that.destination)
                && Objects.equals(type, that.type)
                && Objects.equals(transferStartTime, that.transferStartTime)
                && Objects.equals(transferEndTime, that.transferEndTime)
                && Objects.equals(transferRate, that.transferRate)
                && Objects.equals(processInput, that.processInput)
                && Objects.equals(processOutput, that.processOutput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                source,
                destination,
                type,
                transferStartTime,
                transferEndTime,
                transferRate,
                processInput,
                processOutput);
    }

    @Override
    public String toString() {
        return "DataStagingTaskModel{" + "source=" + source + ", destination=" + destination + ", type=" + type
                + ", transferStartTime=" + transferStartTime + ", transferEndTime=" + transferEndTime
                + ", transferRate=" + transferRate + ", processInput=" + processInput + ", processOutput="
                + processOutput + "}";
    }
}
