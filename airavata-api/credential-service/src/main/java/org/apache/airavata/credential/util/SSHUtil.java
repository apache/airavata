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
package org.apache.airavata.credential.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.credential.ssh.SSHConnectionServiceImpl;
import org.apache.airavata.interfaces.SSHConnectionService;
import org.apache.airavata.interfaces.SSHConnectionService.*;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by machrist on 2/10/17.
 */
public class SSHUtil {

    private static final Logger logger = LoggerFactory.getLogger(SSHUtil.class);

    private static final SSHConnectionService sshService = new SSHConnectionServiceImpl();

    public static boolean validate(String hostname, int port, String username, SSHCredential sshCredential) {
        return sshService.validateCredential(
                hostname,
                port,
                username,
                sshCredential.getPublicKey(),
                sshCredential.getPrivateKey(),
                sshCredential.getPassphrase());
    }

    public static String execute(
            String hostname, int port, String username, SSHCredential sshCredential, String command) {
        try {
            SSHConnection connection = sshService.connectSimple(
                    hostname,
                    port,
                    username,
                    sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(),
                    sshCredential.getPassphrase());
            try {
                try (SSHSession session = connection.startSession()) {
                    SSHCommandResult cmd = session.exec(command);
                    String result = IOUtils.toString(cmd.getInputStream(), "UTF-8");
                    String stderr = IOUtils.toString(cmd.getErrorStream(), "UTF-8");
                    cmd.join(30, TimeUnit.SECONDS);
                    Integer exitStatus = cmd.getExitStatus();

                    logger.debug("Output from command: " + result);
                    logger.debug("Exit status: " + exitStatus);

                    if (exitStatus == null || exitStatus != 0) {
                        if (stderr != null && stderr.length() > 0) {
                            logger.error("STDERR for command [" + command + "]: " + stderr);
                        }
                        throw new RuntimeException("SSH command [" + command + "] exited with exit status: "
                                + exitStatus + ", STDERR=" + stderr);
                    }

                    return result;
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        // Test the validate method
        String username = System.getProperty("user.name");
        String privateKeyFilepath = System.getProperty("user.home") + "/.ssh/id_rsa";
        String publicKeyFilepath = privateKeyFilepath + ".pub";
        String passphrase = "changeme";
        String hostname = "changeme";

        Path privateKeyPath = Paths.get(privateKeyFilepath);
        Path publicKeyPath = Paths.get(publicKeyFilepath);

        SSHCredential sshCredential;
        try {
            sshCredential = SSHCredential.newBuilder()
                    .setPassphrase(passphrase)
                    .setPublicKey(new String(Files.readAllBytes(publicKeyPath), "UTF-8"))
                    .setPrivateKey(new String(Files.readAllBytes(privateKeyPath), "UTF-8"))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean result = validate(hostname, 22, username, sshCredential);
        System.out.println(result);
    }
}
