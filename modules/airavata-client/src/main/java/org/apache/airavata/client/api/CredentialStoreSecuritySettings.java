/*
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
 *
 */

package org.apache.airavata.client.api;

/**
 * Encapsulates security information related to credential-store.
 * Mainly we need information about the token id and and user id of the portal user
 * who is invoking the workflow.
 */
public interface CredentialStoreSecuritySettings {

    /**
     * Returns the token id to get the credentials.
     * @return The token id.
     */
    String getTokenId();

    /**
     * Sets the token to be used when accessing the credential store.
     * @param token The token.
     */
    void setTokenId(String token);

    /**
     * Sets the portal user name.
     * @param portalUserName The name of the portal user.
     */
    void setPortalUser(String portalUserName);

    /**
     * Gets the portal user name.
     * @return portal user name.
     */
    String getPortalUser();

    /**
     * Sets the gateway id.
     * @param gatewayId The gateway id.
     */
    void setGatewayId(String gatewayId);

    /**
     * Gets the gateway id.
     * @return name of the gateway.
     */
    String getGatewayId();



}
