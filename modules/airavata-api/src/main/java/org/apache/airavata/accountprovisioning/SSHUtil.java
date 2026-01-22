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
package org.apache.airavata.accountprovisioning;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import org.apache.airavata.credential.model.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by machrist on 2/10/17.
 * Migrated from JSCH to SSHJ.
 */
public class SSHUtil {

    private static final Logger logger = LoggerFactory.getLogger(SSHUtil.class);

    public static boolean validate(String hostname, int port, String username, SSHCredential sshCredential) {
        SSHClient client = null;
        try {
            client = new SSHClient();
            // Disable strict host key checking (equivalent to JSCH's StrictHostKeyChecking=no)
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(hostname, port);

            // Load private key from bytes
            var keyProvider = loadKeyProvider(sshCredential);
            client.authPublickey(username, keyProvider);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    logger.warn("Error disconnecting SSH client", e);
                }
            }
        }
    }

    public static String execute(
            String hostname, int port, String username, SSHCredential sshCredential, String command) {
        SSHClient client = null;
        try {
            client = new SSHClient();
            // Disable strict host key checking
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(hostname, port);

            // Load private key from bytes
            KeyProvider keyProvider = loadKeyProvider(sshCredential);
            client.authPublickey(username, keyProvider);

            // Execute command
            try (var session = client.startSession()) {
                var cmd = session.exec(command);

                // Read stdout
                var result = new StringBuilder();
                try (var stdout = cmd.getInputStream()) {
                    byte[] tmp = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = stdout.read(tmp)) != -1) {
                        result.append(new String(tmp, 0, bytesRead, StandardCharsets.UTF_8));
                    }
                }

                // Read stderr
                var stderr = new StringBuilder();
                try (var stderrStream = cmd.getErrorStream()) {
                    byte[] tmp = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = stderrStream.read(tmp)) != -1) {
                        stderr.append(new String(tmp, 0, bytesRead, StandardCharsets.UTF_8));
                    }
                }

                // Wait for command to complete
                cmd.join(30, TimeUnit.SECONDS);
                var exitStatus = cmd.getExitStatus();

                logger.debug("Output from command: " + result.toString());
                logger.debug("Exit status: " + exitStatus);

                if (exitStatus == null || exitStatus != 0) {
                    var stderrStr = stderr.toString();
                    if (stderrStr != null && stderrStr.length() > 0) {
                        logger.error("STDERR for command [" + command + "]: " + stderrStr);
                    }
                    throw new RuntimeException("SSH command [" + command + "] exited with exit status: " + exitStatus
                            + ", STDERR=" + stderrStr);
                }

                return result.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    logger.warn("Error disconnecting SSH client", e);
                }
            }
        }
    }

    /**
     * Load KeyProvider from SSHCredential bytes.
     */
    private static KeyProvider loadKeyProvider(SSHCredential sshCredential) throws IOException {
        var privateKeyStr = sshCredential.getPrivateKey();
        var passphrase = sshCredential.getPassphrase();

        // Create a temporary SSHClient to use loadKeys() method
        var tempClient = new SSHClient();
        PasswordFinder passwordFinder = null;
        if (passphrase != null && !passphrase.isEmpty()) {
            passwordFinder = PasswordUtils.createOneOff(passphrase.toCharArray());
        }

        return tempClient.loadKeys(privateKeyStr, null, passwordFinder);
    }
}
