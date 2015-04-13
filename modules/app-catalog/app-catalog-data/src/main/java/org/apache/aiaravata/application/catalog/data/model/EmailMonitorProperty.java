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

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;

@DataCache
@Entity
@Table(name = "EMAIL_MONITOR_PROPERTY")
public class EmailMonitorProperty {

    @Id
    @Column(name = "JOB_SUBMISSION_INTERFACE_ID")
    private String jobSubmissionId;

    @Column(name = "HOST")
    private String host;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "FOLDER_NAME")
    private String folderName;

    @Column(name = "EMAIL_PROTOCOL")
    private String emailProtocol;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "JOB_SUBMISSION_INTERFACE_ID")
    private SshJobSubmission submissionInterface;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getEmailProtocol() {
        return emailProtocol;
    }

    public void setEmailProtocol(String emailProtocol) {
        this.emailProtocol = emailProtocol;
    }

    public String getJobSubmissionId() {
        return jobSubmissionId;
    }

    public void setJobSubmissionId(String jobSubmissionId) {
        this.jobSubmissionId = jobSubmissionId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SshJobSubmission getSubmissionInterface() {
        return submissionInterface;
    }

    public void setSubmissionInterface(SshJobSubmission submissionInterface) {
        this.submissionInterface = submissionInterface;
    }
}
