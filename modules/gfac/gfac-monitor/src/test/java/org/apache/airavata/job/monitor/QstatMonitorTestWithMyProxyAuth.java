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
package org.apache.airavata.job.monitor;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.impl.pull.qstat.QstatMonitor;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QstatMonitorTestWithMyProxyAuth {
    private MonitorManager monitorManager;
    private String myProxyUserName;
    private String myProxyPassword;
    private String certificateLocation;
    private String pbsFilePath;
    private String workingDirectory;
    private HostDescription hostDescription;

    @BeforeClass
    public void setUp() throws Exception {
//        System.setProperty("myproxy.username", "ogce");
//        System.setProperty("myproxy.password", "");
//        System.setProperty("basedir", "/Users/lahirugunathilake/work/airavata/sandbox/gsissh");
//        System.setProperty("gsi.working.directory", "/home/ogce");
//        System.setProperty("trusted.cert.location", "/Users/lahirugunathilake/Downloads/certificates");
        myProxyUserName = System.getProperty("myproxy.username");
        myProxyPassword = System.getProperty("myproxy.password");
        workingDirectory = System.getProperty("gsi.working.directory");
        certificateLocation = System.getProperty("trusted.cert.location");
        if (myProxyUserName == null || myProxyPassword == null || workingDirectory == null) {
            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
                    "E.g :- mvn clean install -Dmyproxy.username=xxx -Dmyproxy.password=xxx -Dgsi.working.directory=/path<<<<<<<");
            throw new Exception("Need my proxy user name password to run tests.");
        }

        monitorManager = new MonitorManager(RegistryFactory.getLoggingRegistry());
        QstatMonitor qstatMonitor = new
                QstatMonitor(monitorManager.getPullQueue(), monitorManager.getMonitorPublisher());
        try {
            monitorManager.addPullMonitor(qstatMonitor);
            monitorManager.launchMonitor();
        } catch (AiravataMonitorException e) {
            e.printStackTrace();
        }

        hostDescription = new HostDescription(GsisshHostType.type);
        hostDescription.getType().setHostAddress("trestles.sdsc.edu");
        hostDescription.getType().setHostName("gsissh-gordon");
        ((GsisshHostType) hostDescription.getType()).setPort(22);
        ((GsisshHostType)hostDescription.getType()).setInstalledPath("/opt/torque/bin/");
    }

    @Test
    public void testQstatMonitor() throws SSHApiException {
        /* now have to submit a job to some machine and add that job to the queue */
        //Create authentication
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);

        // Server info
        ServerInfo serverInfo = new ServerInfo("ogce", hostDescription.getType().getHostAddress());


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
        jobDescriptor.setOwner("ogce");
        inputs.add("Hello World");
        jobDescriptor.setInputValues(inputs);
        //finished construction of job object
        System.out.println(jobDescriptor.toXML());
        for (int i = 0; i < 1; i++) {
            String jobID = pbsCluster.submitBatchJob(jobDescriptor);
            System.out.println("Job submitted successfully, Job ID: " +  jobID);
            MonitorID monitorID = new MonitorID(hostDescription, jobID,null,null, "ogce");
            monitorID.setAuthenticationInfo(authenticationInfo);
            try {
                monitorManager.addAJobToMonitor(monitorID);
            } catch (AiravataMonitorException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        try {
            Thread.sleep(5000);
            BlockingQueue<UserMonitorData> pullQueue = monitorManager.getPullQueue();
            Iterator<UserMonitorData> iterator = pullQueue.iterator();
            UserMonitorData next = iterator.next();
            MonitorID monitorID = next.getHostMonitorData().get(0).getMonitorIDs().get(0);
            org.junit.Assert.assertNotNull(monitorID.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
