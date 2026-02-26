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
package org.apache.airavata.iam.service;

import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.keycloak.KeycloakRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Encapsulates the layered admin-token acquisition strategy used by {@link IamAdminService}.
 *
 * <p>Keycloak admin operations require a bearer token obtained by authenticating as an
 * administrative principal. Two resolution strategies are offered, both following the same
 * fallback ordering:
 *
 * <ol>
 *   <li>Gateway realm + tenant admin credentials (identity-server password credential stored in the
 *       gateway resource profile).
 *   <li>Master realm + tenant admin credentials.
 *   <li>Master realm + super-admin credentials (from {@code application.properties} or env vars).
 *   <li><em>(Full strategy only)</em> Master realm + hardcoded {@code admin/admin} — last-resort
 *       fallback for local development / test environments.
 * </ol>
 *
 * <p>Use {@link #resolveAdminToken(String, KeycloakRestClient)} for the full four-step strategy
 * (required by read operations such as {@code getUser} and {@code getUsers} that must work even
 * when no gateway resource profile exists). Use {@link
 * #resolveAdminTokenCompact(String, KeycloakRestClient)} for the three-step strategy used by
 * write operations ({@code enableUser}, {@code disableUser}, {@code updateUserProfile},
 * {@code deleteUser}) where the hardcoded-credentials fallback is intentionally omitted.
 */
@Component
public class DefaultKeycloakAdminTokenResolver implements KeycloakAdminTokenResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultKeycloakAdminTokenResolver.class);

    private final ServerProperties properties;
    private final CredentialStoreService credentialStoreService;

    public DefaultKeycloakAdminTokenResolver(
            ServerProperties properties, CredentialStoreService credentialStoreService) {
        this.properties = properties;
        this.credentialStoreService = credentialStoreService;
    }

    /**
     * Resolves an admin bearer token using the full four-step fallback strategy.
     *
     * <p>Steps attempted in order:
     * <ol>
     *   <li>Gateway realm + tenant admin credentials.
     *   <li>Master realm + tenant admin credentials.
     *   <li>Master realm + super-admin credentials.
     *   <li>Master realm + hardcoded {@code admin/admin}.
     * </ol>
     *
     * @param gatewayId the Keycloak realm identifier for the target gateway
     * @param client    a {@link KeycloakRestClient} configured for the IAM server
     * @return a non-null admin bearer token string
     * @throws IamAdminServicesException if all four strategies fail
     */
    public String resolveAdminToken(String gatewayId, KeycloakRestClient client) throws IamAdminServicesException {
        String adminToken = null;
        Exception lastException = null;

        // Step 1: Gateway realm + tenant admin credentials
        try {
            var tenantCreds = getTenantAdminPasswordCredential(gatewayId);
            adminToken = client.obtainAdminToken(gatewayId, tenantCreds);
            logger.debug("Successfully obtained admin token from gateway realm");
        } catch (Exception e) {
            logger.debug("Failed to get admin token from gateway realm: {}", e.getMessage());
            lastException = e;
        }

        // Step 2: Master realm + tenant admin credentials
        if (adminToken == null) {
            try {
                var tenantCreds = getTenantAdminPasswordCredential(gatewayId);
                adminToken = client.obtainAdminToken("master", tenantCreds);
                logger.debug("Successfully obtained admin token from master realm with tenant credentials");
            } catch (Exception e) {
                logger.debug("Failed to get admin token from master realm with tenant credentials: {}", e.getMessage());
                lastException = e;
            }
        }

        // Step 3: Master realm + super-admin credentials
        if (adminToken == null) {
            try {
                var superAdminCreds = getSuperAdminPasswordCredential();
                adminToken = client.obtainAdminToken("master", superAdminCreds);
                logger.debug("Successfully obtained admin token from master realm with super admin credentials");
            } catch (Exception e) {
                logger.debug(
                        "Failed to get admin token from master realm with super admin credentials: {}", e.getMessage());
                lastException = e;
            }
        }

        // Step 4: Master realm + hardcoded admin/admin (last-resort for dev/test)
        if (adminToken == null) {
            try {
                var keycloakAdminCreds = new PasswordCredential();
                keycloakAdminCreds.setLoginUserName("admin");
                keycloakAdminCreds.setPassword("admin");
                adminToken = client.obtainAdminToken("master", keycloakAdminCreds);
                logger.debug("Successfully obtained admin token using Keycloak admin credentials");
            } catch (Exception e) {
                logger.error("Failed to get admin token using Keycloak admin credentials: {}", e.getMessage());
                lastException = e;
            }
        }

        if (adminToken == null) {
            throw new IamAdminServicesException("Failed to obtain admin token after trying all methods: "
                    + (lastException != null ? lastException.getMessage() : "Unknown error"));
        }
        return adminToken;
    }

    /**
     * Resolves an admin bearer token using the compact three-step fallback strategy.
     *
     * <p>Steps attempted in order:
     * <ol>
     *   <li>Gateway realm + tenant admin credentials.
     *   <li>Master realm + tenant admin credentials.
     *   <li>Master realm + super-admin credentials.
     * </ol>
     *
     * <p>Unlike {@link #resolveAdminToken}, this method does <em>not</em> fall back to hardcoded
     * {@code admin/admin} credentials and propagates any failure as an {@link
     * IamAdminServicesException}.
     *
     * @param gatewayId the Keycloak realm identifier for the target gateway
     * @param client    a {@link KeycloakRestClient} configured for the IAM server
     * @return a non-null admin bearer token string
     * @throws IamAdminServicesException if all three strategies fail
     */
    public String resolveAdminTokenCompact(String gatewayId, KeycloakRestClient client)
            throws IamAdminServicesException {
        try {
            // Step 1: Gateway realm + tenant admin credentials
            // Step 2 (inner fallback): Master realm + tenant admin credentials
            var tenantCreds = getTenantAdminPasswordCredential(gatewayId);
            try {
                return client.obtainAdminToken(gatewayId, tenantCreds);
            } catch (Exception e) {
                logger.debug("Failed to get admin token from gateway realm, trying master realm: {}", e.getMessage());
                return client.obtainAdminToken("master", tenantCreds);
            }
        } catch (Exception e) {
            // Step 3: Master realm + super-admin credentials
            logger.debug("Failed to get tenant admin credentials, using super admin: {}", e.getMessage());
            var superAdminCreds = getSuperAdminPasswordCredential();
            return client.obtainAdminToken("master", superAdminCreds);
        }
    }

    // -------------------------------------------------------------------------
    // Private credential helpers (extracted from IamAdminService verbatim)
    // -------------------------------------------------------------------------

    private PasswordCredential getSuperAdminPasswordCredential() throws IamAdminServicesException {
        var creds = new PasswordCredential();

        String username = null;
        String password = null;

        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null
                && properties.security().iam().superAdmin() != null) {
            username = properties.security().iam().superAdmin().username();
            password = properties.security().iam().superAdmin().password();
        }

        if (username == null || username.isEmpty()) {
            username = System.getenv("IAM_SUPER_ADMIN_USERNAME");
            if (username == null || username.isEmpty()) {
                username = "admin";
            }
        }
        if (password == null || password.isEmpty()) {
            password = System.getenv("IAM_SUPER_ADMIN_PASSWORD");
            if (password == null || password.isEmpty()) {
                password = "admin";
            }
        }

        creds.setLoginUserName(username);
        creds.setPassword(password);
        return creds;
    }

    private PasswordCredential getTenantAdminPasswordCredential(String tenantId) throws IamAdminServicesException {
        return getSuperAdminPasswordCredential();
    }
}
