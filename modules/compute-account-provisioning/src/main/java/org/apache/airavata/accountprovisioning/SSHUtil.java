/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.accountprovisioning;

import com.jcraft.jsch.*;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Created by machrist on 2/10/17.
 */
public class SSHUtil {

    private final static Logger logger = LoggerFactory.getLogger(SSHUtil.class);

    public static boolean validate(String hostname, int port, String username, SSHCredential sshCredential) {

        JSch jSch = new JSch();
        Session session = null;
        try {
            jSch.addIdentity(UUID.randomUUID().toString(), sshCredential.getPrivateKey().getBytes(), sshCredential.getPublicKey().getBytes(), sshCredential.getPassphrase().getBytes());
            session = jSch.getSession(username, hostname, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            return true;
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static String execute(String hostname, int port, String username, SSHCredential sshCredential, String command) {
        JSch jSch = new JSch();
        Session session = null;
        Channel channel = null;
        try {
            jSch.addIdentity(UUID.randomUUID().toString(), sshCredential.getPrivateKey().getBytes(), sshCredential.getPublicKey().getBytes(), sshCredential.getPassphrase().getBytes());
            session = jSch.getSession(username, hostname, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            ByteArrayOutputStream errOutputStream = new ByteArrayOutputStream();
            ((ChannelExec) channel).setErrStream(errOutputStream);
            channel.connect();

            try (InputStream in = channel.getInputStream()) {
                byte[] tmp = new byte[1024];
                String result = "";
                Integer exitStatus;

                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        result += new String(tmp, 0, i);
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        exitStatus = channel.getExitStatus();
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }

                logger.debug("Output from command: " + result);
                logger.debug("Exit status: " + exitStatus);

                if (exitStatus == null || exitStatus != 0) {
                    String stderr = errOutputStream.toString("UTF-8");
                    if (stderr != null && stderr.length() > 0) {
                        logger.error("STDERR for command [" + command + "]: " + stderr);
                    }
                    throw new RuntimeException("SSH command [" + command + "] exited with exit status: " + exitStatus + ", STDERR=" + stderr);
                }

                return result;
            }
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static void main(String[] args) throws JSchException {

        // Test the validate method
        String username = System.getProperty("user.name");
        String privateKeyFilepath = System.getProperty("user.home") + "/.ssh/id_rsa";
        String publicKeyFilepath = privateKeyFilepath + ".pub";
        String passphrase = "changeme";
        String hostname = "changeme";

        Path privateKeyPath = Paths.get(privateKeyFilepath);
        Path publicKeyPath = Paths.get(publicKeyFilepath);

        SSHCredential sshCredential = new SSHCredential();
        sshCredential.setPassphrase(passphrase);
        try {
            sshCredential.setPublicKey(new String(Files.readAllBytes(publicKeyPath), "UTF-8"));
            sshCredential.setPrivateKey(new String(Files.readAllBytes(privateKeyPath), "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean result = validate(hostname, 22, username, sshCredential);
        System.out.println(result);
    }
}
