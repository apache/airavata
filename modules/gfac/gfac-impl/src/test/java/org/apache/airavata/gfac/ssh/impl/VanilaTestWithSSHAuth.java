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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.gfac.ssh.impl;
//
//import org.apache.airavata.gfac.core.JobDescriptor;
//import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
//import org.apache.airavata.gfac.core.cluster.RemoteCluster;
//import org.apache.airavata.gfac.core.cluster.ServerInfo;
//import org.apache.airavata.gfac.impl.HPCRemoteCluster;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
//import org.apache.airavata.gfac.gsi.ssh.util.CommonUtils;
//import org.testng.AssertJUnit;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//public class VanilaTestWithSSHAuth {
//
//    private String userName;
//    private String password;
//    private String passPhrase;
//    private String hostName;
//    private String workingDirectory;
//    private String privateKeyPath;
//    private String publicKeyPath;
//    private String path;
//
//    @BeforeTest
//    public void setUp() throws Exception {
//        System.out.println("Test case name " + this.getClass().getName());
//        //Trestles
//        this.hostName = "trestles.sdsc.xsede.org";
//        this.userName = "ogce";
//        this.path="/opt/torque/bin/";
//        //Stampede:
////        this.hostName = "stampede.tacc.xsede.org";
////        this.userName = "ogce";
////        this.path="/usr/bin";
//        //Lonestar:
////         this.hostName = "lonestar.tacc.utexas.edu";
////         this.userName = "us3";
////        this.path="/opt/sge6.2/bin/lx24-amd64";
//        //Alamo:
////        this.hostName = "alamo.uthscsa.edu";
////        this.userName = "raminder";
////        this.path="/opt/torque/bin/";
//        //Bigred:
////        this.hostName = "bigred2.uits.iu.edu";
////        this.userName = "cgateway";
////        this.path="/opt/torque/torque-5.0.1/bin/";
//
//        System.setProperty("ssh.host",hostName);
//        System.setProperty("ssh.username", userName);
//        System.setProperty("private.ssh.key", "/home/lginnali/.ssh/id_dsa");
//        System.setProperty("public.ssh.key", "/home/lginnali/.ssh/id_dsa.pub");
//        System.setProperty("ssh.working.directory", "/tmp");
//
//        this.hostName = System.getProperty("ssh.host");
//        this.userName = System.getProperty("ssh.username");
//        this.password = System.getProperty("ssh.password");
//        this.privateKeyPath = System.getProperty("private.ssh.key");
//        this.publicKeyPath = System.getProperty("public.ssh.key");
//
//        System.setProperty("ssh.keypass", "");
//        this.passPhrase = System.getProperty("ssh.keypass");
//        this.workingDirectory = System.getProperty("ssh.working.directory");
//
//
//        if (this.userName == null
//                || (this.password==null && (this.publicKeyPath == null || this.privateKeyPath == null)) || this.workingDirectory == null) {
//            System.out.println("########### In order to test you have to either username password or private,public keys");
//            System.out.println("Use -Dssh.user=xxx -Dssh.password=yyy -Dssh.private.key.passphrase=zzz " +
//                    "-Dssh.private.key.path -Dssh.public.key.path -Dssh.working.directory ");
//        }
//    }
//
//
//    @Test
//    public void testSimplePBSJob() throws Exception {
//
//        AuthenticationInfo authenticationInfo = null;
//        if (password != null) {
//            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
//        } else {
//            authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
//                    this.passPhrase);
//        }
//        // Server info
//        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
//        RemoteCluster pbsRemoteCluster = new HPCRemoteCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager(path));
//
//        String date = new Date().toString();
//        date = date.replaceAll(" ", "_");
//        date = date.replaceAll(":", "_");
//
//        String pomFile =  new File("").getAbsolutePath() + File.separator + "pom.xml";
//
//        workingDirectory = workingDirectory + File.separator
//                + date + "_" + UUID.randomUUID();
//        pbsRemoteCluster.makeDirectory(workingDirectory);
//        Thread.sleep(1000);
//        pbsRemoteCluster.makeDirectory(workingDirectory + File.separator + "inputs");
//        Thread.sleep(1000);
//        pbsRemoteCluster.makeDirectory(workingDirectory + File.separator + "outputs");
//
//
//        // doing file transfer to the remote resource
//        String remoteLocation = workingDirectory + File.separator + "inputs";
//        pbsRemoteCluster.scpTo(remoteLocation, pomFile);
//
//        int i = pomFile.lastIndexOf(File.separator);
//        String fileName = pomFile.substring(i + 1);
//        // constructing the job object
//        JobDescriptor jobDescriptor = new JobDescriptor();
//        jobDescriptor.setWorkingDirectory(workingDirectory);
//        jobDescriptor.setShellName("/bin/bash");
//        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
//        jobDescriptor.setExecutablePath("/bin/echo");
//        jobDescriptor.setAllEnvExport(true);
//        jobDescriptor.setMailOptions("n");
//        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
//        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
//        jobDescriptor.setNodes(1);
//        jobDescriptor.setProcessesPerNode(1);
//        jobDescriptor.setQueueName("normal");
//        jobDescriptor.setMaxWallTime("5");
//        //jobDescriptor.setJobSubmitter("aprun -n 1");
//        List<String> inputs = new ArrayList<String>();
//        inputs.add(remoteLocation + File.separator + fileName);
//        jobDescriptor.setInputValues(inputs);
//        //finished construction of job object
//        System.out.println(jobDescriptor.toXML());
//        if(hostName.contains("trestles")){
//        String jobID = pbsRemoteCluster.submitBatchJob(jobDescriptor);
//        System.out.println("JobID returned : " + jobID);
//
////        RemoteCluster cluster = sshApi.getCluster(serverInfo, authenticationInfo);
//        Thread.sleep(1000);
//        JobDescriptor jobById = pbsRemoteCluster.getJobDescriptorById(jobID);
//
//        //printing job data got from previous call
//        AssertJUnit.assertEquals(jobById.getJobId(), jobID);
//        System.out.println(jobById.getAcountString());
//        System.out.println(jobById.getAllEnvExport());
//        System.out.println(jobById.getCompTime());
//        System.out.println(jobById.getExecutablePath());
//        System.out.println(jobById.getEllapsedTime());
//        System.out.println(jobById.getQueueName());
//        System.out.println(jobById.getExecuteNode());
//        System.out.println(jobById.getJobName());
//        System.out.println(jobById.getCTime());
//        System.out.println(jobById.getSTime());
//        System.out.println(jobById.getMTime());
//        System.out.println(jobById.getCompTime());
//        System.out.println(jobById.getOwner());
//        System.out.println(jobById.getQTime());
//        System.out.println(jobById.getUsedCPUTime());
//        System.out.println(jobById.getUsedMemory());
//        System.out.println(jobById.getVariableList());
//        }
//    }
//
//    @Test
//    public void testSimpleLSFJob() throws Exception {
//
//        AuthenticationInfo authenticationInfo = null;
//        if (password != null) {
//            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
//        } else {
//            authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
//                    this.passPhrase);
//        }
//        // Server info
//        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
//
//
//        // constructing the job object
//        JobDescriptor jobDescriptor = new JobDescriptor();
//        jobDescriptor.setWorkingDirectory(workingDirectory);
//        jobDescriptor.setShellName("/bin/bash");
//        jobDescriptor.setJobName("GSI_SSH_SLEEP_JOB");
//        jobDescriptor.setExecutablePath("/bin/echo");
//        jobDescriptor.setAllEnvExport(true);
//        jobDescriptor.setMailOptions("n");
//        jobDescriptor.setMailAddress("test@gmail.com");
//        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
//        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
//        jobDescriptor.setNodes(1);
//        jobDescriptor.setProcessesPerNode(1);
//        jobDescriptor.setQueueName("long");
//        jobDescriptor.setMaxWallTimeForLSF("5");
//        jobDescriptor.setJobSubmitter("mpiexec");
//        jobDescriptor.setModuleLoadCommands(new String[]{"module load openmpi/1.6.5"});
//        jobDescriptor.setUsedMemory("1000");
//        jobDescriptor.setChassisName("01");
//
//        //jobDescriptor.setJobSubmitter("aprun -n 1");
//        List<String> inputs = new ArrayList<String>();
//        jobDescriptor.setInputValues(inputs);
//        //finished construction of job object
//        System.out.println(jobDescriptor.toXML());
//        RemoteCluster pbsRemoteCluster = new HPCRemoteCluster(CommonUtils.getLSFJobManager(""));
//        ((HPCRemoteCluster) pbsRemoteCluster).generateJobScript(jobDescriptor);
//    }
//
//    @Test
//    public void testSCPFromAndSCPTo() throws Exception {
//
//        AuthenticationInfo authenticationInfo = null;
//        if (password != null) {
//            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
//        } else {
//            authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
//                    this.passPhrase);
//        }
//        // Server info
//        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
//        RemoteCluster pbsRemoteCluster = new HPCRemoteCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager(path));
//        new HPCRemoteCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager(path));;
//
//        String date = new Date().toString();
//        date = date.replaceAll(" ", "_");
//        date = date.replaceAll(":", "_");
//
//        String pomFile = (new File(".")).getAbsolutePath() + File.separator + "pom.xml";
//        File file = new File(pomFile);
//        if(!file.exists()){
//            file.createNewFile();
//        }
//        // Constructing theworking directory for demonstration and creating directories in the remote
//        // resource
//        workingDirectory = workingDirectory + File.separator
//                + date + "_" + UUID.randomUUID();
//        pbsRemoteCluster.makeDirectory(workingDirectory);
//        pbsRemoteCluster.scpTo(workingDirectory, pomFile);
//        Thread.sleep(1000);
//        pbsRemoteCluster.scpFrom(workingDirectory + File.separator + "pom.xml", (new File(".")).getAbsolutePath());
//    }
//}
