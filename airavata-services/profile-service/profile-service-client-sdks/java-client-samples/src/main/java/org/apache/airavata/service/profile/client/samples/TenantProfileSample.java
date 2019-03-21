/**
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
 */
package org.apache.airavata.service.profile.client.samples;

import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.client.util.ProfileServiceClientUtil;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by goshenoy on 3/30/17.
 */
public class TenantProfileSample {
    private static final Logger logger = LoggerFactory.getLogger(TenantProfileSample.class);
    private static TenantProfileService.Client tenantProfileClient;
    private static String testGatewayId = null;
    private static AuthzToken authzToken = new AuthzToken("empy_token");

    public static void main(String[] args) throws Exception {
        try {
            String profileServiceServerHost = ProfileServiceClientUtil.getProfileServiceServerHost();
            int profileServiceServerPort = ProfileServiceClientUtil.getProfileServiceServerPort();

            tenantProfileClient = ProfileServiceClientFactory.createTenantProfileServiceClient(profileServiceServerHost, profileServiceServerPort);

            // test addGateway
            testGatewayId = tenantProfileClient.addGateway(authzToken, getGateway("465"));
            assert (testGatewayId != null) : "Gateway creation failed!";
            System.out.println("Gateway created with gatewayId: " + testGatewayId);


        } catch (Exception ex) {
            logger.error("TenantProfile client-sample Exception: " + ex, ex);
        }
    }

    private static Gateway getGateway(String gatewayId) {
        // get random value for userId
        int gatewayIdValue = ThreadLocalRandom.current().nextInt(1000);

        if (gatewayId != null) {
            gatewayIdValue = Integer.parseInt(gatewayId.replaceAll("test-gateway-", ""));
        }

        Gateway gateway = new Gateway();
        gateway.setGatewayId("test-gateway-" + gatewayIdValue);
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
        gateway.setGatewayName("test-gateway-name");
        gateway.setDomain("test-gateway-domain");
        gateway.setEmailAddress("test-gateway-" + gatewayIdValue + "@domain.com");
        gateway.setGatewayURL("test-gateway-" + gatewayIdValue + ".domain.com");
        gateway.setGatewayAdminFirstName("test-gateway-admin-fname");
        gateway.setGatewayAdminLastName("test-gateway-admin-lname");
        gateway.setGatewayAdminEmail("test-gateway-admin@email.domain.com");
        return gateway;
    }
}
