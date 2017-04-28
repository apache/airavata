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

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class ProcessOutputPK implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(ProcessOutputPK.class);
    private String processId;
    private String outputName;

    @Id
    @Column(name = "PROCESS_ID")
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Id
    @Column(name = "OUTPUT_NAME")
    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessOutputPK that = (ProcessOutputPK) o;

        if (getOutputName() != null ? !getOutputName().equals(that.getOutputName()) : that.getOutputName() != null) return false;
        if (getProcessId() != null ? !getProcessId().equals(that.getProcessId()) : that.getProcessId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getProcessId() != null ? getProcessId().hashCode() : 0;
        result = 31 * result + (getOutputName() != null ? getOutputName().hashCode() : 0);
        return result;
    }
}