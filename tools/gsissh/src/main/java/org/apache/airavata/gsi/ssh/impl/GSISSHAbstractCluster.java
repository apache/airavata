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
package org.apache.airavata.gsi.ssh.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.CommandExecutor;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.authentication.SSHKeyAuthentication;
import org.apache.airavata.gsi.ssh.api.authentication.SSHPasswordAuthentication;
import org.apache.airavata.gsi.ssh.api.authentication.SSHPublicKeyAuthentication;
import org.apache.airavata.gsi.ssh.api.authentication.SSHPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.api.job.JobManagerConfiguration;
import org.apache.airavata.gsi.ssh.api.job.OutputParser;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.jsch.ExtendedJSch;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.gsi.ssh.util.SSHAPIUIKeyboardInteractive;
import org.apache.airavata.gsi.ssh.util.SSHKeyPasswordHandler;
import org.apache.airavata.gsi.ssh.util.SSHUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ExtendedSession;
import com.jcraft.jsch.GSISSHIdentityFile;
import com.jcraft.jsch.GSISSHIdentityRepository;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class GSISSHAbstractCluster implements Cluster {

    private static final Logger log = LoggerFactory.getLogger(GSISSHAbstractCluster.class);
    public static final String X509_CERT_DIR = "X509_CERT_DIR";
    public static final String SSH_SESSION_TIMEOUT = "ssh.session.timeout";

    public JobManagerConfiguration jobManagerConfiguration;

    private ServerInfo serverInfo;

    private AuthenticationInfo authenticationInfo;

    private Session session;

    private ConfigReader configReader;
	
    private JSch defaultJSch;

    private static Identity identityFile = null;

    public GSISSHAbstractCluster(ServerInfo serverInfo, AuthenticationInfo authenticationInfo, JobManagerConfiguration config) throws SSHApiException {
        this(serverInfo, authenticationInfo);
        this.jobManagerConfiguration = config;
    }

    public  GSISSHAbstractCluster(ServerInfo serverInfo, AuthenticationInfo authenticationInfo) throws SSHApiException {

        reconnect(serverInfo, authenticationInfo);
    }

    public GSISSHAbstractCluster(JobManagerConfiguration config) {
        this.jobManagerConfiguration = config;
    }
    private synchronized void reconnect(ServerInfo serverInfo, AuthenticationInfo authenticationInfo) throws SSHApiException {
        this.serverInfo = serverInfo;

        this.authenticationInfo = authenticationInfo;

        if (authenticationInfo instanceof GSIAuthenticationInfo) {
            JSch.setConfig("gssapi-with-mic.x509", "org.apache.airavata.gsi.ssh.GSSContextX509");
            JSch.setConfig("userauth.gssapi-with-mic", "com.jcraft.jsch.UserAuthGSSAPIWithMICGSSCredentials");
            System.setProperty(X509_CERT_DIR, (String) ((GSIAuthenticationInfo) authenticationInfo).getProperties().
                    get("X509_CERT_DIR"));
        }


        try {
            this.configReader = new ConfigReader();
        } catch (IOException e) {
            throw new SSHApiException("Unable to load system configurations.", e);
        }
        try {
        	 if(defaultJSch == null){
             	defaultJSch = createJSch(authenticationInfo);
             }
     	        log.debug("Connecting to server - " + serverInfo.getHost() + ":" + serverInfo.getPort() + " with user name - "
                     + serverInfo.getUserName());

        	session = createSession(defaultJSch,serverInfo.getUserName(), serverInfo.getHost(), serverInfo.getPort());
        	}
        	catch (Exception e) {
            throw new SSHApiException("An exception occurred while creating SSH session." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }

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
                    = (SSHPublicKeyFileAuthentication) authenticationInfo;
            String privateKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(serverInfo.getUserName(), serverInfo.getHost());

            logDebug("The private key file for vanilla SSH " + privateKeyFile);

            String publicKeyFile = sshPublicKeyFileAuthentication.
                    getPublicKeyFile(serverInfo.getUserName(), serverInfo.getHost());

            logDebug("The public key file for vanilla SSH " + publicKeyFile);

            try {
                identityFile = GSISSHIdentityFile.newInstance(privateKeyFile, null, defaultJSch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using files. " +
                        "(private key and public key)." +
                        "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                        " connecting user name - "
                        + serverInfo.getUserName() + " private key file - " + privateKeyFile + ", public key file - " +
                        publicKeyFile, e);
            }

            // Add identity to identity repository
            GSISSHIdentityRepository identityRepository = new GSISSHIdentityRepository(defaultJSch);
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
            try {
                String name = serverInfo.getUserName() + "_" + serverInfo.getHost();
                identityFile = GSISSHIdentityFile.newInstance(name,
                        sshPublicKeyAuthentication.getPrivateKey(serverInfo.getUserName(), serverInfo.getHost()),
                        sshPublicKeyAuthentication.getPublicKey(serverInfo.getUserName(), serverInfo.getHost()), defaultJSch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using byte arrays. " +
                        "(private key and public key)." +
                        "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                        " connecting user name - "
                        + serverInfo.getUserName(), e);
            }

            // Add identity to identity repository
            GSISSHIdentityRepository identityRepository = new GSISSHIdentityRepository(defaultJSch);
            identityRepository.add(identityFile);

            // Set repository to session
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
            session.connect(Integer.parseInt(configReader.getConfiguration(SSH_SESSION_TIMEOUT)));
        } catch (Exception e) {
            throw new SSHApiException("An exception occurred while connecting to server." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }
    }

    public synchronized JobDescriptor cancelJob(String jobID) throws SSHApiException {
        JobStatus jobStatus = getJobStatus(jobID);
        if (jobStatus == null || jobStatus == JobStatus.U) {
            log.info("Validation before cancel is failed, couldn't found job in remote host to cancel. Job may be already completed|failed|canceled");
            return null;
        }
        RawCommandInfo rawCommandInfo = jobManagerConfiguration.getCancelCommand(jobID);

        StandardOutReader stdOutReader = new StandardOutReader();
        log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        String outputifAvailable = getOutputifAvailable(stdOutReader, "Error reading output of job submission", jobManagerConfiguration.getBaseCancelCommand());
        // this might not be the case for all teh resources, if so Cluster implementation can override this method
        // because here after cancelling we try to get the job description and return it back
        try {
            return this.getJobDescriptorById(jobID);
        } catch (Exception e) {
            //its ok to fail to get status when the job is gone
            return null;
        }
    }

    public synchronized String submitBatchJobWithScript(String scriptPath, String workingDirectory) throws SSHApiException {
        this.scpTo(workingDirectory, scriptPath);

        // since this is a constant we do not ask users to fill this

//        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + this.jobManagerConfiguration.getSubmitCommand() + " " +
//                workingDirectory + File.separator + FilenameUtils.getName(scriptPath));

        RawCommandInfo rawCommandInfo = jobManagerConfiguration.getSubmitCommand(workingDirectory,scriptPath);
        StandardOutReader standardOutReader = new StandardOutReader();
        log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
        CommandExecutor.executeCommand(rawCommandInfo, this.session, standardOutReader);

        //Check whether pbs submission is successful or not, if it failed throw and exception in submitJob method
        // with the error thrown in qsub command
        //
        String outputifAvailable = getOutputifAvailable(standardOutReader,"Error reading output of job submission",jobManagerConfiguration.getBaseSubmitCommand());
        OutputParser outputParser = jobManagerConfiguration.getParser();
        return  outputParser.parseJobSubmission(outputifAvailable);
    }

    public synchronized String submitBatchJob(JobDescriptor jobDescriptor) throws SSHApiException {
        TransformerFactory factory = TransformerFactory.newInstance();
        URL resource = this.getClass().getClassLoader().getResource(jobManagerConfiguration.getJobDescriptionTemplateName());

        if (resource == null) {
            String error = "System configuration file '" + jobManagerConfiguration.getJobDescriptionTemplateName()
                    + "' not found in the classpath";
            throw new SSHApiException(error);
        }

        Source xslt = new StreamSource(new File(resource.getPath()));
        Transformer transformer;
        StringWriter results = new StringWriter();
        File tempPBSFile = null;
        try {
            // generate the pbs script using xslt
            transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new ByteArrayInputStream(jobDescriptor.toXML().getBytes()));
            transformer.transform(text, new StreamResult(results));
            String scriptContent = results.toString().replaceAll("^[ |\t]*\n$", "");
            if (scriptContent.startsWith("\n")) {
                scriptContent = scriptContent.substring(1);
            }
//            log.debug("generated PBS:" + results.toString());

            // creating a temporary file using pbs script generated above
            int number = new SecureRandom().nextInt();
            number = (number < 0 ? -number : number);

            tempPBSFile = new File(Integer.toString(number) + jobManagerConfiguration.getScriptExtension());
            FileUtils.writeStringToFile(tempPBSFile, scriptContent);

            //reusing submitBatchJobWithScript method to submit a job
            String jobID = null;
            int retry = 3;
            while(retry>0) {
                try {
                    jobID = this.submitBatchJobWithScript(tempPBSFile.getAbsolutePath(),
                            jobDescriptor.getWorkingDirectory());
                    retry=0;
                } catch (SSHApiException e) {
                    retry--;
                    if(retry==0) {
                        throw e;
                    }else{
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            log.error(e1.getMessage(), e1);
                        }
                        log.error("Error occured during job submission but doing a retry");
                    }
                }
            }
            log.debug("Job has successfully submitted, JobID : " + jobID);
            if (jobID != null) {
                return jobID.replace("\n", "");
            } else {
                return null;
            }
            } catch (TransformerConfigurationException e) {
            throw new SSHApiException("Error parsing PBS transformation", e);
        } catch (TransformerException e) {
            throw new SSHApiException("Error generating PBS script", e);
        } catch (IOException e) {
            throw new SSHApiException("An exception occurred while connecting to server." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        } finally {
            if (tempPBSFile != null) {
                tempPBSFile.delete();
            }
        }
    }


    public void generateJobScript(JobDescriptor jobDescriptor) throws SSHApiException {
        TransformerFactory factory = TransformerFactory.newInstance();
        URL resource = this.getClass().getClassLoader().getResource(jobManagerConfiguration.getJobDescriptionTemplateName());

        if (resource == null) {
            String error = "System configuration file '" + jobManagerConfiguration.getJobDescriptionTemplateName()
                    + "' not found in the classpath";
            throw new SSHApiException(error);
        }

        Source xslt = new StreamSource(new File(resource.getPath()));
        Transformer transformer;
        StringWriter results = new StringWriter();
        File tempPBSFile = null;
        try {
            // generate the pbs script using xslt
            transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new ByteArrayInputStream(jobDescriptor.toXML().getBytes()));
            transformer.transform(text, new StreamResult(results));
            String scriptContent = results.toString().replaceAll("^[ |\t]*\n$", "");
            if (scriptContent.startsWith("\n")) {
                scriptContent = scriptContent.substring(1);
            }
//            log.debug("generated PBS:" + results.toString());

            // creating a temporary file using pbs script generated above
            int number = new SecureRandom().nextInt();
            number = (number < 0 ? -number : number);

            tempPBSFile = new File(Integer.toString(number) + jobManagerConfiguration.getScriptExtension());
            log.info("File Path: " + tempPBSFile.getAbsolutePath());
            log.info("File Content: " + scriptContent);
            FileUtils.writeStringToFile(tempPBSFile, scriptContent);
        } catch (TransformerConfigurationException e) {
            throw new SSHApiException("Error parsing PBS transformation", e);
        } catch (TransformerException e) {
            throw new SSHApiException("Error generating PBS script", e);
        } catch (IOException e) {
            throw new SSHApiException("An exception occurred while connecting to server." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        } finally {
            if (tempPBSFile != null) {
                tempPBSFile.delete();
            }
        }
    }



    public synchronized JobDescriptor getJobDescriptorById(String jobID) throws SSHApiException {
        RawCommandInfo rawCommandInfo = jobManagerConfiguration.getMonitorCommand(jobID);
        StandardOutReader stdOutReader = new StandardOutReader();
        log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        String result = getOutputifAvailable(stdOutReader, "Error getting job information from the resource !",jobManagerConfiguration.getBaseMonitorCommand());
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobManagerConfiguration.getParser().parseSingleJob(jobDescriptor, result);
        return jobDescriptor;
    }

    public synchronized JobStatus getJobStatus(String jobID) throws SSHApiException {
        RawCommandInfo rawCommandInfo = jobManagerConfiguration.getMonitorCommand(jobID);
        StandardOutReader stdOutReader = new StandardOutReader();
        log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        String result = getOutputifAvailable(stdOutReader, "Error getting job information from the resource !", jobManagerConfiguration.getBaseMonitorCommand());
        return jobManagerConfiguration.getParser().parseJobStatus(jobID, result);
    }

    @Override
    public String getJobIdByJobName(String jobName, String userName) throws SSHApiException {
        RawCommandInfo rawCommandInfo = jobManagerConfiguration.getJobIdMonitorCommand(jobName, userName);
        StandardOutReader stdOutReader = new StandardOutReader();
        log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        String result = getOutputifAvailable(stdOutReader, "Error getting job information from the resource !",
                jobManagerConfiguration.getJobIdMonitorCommand(jobName,userName).getCommand());
        return jobManagerConfiguration.getParser().parseJobId(jobName, result);
    }

    private static void logDebug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    public JobManagerConfiguration getJobManagerConfiguration() {
        return jobManagerConfiguration;
    }

    public void setJobManagerConfiguration(JobManagerConfiguration jobManagerConfiguration) {
        this.jobManagerConfiguration = jobManagerConfiguration;
    }

    public synchronized void scpTo(String remoteFile, String localFile) throws SSHApiException {
        int retry = 3;
        while (retry > 0) {
            try {
                if (!session.isConnected()) {
                    session.connect();
                }
                log.info("Transfering file:/" + localFile + " To:" + serverInfo.getHost() + ":" + remoteFile);
                SSHUtils.scpTo(remoteFile, localFile, session);
                retry = 0;
            } catch (IOException e) {
                retry--;
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                            + serverInfo.getHost() + ":rFile : " + remoteFile, e);
                }
            } catch (JSchException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                            + serverInfo.getHost() + ":rFile : " + remoteFile, e);
                }
            }
        }
    }

    public synchronized void scpFrom(String remoteFile, String localFile) throws SSHApiException {
        int retry = 3;
        while(retry>0) {
            try {
                if (!session.isConnected()) {
                    session.connect();
                }
                log.info("Transfering from:" + serverInfo.getHost() + ":" + remoteFile + " To:" + "file:/" + localFile);
                SSHUtils.scpFrom(remoteFile, localFile, session);
                retry=0;
            } catch (IOException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                            + serverInfo.getHost() + ":rFile", e);
                }else{
                    log.error("Error performing scp but doing a retry");
                }
            } catch (JSchException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if(retry==0) {
                    throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                            + serverInfo.getHost() + ":rFile", e);
                }else{
                    log.error("Error performing scp but doing a retry");
                }
            }
        }
    }
    
    public synchronized void scpThirdParty(String remoteFileSource, String remoteFileTarget) throws SSHApiException {
        try {
            if(!session.isConnected()){
                session.connect();
            }
            log.info("Transfering from:" + remoteFileSource + " To: " + remoteFileTarget);
            SSHUtils.scpThirdParty(remoteFileSource, remoteFileTarget, session);
        } catch (IOException e) {
            throw new SSHApiException("Failed during scping  file:" + remoteFileSource + " to remote file "
                    +remoteFileTarget , e);
        } catch (JSchException e) {
            throw new SSHApiException("Failed during scping  file:" + remoteFileSource + " to remote file "
                    +remoteFileTarget, e);
        }
    }

    public synchronized void makeDirectory(String directoryPath) throws SSHApiException {
        int retry = 3;
        while (retry > 0) {
            try {
                if (!session.isConnected()) {
                    session.connect();
                }
                log.info("Creating directory: " + serverInfo.getHost() + ":" + directoryPath);
                SSHUtils.makeDirectory(directoryPath, session);
                retry = 0;
            } catch (IOException e) {
                throw new SSHApiException("Failed during creating directory:" + directoryPath + " to remote file "
                        + serverInfo.getHost() + ":rFile", e);
            } catch (JSchException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during creating directory :" + directoryPath + " to remote file "
                            + serverInfo.getHost() + ":rFile", e);
                }
            } catch (SSHApiException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during creating directory :" + directoryPath + " to remote file "
                            + serverInfo.getHost() + ":rFile", e);
                }
            }
        }
    }

    public synchronized List<String> listDirectory(String directoryPath) throws SSHApiException {
        int retry = 3;
        List<String> files = null;
        while (retry > 0) {
            try {
                if (!session.isConnected()) {
                    session.connect();
                }
                log.info("Listing directory: " + serverInfo.getHost() + ":" + directoryPath);
                files = SSHUtils.listDirectory(directoryPath, session, false);
                retry=0;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during listing directory:" + directoryPath + " to remote file ", e);
                }
            } catch (JSchException e) {
                retry--;
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during listing directory :" + directoryPath + " to remote file ", e);
                }
            }catch (SSHApiException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during listing directory :" + directoryPath + " to remote file "
                            + serverInfo.getHost() + ":rFile", e);
                }
            }
        }
        return files;
    }
    public synchronized List<String> listDirectory(String directoryPath, boolean recursive) throws SSHApiException {
        int retry = 3;
        List<String> files = null;
        while (retry > 0) {
            try {
                if (!session.isConnected()) {
                    session.connect();
                }
                log.info("Listing directory: " + serverInfo.getHost() + ":" + directoryPath);
                files = SSHUtils.listDirectory(directoryPath, session, recursive);
                retry=0;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during listing directory:" + directoryPath + " to remote file ", e);
                }
            } catch (JSchException e) {
                retry--;
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during listing directory :" + directoryPath + " to remote file ", e);
                }
            }catch (SSHApiException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed during listing directory :" + directoryPath + " to remote file "
                            + serverInfo.getHost() + ":rFile", e);
                }
            }
        }
        return files;
    }

    @Deprecated
    public synchronized void getJobStatuses(String userName, Map<String,JobStatus> jobIDs)throws SSHApiException {
        int retry = 3;
        RawCommandInfo rawCommandInfo = jobManagerConfiguration.getUserBasedMonitorCommand(userName);
        StandardOutReader stdOutReader = new StandardOutReader();
        while (retry > 0){
            try {
                log.info("Executing RawCommand : " + rawCommandInfo.getCommand());
                CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
                retry=0;
            } catch (SSHApiException e) {
                retry--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                reconnect(serverInfo, authenticationInfo);
                if (retry == 0) {
                    throw new SSHApiException("Failed Getting statuses  to remote file", e);
                }
            }
        }
        String result = getOutputifAvailable(stdOutReader, "Error getting job information from the resource !", jobManagerConfiguration.getBaseMonitorCommand());
        jobManagerConfiguration.getParser().parseJobStatuses(userName, jobIDs, result);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    /**
     * This gaurantee to return a valid session
     *
     * @return
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * This method will read standard output and if there's any it will be parsed
     *
     * @param jobIDReaderCommandOutput
     * @param errorMsg
     * @return
     * @throws SSHApiException
     */
    private String getOutputifAvailable(StandardOutReader jobIDReaderCommandOutput, String errorMsg, String command) throws SSHApiException {
        String stdOutputString = jobIDReaderCommandOutput.getStdOutputString();
        String stdErrorString = jobIDReaderCommandOutput.getStdErrorString();
        log.info("StandardOutput Returned:" + stdOutputString);
        log.info("StandardError  Returned:" +stdErrorString);
        String[] list = command.split(File.separator);
        command = list[list.length - 1];
        // We are checking for stderr containing the command issued. Thus ignores the verbose logs in stderr.
        if (stdErrorString != null && stdErrorString.contains(command.trim()) && !stdErrorString.contains("Warning")) {
            log.error("Standard Error output : " + stdErrorString);
            throw new SSHApiException(errorMsg + "\n\r StandardOutput: "+ stdOutputString + "\n\r StandardError: "+ stdErrorString);
        }else if(stdOutputString.contains("error")){
            throw new SSHApiException(errorMsg + "\n\r StandardOutput: "+ stdOutputString + "\n\r StandardError: "+ stdErrorString);
        }
        return stdOutputString;
    }

    public void disconnect() throws SSHApiException {
    	if(getSession().isConnected()){
    		getSession().disconnect();
    	}
    }
    /**
	
	 *            the file system abstraction which will be necessary to
	 *            perform certain file system operations.
	 * @return the new default JSch implementation.
	 * @throws JSchException
	 *             known host keys cannot be loaded.
	 */
	protected JSch createJSch(AuthenticationInfo authenticationInfo) throws JSchException {
//		final File fs = new File(System.getProperty("user.home"));
		if(authenticationInfo instanceof GSIAuthenticationInfo){
			final JSch jsch = new ExtendedJSch();
//			knownHosts(jsch, fs);
			return jsch;
		}else{
		final JSch jsch = new JSch();
//		knownHosts(jsch, fs);
		return jsch;
		}
		
	}
	/**
	 * Create a new remote session for the requested address.
	 *
	 * @param user
	 *            login to authenticate as.
	 * @param host
	 *            server name to connect to.
	 * @param port
	 *            port number of the SSH daemon (typically 22).
	 * @return new session instance, but otherwise unconfigured.
	 * @throws JSchException
	 *             the session could not be created.
	 */
	private Session createSession(JSch jsch, String user, String host, int port) throws JSchException {
		final Session session = jsch.getSession(user, host, port);
		// We retry already in getSession() method. JSch must not retry
		// on its own.
		session.setConfig("MaxAuthTries", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		session.setTimeout(Integer.parseInt(configReader.getConfiguration(SSH_SESSION_TIMEOUT)));
	    java.util.Properties config = this.configReader.getProperties();
	    session.setConfig(config);
	    
    	return session;
	}
	private static void knownHosts(final JSch sch,final File home) throws JSchException {
		if (home == null)
			return;
		final File known_hosts = new File(new File(home, ".ssh"), "known_hosts"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			final FileInputStream in = new FileInputStream(known_hosts);
			try {
				sch.setKnownHosts(in);
			} finally {
				in.close();
			}
		} catch (FileNotFoundException none) {
			// Oh well. They don't have a known hosts in home.
		} catch (IOException err) {
			// Oh well. They don't have a known hosts in home.
		}
	}
}
