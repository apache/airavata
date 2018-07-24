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
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import java.io.Serializable;
import java.util.Objects;

public class NotificationEmailPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String workflowId;
    private String email;

    public NotificationEmailPK() {
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationEmailPK that = (NotificationEmailPK) o;
        return Objects.equals(workflowId, that.workflowId) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {

        return Objects.hash(workflowId, email);
    }
}
