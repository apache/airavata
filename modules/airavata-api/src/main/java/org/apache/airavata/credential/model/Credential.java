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

import java.io.Serializable;
import java.util.Date;

/**
 * Sealed interface for all credential types.
 *
 * <p>The credential can be a certificate, user name password or a SSH key.
 * The {@code userId} field tracks who owns/created this credential.
 */
public sealed interface Credential extends Serializable
        permits SSHCredential, PasswordCredential, CertificateCredential {

    String getUserId();

    void setUserId(String userId);

    Date getPersistedTime();

    void setPersistedTime(Date persistedTime);

    String getToken();

    void setToken(String token);

    String getGatewayId();

    void setGatewayId(String gatewayId);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);
}
