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
package org.apache.airavata.credential.store.credential.impl.certificate;

import org.apache.airavata.credential.store.credential.AuditInfo;
import org.apache.airavata.credential.store.credential.CommunityUser;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Audit information related to community credential.
 */
@XmlRootElement
public class CertificateAuditInfo implements AuditInfo {

    private static final long serialVersionUID = 13213123L;

    private String gatewayName;
    private String communityUserName;
    private String portalUserName;
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

    public void setCommunityUserName(String communityUserName) {
        this.communityUserName = communityUserName;
    }

    public void setPortalUserName(String portalUserName) {
        this.portalUserName = portalUserName;
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

    public CommunityUser getCommunityUser() {
        return new CommunityUser(gatewayName, communityUserName);
    }

    public String getPortalUserId() {
        return portalUserName;
    }

    public Date getTimePersisted() {
        return credentialsRequestedTime;
    }
}
