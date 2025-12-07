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
package org.apache.airavata.credential.store.impl.db;

import java.io.File;
import java.io.FileInputStream;
import org.apache.airavata.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.impl.store.SSHCredentialWriter;
import org.apache.airavata.credential.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHCredentialTest {

    private static final Logger logger = LoggerFactory.getLogger(SSHCredentialTest.class);

    public static void main(String[] args) {
        String gatewayId = "phasta";
        String privateKeyPath = "/Users/chathuri/Desktop/ssh_gw111/id_rsa";
        String pubKeyPath = "/Users/chathuri/Desktop/ssh_gw111/id_rsa.pub";

        try {
            // Note: SSHCredentialWriter is now a Spring component and should be autowired
            // This test needs to be updated to use Spring Boot test context
            SSHCredentialWriter writer = new SSHCredentialWriter();
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setGateway(gatewayId);
            String token = TokenGenerator.generateToken(gatewayId, null);
            sshCredential.setToken(token);
            sshCredential.setPortalUserName("phasta");
            sshCredential.setDescription("dummy creds for testing");
            FileInputStream privateKeyStream = new FileInputStream(privateKeyPath);
            File filePri = new File(privateKeyPath);
            byte[] bFilePri = new byte[(int) filePri.length()];
            privateKeyStream.read(bFilePri);
            FileInputStream pubKeyStream = new FileInputStream(pubKeyPath);
            File filePub = new File(pubKeyPath);
            byte[] bFilePub = new byte[(int) filePub.length()];
            pubKeyStream.read(bFilePub);
            privateKeyStream.close();
            pubKeyStream.close();
            sshCredential.setPrivateKey(bFilePri);
            sshCredential.setPublicKey(bFilePub);
            sshCredential.setPassphrase("ultrascan");
            writer.writeCredentials(sshCredential);
            logger.info("SSH Token :{}", token);
        } catch (Exception e) {
            logger.error("Error writing SSH credential", e);
        }
    }
}
