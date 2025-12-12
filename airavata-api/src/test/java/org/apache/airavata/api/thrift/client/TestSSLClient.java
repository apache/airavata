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
package org.apache.airavata.api.thrift.client;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.apache.airavata.model.credential.store.CertificateCredential;
import org.apache.airavata.model.credential.store.CommunityUser;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class TestSSLClient {

    private static final Logger logger = LoggerFactory.getLogger(TestSSLClient.class);

    private final CredentialStoreService credentialService;

    public TestSSLClient(CredentialStoreService credentialService) {
        this.credentialService = credentialService;
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
        } catch (Exception e) {
            logger.error("Error adding SSH credential", e);
        }
    }

    public void testCertificateCredential(CredentialStoreService credentialService) {
        try {
            CertificateCredential certificateCredential = new CertificateCredential();
            CommunityUser communityUser = new CommunityUser("testGateway", "test", "test@ddsd");
            certificateCredential.setCommunityUser(communityUser);
            X509Certificate[] x509Certificates = new X509Certificate[1];
            KeyStore ks = KeyStore.getInstance("JKS");
            File keyStoreFile = new File(
                    "/Users/smarru/code/airavata-master/modules/configuration/server/src/main/resources/airavata.p12");
            FileInputStream fis = new FileInputStream(keyStoreFile);
            char[] password = "airavata".toCharArray();
            ks.load(fis, password);
            x509Certificates[0] = (X509Certificate) ks.getCertificate("airavata");
            Base64 encoder = new Base64(64);
            String cert_begin = "-----BEGIN CERTIFICATE-----\n";
            String end_cert = "-----END CERTIFICATE-----";
            byte[] derCert = x509Certificates[0].getEncoded();
            String pemCertPre = new String(encoder.encode(derCert));
            String pemCert = cert_begin + pemCertPre + end_cert;
            certificateCredential.setX509Cert(pemCert);
            String token = credentialService.addCertificateCredential(certificateCredential);
            logger.info("Certificate Token :{}", token);
            CertificateCredential credential = credentialService.getCertificateCredential(token, "testGateway");
            logger.info("certificate : {}", credential.getX509Cert());
            logger.info("gateway name  : {}", credential.getCommunityUser().getGatewayName());
        } catch (Exception e) {
            logger.error("Error adding certificate credential", e);
        }
    }
}
