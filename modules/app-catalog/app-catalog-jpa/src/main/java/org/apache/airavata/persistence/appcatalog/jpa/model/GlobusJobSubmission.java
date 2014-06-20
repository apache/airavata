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

package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "GLOBUS_SUBMISSION")
public class GlobusJobSubmission {
    @Column(name = "RESOURCE_ID")
    private String resourceID;
    @Id
    @Column(name = "SUBMISSION_ID")
    private String submissionID;
    @Column(name = "RESOURCE_JOB_MANAGER")
    private String resourceJobManager;
    @Column(name = "SECURITY_PROTOCAL")
    private String securityProtocol;
    @Column(name = "GLOBUS_GATEKEEPER_EP")
    private String globusEP;

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

    public String getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(String resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getGlobusEP() {
        return globusEP;
    }

    public void setGlobusEP(String globusEP) {
        this.globusEP = globusEP;
    }

    public ComputeResource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResource computeResource) {
        this.computeResource = computeResource;
    }
}
