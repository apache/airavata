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
package org.apache.airavata.models.credential.store;

import java.io.Serializable;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.credential.store.proto.CertificateCredential}.
 *
 * <p>Implements {@link Serializable} because instances are persisted at rest via Java
 * serialization inside a {@code StoredCredential} (see {@code CredentialEncryptionUtil}).
 */
public record CertificateCredential(
        CommunityUser communityUser,
        String x509Cert,
        String notAfter,
        String privateKey,
        long lifeTime,
        String notBefore,
        long persistedTime,
        String token)
        implements Serializable {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private CommunityUser communityUser;
        private String x509Cert = "";
        private String notAfter = "";
        private String privateKey = "";
        private long lifeTime;
        private String notBefore = "";
        private long persistedTime;
        private String token = "";

        private Builder() {}

        private Builder(CertificateCredential source) {
            this.communityUser = source.communityUser;
            this.x509Cert = source.x509Cert;
            this.notAfter = source.notAfter;
            this.privateKey = source.privateKey;
            this.lifeTime = source.lifeTime;
            this.notBefore = source.notBefore;
            this.persistedTime = source.persistedTime;
            this.token = source.token;
        }

        public Builder setCommunityUser(CommunityUser communityUser) {
            this.communityUser = communityUser;
            return this;
        }

        public Builder setX509Cert(String x509Cert) {
            this.x509Cert = x509Cert;
            return this;
        }

        public Builder setNotAfter(String notAfter) {
            this.notAfter = notAfter;
            return this;
        }

        public Builder setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder setLifeTime(long lifeTime) {
            this.lifeTime = lifeTime;
            return this;
        }

        public Builder setNotBefore(String notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public Builder setPersistedTime(long persistedTime) {
            this.persistedTime = persistedTime;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public CertificateCredential build() {
            return new CertificateCredential(
                    communityUser, x509Cert, notAfter, privateKey, lifeTime, notBefore, persistedTime, token);
        }
    }
}
