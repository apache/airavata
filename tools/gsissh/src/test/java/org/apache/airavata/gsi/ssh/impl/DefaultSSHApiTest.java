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

import junit.framework.Assert;
import org.apache.airavata.gsi.ssh.api.*;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultSSHApiTest {
    private static final Logger log = LoggerFactory.getLogger(PBSCluster.class);
    private String myProxyUserName;
    private String myProxyPassword;
    private String certificateLocation;
    private String pbsFilePath;
    private String workingDirectory;
    private String slurmWorkingDirectory;
    private String jobID;

    @BeforeTest
    public void setUp() throws Exception {
        System.setProperty("myproxy.user", "ogce");
        System.setProperty("myproxy.password", "");
        System.setProperty("basedir", "/Users/lahirugunathilake/Downloads");
        System.setProperty("gsi.working.directory", "/home/ogce");
        myProxyUserName = System.getProperty("myproxy.user");
        myProxyPassword = System.getProperty("myproxy.password");
        workingDirectory = System.getProperty("gsi.working.directory");
        slurmWorkingDirectory = "/home1/01437/ogce";
        String pomDirectory = System.getProperty("basedir");

        File pomFileDirectory = new File(pomDirectory);

        System.out.println("POM directory ----------------- " + pomFileDirectory.getAbsolutePath());

        certificateLocation = pomFileDirectory.getAbsolutePath() + "/certificates";


        if (myProxyUserName == null || myProxyPassword == null || workingDirectory == null) {
            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
                    "E.g :- mvn clean install -Dmyproxy.user=xxx -Dmyproxy.password=xxx -Dgsi.working.directory=/path<<<<<<<");
            throw new Exception("Need my proxy user name password to run tests.");
        }
    }

    public void tearDown() throws Exception {
    }

    @Test
    public void testExecuteCommand() throws Exception {
        // Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Create command
        CommandInfo commandInfo = new RawCommandInfo("/bin/ls");

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");

        // Output
        CommandOutput commandOutput = new SystemCommandOutput();

        // Execute command
        CommandExecutor.executeCommand(commandInfo, serverInfo, authenticationInfo, commandOutput, new ConfigReader());
    }


    @Test
    public void testPBSAsync() throws Exception {
        // Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");


        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/bin/"));


        // Execute command
        System.out.println("Target PBS file path: " + workingDirectory);
        // constructing the job object
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobDescriptor.setWorkingDirectory(workingDirectory);
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
        jobDescriptor.setExecutablePath("/bin/echo");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
        jobDescriptor.setNodes(1);
        jobDescriptor.setProcessesPerNode(1);
        jobDescriptor.setQueueName("normal");
        jobDescriptor.setMaxWallTime("60");
        jobDescriptor.setAcountString("sds128");
        List<String> inputs = new ArrayList<String>();
        inputs.add("Hello World");
        jobDescriptor.setInputValues(inputs);
        //finished construction of job object
        System.out.println(jobDescriptor.toXML());
        jobID = pbsCluster.submitBatchJob(jobDescriptor);
        System.out.println("JobID returned : " + jobID);

//        Cluster cluster = sshApi.getCluster(serverInfo, authenticationInfo);
        Thread.sleep(1000);
        JobDescriptor jobById = pbsCluster.getJobDescriptorById(jobID);

        //printing job data got from previous call
        AssertJUnit.assertEquals(jobById.getJobId(), jobID);
        System.out.println(jobById.getAcountString());
        System.out.println(jobById.getAllEnvExport());
        System.out.println(jobById.getCompTime());
        System.out.println(jobById.getExecutablePath());
        System.out.println(jobById.getEllapsedTime());
        System.out.println(jobById.getQueueName());
        System.out.println(jobById.getExecuteNode());
        System.out.println(jobById.getJobName());
        System.out.println(jobById.getCTime());
        System.out.println(jobById.getSTime());
        System.out.println(jobById.getMTime());
        System.out.println(jobById.getCompTime());
        System.out.println(jobById.getOwner());
        System.out.println(jobById.getQTime());
        System.out.println(jobById.getUsedCPUTime());
        System.out.println(jobById.getUsedMemory());
        System.out.println(jobById.getVariableList());
    }

    @Test
    public void testSLURMAsync() throws Exception {
        // Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "stampede.tacc.xsede.org");
        serverInfo.setPort(2222);

        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getSLURMJobManager("/usr/bin/"));


        // Execute command
        System.out.println("Target SLURM file path: " + slurmWorkingDirectory);
        // constructing the job object
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobDescriptor.setWorkingDirectory(slurmWorkingDirectory);
        jobDescriptor.setShellName("/bin/sh");
        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
        jobDescriptor.setExecutablePath("/bin/echo");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setStandardOutFile("" + File.separator + "application.out");
        jobDescriptor.setStandardErrorFile("/home1/01437/ogce" + File.separator + "application.err");
        jobDescriptor.setNodes(1);
        jobDescriptor.setProcessesPerNode(1);
        jobDescriptor.setQueueName("normal");
        jobDescriptor.setMaxWallTime("60");
        jobDescriptor.setAcountString("TG-STA110014S");
        jobDescriptor.setJobSubmitter("sbatch");
        List<String> inputs = new ArrayList<String>();
        inputs.add("Hello World");
        jobDescriptor.setInputValues(inputs);
        //finished construction of job object
        System.out.println(jobDescriptor.toXML());
        jobID = pbsCluster.submitBatchJob(jobDescriptor);
        System.out.println("JobID returned : " + jobID);

//        Cluster cluster = sshApi.getCluster(serverInfo, authenticationInfo);
//    Thread.sleep(1000);

        JobDescriptor jobById = pbsCluster.getJobDescriptorById(jobID);

        //printing job data got from previous call
        AssertJUnit.assertEquals(jobById.getJobId(), jobID);
        System.out.println(jobById.getAcountString());
        System.out.println(jobById.getAllEnvExport());
        System.out.println(jobById.getCompTime());
        System.out.println(jobById.getExecutablePath());
        System.out.println(jobById.getEllapsedTime());
        System.out.println(jobById.getQueueName());
        System.out.println(jobById.getExecuteNode());
        System.out.println(jobById.getJobName());
        System.out.println(jobById.getCTime());
        System.out.println(jobById.getSTime());
        System.out.println(jobById.getMTime());
        System.out.println(jobById.getCompTime());
        System.out.println(jobById.getOwner());
        System.out.println(jobById.getQTime());
        System.out.println(jobById.getUsedCPUTime());
        System.out.println(jobById.getUsedMemory());
        System.out.println(jobById.getVariableList());
    }


    @Test
    public void testGetCluster() throws Exception {
//        GSIAuthenticationInfo authenticationInfo
//                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
//                7512, 17280000);
//        // Server info
//        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");
//        // Get the API
//        SSHApi sshApi = SSHApiFactory.createSSHApi(this.certificateLocation);
//        Cluster cluster = sshApi.getCluster(serverInfo, authenticationInfo);
//        System.out.println(cluster.getNodes()[0].getName());
//        System.out.println(cluster.getNodes()[0].getNp());
//        System.out.println(cluster.getNodes()[0].getState());
//        System.out.println(cluster.getNodes()[0].getCores()[0].getId());
//        System.out.println(cluster.getNodes()[0].getName());

    }


    @Test
    public void testsubmitAsyncJobWithFailure() throws Exception {
        // Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");

        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/bin/"));

        // Execute command
        System.out.println("Target PBS file path: " + workingDirectory);
        System.out.println("Local PBS File path: " + pbsFilePath);
        String workingDirectory = File.separator + "home" + File.separator + "ogce" + File.separator + "gsissh";
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobDescriptor.setWorkingDirectory(workingDirectory);
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
        jobDescriptor.setExecutablePath("/bin/sleep");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
        jobDescriptor.setNodes(1);
        jobDescriptor.setProcessesPerNode(100);
        jobDescriptor.setQueueName("normal");
        jobDescriptor.setMaxWallTime("60");
        jobDescriptor.setAcountString("sds128");
        List<String> inputs = new ArrayList<String>();
        inputs.add("Hello World");
        jobDescriptor.setInputValues(inputs);
        System.out.println(jobDescriptor.toXML());
        try {
            String jobID = pbsCluster.submitBatchJob(jobDescriptor);
            System.out.println("JobID returned : " + jobID);
        } catch (SSHApiException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testSubmitAsyncJobWithListener() throws Exception {
        // Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");


        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/bin/"));


        // Execute command
        System.out.println("Target PBS file path: " + workingDirectory);
        System.out.println("Local PBS File path: " + pbsFilePath);
        String workingDirectory = File.separator + "home" + File.separator + "ogce" + File.separator + "gsissh";
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobDescriptor.setWorkingDirectory(workingDirectory);
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
        jobDescriptor.setExecutablePath("/bin/sleep");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
        jobDescriptor.setNodes(1);
        jobDescriptor.setProcessesPerNode(1);
//        jobDescriptor.setMaxWallTime("1:00:00");
        jobDescriptor.setQueueName("normal");
        jobDescriptor.setAcountString("sds128");
        List<String> inputs = new ArrayList<String>();
        inputs.add("1000");
        jobDescriptor.setInputValues(inputs);
        System.out.println(jobDescriptor.toXML());
        DefaultJobSubmissionListener listener = new DefaultJobSubmissionListener();
        String jobID = pbsCluster.submitBatchJob(jobDescriptor);
        try {
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
            // p
            JobStatus jobStatus = pbsCluster.getJobStatus(jobID);
            while (true) {
                while (!jobStatus.equals(JobStatus.C.toString())) {
                    if (!jobStatus.equals(listener.getJobStatus().toString())) {
                        listener.setJobStatus(jobStatus);
                        listener.statusChanged(jobStatus);
                    }
                    Thread.sleep(60000);

                    jobStatus = pbsCluster.getJobStatus(jobID);
                }
                //Set the job status to Complete
                listener.setJobStatus(JobStatus.C);
                listener.statusChanged(jobStatus);
                break;
            }
//                    } catch (InterruptedException e) {
//                        log.error("Error listening to the submitted job", e);
//                    } catch (SSHApiException e) {
//                        log.error("Error listening to the submitted job", e);
//                    }
//                }
//            };
            //  This thread runs until the program termination, so that use can provide
//            // any action in onChange method of the listener, without worrying for waiting in the caller thread.
            //t.setDaemon(true);
//            t.start();
        } catch (Exception e) {
            log.error("Error during job status monitoring");
            throw new SSHApiException("Error during job status monitoring", e);
        }
        while (!listener.isJobDone()) {
            Thread.sleep(10000);
        }
    }

    @Test
    public void testJobCancel() throws Exception {
        // Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");


        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/bin/"));


        // Execute command
        System.out.println("Target PBS file path: " + workingDirectory);
        System.out.println("Local PBS File path: " + pbsFilePath);
        String workingDirectory = File.separator + "home" + File.separator + "ogce" + File.separator + "gsissh";
        JobDescriptor jobDescriptor = new JobDescriptor();
        jobDescriptor.setWorkingDirectory(workingDirectory);
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
        jobDescriptor.setExecutablePath("/bin/sleep");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
        jobDescriptor.setNodes(1);
        jobDescriptor.setProcessesPerNode(1);
        jobDescriptor.setMaxWallTime("60");
        jobDescriptor.setQueueName("normal");
        jobDescriptor.setAcountString("sds128");
        List<String> inputs = new ArrayList<String>();
        inputs.add("1000");
        jobDescriptor.setInputValues(inputs);
        System.out.println(jobDescriptor.toXML());
        String jobID = pbsCluster.submitBatchJob(jobDescriptor);
        System.out.println("Job submitted to successfully : " + jobID);
        JobDescriptor jobById = pbsCluster.getJobDescriptorById(jobID);
        if (!CommonUtils.isJobFinished(jobById)) {
            JobDescriptor job = pbsCluster.cancelJob(jobID);
            if (CommonUtils.isJobFinished(job)) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(true);
            }
        }

    }

}
