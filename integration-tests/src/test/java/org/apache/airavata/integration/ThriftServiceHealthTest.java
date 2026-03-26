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
package org.apache.airavata.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that all 9 multiplexed Thrift services on port 8930 respond to
 * getAPIVersion(). Each service is tested independently; a failure in one
 * does not prevent the others from running.
 */
@Tag("integration")
class ThriftServiceHealthTest {

    private static final String HOST = System.getProperty("airavata.thrift.host", "localhost");
    private static final int PORT = Integer.parseInt(System.getProperty("airavata.thrift.port", "8930"));

    static Stream<String> serviceNames() {
        return Stream.of(
                "Airavata",
                "RegistryService",
                "SharingRegistry",
                "CredentialStore",
                profile_user_cpiConstants.USER_PROFILE_CPI_NAME,
                profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME,
                iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME,
                group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME,
                "Orchestrator");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("serviceNames")
    void serviceShouldRespondToGetAPIVersion(String serviceName) throws Exception {
        try (TTransport transport = new TSocket(HOST, PORT)) {
            transport.open();
            TProtocol base = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexed = new TMultiplexedProtocol(base, serviceName);

            String version = callGetAPIVersion(serviceName, multiplexed);

            assertNotNull(version, "getAPIVersion() returned null for service: " + serviceName);
        }
    }

    /**
     * Instantiates the appropriate generated client and calls getAPIVersion().
     * Each generated client extends BaseAPI.Client which provides the method.
     */
    private String callGetAPIVersion(String serviceName, TProtocol protocol) throws Exception {
        return assertDoesNotThrow(
                () -> switch (serviceName) {
                    case "Airavata" -> new Airavata.Client(protocol).getAPIVersion();
                    case "RegistryService" -> new RegistryService.Client(protocol).getAPIVersion();
                    case "SharingRegistry" -> new SharingRegistryService.Client(protocol).getAPIVersion();
                    case "CredentialStore" -> new CredentialStoreService.Client(protocol).getAPIVersion();
                    case "Orchestrator" -> new OrchestratorService.Client(protocol).getAPIVersion();
                    default -> getProfileServiceVersion(serviceName, protocol);
                },
                "getAPIVersion() threw for service: " + serviceName);
    }

    private String getProfileServiceVersion(String serviceName, TProtocol protocol) throws Exception {
        if (profile_user_cpiConstants.USER_PROFILE_CPI_NAME.equals(serviceName)) {
            return new UserProfileService.Client(protocol).getAPIVersion();
        } else if (profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME.equals(serviceName)) {
            return new TenantProfileService.Client(protocol).getAPIVersion();
        } else if (iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME.equals(serviceName)) {
            return new IamAdminServices.Client(protocol).getAPIVersion();
        } else if (group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME.equals(serviceName)) {
            return new GroupManagerService.Client(protocol).getAPIVersion();
        }
        throw new IllegalArgumentException("Unknown service: " + serviceName);
    }
}
