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
package org.apache.airavata.credential;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for all credential types.
 *
 * <p>The credential can be a certificate, user name password or a SSH key.
 * The {@code userId} field tracks who owns/created this credential.
 */
public abstract class Credential implements Serializable {

    private static final long serialVersionUID = -3653870227035604734L;

    /**
     * The user ID of the credential owner.
     */
    private String userId;

    /**
     * When the credential was persisted.
     */
    private Date persistedTime;

    /**
     * Unique token identifying this credential.
     */
    private String token;

    /**
     * The gateway this credential belongs to.
     */
    private String gatewayId;

    /**
     * User-given name to identify this credential (e.g. "Laptop SSH", "HPC login").
     */
    private String name;

    /**
     * Optional longer description or notes.
     */
    private String description;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getPersistedTime() {
        return persistedTime;
    }

    public void setPersistedTime(Date persistedTime) {
        this.persistedTime = persistedTime;
    }
}
