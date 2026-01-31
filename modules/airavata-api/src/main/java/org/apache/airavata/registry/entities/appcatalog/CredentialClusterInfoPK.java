/**
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
package org.apache.airavata.registry.entities.appcatalog;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for CREDENTIAL_CLUSTER_INFO table.
 * Includes gatewayId for referential integrity with CREDENTIALS(GATEWAY_ID, TOKEN_ID).
 */
public class CredentialClusterInfoPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gatewayId;
    private String credentialToken;
    private String computeResourceId;

    public CredentialClusterInfoPK() {}

    public CredentialClusterInfoPK(String gatewayId, String credentialToken, String computeResourceId) {
        this.gatewayId = gatewayId;
        this.credentialToken = credentialToken;
        this.computeResourceId = computeResourceId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

    public void setCredentialToken(String credentialToken) {
        this.credentialToken = credentialToken;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialClusterInfoPK that = (CredentialClusterInfoPK) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(credentialToken, that.credentialToken)
                && Objects.equals(computeResourceId, that.computeResourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, credentialToken, computeResourceId);
    }
}
