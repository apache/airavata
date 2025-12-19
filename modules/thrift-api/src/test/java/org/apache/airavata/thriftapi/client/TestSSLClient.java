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
package org.apache.airavata.thriftapi.client;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CommunityUser;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.service.security.CredentialStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSSLClient {

    private static final Logger logger = LoggerFactory.getLogger(TestSSLClient.class);

    private CredentialStoreService credentialService;

    private final Map<String, SSHCredential> sshStore = new ConcurrentHashMap<>();
    private final Map<String, CertificateCredential> certStore = new ConcurrentHashMap<>();

    @BeforeEach
    void setupMocks() throws Exception {
        credentialService = Mockito.mock(CredentialStoreService.class);
        doAnswer(invocation -> {
                    SSHCredential incoming = invocation.getArgument(0, SSHCredential.class);
                    String token = UUID.randomUUID().toString();
                    SSHCredential stored = new SSHCredential();
                    stored.setToken(token);
                    stored.setGatewayId(incoming.getGatewayId());
                    stored.setUsername(incoming.getUsername());
                    stored.setPrivateKey("private");
                    stored.setPublicKey("public");
                    sshStore.put(token, stored);
                    return token;
                })
                .when(credentialService)
                .addSSHCredential(org.mockito.ArgumentMatchers.any());

        when(credentialService.getSSHCredential(
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> sshStore.get(invocation.getArgument(0, String.class)));

        doAnswer(invocation -> {
                    CertificateCredential incoming = invocation.getArgument(0, CertificateCredential.class);
                    String token = UUID.randomUUID().toString();
                    CertificateCredential stored = new CertificateCredential();
                    stored.setToken(token);
                    stored.setGatewayId(
                            incoming.getCommunityUser() != null
                                    ? incoming.getCommunityUser().getGatewayName()
                                    : "gateway");
                    stored.setCommunityUser(incoming.getCommunityUser());
                    stored.setX509Cert(incoming.getX509Cert());
                    certStore.put(token, stored);
                    return token;
                })
                .when(credentialService)
                .addCertificateCredential(org.mockito.ArgumentMatchers.any());

        when(credentialService.getCertificateCredential(
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> certStore.get(invocation.getArgument(0, String.class)));
    }

    @Test
    public void invoke() {
        testSSHCredential(credentialService);
        testCertificateCredential(credentialService);
    }

    public void testSSHCredential(CredentialStoreService credentialService) {
        try {
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("test");
            sshCredential.setGatewayId("testGateway");
            sshCredential.setPassphrase("mypassphrase");
            String token = credentialService.addSSHCredential(sshCredential);
            logger.info("SSH Token :{}", token);
            SSHCredential credential = credentialService.getSSHCredential(token, "testGateway");
            logger.info("private key : {}", credential.getPrivateKey());
            logger.info("public key : {}", credential.getPublicKey());
            org.junit.jupiter.api.Assertions.assertNotNull(credential);
        } catch (Exception e) {
            logger.error("Error adding SSH credential", e);
            throw new RuntimeException(e);
        }
    }

    public void testCertificateCredential(CredentialStoreService credentialService) {
        try {
            CertificateCredential certificateCredential = new CertificateCredential();
            CommunityUser communityUser = new CommunityUser("testGateway", "test", "test@ddsd");
            certificateCredential.setCommunityUser(communityUser);
            certificateCredential.setX509Cert(generateDummyPemCertificate());
            String token = credentialService.addCertificateCredential(certificateCredential);
            logger.info("Certificate Token :{}", token);
            CertificateCredential credential = credentialService.getCertificateCredential(token, "testGateway");
            logger.info("certificate : {}", credential.getX509Cert());
            logger.info("gateway name  : {}", credential.getCommunityUser().getGatewayName());
            org.junit.jupiter.api.Assertions.assertNotNull(credential);
        } catch (Exception e) {
            logger.error("Error adding certificate credential", e);
            throw new RuntimeException(e);
        }
    }

    private String generateDummyPemCertificate() {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
        String certBegin = "-----BEGIN CERTIFICATE-----\n";
        String endCert = "-----END CERTIFICATE-----";
        byte[] randomBytes = new byte[256];
        new SecureRandom().nextBytes(randomBytes);
        return certBegin + encoder.encodeToString(randomBytes) + endCert;
    }
}
