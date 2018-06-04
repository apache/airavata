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
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.ProviderName;
import org.apache.airavata.model.data.movement.SecurityProtocol;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the cloud_job_submission database table.
 */
@Entity
@Table(name = "CLOUD_JOB_SUBMISSION")
public class CloudJobSubmissionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "JOB_SUBMISSION_INTERFACE_ID")
    private String jobSubmissionInterfaceId;

    @Column(name = "SECURITY_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private SecurityProtocol securityProtocol;

    @Column(name = "NODE_ID")
    private String nodeId;

    @Column(name = "EXECUTABLE_TYPE")
    private String executableType;

    @Column(name = "PROVIDER_NAME")
    @Enumerated(EnumType.STRING)
    private ProviderName providerName;

    @Column(name = "USER_ACCOUNT_NAME")
    private String userAccountName;


    public String getExecutableType() {
        return executableType;
    }

    public void setExecutableType(String executableType) {
        this.executableType = executableType;
    }

    public ProviderName getProviderName() {
        return providerName;
    }

    public void setProviderName(ProviderName providerName) {
        this.providerName = providerName;
    }

    public String getUserAccountName() {
        return userAccountName;
    }

    public void setUserAccountName(String userAccountName) {
        this.userAccountName = userAccountName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }


    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }


    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId=jobSubmissionInterfaceId;
    }


    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol=securityProtocol;
    }
}
