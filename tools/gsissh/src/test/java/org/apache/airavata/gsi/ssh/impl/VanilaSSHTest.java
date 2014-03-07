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

import org.apache.airavata.gsi.ssh.api.*;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.config.ConfigReader;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VanilaSSHTest {

    private String userName;
    private String password;
    private String passPhrase;
    private String hostName;
    private String workingDirectory;
    private String privateKeyPath;
    private String publicKeyPath;

    @BeforeTest
    public void setUp() throws Exception {
        this.hostName = "bigred2.uits.iu.edu";

//        this.userName = System.setProperty("my.ssh.user", "lginnali");
//        this.password = System.setProperty("my.ssh.password", "");
//        this.workingDirectory = System.setProperty("working.directory", "/N/u/lginnali/BigRed2/myjob");
//        System.setProperty("basedir","/Users/lahirugunathilake/work/airavata/sandbox/gsissh");
        this.userName = System.getProperty("my.ssh.user");
        this.password = System.getProperty("my.ssh.password");
        this.privateKeyPath = System.getProperty("my.private.key.path");
        this.publicKeyPath = System.getProperty("my.public.key.path");
        this.passPhrase = System.getProperty("my.ssh.user.pass.phrase");
        this.workingDirectory = System.getProperty("ssh.working.directory");

        System.out.println();


        if (this.userName == null || (this.userName != null && this.password == null)
                || (this.password==null && (this.publicKeyPath == null || this.privateKeyPath == null)) || this.workingDirectory == null) {
            System.out.println("########### In order to test you have to either username password or private,public keys");
            System.out.println("Use -Dmy.ssh.user=xxx -Dmy.ssh.user.password=yyy -Dmy.ssh.user.pass.phrase=zzz " +
                    "-Dmy.private.key.path -Dmy.public.key.path -Dssh.working.directory ");
        }
    }


    @Test
    public void testSimpleCommand1() throws Exception {

        System.out.println("Starting vanila SSH test ....");
        AuthenticationInfo authenticationInfo = null;
        if (password != null) {
            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
        } else {
            new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
                    this.passPhrase);
        }

        // Create command
        CommandInfo commandInfo = new RawCommandInfo("/opt/torque/torque-4.2.3.1/bin/qstat");

        // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);

        // Output
        CommandOutput commandOutput = new SystemCommandOutput();

        // Execute command
        CommandExecutor.executeCommand(commandInfo, serverInfo, authenticationInfo, commandOutput, new ConfigReader());


    }


    @Test
    public void testSimplePBSJob() throws Exception {

        AuthenticationInfo authenticationInfo = null;
        if (password != null) {
            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
        } else {
            new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
                    this.passPhrase);
        }
        // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/torque-4.2.3.1/bin/"));

        String date = new Date().toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        String pomFile = System.getProperty("basedir") + File.separator + "pom.xml";

        workingDirectory = workingDirectory + File.separator
                + date + "_" + UUID.randomUUID();
        pbsCluster.makeDirectory(workingDirectory);
        Thread.sleep(1000);
        pbsCluster.makeDirectory(workingDirectory + File.separator + "inputs");
        Thread.sleep(1000);
        pbsCluster.makeDirectory(workingDirectory + File.separator + "outputs");


        // doing file transfer to the remote resource
        String remoteLocation = workingDirectory + File.separator + "inputs";
        pbsCluster.scpTo(remoteLocation, pomFile);

        int i = pomFile.lastIndexOf(File.separator);
        String fileName = pomFile.substring(i + 1);
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
        jobDescriptor.setMaxWallTime("5");
        jobDescriptor.setJobSubmitter("aprun -n 1");
        List<String> inputs = new ArrayList<String>();
        inputs.add(remoteLocation + File.separator + fileName);
        jobDescriptor.setInputValues(inputs);
        //finished construction of job object
        System.out.println(jobDescriptor.toXML());
        String jobID = pbsCluster.submitBatchJob(jobDescriptor);
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
    public void testSCPFrom() throws Exception {

        AuthenticationInfo authenticationInfo = null;
        if (password != null) {
            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
        } else {
            new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
                    this.passPhrase);
        }
        // Server info
        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
        Cluster pbsCluster = new PBSCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/opt/torque/torque-4.2.3.1/bin/"));

        String date = new Date().toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        String pomFile = System.getProperty("basedir") + File.separator + "pom.xml";

        // Constructing theworking directory for demonstration and creating directories in the remote
        // resource
        workingDirectory = workingDirectory + File.separator
                + date + "_" + UUID.randomUUID();
        pbsCluster.makeDirectory(workingDirectory);
        pbsCluster.scpTo(workingDirectory, pomFile);
        Thread.sleep(1000);
        pbsCluster.scpFrom(workingDirectory + File.separator + "pom.xml", System.getProperty("basedir"));
    }


}
