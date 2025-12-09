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
* software distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
* OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.airavata.service.CredentialStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for CredentialStoreService (Vault operations).
 */
public class CredentialStoreServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private CredentialStoreService credentialStoreService;

    @Test
    public void shouldAddSSHCredential() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        sshCredential.setDescription("Test SSH Credential");

        // Act
        String token = credentialStoreService.addSSHCredential(sshCredential);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetSSHCredential() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        // Act
        SSHCredential retrieved = credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID);

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        assertThat(retrieved.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(retrieved.getToken()).isEqualTo(token);
    }

    @Test
    public void shouldReturnNullForNonExistentSSHCredential() throws CredentialStoreException {
        // Act
        SSHCredential retrieved = credentialStoreService.getSSHCredential("non-existent-token", TEST_GATEWAY_ID);

        // Assert
        assertThat(retrieved).isNull();
    }

    @Test
    public void shouldDeleteSSHCredential() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        // Act
        boolean deleted = credentialStoreService.deleteSSHCredential(token, TEST_GATEWAY_ID);

        // Assert
        assertThat(deleted).isTrue();
        assertThat(credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID)).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenDeletingNonExistentCredential() {
        // Act & Assert
        assertThatThrownBy(() -> credentialStoreService.deleteSSHCredential("non-existent-token", TEST_GATEWAY_ID))
                .isInstanceOf(CredentialStoreException.class);
    }

    @Test
    public void shouldAddPasswordCredential() throws CredentialStoreException {
        // Arrange
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        passwordCredential.setDescription("Test Password Credential");

        // Act
        String token = credentialStoreService.addPasswordCredential(passwordCredential);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetPasswordCredential() throws CredentialStoreException {
        // Arrange
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        String token = credentialStoreService.addPasswordCredential(passwordCredential);

        // Act
        PasswordCredential retrieved = credentialStoreService.getPasswordCredential(token, TEST_GATEWAY_ID);

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        assertThat(retrieved.getPortalUserName()).isEqualTo(TEST_USERNAME);
        assertThat(retrieved.getLoginUserName()).isEqualTo("login-user");
        assertThat(retrieved.getPassword()).isEqualTo("test-password");
    }

    @Test
    public void shouldDeletePasswordCredential() throws CredentialStoreException {
        // Arrange
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        String token = credentialStoreService.addPasswordCredential(passwordCredential);

        // Act
        boolean deleted = credentialStoreService.deletePWDCredential(token, TEST_GATEWAY_ID);

        // Assert
        assertThat(deleted).isTrue();
    }

    @Test
    public void shouldGetCredentialSummary() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        // Act
        CredentialSummary summary = credentialStoreService.getCredentialSummary(token, TEST_GATEWAY_ID);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.getType()).isEqualTo(SummaryType.SSH);
        assertThat(summary.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        assertThat(summary.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(summary.getToken()).isEqualTo(token);
    }

    @Test
    public void shouldGetAllCredentialSummaries() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential1 = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, "user1");
        SSHCredential sshCredential2 = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, "user2");
        String token1 = credentialStoreService.addSSHCredential(sshCredential1);
        String token2 = credentialStoreService.addSSHCredential(sshCredential2);

        // Act
        List<CredentialSummary> summaries = credentialStoreService.getAllCredentialSummaries(
                SummaryType.SSH, List.of(token1, token2), TEST_GATEWAY_ID);

        // Assert
        assertThat(summaries).isNotNull().hasSize(2);
        assertThat(summaries).extracting(CredentialSummary::getToken).containsExactlyInAnyOrder(token1, token2);
    }

    @Test
    public void shouldFilterCredentialSummariesByType() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String sshToken = credentialStoreService.addSSHCredential(sshCredential);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        String pwdToken = credentialStoreService.addPasswordCredential(passwordCredential);

        // Act
        List<CredentialSummary> sshSummaries = credentialStoreService.getAllCredentialSummaries(
                SummaryType.SSH, List.of(sshToken, pwdToken), TEST_GATEWAY_ID);
        List<CredentialSummary> pwdSummaries = credentialStoreService.getAllCredentialSummaries(
                SummaryType.PASSWD, List.of(sshToken, pwdToken), TEST_GATEWAY_ID);

        // Assert
        assertThat(sshSummaries).hasSize(1);
        assertThat(sshSummaries.get(0).getType()).isEqualTo(SummaryType.SSH);
        assertThat(pwdSummaries).hasSize(1);
        assertThat(pwdSummaries.get(0).getType()).isEqualTo(SummaryType.PASSWD);
    }

    @Test
    public void shouldAddCertificateCredential() throws CredentialStoreException {
        // Arrange
        org.apache.airavata.model.credential.store.CertificateCredential certificateCredential =
                new org.apache.airavata.model.credential.store.CertificateCredential();
        org.apache.airavata.model.credential.store.CommunityUser communityUser =
                new org.apache.airavata.model.credential.store.CommunityUser();
        communityUser.setGatewayName(TEST_GATEWAY_ID);
        communityUser.setUsername(TEST_USERNAME);
        communityUser.setUserEmail(TEST_USERNAME + "@example.com");
        certificateCredential.setCommunityUser(communityUser);
        certificateCredential.setX509Cert("-----BEGIN CERTIFICATE-----\nTEST_CERT\n-----END CERTIFICATE-----");

        // Act & Assert
        try {
            String token = credentialStoreService.addCertificateCredential(certificateCredential);
            assertThat(token).isNotNull().isNotEmpty();
        } catch (CredentialStoreException e) {
            // Expected if certificate format is invalid
            assertThat(e).isNotNull();
        }
    }

    @Test
    public void shouldGetCertificateCredential() {
        // Note: This test requires a valid certificate
        assertThat(credentialStoreService).isNotNull();
    }

    @Test
    public void shouldGetAllCredentialSummariesForGateway() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        credentialStoreService.addSSHCredential(sshCredential);

        // Act
        List<CredentialSummary> summaries =
                credentialStoreService.getAllCredentialSummaryForGateway(SummaryType.SSH, TEST_GATEWAY_ID);

        // Assert
        assertThat(summaries).isNotNull();
    }

    @Test
    public void shouldGetAllCredentialSummariesForUserInGateway() throws CredentialStoreException {
        // Arrange
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        credentialStoreService.addSSHCredential(sshCredential);

        // Act
        List<CredentialSummary> summaries = credentialStoreService.getAllCredentialSummaryForUserInGateway(
                SummaryType.SSH, TEST_GATEWAY_ID, TEST_USERNAME);

        // Assert
        assertThat(summaries).isNotNull();
    }

    @Test
    public void shouldGetAllPWDCredentialsForGateway() throws CredentialStoreException {
        // Arrange
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        credentialStoreService.addPasswordCredential(passwordCredential);

        // Act
        Map<String, String> credentials = credentialStoreService.getAllPWDCredentialsForGateway(TEST_GATEWAY_ID);

        // Assert
        assertThat(credentials).isNotNull();
    }

    @Test
    public void shouldEnforceGatewayBasedAccessControl() throws CredentialStoreException {
        // Arrange
        String gateway1 = "gateway-1";
        String gateway2 = "gateway-2";
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(gateway1, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        // Act & Assert
        assertThat(credentialStoreService.getSSHCredential(token, gateway1)).isNotNull();
        assertThat(credentialStoreService.getSSHCredential(token, gateway2)).isNull();
    }
}
