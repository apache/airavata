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
 * X.509 certificate-based credential.
 */
public class CertificateCredential extends Credential {
    private static final long serialVersionUID = 1L;

    /**
     * Community user information for this certificate.
     */
    private CommunityUser communityUser;

    /**
     * The X.509 certificate in PEM format.
     */
    private String x509Cert;

    /**
     * Certificate expiration time.
     */
    private String notAfter;

    /**
     * The private key in PEM format.
     */
    private String privateKey;

    /**
     * Certificate lifetime in seconds.
     */
    private Long lifeTime;

    /**
     * Certificate start time.
     */
    private String notBefore;

    /**
     * Parsed X.509 certificate chain.
     */
    private X509Certificate[] certificates;

    /**
     * Parsed private key object.
     */
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
        return Objects.equals(getUserId(), that.getUserId())
                && Objects.equals(getGatewayId(), that.getGatewayId())
                && Objects.equals(x509Cert, that.x509Cert)
                && Objects.equals(notAfter, that.notAfter)
                && Objects.equals(privateKey, that.privateKey)
                && Objects.equals(lifeTime, that.lifeTime)
                && Objects.equals(notBefore, that.notBefore)
                && Objects.equals(getPersistedTime(), that.getPersistedTime())
                && Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getGatewayId(), x509Cert, notAfter, privateKey,
                lifeTime, notBefore, getPersistedTime(), getToken());
    }

    @Override
    public String toString() {
        return "CertificateCredential{"
                + "userId='" + getUserId() + '\''
                + ", gatewayId='" + getGatewayId() + '\''
                + ", x509Cert='" + (x509Cert != null ? "***" : null) + '\''
                + ", notAfter='" + notAfter + '\''
                + ", privateKey='" + (privateKey != null ? "***" : null) + '\''
                + ", lifeTime=" + lifeTime
                + ", notBefore='" + notBefore + '\''
                + ", persistedTime=" + getPersistedTime()
                + ", token='" + getToken() + '\''
                + '}';
    }
}
