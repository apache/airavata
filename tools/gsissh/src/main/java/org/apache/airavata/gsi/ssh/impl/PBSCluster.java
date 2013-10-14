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

import com.jcraft.jsch.*;
import org.apache.airavata.gsi.ssh.api.*;
import org.apache.airavata.gsi.ssh.api.authentication.*;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.jsch.ExtendedJSch;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.gsi.ssh.util.SSHAPIUIKeyboardInteractive;
import org.apache.airavata.gsi.ssh.util.SSHKeyPasswordHandler;
import org.apache.airavata.gsi.ssh.util.SSHUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * This is the default implementation of a cluster.
 * this has most of the methods to be used by the end user of the
 * library.
 */
public class PBSCluster implements Cluster {
    static {
        JSch.setConfig("gssapi-with-mic.x509", "org.apache.airavata.gsi.ssh.GSSContextX509");
        JSch.setConfig("userauth.gssapi-with-mic", "com.jcraft.jsch.UserAuthGSSAPIWithMICGSSCredentials");

    }

    private static final Logger log = LoggerFactory.getLogger(PBSCluster.class);
    public static final String X509_CERT_DIR = "X509_CERT_DIR";
    public static final String SSH_SESSION_TIMEOUT = "ssh.session.timeout";
    public static final String PBSTEMPLATE_XSLT = "PBSTemplate.xslt";

    private Node[] Nodes;

    private ServerInfo serverInfo;

    private AuthenticationInfo authenticationInfo;

    private Session session;

    private ConfigReader configReader;

    private String installedPath;

    public PBSCluster(ServerInfo serverInfo, AuthenticationInfo authenticationInfo, String installedPath) throws SSHApiException {

        this.serverInfo = serverInfo;

        this.authenticationInfo = authenticationInfo;

        if (authenticationInfo instanceof GSIAuthenticationInfo) {
            System.setProperty(X509_CERT_DIR, (String) ((GSIAuthenticationInfo) authenticationInfo).getProperties().
                    get("X509_CERT_DIR"));
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

        log.debug("Connecting to server - " + serverInfo.getHost() + ":" + serverInfo.getPort() + " with user name - "
                + serverInfo.getUserName());

        try {
            session = jSch.getSession(serverInfo.getUserName(), serverInfo.getHost(), serverInfo.getPort());
            session.setTimeout(Integer.parseInt(configReader.getConfiguration(SSH_SESSION_TIMEOUT)));
        } catch (Exception e) {
            throw new SSHApiException("An exception occurred while creating SSH session." +
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }

        java.util.Properties config = this.configReader.getProperties();
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
                    = (SSHPublicKeyFileAuthentication) authenticationInfo;

            String privateKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(serverInfo.getUserName(), serverInfo.getHost());

            logDebug("The private key file for vanilla SSH " + privateKeyFile);

            String publicKeyFile = sshPublicKeyFileAuthentication.
                    getPrivateKeyFile(serverInfo.getUserName(), serverInfo.getHost());

            logDebug("The public key file for vanilla SSH " + publicKeyFile);

            Identity identityFile;

            try {
                identityFile = GSISSHIdentityFile.newInstance(privateKeyFile, null, jSch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using files. " +
                        "(private key and public key)." +
                        "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                        " connecting user name - "
                        + serverInfo.getUserName() + " private key file - " + privateKeyFile + ", public key file - " +
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
                String name = serverInfo.getUserName() + "_" + serverInfo.getHost();
                identityFile = GSISSHIdentityFile.newInstance(name,
                        sshPublicKeyAuthentication.getPrivateKey(serverInfo.getUserName(), serverInfo.getHost()),
                        sshPublicKeyAuthentication.getPublicKey(serverInfo.getUserName(), serverInfo.getHost()), jSch);
            } catch (JSchException e) {
                throw new SSHApiException("An exception occurred while initializing keys using byte arrays. " +
                        "(private key and public key)." +
                        "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                        " connecting user name - "
                        + serverInfo.getUserName(), e);
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
                    "Connecting server - " + serverInfo.getHost() + ":" + serverInfo.getPort() +
                    " connecting user name - "
                    + serverInfo.getUserName(), e);
        }
        System.out.println(session.isConnected());
    }


    public String submitBatchJobWithPBS(String pbsFilePath, String workingDirectory) throws SSHApiException {

        this.scpTo(workingDirectory, pbsFilePath);

        // since this is a constant we do not ask users to fill this
        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + "qsub " +
                workingDirectory + File.separator + FilenameUtils.getName(pbsFilePath));

        StandardOutReader jobIDReaderCommandOutput = new StandardOutReader();
        CommandExecutor.executeCommand(rawCommandInfo, this.session, jobIDReaderCommandOutput);

        //Check whether pbs submission is successful or not, if it failed throw and exception in submitJob method
        // with the error thrown in qsub command
        //

        if (!jobIDReaderCommandOutput.getStdErrorString().equals("")) {
            throw new SSHApiException(jobIDReaderCommandOutput.getStandardError().toString());
        } else {
            return jobIDReaderCommandOutput.getStdOutputString();
        }
    }

    public String submitBatchJob(JobDescriptor jobDescriptor) throws SSHApiException {
        TransformerFactory factory = TransformerFactory.newInstance();
        URL resource = this.getClass().getClassLoader().getResource(PBSTEMPLATE_XSLT);

        if (resource == null) {
            String error = "System configuration file '" + PBSCluster.PBSTEMPLATE_XSLT
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

            System.out.println(results.toString());
//            log.debug("generated PBS:" + results.toString());

            // creating a temporary file using pbs script generated above
            int number = new SecureRandom().nextInt();
            number = (number < 0 ? -number : number);

            tempPBSFile = new File(Integer.toString(number) + ".pbs");
            FileUtils.writeStringToFile(tempPBSFile, results.toString());

            //reusing submitBatchJobWithPBS method to submit a job

            String jobID = this.submitBatchJobWithPBS(tempPBSFile.getAbsolutePath(),
                    jobDescriptor.getWorkingDirectory());
            log.debug("Job has successfully submitted, JobID : " + jobID);
            return jobID.replace("\n", "");
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


    public Cluster loadCluster() throws SSHApiException {
        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + "qnodes");

        StandardOutReader stdOutReader = new StandardOutReader();
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        if (stdOutReader.getStdErrorString() != null) {
            throw new SSHApiException(stdOutReader.getStandardError().toString());
        }
        String result = stdOutReader.getStdOutputString();
        String[] Nodes = result.split("\n");
        String[] line;
        String header, value;
        Node Node;
        Core[] Cores = null;
        ArrayList<Node> Machines = new ArrayList<Node>();
        int i = 0;
        while (i < Nodes.length) {
            Node = new Node();
            Node.setName(Nodes[i]);
            i++;

            while (i < Nodes.length) {
                if (!Nodes[i].startsWith(" ")) {
                    i++;
                    break;
                }

                line = Nodes[i].split("=");
                header = line[0].trim();
                value = line[1].trim();

                if ("state".equals(header))
                    Node.setState(value);
                else if ("np".equals(header)) {
                    Node.setNp(value);
                    int np = Integer.parseInt(Node.getNp());
                    Cores = new Core[np];
                    for (int n = 0; n < np; n++) {
                        Cores[n] = new Core("" + n);
                    }
                } else if ("ntype".equals(header))
                    Node.setNtype(value);
                else if ("jobs".equals(header)) {
                    String[] jobs = value.split(", ");
                    JobDescriptor jo;
                    //Job[] Jobs = new Job[jobs.length];
                    for (String job : jobs) {
                        String[] c = job.split("/");
                        String Jid = c[1];
                        jo = this.getJobDescriptorById(Jid);
                        int core = Integer.parseInt(c[0]);
                        assert Cores != null;
                        Cores[core].setJob(jo);

                    }


                }
                i++;
            }
            Node.setCores(Cores);
            Machines.add(Node);
        }
        this.setNodes(Machines.toArray(new Node[Machines.size()]));
        return this;
    }


    public JobDescriptor getJobDescriptorById(String jobID) throws SSHApiException {
        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + "qstat -f " + jobID);

        StandardOutReader stdOutReader = new StandardOutReader();
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        if (!stdOutReader.getStdErrorString().equals("")) {
            throw new SSHApiException(stdOutReader.getStandardError().toString());
        }
        String result = stdOutReader.getStdOutputString();
        String[] info = result.split("\n");
        JobDescriptor jobDescriptor = new JobDescriptor();
        String[] line;
        for (int i = 0; i < info.length; i++) {
            if (info[i].contains("=")) {
                line = info[i].split("=", 2);
            } else {
                line = info[i].split(":", 2);
            }
            if (line.length >= 2) {
                String header = line[0].trim();
                log.debug("Header = " + header);
                String value = line[1].trim();
                log.debug("value = " + value);

                if (header.equals("Variable_List")) {
                    while (info[i + 1].startsWith("\t")) {
                        value += info[i + 1];
                        i++;
                    }
                    value = value.replaceAll("\t", "");
                    jobDescriptor.setVariableList(value);
                } else if ("Job Id".equals(header)) {
                    jobDescriptor.setJobID(value);
                } else if ("Job_Name".equals(header)) {
                    jobDescriptor.setJobName(value);
                } else if ("Account_Name".equals(header)) {
                    jobDescriptor.setAcountString(value);
                } else if ("job_state".equals(header)) {
                    jobDescriptor.setStatus(value);
                } else if ("Job_Owner".equals(header)) {
                    jobDescriptor.setOwner(value);
                } else if ("resources_used.cput".equals(header)) {
                    jobDescriptor.setUsedCPUTime(value);
                } else if ("resources_used.mem".equals(header)) {
                    jobDescriptor.setUsedMemory(value);
                } else if ("resources_used.walltime".equals(header)) {
                    jobDescriptor.setEllapsedTime(value);
                } else if ("job_state".equals(header)) {
                    jobDescriptor.setStatus(value);
                } else if ("queue".equals(header))
                    jobDescriptor.setQueueName(value);
                else if ("ctime".equals(header)) {
                    jobDescriptor.setCTime(value);
                } else if ("qtime".equals(header)) {
                    jobDescriptor.setQTime(value);
                } else if ("mtime".equals(header)) {
                    jobDescriptor.setMTime(value);
                } else if ("start_time".equals(header)) {
                    jobDescriptor.setSTime(value);
                } else if ("comp_time".equals(header)) {
                    jobDescriptor.setCompTime(value);
                } else if ("exec_host".equals(header)) {
                    jobDescriptor.setExecuteNode(value);
                } else if ("Output_Path".equals(header)) {
                    if (info[i + 1].contains("=") || info[i + 1].contains(":"))
                        jobDescriptor.setStandardOutFile(value);
                    else {
                        jobDescriptor.setStandardOutFile(value + info[i + 1].trim());
                        i++;
                    }
                } else if ("Error_Path".equals(header)) {
                    if (info[i + 1].contains("=") || info[i + 1].contains(":"))
                        jobDescriptor.setStandardErrorFile(value);
                    else {
                        String st = info[i + 1].trim();
                        jobDescriptor.setStandardErrorFile(value + st);
                        i++;
                    }

                } else if ("submit_args".equals(header)) {
                    while (i + 1 < info.length) {
                        if (info[i + 1].startsWith("\t")) {
                            value += info[i + 1];
                            i++;
                        } else
                            break;
                    }
                    value = value.replaceAll("\t", "");
                    jobDescriptor.setSubmitArgs(value);
                }
            }
        }
        return jobDescriptor;
    }

    public void scpTo(String remoteFile, String localFile) throws SSHApiException {
        try {
             SSHUtils.scpTo(remoteFile, localFile, session);
        } catch (IOException e) {
            throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                    + serverInfo.getHost() + ":rFile", e);
        } catch (JSchException e) {
            throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                    + serverInfo.getHost() + ":rFile", e);
        }
    }

    public void scpFrom(String remoteFile, String localFile) throws SSHApiException {
        try {
             SSHUtils.scpFrom(remoteFile, localFile, session);
        } catch (IOException e) {
            throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                    + serverInfo.getHost() + ":rFile", e);
        } catch (JSchException e) {
            throw new SSHApiException("Failed during scping local file:" + localFile + " to remote file "
                    + serverInfo.getHost() + ":rFile", e);
        }
    }

    public void makeDirectory(String directoryPath) throws SSHApiException {
        try {
            SSHUtils.makeDirectory(directoryPath, session);
        } catch (IOException e) {
            throw new SSHApiException("Failed during creating directory:" + directoryPath + " to remote file "
                    + serverInfo.getHost() + ":rFile", e);
        } catch (JSchException e) {
            throw new SSHApiException("Failed during creating directory :" + directoryPath + " to remote file "
                    + serverInfo.getHost() + ":rFile", e);
        }
    }

//
//    public String submitAsyncJob(Job jobDescriptor, JobSubmissionListener listener) throws SSHApiException {
////        final Cluster cluster = this;
//        String jobID = this.submitBatchJob(jobDescriptor);
////        final JobSubmissionListener jobSubmissionListener = listener;
//        try {
//            // Wait 5 seconds to start the first poll, this is hard coded, user doesn't have
//            // to configure this.
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            log.error("Error during job status monitoring");
//            throw new SSHApiException("Error during job status monitoring", e);
//        }
//        // Get the job status first
//        try {
////
////            Thread t = new Thread() {
////                @Override
////                public void run() {
////                    try {
//            Job jobById = this.getJobDescriptorById(jobID);
//            while (true) {
//                while (!jobById.getStatus().equals(JobStatus.C.toString())) {
//                    if (!jobById.getStatus().equals(listener.getJobStatus().toString())) {
//                        listener.setJobStatus(JobStatus.fromString(jobById.getStatus()));
//                        listener.statusChanged(jobById);
//                    }
//                    Thread.sleep(Long.parseLong(configReader.getConfiguration(POLLING_FREQUENCEY)));
//
//                    jobById = this.getJobDescriptorById(jobID);
//                }
//                //Set the job status to Complete
//                listener.setJobStatus(JobStatus.C);
//                listener.statusChanged(jobById);
//                break;
//            }
////                    } catch (InterruptedException e) {
////                        log.error("Error listening to the submitted job", e);
////                    } catch (SSHApiException e) {
////                        log.error("Error listening to the submitted job", e);
////                    }
////                }
////            };
//            //  This thread runs until the program termination, so that use can provide
//            // any action in onChange method of the listener, without worrying for waiting in the caller thread.
//            //t.setDaemon(true);
////            t.start();
//        } catch (Exception e) {
//            log.error("Error during job status monitoring");
//            throw new SSHApiException("Error during job status monitoring", e);
//        }
//        return jobID;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    public JobStatus getJobStatus(String jobID) throws SSHApiException {
        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + "qstat -f " + jobID);

        StandardOutReader stdOutReader = new StandardOutReader();
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        // check the standard error, incase user gave wrong jobID
        if (!stdOutReader.getStdErrorString().equals("")) {
            throw new SSHApiException(stdOutReader.getStandardError().toString());
        }
        String result = stdOutReader.getStdOutputString();
        String[] info = result.split("\n");
        String[] line = null;
        for (String anInfo : info) {
            if (anInfo.contains("=")) {
                line = anInfo.split("=", 2);
                if (line.length != 0) {
                    if (line[0].contains("job_state")) {
                        return JobStatus.valueOf(line[1].replaceAll(" ", ""));
                    }
                }
            }
        }
        return null;
    }

    public JobDescriptor cancelJob(String jobID) throws SSHApiException {
        RawCommandInfo rawCommandInfo = new RawCommandInfo(this.installedPath + "qdel " + jobID);

        StandardOutReader stdOutReader = new StandardOutReader();
        CommandExecutor.executeCommand(rawCommandInfo, this.getSession(), stdOutReader);
        if (!stdOutReader.getStdErrorString().equals("")) {
            throw new SSHApiException(stdOutReader.getStandardError().toString());
        }

        JobDescriptor jobById = this.getJobDescriptorById(jobID);
        if (CommonUtils.isJobFinished(jobById)) {
            log.debug("Job Cancel operation was successful !");
            return jobById;
        } else {
            log.debug("Job Cancel operation was not successful !");
            return null;
        }
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return cluster Nodes as array of machines
     */
    public Node[] getNodes() {
        return Nodes;
    }

    public void setNodes(Node[] Nodes) {
        this.Nodes = Nodes;
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

    private static void logDebug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }
}
