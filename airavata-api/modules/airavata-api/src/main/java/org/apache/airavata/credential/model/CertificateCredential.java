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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * X.509 certificate-based credential.
 */
public final class CertificateCredential implements Credential {
    private static final long serialVersionUID = 2L;

    // Base credential fields
    private String userId;
    private long createdAt;
    private String token;
    private String gatewayId;
    private String name;
    private String description;

    // Certificate-specific fields
    private CommunityUser communityUser;
    private String x509Cert;
    private String notAfter;
    private String privateKey;
    private Long lifeTime;
    private String notBefore;

    @JsonIgnore
    private X509Certificate[] certificates;

    @JsonIgnore
    private PrivateKey privateKeyObject;

    public CertificateCredential() {}

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getGatewayId() {
        return gatewayId;
    }

    @Override
    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public CommunityUser getCommunityUser() {
        return communityUser;
    }

    public void setCommunityUser(CommunityUser communityUser) {
        this.communityUser = communityUser;
    }

    public String getX509Cert() {
        return x509Cert;
    }

    public void setX509Cert(String x509Cert) {
        this.x509Cert = x509Cert;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(Long lifeTime) {
        this.lifeTime = lifeTime;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public X509Certificate[] getCertificates() {
        return certificates;
    }

    public void setCertificates(X509Certificate[] certificates) {
        this.certificates = certificates;
    }

    public PrivateKey getPrivateKeyObject() {
        return privateKeyObject;
    }

    public void setPrivateKeyObject(PrivateKey privateKeyObject) {
        this.privateKeyObject = privateKeyObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertificateCredential that = (CertificateCredential) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(x509Cert, that.x509Cert)
                && Objects.equals(notAfter, that.notAfter)
                && Objects.equals(privateKey, that.privateKey)
                && Objects.equals(lifeTime, that.lifeTime)
                && Objects.equals(notBefore, that.notBefore)
                && createdAt == that.createdAt
                && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, gatewayId, x509Cert, notAfter, privateKey, lifeTime, notBefore, createdAt, token);
    }

    @Override
    public String toString() {
        return "CertificateCredential{"
                + "userId='" + userId + '\''
                + ", gatewayId='" + gatewayId + '\''
                + ", x509Cert='" + (x509Cert != null ? "***" : null) + '\''
                + ", notAfter='" + notAfter + '\''
                + ", privateKey='" + (privateKey != null ? "***" : null) + '\''
                + ", lifeTime=" + lifeTime
                + ", notBefore='" + notBefore + '\''
                + ", createdAt=" + createdAt
                + ", token='" + token + '\''
                + '}';
    }
}
