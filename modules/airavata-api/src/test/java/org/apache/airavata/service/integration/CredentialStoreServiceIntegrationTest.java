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
package org.apache.airavata.service.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CommunityUser;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.service.security.CredentialStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestConstructor;

/**
 * Comprehensive integration tests for CredentialStoreService.
 *
 * <p>Tests cover:
 * - SSH credential creation, retrieval, and deletion
 * - Certificate credential operations
 * - Credential summary listing
 * - Gateway-scoped credential operations
 * - Error handling scenarios
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("CredentialStoreService Integration Tests")
public class CredentialStoreServiceIntegrationTest extends ServiceIntegrationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(CredentialStoreServiceIntegrationTest.class);

    private final CredentialStoreService credentialStoreService;

    public CredentialStoreServiceIntegrationTest(CredentialStoreService credentialStoreService) {
        this.credentialStoreService = credentialStoreService;
    }

    @Nested
    @DisplayName("SSH Credential Tests")
    class SSHCredentialTests {

        @Test
        @DisplayName("Should create and retrieve SSH credential")
        void shouldCreateAndRetrieveSSHCredential() throws Exception {
            // Given
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("testuser");
            sshCredential.setGatewayId(TEST_GATEWAY_ID);
            sshCredential.setPassphrase("testpassphrase");
            sshCredential.setDescription("Test SSH credential for integration test");

            // When
            String token = credentialStoreService.addSSHCredential(sshCredential);

            // Then
            assertNotNull(token, "Token should be generated");
            logger.info("Generated SSH credential token: {}", token);

            // Retrieve and verify
            SSHCredential retrieved = credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID);
            assertNotNull(retrieved, "Retrieved credential should not be null");
            assertEquals("testuser", retrieved.getUsername());
            assertEquals(TEST_GATEWAY_ID, retrieved.getGatewayId());
            assertNotNull(retrieved.getPrivateKey(), "Private key should be generated");
            assertNotNull(retrieved.getPublicKey(), "Public key should be generated");
        }

        @Test
        @DisplayName("Should create SSH credential with custom keys")
        void shouldCreateSSHCredentialWithCustomKeys() throws Exception {
            // Given
            String customPrivateKey = generateDummyPrivateKey();
            String customPublicKey = generateDummyPublicKey();

            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("customkeyuser");
            sshCredential.setGatewayId(TEST_GATEWAY_ID);
            sshCredential.setPrivateKey(customPrivateKey);
            sshCredential.setPublicKey(customPublicKey);
            sshCredential.setDescription("SSH credential with custom keys");

            // When
            String token = credentialStoreService.addSSHCredential(sshCredential);

            // Then
            assertNotNull(token);
            SSHCredential retrieved = credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID);
            assertNotNull(retrieved, "Retrieved credential should not be null");
            assertEquals("customkeyuser", retrieved.getUsername(), "Username should match");
            assertEquals("SSH credential with custom keys", retrieved.getDescription(), "Description should match");
            // Keys are stored - verify they exist (may be encrypted)
            assertNotNull(retrieved.getPrivateKey(), "Private key should be stored");
            assertNotNull(retrieved.getPublicKey(), "Public key should be stored");
        }

        @Test
        @DisplayName("Should delete SSH credential")
        void shouldDeleteSSHCredential() throws Exception {
            // Given
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("deleteuser");
            sshCredential.setGatewayId(TEST_GATEWAY_ID);
            String token = credentialStoreService.addSSHCredential(sshCredential);

            // When
            boolean deleted = credentialStoreService.deleteSSHCredential(token, TEST_GATEWAY_ID);

            // Then
            assertTrue(deleted, "Deletion should succeed");

            // Verify deletion
            SSHCredential retrieved = credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID);
            assertNull(retrieved, "Credential should be deleted");
        }

        @Test
        @DisplayName("Should return null for non-existent SSH credential")
        void shouldReturnNullForNonExistentCredential() throws Exception {
            // When
            SSHCredential retrieved = credentialStoreService.getSSHCredential(
                    "non-existent-token-" + UUID.randomUUID(), TEST_GATEWAY_ID);

            // Then
            assertNull(retrieved, "Should return null for non-existent credential");
        }
    }

    @Nested
    @DisplayName("Certificate Credential Tests")
    class CertificateCredentialTests {

        @Test
        @DisplayName("Should create and retrieve certificate credential")
        void shouldCreateAndRetrieveCertificateCredential() throws Exception {
            // Given
            String certContent = generateDummyPemCertificate();
            CertificateCredential certCredential = new CertificateCredential();
            certCredential.setUserId("certuser");
            certCredential.setGatewayId(TEST_GATEWAY_ID);
            CommunityUser communityUser = new CommunityUser(TEST_GATEWAY_ID, "certuser", "cert@test.com");
            certCredential.setCommunityUser(communityUser);
            certCredential.setX509Cert(certContent);

            // When
            String token = credentialStoreService.addCertificateCredential(certCredential);

            // Then
            assertNotNull(token, "Token should be generated");
            assertFalse(token.isEmpty(), "Token should not be empty");
            logger.info("Generated certificate credential token: {}", token);

            // Retrieve and verify the credential exists and can be retrieved
            CertificateCredential retrieved = credentialStoreService.getCertificateCredential(token, TEST_GATEWAY_ID);
            assertNotNull(retrieved, "Retrieved credential should not be null");
            assertEquals("certuser", retrieved.getUserId(), "User ID should match");
            assertEquals(TEST_GATEWAY_ID, retrieved.getGatewayId(), "Gateway ID should match");
        }

        @Test
        @DisplayName("Should verify certificate credential has persisted time and token")
        void shouldVerifyCertificateCredentialMetadata() throws Exception {
            // Given
            CertificateCredential certCredential = new CertificateCredential();
            certCredential.setUserId("metadata-cert-user");
            certCredential.setGatewayId(TEST_GATEWAY_ID);
            CommunityUser communityUser = new CommunityUser(TEST_GATEWAY_ID, "metadata-cert-user", "metadata@test.com");
            certCredential.setCommunityUser(communityUser);
            certCredential.setX509Cert(generateDummyPemCertificate());

            // When
            String token = credentialStoreService.addCertificateCredential(certCredential);
            CertificateCredential retrieved = credentialStoreService.getCertificateCredential(token, TEST_GATEWAY_ID);

            // Then - Verify metadata
            assertNotNull(retrieved, "Retrieved credential should not be null");
            assertNotNull(retrieved.getPersistedTime(), "Persisted time should be set");
            assertEquals(token, retrieved.getToken(), "Token should match");
        }
    }

    @Nested
    @DisplayName("Credential Summary Tests")
    class CredentialSummaryTests {

        @Test
        @DisplayName("Should get all credential summaries for gateway")
        void shouldGetAllCredentialSummariesForGateway() throws Exception {
            // Given - Create multiple credentials
            SSHCredential ssh1 = new SSHCredential();
            ssh1.setUsername("summary-user-1");
            ssh1.setGatewayId(TEST_GATEWAY_ID);
            ssh1.setDescription("Summary test credential 1");
            String token1 = credentialStoreService.addSSHCredential(ssh1);

            SSHCredential ssh2 = new SSHCredential();
            ssh2.setUsername("summary-user-2");
            ssh2.setGatewayId(TEST_GATEWAY_ID);
            ssh2.setDescription("Summary test credential 2");
            String token2 = credentialStoreService.addSSHCredential(ssh2);

            // When
            List<CredentialSummary> summaries = credentialStoreService.getAllCredentialSummariesCombined(
                    null, TEST_GATEWAY_ID);

            // Then
            assertNotNull(summaries, "Summaries should not be null");
            assertTrue(summaries.size() >= 2, "Should have at least 2 credentials");

            // Verify summaries contain our tokens
            boolean foundToken1 = summaries.stream().anyMatch(s -> token1.equals(s.getToken()));
            boolean foundToken2 = summaries.stream().anyMatch(s -> token2.equals(s.getToken()));
            assertTrue(foundToken1, "Should find first credential in summaries");
            assertTrue(foundToken2, "Should find second credential in summaries");

            logger.info("Retrieved {} credential summaries", summaries.size());
        }

        @Test
        @DisplayName("Should get credential summary by token")
        void shouldGetCredentialSummaryByToken() throws Exception {
            // Given
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("summary-single");
            sshCredential.setGatewayId(TEST_GATEWAY_ID);
            sshCredential.setDescription("Single summary test");
            String token = credentialStoreService.addSSHCredential(sshCredential);

            // When
            CredentialSummary summary = credentialStoreService.getCredentialSummary(token, TEST_GATEWAY_ID);

            // Then
            assertNotNull(summary, "Summary should not be null");
            assertEquals(token, summary.getToken());
            assertEquals("summary-single", summary.getUsername());
            assertEquals("Single summary test", summary.getDescription());
        }
    }

    @Nested
    @DisplayName("Gateway Isolation Tests")
    class GatewayIsolationTests {

        @Test
        @DisplayName("Should isolate credentials by gateway")
        void shouldIsolateCredentialsByGateway() throws Exception {
            // Given - Create credential in default gateway
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("isolated-user");
            sshCredential.setGatewayId(TEST_GATEWAY_ID);
            String token = credentialStoreService.addSSHCredential(sshCredential);

            // When - Try to retrieve with wrong gateway
            SSHCredential retrieved = credentialStoreService.getSSHCredential(token, "wrong-gateway");

            // Then
            assertNull(retrieved, "Should not retrieve credential with wrong gateway");

            // Verify correct gateway works
            SSHCredential correct = credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID);
            assertNotNull(correct, "Should retrieve with correct gateway");
        }
    }

    // Helper methods

    private String generateDummyPrivateKey() {
        return "-----BEGIN RSA PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes())
                        .encodeToString(generateRandomBytes(256)) +
                "\n-----END RSA PRIVATE KEY-----";
    }

    private String generateDummyPublicKey() {
        return "ssh-rsa " + Base64.getEncoder().encodeToString(generateRandomBytes(128)) + " test@localhost";
    }

    private String generateDummyPemCertificate() {
        return "-----BEGIN CERTIFICATE-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes())
                        .encodeToString(generateRandomBytes(256)) +
                "\n-----END CERTIFICATE-----";
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
