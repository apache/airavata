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

package org.apache.aiaravata.application.catalog.data.model;

import javax.persistence.*;

@Entity
@Table(name = "JOB_SUBMISSION_PROTOCOL")
@IdClass(JobSubmissionProtocolPK.class)
public class JobSubmissionProtocol {
    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceID;
    @Id
    @Column(name = "SUBMISSION_ID")
    private String submissionID;

    @Id
    @Column(name = "JOB_TYPE")
    private String jobType;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "RESOURCE_ID")
    private ComputeResource computeResource;

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public ComputeResource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResource computeResource) {
        this.computeResource = computeResource;
    }
}
