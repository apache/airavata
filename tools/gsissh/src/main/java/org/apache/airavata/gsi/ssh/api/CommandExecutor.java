/*
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
 *
*/
package org.apache.airavata.gsi.ssh.api;

import com.jcraft.jsch.*;
import org.apache.airavata.gsi.ssh.api.authentication.*;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.jsch.ExtendedJSch;
import org.apache.airavata.gsi.ssh.util.SSHAPIUIKeyboardInteractive;
import org.apache.airavata.gsi.ssh.util.SSHKeyPasswordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a generic class which take care of command execution
 * in a shell, this is used through out the other places of the API.
 */
public class CommandExecutor {
    static {
        JSch.setConfig("gssapi-with-mic.x509", "org.apache.airavata.gsi.ssh.GSSContextX509");
        JSch.setConfig("userauth.gssapi-with-mic", "com.jcraft.jsch.UserAuthGSSAPIWithMICGSSCredentials");

    }

    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    public static final String X509_CERT_DIR = "X509_CERT_DIR";

    /**
     * This will execute the given command with given session and session is not closed at the end.
     *
     * @param commandInfo
     * @param session
     * @param commandOutput
     * @throws SSHApiException
     */
    public static Session executeCommand(CommandInfo commandInfo, Session session,
                                         CommandOutput commandOutput) throws SSHApiException {

        String command = commandInfo.getCommand();

        Channel channel = null;
        try {
            if (!session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
        } catch (JSchException e) {
            session.disconnect();

            throw new SSHApiException("Unable to execute command - ", e);
        }

        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(commandOutput.getStandardError());
        try {
            channel.connect();
        } catch (JSchException e) {

            channel.disconnect();
            session.disconnect();
            throw new SSHApiException("Unable to retrieve command output. Command - " + command, e);
        }


        commandOutput.onOutput(channel);
        //Only disconnecting the channel, session can be reused
        channel.disconnect();
        return session;
    }

    /**
     * This will not reuse any session, it will create the session and close it at the end
     *
     * @param commandInfo        Encapsulated information about command. E.g :- executable name
     *                           parameters etc ...
     * @param serverInfo         The SSHing server information.
     * @param authenticationInfo Security data needs to be communicated with remote server.
     * @param commandOutput      The output of the command.
     * @param configReader       configuration required for ssh/gshissh connection
     * @throws SSHApiException   throw exception when error occurs
     */
    public static void executeCommand(CommandInfo commandInfo, ServerInfo serverInfo,
                                      AuthenticationInfo authenticationInfo,
                                      CommandOutput commandOutput, ConfigReader configReader) throws SSHApiException {

        if (authenticationInfo instanceof GSIAuthenticationInfo) {
            System.setProperty(X509_CERT_DIR, (String) ((GSIAuthenticationInfo)authenticationInfo).getProperties().
                    get("X509_CERT_DIR"));
        }


        JSch jsch = new ExtendedJSch();

        log.debug("Connecting to server - " + serverInfo.getHost() + ":" + serverInfo.getPort() + " with user name - "
                + serverInfo.getUserName());

        Session session;

        try {
            session = jsch.getSession(serverInfo.getUserName(), serverInfo.getHost(), serverInfo.getPort());
        } catch (JSchException e) {
            throw new SSHApiException("An exception occurred while creating SSH session." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }

        java.util.Properties config = configReader.getProperties();
        session.setConfig(config);

        //=============================================================
        // Handling vanilla SSH pieces
        //=============================================================
        if (authenticationInfo instanceof SSHPasswordAuthentication) {
            String password = ((SSHPasswordAuthentication) authenticationInfo).
                    getPassword(serverInfo.getUserName(), serverInfo.getHost());

            session.setUserInfo(new SSHAPIUIKeyboardInteractive(password));

            // TODO figure out why we need to set password to session
            session.setPassword(password);

        } else if (authenticationInfo instanceof SSHPublicKeyFileAuthentication) {
            SSHPublicKeyFileAuthentication sshPublicKeyFileAuthentication
                    = (SSHPublicKeyFileAuthentication)authenticationInfo;

            String privateKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(serverInfo.getUserName(), serverInfo.getHost());

            logDebug("The private key file for vanilla SSH " + privateKeyFile);

            String publicKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(serverInfo.getUserName(), serverInfo.getHost());

            logDebug("The public key file for vanilla SSH " + publicKeyFile);

            Identity identityFile;

            try {
                identityFile = GSISSHIdentityFile.newInstance(privateKeyFile, null, jsch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using files. " +
                        "(private key and public key)." +
                        "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                        " connecting user name - "
                        + serverInfo.getUserName() + " private key file - " + privateKeyFile + ", public key file - " +
                        publicKeyFile, e);
            }

            // Add identity to identity repository
            GSISSHIdentityRepository identityRepository = new GSISSHIdentityRepository(jsch);
            identityRepository.add(identityFile);

            // Set repository to session
            session.setIdentityRepository(identityRepository);

            // Set the user info
            SSHKeyPasswordHandler sshKeyPasswordHandler
                    = new SSHKeyPasswordHandler((SSHKeyAuthentication)authenticationInfo);

            session.setUserInfo(sshKeyPasswordHandler);

        } else if (authenticationInfo instanceof SSHPublicKeyAuthentication) {

            SSHPublicKeyAuthentication sshPublicKeyAuthentication
                    = (SSHPublicKeyAuthentication)authenticationInfo;

            Identity identityFile;

            try {
                String name = serverInfo.getUserName() + "_" + serverInfo.getHost();
                identityFile = GSISSHIdentityFile.newInstance(name,
                        sshPublicKeyAuthentication.getPrivateKey(serverInfo.getUserName(), serverInfo.getHost()),
                        sshPublicKeyAuthentication.getPublicKey(serverInfo.getUserName(), serverInfo.getHost()), jsch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using byte arrays. " +
                        "(private key and public key)." +
                        "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                        " connecting user name - "
                        + serverInfo.getUserName(), e);
            }

            // Add identity to identity repository
            GSISSHIdentityRepository identityRepository = new GSISSHIdentityRepository(jsch);
            identityRepository.add(identityFile);

            // Set repository to session
            session.setIdentityRepository(identityRepository);

            // Set the user info
            SSHKeyPasswordHandler sshKeyPasswordHandler
                    = new SSHKeyPasswordHandler((SSHKeyAuthentication)authenticationInfo);

            session.setUserInfo(sshKeyPasswordHandler);

        }

        // Not a good way, but we dont have any choice
        if (session instanceof ExtendedSession) {
            if (authenticationInfo instanceof GSIAuthenticationInfo) {
                ((ExtendedSession) session).setAuthenticationInfo((GSIAuthenticationInfo)authenticationInfo);
            }
        }

        try {
            session.connect();
        } catch (JSchException e) {
            throw new SSHApiException("An exception occurred while connecting to server." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }

        String command = commandInfo.getCommand();

        Channel channel;
        try {
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
        } catch (JSchException e) {
            session.disconnect();

            throw new SSHApiException("Unable to execute command - " + command +
                    " on server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }


        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(commandOutput.getStandardError());

        try {
            channel.connect();
        } catch (JSchException e) {

            channel.disconnect();
            session.disconnect();

            throw new SSHApiException("Unable to retrieve command output. Command - " + command +
                    " on server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }

        commandOutput.onOutput(channel);

        channel.disconnect();
        session.disconnect();
    }

    private static void logDebug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }


}
