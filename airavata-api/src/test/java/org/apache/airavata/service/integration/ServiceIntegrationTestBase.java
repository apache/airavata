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
package org.apache.airavata.service.integration;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.security.AuthzToken;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all service integration tests.
 * Provides Spring Boot test setup with H2 in-memory databases.
 * All tests use @Transactional for automatic rollback.
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, org.apache.airavata.config.AiravataServerProperties.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public abstract class ServiceIntegrationTestBase {

    protected static final String TEST_GATEWAY_ID = "test-gateway";
    protected static final String TEST_USERNAME = "test-user";
    protected static final String TEST_ACCESS_TOKEN = "test-access-token";

    protected AuthzToken testAuthzToken;

    @BeforeEach
    public void setUpBase() {
        testAuthzToken = createTestAuthzToken(TEST_GATEWAY_ID, TEST_USERNAME);
    }

    /**
     * Creates a test AuthzToken with the specified gateway ID and username.
     */
    protected AuthzToken createTestAuthzToken(String gatewayId, String username) {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(TEST_ACCESS_TOKEN);
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.GATEWAY_ID, gatewayId);
        claimsMap.put(Constants.USER_NAME, username);
        authzToken.setClaimsMap(claimsMap);
        return authzToken;
    }

    /**
     * Helper method to end the current transaction and start a new one.
     * Useful for testing scenarios where you need to commit data.
     */
    protected void commitTransaction() {
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
        TestTransaction.start();
    }
}

