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
package org.apache.airavata.job.monitor.impl.pull.qstat;

import com.jcraft.jsch.*;
import org.apache.airavata.gsi.ssh.api.CommandExecutor;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.authentication.*;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.impl.RawCommandInfo;
import org.apache.airavata.gsi.ssh.impl.StandardOutReader;
import org.apache.airavata.gsi.ssh.jsch.ExtendedJSch;
import org.apache.airavata.gsi.ssh.util.SSHAPIUIKeyboardInteractive;
import org.apache.airavata.gsi.ssh.util.SSHKeyPasswordHandler;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.model.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResourceConnection {
    static {
        JSch.setConfig("gssapi-with-mic.x509", "org.apache.airavata.gsi.ssh.GSSContextX509");
        JSch.setConfig("userauth.gssapi-with-mic", "com.jcraft.jsch.UserAuthGSSAPIWithMICGSSCredentials");

    }

    private static final Logger log = LoggerFactory.getLogger(ResourceConnection.class);
    public static final String X509_CERT_DIR = "X509_CERT_DIR";
    public static final String SSH_SESSION_TIMEOUT = "ssh.session.timeout";

    private Session session;

    private ConfigReader configReader;

    private String installedPath;

    public ResourceConnection(MonitorID monitorID, String installedPath) throws SSHApiException {
        AuthenticationInfo authenticationInfo = monitorID.getAuthenticationInfo();
        String hostAddress = monitorID.getHost().getType().getHostAddress();
        String userName = monitorID.getUserName();
        int port = monitorID.getPort();
        if (authenticationInfo instanceof GSIAuthenticationInfo) {
            System.setProperty(X509_CERT_DIR, (String) ((GSIAuthenticationInfo) authenticationInfo).getProperties().
                    get("X509_CERT_DIR"));
        }
        if (installedPath == null) {
            throw new SSHApiException("Installed path cannot be null !!");
        }
        if (installedPath.endsWith("/")) {
            this.installedPath = installedPath;
        } else {
            this.installedPath = installedPath + "/";
        }


        try {
            this.configReader = new ConfigReader();
        } catch (IOException e) {
            throw new SSHApiException("Unable to load system configurations.", e);
        }
        JSch jSch = new ExtendedJSch();

        log.debug("Connecting to server - " + monitorID.getHost().getType().getHostName() + ":" + "22" + " with user name - "
                + userName);

        try {
            session = jSch.getSession(userName, hostAddress, 22);
            session.setTimeout(Integer.parseInt(configReader.getConfiguration(SSH_SESSION_TIMEOUT)));
        } catch (Exception e) {
            throw new SSHApiException("An exception occurred while creating SSH session." +
                    "Connecting server - " + hostAddress + ":" + 22 +
                    " connecting user name - "
                    + userName, e);
        }

        java.util.Properties config = this.configReader.getProperties();
        session.setConfig(config);


        //=============================================================
        // Handling vanilla SSH pieces
        //=============================================================
        if (authenticationInfo instanceof SSHPasswordAuthentication) {
            String password = ((SSHPasswordAuthentication) authenticationInfo).
                    getPassword(userName, hostAddress);

            session.setUserInfo(new SSHAPIUIKeyboardInteractive(password));

            // TODO figure out why we need to set password to session
            session.setPassword(password);

        } else if (authenticationInfo instanceof SSHPublicKeyFileAuthentication) {
            SSHPublicKeyFileAuthentication sshPublicKeyFileAuthentication
                    = (SSHPublicKeyFileAuthentication) authenticationInfo;

            String privateKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(userName, hostAddress);

            log.debug("The private key file for vanilla SSH " + privateKeyFile);

            String publicKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(userName, hostAddress);

            log.debug("The public key file for vanilla SSH " + publicKeyFile);

            Identity identityFile;

            try {
                identityFile = GSISSHIdentityFile.newInstance(privateKeyFile, null, jSch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using files. " +
                        "(private key and public key)." +
                        "Connecting server - " + hostAddress + ":" + port +
                        " connecting user name - "
                        + userName + " private key file - " + privateKeyFile + ", public key file - " +
                        publicKeyFile, e);
            }

            // Add identity to identity repository
            GSISSHIdentityRepository identityRepository = new GSISSHIdentityRepository(jSch);
            identityRepository.add(identityFile);

            // Set repository to session
            session.setIdentityRepository(identityRepository);

            // Set the user info
            SSHKeyPasswordHandler sshKeyPasswordHandler
                    = new SSHKeyPasswordHandler((SSHKeyAuthentication) authenticationInfo);

            session.setUserInfo(sshKeyPasswordHandler);

        } else if (authenticationInfo instanceof SSHPublicKeyAuthentication) {

            SSHPublicKeyAuthentication sshPublicKeyAuthentication
                    = (SSHPublicKeyAuthentication) authenticationInfo;

            Identity identityFile;

            try {
                String name = userName + "_" + hostAddress;
                identityFile = GSISSHIdentityFile.newInstance(name,
                        sshPublicKeyAuthentication.getPrivateKey(userName, hostAddress),
                        sshPublicKeyAuthentication.getPublicKey(userName, hostAddress), jSch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using byte arrays. " +
                        "(private key and public key)." +
                        "Connecting server - " + hostAddress + ":" + port +
                        " connecting user name - "
                        + userName, e);
            }

            // Add identity to identity repository
            GSISSHIdentityRepository identityRepository = new GSISSHIdentityRepository(jSch);
            identityRepository.add(identityFile);

            // Set repository to session                                                                            j
            session.setIdentityRepository(identityRepository);

            // Set the user info
            SSHKeyPasswordHandler sshKeyPasswordHandler
                    = new SSHKeyPasswordHandler((SSHKeyAuthentication) authenticationInfo);

            session.setUserInfo(sshKeyPasswordHandler);

        }

        // Not a good way, but we dont have any choice
        if (session instanceof ExtendedSession) {
            if (authenticationInfo instanceof GSIAuthenticationInfo) {
                ((ExtendedSession) session).setAuthenticationInfo((GSIAuthenticationInfo) authenticationInfo);
            }
        }

        try {
            session.connect();
        } catch (JSchException e) {
            throw new SSHApiException("An exception occurred while connecting to server." +
                    "Connecting server - " + hostAddress + ":" + port +
                    " connecting user name - "
                    + userName, e);
        }
        System.out.println(session.isConnected());
    }

    public JobState getJobStatus(MonitorID monitorID) throws SSHApiException {
        String jobID = monitorID.getJobID();
        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + "qstat -f " + jobID);

        StandardOutReader stdOutReader = new StandardOutReader();
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);


        String result = getOutputifAvailable(stdOutReader, "Error getting job status with job ID: " + jobID);
        String[] info = result.split("\n");
        String[] line = null;
        for (String anInfo : info) {
            if (anInfo.contains("=")) {
                line = anInfo.split("=", 2);
                if (line.length != 0) {
                    if (line[0].contains("job_state")) {
                       return getStatusFromString(line[1].replaceAll(" ", ""));
                    }
                }
            }
        }
        return null;
    }

    private JobState getStatusFromString(String status) {
        if(status != null){
            if("C".equals(status)){
                return JobState.COMPLETE;
            }else if("E".equals(status)){
                return JobState.COMPLETE;
            }else if("H".equals(status)){
                return JobState.HELD;
            }else if("Q".equals(status)){
                return JobState.QUEUED;
            }else if("R".equals(status)){
                return JobState.ACTIVE;
            }else if ("T".equals(status)) {
                return JobState.HELD;
            } else if ("W".equals(status)) {
                return JobState.QUEUED;
            } else if ("S".equals(status)) {
                return JobState.SUSPENDED;
            }
        }
        return null;
    }
    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * This method will read standard output and if there's any it will be parsed
     *
     * @param jobIDReaderCommandOutput
     * @param errorMsg
     * @return
     * @throws SSHApiException
     */
    private String getOutputifAvailable(StandardOutReader jobIDReaderCommandOutput, String errorMsg) throws SSHApiException {
        String stdOutputString = jobIDReaderCommandOutput.getStdOutputString();
        String stdErrorString = jobIDReaderCommandOutput.getStdErrorString();

        if (stdOutputString == null && "".equals(stdOutputString) ||
                ((stdErrorString != null) && !("".equals(stdErrorString)))) {
            log.error("Standard Error output : " + stdErrorString);
            throw new SSHApiException(errorMsg + stdErrorString);
        }
        return stdOutputString;
    }
}
