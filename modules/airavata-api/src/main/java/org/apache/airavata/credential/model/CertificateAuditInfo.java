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
package org.apache.airavata.credential.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import org.apache.airavata.credential.AuditInfo;

/**
 * Audit information related to a certificate credential.
 */
@XmlRootElement
public class CertificateAuditInfo implements AuditInfo {

    private static final long serialVersionUID = 13213123L;

    private String gatewayName;
    private String userId;
    private Date credentialsRequestedTime;
    private String notBefore;
    private String notAfter;
    private long credentialLifeTime;

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCredentialsRequestedTime(Date credentialsRequestedTime) {
        this.credentialsRequestedTime = credentialsRequestedTime;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public long getCredentialLifeTime() {
        return credentialLifeTime;
    }

    public void setCredentialLifeTime(long credentialLifeTime) {
        this.credentialLifeTime = credentialLifeTime;
    }

    @Override
    public Date getTimePersisted() {
        return credentialsRequestedTime;
    }
}
