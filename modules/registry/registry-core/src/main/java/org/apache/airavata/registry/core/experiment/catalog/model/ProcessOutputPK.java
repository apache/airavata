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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class ProcessOutputPK implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(ProcessOutputPK.class);
    private String processId;
    private String outputName;

    @Column(name = "PROCESS_ID")
    @Basic
    @Id
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Column(name = "OUTPUT_NAME")
    @Id
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

        if (outputName != null ? !outputName.equals(that.outputName) : that.outputName != null) return false;
        if (processId != null ? !processId.equals(that.processId) : that.processId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = processId != null ? processId.hashCode() : 0;
        result = 31 * result + (outputName != null ? outputName.hashCode() : 0);
        return result;
    }
}