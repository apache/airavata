/*
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

package org.apache.airavata.service.security;

import mockit.Expectations;
import mockit.Mocked;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.util.TrustStoreManager;
import org.apache.airavata.service.security.authzcache.AuthzCacheIndex;
import org.apache.airavata.service.security.authzcache.AuthzCacheManager;
import org.apache.airavata.service.security.authzcache.AuthzCacheManagerFactory;
import org.apache.airavata.service.security.authzcache.AuthzCachedStatus;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyCloakSecurityManagerTest {
    public static final String TEST_USERNAME = "test-user";
    public static final String TEST_GATEWAY = "test-gateway";
    public static final String TEST_ACCESS_TOKEN = "abc123";
    @Mocked
    private TrustStoreManager mockTrustStoreManager;
    @Mocked
    private ServerSettings mockServerSettings;
    @Mocked
    private RegistryServiceClientFactory mockRegistryServiceClientFactory;
    @Mocked
    private RegistryService.Client mockRegistryServiceClient;
    @Mocked
    private SharingRegistryServiceClientFactory mockSharingRegistryServiceClientFactory;
    @Mocked
    private SharingRegistryService.Client mockSharingRegistryServiceClient;
    @Mocked
    private AuthzCacheManagerFactory mockAuthzCacheManagerFactory;
    @Mocked
    private AuthzCacheManager mockAuthzCacheManager;

    @Before
    public void setUp() throws AiravataSecurityException, ApplicationSettingsException {
        new Expectations() {{
            mockServerSettings.isTrustStorePathDefined(); result = true;
            mockTrustStoreManager.initializeTrustStoreManager(anyString, anyString);
            mockServerSettings.isAPISecured(); result = true;
            mockServerSettings.getRegistryServerHost(); result = "localhost"; minTimes = 0;
            mockServerSettings.getRegistryServerPort(); result = "8970"; minTimes = 0;
            mockServerSettings.getSharingRegistryHost(); result = "localhost"; minTimes = 0;
            mockServerSettings.getSharingRegistryPort(); result = "7878"; minTimes = 0;
            mockServerSettings.getRemoteIDPServiceUrl(); result = "https://iam.server/auth"; minTimes = 0;
        }};
    }

    @Test
    public void testDisallowedGatewayUserMethod(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        runGatewayUserMethodTest(openidConfigHttpURLConnection, userinfoHttpURLConnection, "getAllGatewaySSHPubKeys", false);
    }

    @Test
    public void testAllowedGatewayUserMethod(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        runGatewayUserMethodTest(openidConfigHttpURLConnection, userinfoHttpURLConnection, "createProject", true);
    }

    @Test
    public void testAllowedGatewayUserMethod2(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        runGatewayUserMethodTest(openidConfigHttpURLConnection, userinfoHttpURLConnection, "userHasAccess", true);
    }

    @Test
    public void testAllowedGatewayUserMethod3(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        runGatewayUserMethodTest(openidConfigHttpURLConnection, userinfoHttpURLConnection, "getGroupResourceList", true);
    }

    @Test
    public void testAllowedGatewayUserMethod4(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        runGatewayUserMethodTest(openidConfigHttpURLConnection, userinfoHttpURLConnection, "revokeSharingOfResourceFromGroups", true);
    }

    @Test
    public void testAllowedGatewayUserMethod5(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        runGatewayUserMethodTest(openidConfigHttpURLConnection, userinfoHttpURLConnection, "getApplicationDeployment", true);
    }

    private void runGatewayUserMethodTest(@Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection, String methodName, boolean expectedAuthorization) throws IOException, ApplicationSettingsException, AiravataSecurityException, TException {
        createExpectationsForTokenVerification(openidConfigHttpURLConnection, userinfoHttpURLConnection);
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(false, false);

        runIsUserAuthorizedTest(methodName, expectedAuthorization);
    }

    @Test
    public void testAllowedAdminUserMethod(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        createExpectationsForTokenVerification(openidConfigHttpURLConnection, userinfoHttpURLConnection);
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(true, false);

        runIsUserAuthorizedTest("deleteGateway", true);
    }
    
    @Test
    public void testAllowedReadOnlyAdminUserMethod(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        createExpectationsForTokenVerification(openidConfigHttpURLConnection, userinfoHttpURLConnection);
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(false, true);

        runIsUserAuthorizedTest("getAllGatewaySSHPubKeys", true);
    }

    @Test
    public void testDisallowedReadOnlyAdminUserMethod(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        createExpectationsForTokenVerification(openidConfigHttpURLConnection, userinfoHttpURLConnection);
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(false, true);

        runIsUserAuthorizedTest("deleteGateway", false);
    }

    @Test
    public void testAuthorizedMethodFromCache() throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        createExpectationsForAuthzCache(true, "someMethod", AuthzCachedStatus.AUTHORIZED);

        runIsUserAuthorizedTest("someMethod", true);
    }

    @Test
    public void testNotAuthorizedMethodFromCache() throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        createExpectationsForAuthzCache(true, "someMethod", AuthzCachedStatus.NOT_AUTHORIZED);

        runIsUserAuthorizedTest("someMethod", false);
    }

    @Test
    public void testWithAuthzDecisionNotInCache(@Mocked URL anyURL, @Mocked HttpURLConnection openidConfigHttpURLConnection, @Mocked HttpURLConnection userinfoHttpURLConnection) throws AiravataSecurityException, ApplicationSettingsException, IOException, TException {

        createExpectationsForTokenVerification(openidConfigHttpURLConnection, userinfoHttpURLConnection);
        createExpectationsForGatewayGroupsMembership(false, true);
        createExpectationsForAuthzCache(true, "getAllGatewaySSHPubKeys", AuthzCachedStatus.NOT_CACHED);

        runIsUserAuthorizedTest("getAllGatewaySSHPubKeys", true);
    }

    private void runIsUserAuthorizedTest(String apiMethod, boolean expectedAuthorization) throws AiravataSecurityException {

        KeyCloakSecurityManager keyCloakSecurityManager = new KeyCloakSecurityManager();

        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(TEST_ACCESS_TOKEN);
        Map<String,String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.USER_NAME, TEST_USERNAME);
        claimsMap.put(Constants.GATEWAY_ID, TEST_GATEWAY);
        authzToken.setClaimsMap(claimsMap);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Constants.API_METHOD_NAME, apiMethod);
        boolean authorized = keyCloakSecurityManager.isUserAuthorized(authzToken, metadata);
        if (expectedAuthorization) {

            Assert.assertTrue("User should be authorized for method " + apiMethod, authorized);
        } else {

            Assert.assertFalse("User should NOT be authorized for method " + apiMethod, authorized);
        }
    }

    private void createExpectationsForTokenVerification(HttpURLConnection openidConfigHttpURLConnection, HttpURLConnection userinfoHttpURLConnection) throws IOException, ApplicationSettingsException {

        new Expectations() {{

            // Load openid configuration
            URL openidConfigUrl = new URL(withSuffix(".well-known/openid-configuration"));
            openidConfigUrl.openConnection();
            result = openidConfigHttpURLConnection;
            String userinfoUrlString = "https://iam.server/auth/realms/test-gateway/protocol/openid-connect/userinfo";
            openidConfigHttpURLConnection.getInputStream();
            result = new ByteArrayInputStream(("{\"userinfo_endpoint\": \"" + userinfoUrlString + "\"}").getBytes(StandardCharsets.UTF_8));

            // Load userinfo using token
            URL userinfoUrl = new URL(userinfoUrlString);
            userinfoUrl.openConnection();
            result = userinfoHttpURLConnection;
            userinfoHttpURLConnection.getInputStream();
            result = new ByteArrayInputStream(("{" +
                    "\"preferred_username\": \"test-user\", " +
                    "\"sub\": \"c7f06e26-120c-41d8-8e5f-d768d6be91cf\", " +
                    "\"name\": \"Bob Smith\", " +
                    "\"given_name\": \"Bob\", " +
                    "\"family_name\": \"Smith\", " +
                    "\"email\": \"bob@smith.name\"" +
                    "}").getBytes(StandardCharsets.UTF_8));
        }};
    }
    
    private void createExpectationsForGatewayGroupsMembership(boolean isInAdminsGroup, boolean isInReadOnlyAdminsGroup) throws TException {

        new Expectations() {{

            mockRegistryServiceClient.isGatewayGroupsExists(TEST_GATEWAY); result = true;
            mockRegistryServiceClient.getGatewayGroups(TEST_GATEWAY); result = new GatewayGroups(TEST_GATEWAY, "admins-group-id", "read-only-admins-group-id", "default-gateway-users-group-id");
            List<UserGroup> userGroups = new ArrayList<>();
            UserGroup dummyGroup1 = new UserGroup();
            dummyGroup1.setGroupId("dummy1-group-id");
            userGroups.add(dummyGroup1);
            UserGroup dummyGroup2 = new UserGroup();
            dummyGroup2.setGroupId("dummy2-group-id");
            userGroups.add(dummyGroup2);
            if (isInAdminsGroup) {
                UserGroup adminsGroup = new UserGroup();
                adminsGroup.setGroupId("admins-group-id");
                userGroups.add(adminsGroup);
            }
            if (isInReadOnlyAdminsGroup) {
                UserGroup readOnlyAdminsGroup = new UserGroup();
                readOnlyAdminsGroup.setGroupId("read-only-admins-group-id");
                userGroups.add(readOnlyAdminsGroup);
            }
            mockSharingRegistryServiceClient.getAllMemberGroupsForUser(TEST_GATEWAY, TEST_USERNAME + "@" + TEST_GATEWAY); result = userGroups;
        }};
    }

    private void createExpectationsForAuthzCacheDisabled() throws ApplicationSettingsException, AiravataSecurityException {

        createExpectationsForAuthzCache(false, null, null);
    }

    private void createExpectationsForAuthzCache(boolean cacheEnabled, String apiMethod, AuthzCachedStatus authzCachedStatus) throws ApplicationSettingsException, AiravataSecurityException {

        new Expectations() {{

            mockServerSettings.isAuthzCacheEnabled(); result = cacheEnabled;

            if (cacheEnabled) {

                mockAuthzCacheManager.getAuthzCachedStatus(new AuthzCacheIndex(TEST_USERNAME, TEST_GATEWAY, TEST_ACCESS_TOKEN, "/airavata/" + apiMethod));
                result = authzCachedStatus;
            }
        }};

    }
}
