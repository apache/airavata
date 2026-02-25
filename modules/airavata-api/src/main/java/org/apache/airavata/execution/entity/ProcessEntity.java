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
package org.apache.airavata.execution.entity;

import org.apache.airavata.status.entity.EventEntity;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.compute.resource.entity.JobEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Process entity mapping to the PROCESS table. Represents the operations layer of an
 * experiment, capturing resource binding, scheduling, and execution detail.
 * Status and error events are transient and loaded separately by the service layer.
 */
@Entity
@Table(name = "process")
@EntityListeners(AuditingEntityListener.class)
public class ProcessEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "process_id")
    private String processId;

    @Column(name = "experiment_id", nullable = false)
    private String experimentId;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "binding_id")
    private String bindingId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_context", columnDefinition = "JSON")
    private String providerContext;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_schedule", columnDefinition = "json")
    private Map<String, Object> resourceSchedule;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Transient
    private List<EventEntity> processStatuses;

    @Transient
    private List<EventEntity> processErrors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", insertable = false, updatable = false)
    private ExperimentEntity experiment;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "process", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobEntity> jobs;

    public ProcessEntity() {}

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getProviderContext() {
        return providerContext;
    }

    public void setProviderContext(String providerContext) {
        this.providerContext = providerContext;
    }

    public Map<String, Object> getResourceSchedule() {
        return resourceSchedule;
    }

    public void setResourceSchedule(Map<String, Object> resourceSchedule) {
        this.resourceSchedule = resourceSchedule;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<EventEntity> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(List<EventEntity> processStatuses) {
        this.processStatuses = processStatuses;
    }

    public List<EventEntity> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(List<EventEntity> processErrors) {
        this.processErrors = processErrors;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }

    public List<JobEntity> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobEntity> jobs) {
        this.jobs = jobs;
    }
}
