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
package org.apache.airavata.registry.core.entities.workflowcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the workflow_status database table.
 */
@Entity
@Table(name = "WORKFLOW_STATUS")
@IdClass(WorkflowStatusPK.class)
public class WorkflowStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STATUS_ID")
    private String statusId;

    @Id
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "STATE")
    private String state;

    @Column(name = "UPDATE_TIME")
    private Timestamp timeOfStateChange;

    public WorkflowStatusEntity() {
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(Timestamp timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}