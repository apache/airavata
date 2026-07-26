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
 * {@code org.apache.airavata.model.credential.store.proto.StoredCredential}.
 *
 * <p>Wrapper used for persisting any credential type as a single blob. Implements
 * {@link Serializable} directly (rather than via protobuf {@code toByteArray}/{@code parseFrom})
 * because {@code CredentialEncryptionUtil} now serializes instances with Java serialization before
 * encrypting them at rest.
 */
public sealed interface StoredCredential extends Serializable {
    record Ssh(SSHCredential sshCredential) implements StoredCredential {}

    record Password(PasswordCredential passwordCredential) implements StoredCredential {}

    record Certificate(CertificateCredential certificateCredential) implements StoredCredential {}
}
