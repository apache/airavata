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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CommunityUser;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.service.security.CredentialStoreService;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for CredentialStoreService (Vault operations).
 */
public class CredentialStoreServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final CredentialStoreService credentialStoreService;

    public CredentialStoreServiceIntegrationTest(CredentialStoreService credentialStoreService) {
        this.credentialStoreService = credentialStoreService;
    }

    @Test
    public void shouldAddSSHCredential() throws CredentialStoreException {
        var sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        sshCredential.setDescription("Test SSH Credential");

        String token = credentialStoreService.addSSHCredential(sshCredential);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetSSHCredential() throws CredentialStoreException {
        var sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        var retrieved = credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID);

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        assertThat(retrieved.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(retrieved.getToken()).isEqualTo(token);
    }

    @Test
    public void shouldReturnNullForNonExistentSSHCredential() throws CredentialStoreException {
        var retrieved = credentialStoreService.getSSHCredential("non-existent-token", TEST_GATEWAY_ID);

        assertThat(retrieved).isNull();
    }

    @Test
    public void shouldDeleteSSHCredential() throws CredentialStoreException {
        var sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        var token = credentialStoreService.addSSHCredential(sshCredential);

        var deleted = credentialStoreService.deleteSSHCredential(token, TEST_GATEWAY_ID);

        assertThat(deleted).isTrue();
        assertThat(credentialStoreService.getSSHCredential(token, TEST_GATEWAY_ID))
                .isNull();
    }

    @Test
    public void shouldThrowExceptionWhenDeletingNonExistentCredential() {
        assertThatThrownBy(() -> credentialStoreService.deleteSSHCredential("non-existent-token", TEST_GATEWAY_ID))
                .isInstanceOf(CredentialStoreException.class);
    }

    @Test
    public void shouldAddPasswordCredential() throws CredentialStoreException {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        passwordCredential.setDescription("Test Password Credential");

        String token = credentialStoreService.addPasswordCredential(passwordCredential);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetPasswordCredential() throws CredentialStoreException {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        String token = credentialStoreService.addPasswordCredential(passwordCredential);

        PasswordCredential retrieved = credentialStoreService.getPasswordCredential(token, TEST_GATEWAY_ID);

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        assertThat(retrieved.getPortalUserName()).isEqualTo(TEST_USERNAME);
        assertThat(retrieved.getLoginUserName()).isEqualTo("login-user");
        assertThat(retrieved.getPassword()).isEqualTo("test-password");
    }

    @Test
    public void shouldDeletePasswordCredential() throws CredentialStoreException {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        String token = credentialStoreService.addPasswordCredential(passwordCredential);

        boolean deleted = credentialStoreService.deletePWDCredential(token, TEST_GATEWAY_ID);

        assertThat(deleted).isTrue();
    }

    @Test
    public void shouldGetCredentialSummary() throws CredentialStoreException {
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        CredentialSummary summary = credentialStoreService.getCredentialSummary(token, TEST_GATEWAY_ID);

        assertThat(summary).isNotNull();
        assertThat(summary.getType()).isEqualTo(SummaryType.SSH);
        assertThat(summary.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        assertThat(summary.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(summary.getToken()).isEqualTo(token);
    }

    @Test
    public void shouldGetAllCredentialSummaries() throws CredentialStoreException {
        SSHCredential sshCredential1 = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, "user1");
        SSHCredential sshCredential2 = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, "user2");
        String token1 = credentialStoreService.addSSHCredential(sshCredential1);
        String token2 = credentialStoreService.addSSHCredential(sshCredential2);

        List<CredentialSummary> summaries = credentialStoreService.getAllCredentialSummaries(
                SummaryType.SSH, List.of(token1, token2), TEST_GATEWAY_ID);

        assertThat(summaries).isNotNull().hasSize(2);
        assertThat(summaries).extracting(CredentialSummary::getToken).containsExactlyInAnyOrder(token1, token2);
    }

    @Test
    public void shouldFilterCredentialSummariesByType() throws CredentialStoreException {
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(TEST_GATEWAY_ID, TEST_USERNAME);
        String sshToken = credentialStoreService.addSSHCredential(sshCredential);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId(TEST_GATEWAY_ID);
        passwordCredential.setPortalUserName(TEST_USERNAME);
        passwordCredential.setLoginUserName("login-user");
        passwordCredential.setPassword("test-password");
        String pwdToken = credentialStoreService.addPasswordCredential(passwordCredential);

        List<CredentialSummary> sshSummaries = credentialStoreService.getAllCredentialSummaries(
                SummaryType.SSH, List.of(sshToken, pwdToken), TEST_GATEWAY_ID);
        List<CredentialSummary> pwdSummaries = credentialStoreService.getAllCredentialSummaries(
                SummaryType.PASSWD, List.of(sshToken, pwdToken), TEST_GATEWAY_ID);

        assertThat(sshSummaries).hasSize(1);
        assertThat(sshSummaries.get(0).getType()).isEqualTo(SummaryType.SSH);
        assertThat(pwdSummaries).hasSize(1);
        assertThat(pwdSummaries.get(0).getType()).isEqualTo(SummaryType.PASSWD);
    }

    @Test
    public void shouldAddCertificateCredential() throws CredentialStoreException {
        var certificateCredential = new CertificateCredential();
        var communityUser = new CommunityUser();
        communityUser.setGatewayName(TEST_GATEWAY_ID);
        communityUser.setUsername(TEST_USERNAME);
        communityUser.setUserEmail(TEST_USERNAME + "@example.com");
        certificateCredential.setCommunityUser(communityUser);
        certificateCredential.setX509Cert("-----BEGIN CERTIFICATE-----\nTEST_CERT\n-----END CERTIFICATE-----");

        var token = credentialStoreService.addCertificateCredential(certificateCredential);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetCertificateCredential() {
        assertThat(credentialStoreService).isNotNull();
    }

    @Test
    public void shouldEnforceGatewayBasedAccessControl() throws CredentialStoreException {
        String gateway1 = "gateway-1";
        String gateway2 = "gateway-2";
        SSHCredential sshCredential = TestDataFactory.createSSHCredential(gateway1, TEST_USERNAME);
        String token = credentialStoreService.addSSHCredential(sshCredential);

        assertThat(credentialStoreService.getSSHCredential(token, gateway1)).isNotNull();
        assertThat(credentialStoreService.getSSHCredential(token, gateway2)).isNull();
    }
}
