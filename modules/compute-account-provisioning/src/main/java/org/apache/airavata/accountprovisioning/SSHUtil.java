/*
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
 *
 */

package org.apache.airavata.accountprovisioning;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.model.credential.store.SSHCredential;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Created by machrist on 2/10/17.
 */
public class SSHUtil {

    public static boolean validate(String username, String hostname, int port, SSHCredential sshCredential) {

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
            throw new RuntimeException(e.getMessage(), e);
        } finally {
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
        boolean result = validate(username, hostname, 22, sshCredential);
        System.out.println(result);
    }
}
