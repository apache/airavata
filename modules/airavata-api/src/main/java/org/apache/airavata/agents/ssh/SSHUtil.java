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
package org.apache.airavata.agents.ssh;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SSHUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHUtil.class);

    private SSHUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generate SSH public key from private key PEM using SSHJ.
     * Replaces BouncyCastle implementation with standard Java/SSHJ.
     */
    public static String generatePublicKey(String privateKeyPEM) throws Exception {
        try {
            // Use SSHJ to load the private key and extract the public key
            var tempClient = new SSHClient();
            var keyProvider = tempClient.loadKeys(privateKeyPEM, null, null);
            var publicKey = keyProvider.getPublic();

            if (!(publicKey instanceof RSAPublicKey)) {
                throw new Exception("Only RSA keys are supported");
            }

            var rsaPublicKey = (RSAPublicKey) publicKey;

            var byteOs = new ByteArrayOutputStream();
            var dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-rsa".getBytes().length);
            dos.write("ssh-rsa".getBytes());
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());

            return "ssh-rsa " + Base64.getEncoder().encodeToString(byteOs.toByteArray()) + " airavata-generated";
        } catch (IOException e) {
            LOGGER.error("Error while generating the public key", e);
            throw new Exception("Error while generating the public key", e);
        }
    }
}
