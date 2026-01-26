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

import java.io.Serializable;
import java.util.Objects;

/**
 * The primary key class for the PROCESS_WORKFLOW database table.
 */
public class ProcessWorkflowPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String processId;
    private String workflowId;

    public ProcessWorkflowPK() {}

    public ProcessWorkflowPK(String processId, String workflowId) {
        this.processId = processId;
        this.workflowId = workflowId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

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
        return Objects.equals(processId, that.processId)
                && Objects.equals(workflowId, that.workflowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, workflowId);
    }

    @Override
    public String toString() {
        return "ProcessWorkflowPK{"
                + "processId='" + processId + '\''
                + ", workflowId='" + workflowId + '\''
                + '}';
    }
}
