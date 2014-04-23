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
package org.apache.airavata.job;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.UserMonitorData;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.impl.push.amqp.AMQPMonitor;
import org.apache.airavata.job.monitor.state.JobStatusChangeRequest;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AMQPMonitorTest {

    private String myProxyUserName;
    private String myProxyPassword;
    private String certificateLocation;
    private String pbsFilePath;
    private String workingDirectory;
    private HostDescription hostDescription;
    private MonitorPublisher monitorPublisher;
    private BlockingQueue<MonitorID> finishQueue;
    private BlockingQueue<MonitorID> pushQueue;
    private Thread pushThread;
    private String proxyFilePath;
    @Before
    public void setUp() throws Exception {
        System.setProperty("myproxy.username", "ogce");
        System.setProperty("myproxy.password", "OpenGwy14");
        System.setProperty("basedir", "/Users/lahirugunathilake/work/airavata/sandbox/gsissh");
        System.setProperty("gsi.working.directory", "/home1/01437/ogce");
        System.setProperty("trusted.cert.location", "/Users/lahirugunathilake/Downloads/certificates");
        System.setProperty("proxy.file.path", "/Users/lahirugunathilake/Downloads/x509up_u503876");
        myProxyUserName = System.getProperty("myproxy.username");
        myProxyPassword = System.getProperty("myproxy.password");
        workingDirectory = System.getProperty("gsi.working.directory");
        certificateLocation = System.getProperty("trusted.cert.location");
        proxyFilePath = System.getProperty("proxy.file.path");
        System.setProperty("connection.name", "xsede");
        if (myProxyUserName == null || myProxyPassword == null || workingDirectory == null) {
            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
                    "E.g :- mvn clean install -Dmyproxy.user=xxx -Dmyproxy.password=xxx -Dgsi.working.directory=/path<<<<<<<");
            throw new Exception("Need my proxy user name password to run tests.");
        }

        monitorPublisher =  new MonitorPublisher(new EventBus());
        pushQueue = new LinkedBlockingQueue<MonitorID>();
        finishQueue = new LinkedBlockingQueue<MonitorID>();


        AMQPMonitor amqpMonitor = new
                AMQPMonitor(monitorPublisher,
                pushQueue, finishQueue,proxyFilePath,"xsede",
                Arrays.asList("info1.dyn.teragrid.org,info2.dyn.teragrid.org".split(",")));
        try {
            pushThread = (new Thread(amqpMonitor));
            pushThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        hostDescription = new HostDescription(GsisshHostType.type);
        hostDescription.getType().setHostAddress("login1.stampede.tacc.utexas.edu");
        hostDescription.getType().setHostName("stampede-host");
        ((GsisshHostType) hostDescription.getType()).setJobManager("slurm");
        ((GsisshHostType) hostDescription.getType()).setInstalledPath("/usr/bin/");
        ((GsisshHostType) hostDescription.getType()).setPort(2222);
        ((GsisshHostType) hostDescription.getType()).setMonitorMode("push");
    }

    @Test
    public void testAMQPMonitor() throws SSHApiException {
        /* now have to submit a job to some machine and add that job to the queue */
        //Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", "login1.stampede.tacc.utexas.edu",2222);


        Cluster pbsCluster = new
                PBSCluster(serverInfo, authenticationInfo, org.apache.airavata.gsi.ssh.util.CommonUtils.getPBSJobManager("/usr/bin/"));


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
        jobDescriptor.setAcountString("TG-STA110014S");
        List<String> inputs = new ArrayList<String>();
        jobDescriptor.setOwner("ogce");
        inputs.add("Hello World");
        jobDescriptor.setInputValues(inputs);
        //finished construction of job object
        System.out.println(jobDescriptor.toXML());
        String jobID = pbsCluster.submitBatchJob(jobDescriptor);
        System.out.println(jobID);
        try {
            pushQueue.add(new MonitorID(hostDescription, jobID,null,null,null, "ogce"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pushThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        class InnerClassAMQP{
            @Subscribe
            private void getStatus(JobStatusChangeRequest status){
                Assert.assertNotNull(status);
                pushThread.interrupt();
            }
        }
        monitorPublisher.registerListener(new InnerClassAMQP());
//        try {
//            pushThread.join(5000);
//            Iterator<MonitorID> iterator = pushQueue.iterator();
//            MonitorID next = iterator.next();
//            org.junit.Assert.assertNotNull(next.getStatus());
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }
}
