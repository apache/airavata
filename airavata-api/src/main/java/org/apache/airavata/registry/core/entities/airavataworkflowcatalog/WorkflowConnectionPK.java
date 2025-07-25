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
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import java.io.Serializable;
import java.util.Objects;

public class WorkflowConnectionPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String workflowId;

    public WorkflowConnectionPK() {}

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowConnectionPK that = (WorkflowConnectionPK) o;
        return Objects.equals(id, that.id) && Objects.equals(workflowId, that.workflowId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, workflowId);
    }
}
