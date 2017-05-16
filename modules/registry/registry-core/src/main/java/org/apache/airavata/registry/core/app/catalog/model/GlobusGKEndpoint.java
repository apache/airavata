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
package org.apache.airavata.registry.core.app.catalog.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "GLOBUS_GK_ENDPOINT")
@IdClass(GlobusGKEndPointPK.class)
public class GlobusGKEndpoint implements Serializable {
    @Id
    @Column(name = "SUBMISSION_ID")
    private String submissionID;
    @Id
    @Column(name = "ENDPOINT")
    private String endpoint;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "SUBMISSION_ID")
    private GlobusJobSubmission globusSubmission;

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public GlobusJobSubmission getGlobusSubmission() {
        return globusSubmission;
    }

    public void setGlobusSubmission(GlobusJobSubmission globusSubmission) {
        this.globusSubmission = globusSubmission;
    }
}
