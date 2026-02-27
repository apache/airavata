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
package org.apache.airavata.protocol.ssh;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import org.apache.airavata.credential.model.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SSHUtil {

    private static final Logger logger = LoggerFactory.getLogger(SSHUtil.class);

    private SSHUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generate SSH public key from private key PEM using SSHJ.
     */
    public static String generatePublicKey(String privateKeyPEM) throws Exception {
        try {
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
            logger.error("Error while generating the public key", e);
            throw new Exception("Error while generating the public key", e);
        }
    }

    /**
     * Validate SSH connectivity to a remote host using the given credential.
     */
    public static boolean validate(String hostname, int port, String username, SSHCredential sshCredential) {
        SSHClient client = null;
        try {
            client = new SSHClient();
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(hostname, port);
            var keyProvider = loadKeyProvider(sshCredential);
            client.authPublickey(username, keyProvider);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            disconnectQuietly(client);
        }
    }

    /**
     * Execute an SSH command on a remote host and return stdout.
     */
    public static String execute(
            String hostname, int port, String username, SSHCredential sshCredential, String command) {
        SSHClient client = null;
        try {
            client = new SSHClient();
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(hostname, port);
            KeyProvider keyProvider = loadKeyProvider(sshCredential);
            client.authPublickey(username, keyProvider);

            try (var session = client.startSession()) {
                var cmd = session.exec(command);

                var result = readStream(cmd.getInputStream());
                var stderr = readStream(cmd.getErrorStream());

                cmd.join(30, TimeUnit.SECONDS);
                var exitStatus = cmd.getExitStatus();

                logger.debug("Output from command: {}", result);
                logger.debug("Exit status: {}", exitStatus);

                if (exitStatus == null || exitStatus != 0) {
                    if (!stderr.isEmpty()) {
                        logger.error("STDERR for command [{}]: {}", command, stderr);
                    }
                    throw new IllegalStateException("SSH command [" + command + "] exited with exit status: "
                            + exitStatus + ", STDERR=" + stderr);
                }

                return result;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            disconnectQuietly(client);
        }
    }

    private static KeyProvider loadKeyProvider(SSHCredential sshCredential) throws IOException {
        var privateKeyStr = sshCredential.getPrivateKey();
        var passphrase = sshCredential.getPassphrase();
        PasswordFinder passwordFinder = null;
        if (passphrase != null && !passphrase.isEmpty()) {
            passwordFinder = PasswordUtils.createOneOff(passphrase.toCharArray());
        }
        try (var tempClient = new SSHClient()) {
            return tempClient.loadKeys(privateKeyStr, null, passwordFinder);
        }
    }

    private static String readStream(java.io.InputStream is) throws IOException {
        var sb = new StringBuilder();
        byte[] tmp = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(tmp)) != -1) {
            sb.append(new String(tmp, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    /**
     * Escapes shell metacharacters in file paths to prevent interpretation by remote shells.
     */
    public static String escapeSpecialCharacters(String inputString) {
        final String[] metaCharacters = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", "?", "&", "%"};

        for (String metaCharacter : metaCharacters) {
            if (inputString.contains(metaCharacter)) {
                inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return inputString;
    }

    private static void disconnectQuietly(SSHClient client) {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                logger.warn("Error disconnecting SSH client", e);
            }
        }
    }
}
