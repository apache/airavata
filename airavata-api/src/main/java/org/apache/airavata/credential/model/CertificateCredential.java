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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;
import org.apache.airavata.credential.Credential;

/**
 * Domain model: CertificateCredential
 */
public class CertificateCredential extends Credential {
    private CommunityUser communityUser;
    private String x509Cert;
    private String notAfter;
    private String privateKey;
    private Long lifeTime;
    private String notBefore;
    private Long persistedTime;
    private String token;
    private X509Certificate[] certificates;
    private PrivateKey privateKeyObject;

    public CertificateCredential() {}

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

    public Long getPersistedTime() {
        return persistedTime;
    }

    public void setPersistedTime(Long persistedTime) {
        this.persistedTime = persistedTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
        return Objects.equals(communityUser, that.communityUser)
                && Objects.equals(x509Cert, that.x509Cert)
                && Objects.equals(notAfter, that.notAfter)
                && Objects.equals(privateKey, that.privateKey)
                && Objects.equals(lifeTime, that.lifeTime)
                && Objects.equals(notBefore, that.notBefore)
                && Objects.equals(persistedTime, that.persistedTime)
                && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(communityUser, x509Cert, notAfter, privateKey, lifeTime, notBefore, persistedTime, token);
    }

    @Override
    public String toString() {
        return "CertificateCredential{" + "communityUser="
                + communityUser + ", x509Cert='"
                + (x509Cert != null ? "***" : null) + '\'' + ", notAfter='"
                + notAfter + '\'' + ", privateKey='"
                + (privateKey != null ? "***" : null) + '\'' + ", lifeTime="
                + lifeTime + ", notBefore='"
                + notBefore + '\'' + ", persistedTime="
                + persistedTime + ", token='"
                + token + '\'' + '}';
    }
}
