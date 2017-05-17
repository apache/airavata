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
//*/
//package org.apache.airavata.job;
//
//import com.google.common.eventbus.EventBus;
//import com.google.common.eventbus.Subscribe;
//import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
//import org.apache.airavata.gfac.core.cluster.RemoteCluster;
//import org.apache.airavata.gfac.impl.HPCRemoteCluster;
//import org.apache.airavata.registry.cpi.AppCatalog;
//import org.apache.airavata.common.utils.LocalEventPublisher;
//import org.apache.airavata.gfac.core.JobDescriptor;
//import org.apache.airavata.gfac.core.SSHApiException;
//import org.apache.airavata.gfac.core.authentication.GSIAuthenticationInfo;
//import org.apache.airavata.gfac.core.cluster.ServerInfo;
//import org.apache.airavata.gfac.core.monitor.MonitorID;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
//import org.apache.airavata.gfac.gsi.ssh.util.CommonUtils;
//import org.apache.airavata.gfac.monitor.impl.push.amqp.AMQPMonitor;
//import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
//import org.apache.airavata.model.appcatalog.computeresource.DataMovementInterface;
//import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
//import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
//import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
//import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
//import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
//import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
//import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
//import org.apache.airavata.model.appcatalog.computeresource.SecurityProtocol;
//import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class AMQPMonitorTest {
//
//    private String myProxyUserName;
//    private String myProxyPassword;
//    private String certificateLocation;
//    private String pbsFilePath;
//    private String workingDirectory;
//    private LocalEventPublisher localEventPublisher;
//    private BlockingQueue<MonitorID> finishQueue;
//    private BlockingQueue<MonitorID> pushQueue;
//    private Thread pushThread;
//    private String proxyFilePath;
//    private ComputeResourceDescription computeResourceDescription;
//    private final static Logger logger = LoggerFactory.getLogger(AMQPMonitorTest.class);
//
//    @Before
//    public void setUp() throws Exception {
//        System.setProperty("myproxy.username", "ogce");
//        System.setProperty("myproxy.password", "");
//        System.setProperty("basedir", "/Users/lahirugunathilake/work/airavata/sandbox/gsissh");
//        System.setProperty("gsi.working.directory", "/home1/01437/ogce");
//        System.setProperty("trusted.cert.location", "/Users/lahirugunathilake/Downloads/certificates");
//        System.setProperty("proxy.file.path", "/Users/lahirugunathilake/Downloads/x509up_u503876");
//        myProxyUserName = System.getProperty("myproxy.username");
//        myProxyPassword = System.getProperty("myproxy.password");
//        workingDirectory = System.getProperty("gsi.working.directory");
//        certificateLocation = System.getProperty("trusted.cert.location");
//        proxyFilePath = System.getProperty("proxy.file.path");
//        System.setProperty("connection.name", "xsede");
//        if (myProxyUserName == null || myProxyPassword == null || workingDirectory == null) {
//            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
//                    "E.g :- mvn clean install -Dmyproxy.user=xxx -Dmyproxy.password=xxx -Dgsi.working.directory=/path<<<<<<<");
//            throw new Exception("Need my proxy user name password to run tests.");
//        }
//
//        localEventPublisher =  new LocalEventPublisher(new EventBus());
//        pushQueue = new LinkedBlockingQueue<MonitorID>();
//        finishQueue = new LinkedBlockingQueue<MonitorID>();
//
//
//        final AMQPMonitor amqpMonitor = new
//                AMQPMonitor(localEventPublisher,
//                pushQueue, finishQueue,proxyFilePath,"xsede",
//                Arrays.asList("info1.dyn.teragrid.org,info2.dyn.teragrid.org".split(",")));
//        try {
//            (new Thread(){
//                public void run(){
//                    amqpMonitor.run();
//                }
//            }).start();
//        } catch (Exception e) {
//           logger.error(e.getMessage(), e);
//        }
//        computeResourceDescription = new ComputeResourceDescription("TestComputerResoruceId", "TestHostName");
//        computeResourceDescription.setHostName("stampede-host");
//        computeResourceDescription.addToIpAddresses("login1.stampede.tacc.utexas.edu");
//        ResourceJobManager resourceJobManager = new ResourceJobManager("1234", ResourceJobManagerType.SLURM);
//        Map<JobManagerCommand, String> commandMap = new HashMap<JobManagerCommand, String>();
//        commandMap.put(JobManagerCommand.SUBMISSION, "test");
//        resourceJobManager.setJobManagerCommands(commandMap);
//        resourceJobManager.setJobManagerBinPath("/usr/bin/");
//        resourceJobManager.setPushMonitoringEndpoint("push"); // TODO - add monitor mode
//        SSHJobSubmission sshJobSubmission = new SSHJobSubmission("TestSSHJobSubmissionInterfaceId", SecurityProtocol.GSI,
//                resourceJobManager);
//
//        AppCatalog appCatalog = RegistryFactory.getAppCatalog();
//        String jobSubmissionID = appCatalog.getComputeResource().addSSHJobSubmission(sshJobSubmission);
//
//        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface(jobSubmissionID, JobSubmissionProtocol.SSH, 1);
//
//        computeResourceDescription.addToJobSubmissionInterfaces(jobSubmissionInterface);
//        computeResourceDescription.addToDataMovementInterfaces(new DataMovementInterface("4532", DataMovementProtocol.SCP, 1));
//
//    }
//
//    @Test
//    public void testAMQPMonitor() throws SSHApiException {
//        /* now have to submit a job to some machine and add that job to the queue */
//        //Create authentication
//        GSIAuthenticationInfo authenticationInfo
//                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
//                7512, 17280000, certificateLocation);
//
//        // Server info
//        ServerInfo serverInfo = new ServerInfo("ogce", "login1.stampede.tacc.utexas.edu",2222);
//
//
//        RemoteCluster pbsRemoteCluster = new
//                HPCRemoteCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager("/usr/bin/"));
//
//
//        // Execute command
//        System.out.println("Target PBS file path: " + workingDirectory);
//        // constructing the job object
//        String jobName = "GSI_SSH_SLEEP_JOB";
//        JobDescriptor jobDescriptor = new JobDescriptor();
//        jobDescriptor.setWorkingDirectory(workingDirectory);
//        jobDescriptor.setShellName("/bin/bash");
//        jobDescriptor.setJobName(jobName);
//        jobDescriptor.setExecutablePath("/bin/echo");
//        jobDescriptor.setAllEnvExport(true);
//        jobDescriptor.setMailOptions("n");
//        jobDescriptor.setStandardOutFile(workingDirectory + File.separator + "application.out");
//        jobDescriptor.setStandardErrorFile(workingDirectory + File.separator + "application.err");
//        jobDescriptor.setNodes(1);
//        jobDescriptor.setProcessesPerNode(1);
//        jobDescriptor.setQueueName("normal");
//        jobDescriptor.setMaxWallTime("60");
//        jobDescriptor.setAcountString("TG-STA110014S");
//        List<String> inputs = new ArrayList<String>();
//        jobDescriptor.setOwner("ogce");
//        inputs.add("Hello World");
//        jobDescriptor.setInputValues(inputs);
//        //finished construction of job object
//        System.out.println(jobDescriptor.toXML());
//        String jobID = pbsRemoteCluster.submitBatchJob(jobDescriptor);
//        System.out.println(jobID);
//        try {
//            pushQueue.add(new MonitorID(computeResourceDescription, jobID,null,null,null, "ogce", jobName));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            pushThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        class InnerClassAMQP{
//            @Subscribe
//            private void getStatus(JobStatusChangeEvent status){
//                Assert.assertNotNull(status);
//                pushThread.interrupt();
//            }
//        }
//        localEventPublisher.registerListener(new InnerClassAMQP());
////        try {
////            pushThread.join(5000);
////            Iterator<MonitorID> iterator = pushQueue.iterator();
////            MonitorID next = iterator.next();
////            org.junit.Assert.assertNotNull(next.getStatus());
////        } catch (Exception e) {
////            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
////        }
//    }
//}
