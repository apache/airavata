package org.apache.airavata;

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

import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.thrift.TException;

public class KeycloakTenantCreationManager {

    private String profileServiceServerHost = "localhost";
    private int profileServiceServerPort = 8962;
    private String masterAdminUsername = "admin";
    private String masterAdminPassword = "password";

    private IamAdminServices.Client iamAdminServiceClient = null;

    public void createTenant(Gateway gateway) {
        PasswordCredential passwordCredential = getPasswordCredential();
        try {
            // TODO: replace with real authz token?
            getIamAdminServiceClient().setUpGateway(new AuthzToken("empty"), gateway, passwordCredential);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    private IamAdminServices.Client getIamAdminServiceClient() {
        if (iamAdminServiceClient == null) {
            try {
                iamAdminServiceClient = ProfileServiceClientFactory.createIamAdminServiceClient(this.profileServiceServerHost, this.profileServiceServerPort);
            } catch (IamAdminServicesException e) {
                throw new RuntimeException(e);
            }
        }
        return iamAdminServiceClient;
    }

    private PasswordCredential getPasswordCredential() {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId("dummy");
        passwordCredential.setPortalUserName("dummy");
        passwordCredential.setLoginUserName(masterAdminUsername);
        passwordCredential.setPassword(masterAdminPassword);
        return passwordCredential;
    }

    public static void main(String[] args) {

        // Configuration ...
        KeycloakTenantCreationManager keycloakTenantCreationManager = new KeycloakTenantCreationManager();
        keycloakTenantCreationManager.masterAdminUsername = "";
        keycloakTenantCreationManager.masterAdminPassword = "";
        keycloakTenantCreationManager.profileServiceServerHost = "";

        Gateway gateway = new Gateway();
        gateway.setGatewayId("");
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
        gateway.setGatewayName("");
        gateway.setIdentityServerUserName("");
        gateway.setGatewayAdminFirstName("");
        gateway.setGatewayAdminLastName("");
        gateway.setGatewayAdminEmail("");
        gateway.setGatewayURL("");

        keycloakTenantCreationManager.createTenant(gateway);
    }
}
