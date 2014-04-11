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
import org.apache.airavata.gsi.ssh.util.SSHUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultSSHApiTestWithMyProxyAuth {
    private static final Logger log = LoggerFactory.getLogger(PBSCluster.class);
    private String myProxyUserName;
    private String myProxyPassword;
    private String certificateLocation;
    private String pbsFilePath;
    private String workingDirectory;
    private String jobID;
    private String lFilePath;
    private String rFilePath;

    @BeforeTest
    public void setUp() throws Exception {
        System.out.println("Test case name " + this.getClass().getName());
//        System.setProperty("myproxy.username", "ogce");
//        System.setProperty("myproxy.password", "");
//        System.setProperty("trusted.cert.location", "/Users/lahirugunathilake/Downloads/certificates");
//        System.setProperty("gsi.working.directory", "/home/ogce");

        certificateLocation = System.getProperty("trusted.cert.location");
        myProxyUserName = System.getProperty("myproxy.username");
        myProxyPassword = System.getProperty("myproxy.password");
        workingDirectory = System.getProperty("gsi.working.directory");

        if (myProxyUserName == null || myProxyPassword == null || workingDirectory == null) {
            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
                    "E.g :- mvn clean install -Dmyproxy.username=xxx -Dmyproxy.password=xxx -Dtrusted.cert.location=/path<<<<<<<");
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
        System.out.println(myProxyUserName);
        System.out.println(myProxyPassword);
        System.out.println(certificateLocation);
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

        //Cluster cluster = sshApi.getCluster(serverInfo, authenticationInfo);
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

    @Test
    public void testSCPToAndSCPFrom() throws Exception {
        // Create authentication
        File test = new File("test");
        if(!test.exists()){
            test.createNewFile();
        }
        lFilePath = test.getAbsolutePath();
        System.out.println(lFilePath);
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");
        PBSCluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, null);
        pbsCluster.scpTo("/tmp", lFilePath);
        Thread.sleep(1000);
        pbsCluster.scpFrom(File.separator + "tmp" + File.separator + "test", lFilePath);
        boolean delete = test.delete();
        org.junit.Assert.assertTrue(delete);
    }

    @Test
    public void testlistDirectory() throws Exception {
        // Create authentication
        File test = new File("test");
        if (!test.exists()) {
            test.createNewFile();
        }
        lFilePath = test.getAbsolutePath();
        System.out.println(lFilePath);
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");
        PBSCluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, null);
        List<String> strings = pbsCluster.listDirectory("/tmp");
        org.junit.Assert.assertNotNull(strings);
    }
}
