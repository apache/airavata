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
package org.apache.airavata.registry.entities.expcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.io.Serializable;

public class ProcessWorkflowPK implements Serializable {

    private String processId;
    private String workflowId;

    @Id
    @Column(name = "PROCESS_ID")
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Id
    @Column(name = "WORKFLOW_ID")
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessWorkflowPK that = (ProcessWorkflowPK) o;

        return (getProcessId() != null ? getProcessId().equals(that.getProcessId()) : that.getProcessId() == null)
                && (getWorkflowId() != null
                        ? getWorkflowId().equals(that.getWorkflowId())
                        : that.getWorkflowId() == null);
    }

    @Override
    public int hashCode() {
        int result = getProcessId() != null ? getProcessId().hashCode() : 0;
        result = 31 * result + (getWorkflowId() != null ? getWorkflowId().hashCode() : 0);
        return result;
    }
}
