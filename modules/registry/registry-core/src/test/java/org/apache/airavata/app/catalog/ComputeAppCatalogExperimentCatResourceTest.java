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
package org.apache.airavata.app.catalog;


import org.apache.airavata.app.catalog.util.Initialize;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatAbstractResource;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class ComputeAppCatalogExperimentCatResourceTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;
    private static final Logger logger = LoggerFactory.getLogger(ComputeAppCatalogExperimentCatResourceTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            appcatalog = RegistryFactory.getAppCatalog();
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();

    }

    @Test
    public void testAddComputeResource (){
        try {
            ComputeResource computeResource = appcatalog.getComputeResource();
            ComputeResourceDescription description = new ComputeResourceDescription();

            description.setHostName("localhost");
            description.setResourceDescription("test compute resource");
            List<String> ipdaresses = new ArrayList<String>();
            ipdaresses.add("222.33.43.444");
            ipdaresses.add("23.344.44.454");
            description.setIpAddresses(ipdaresses);
//            List<String> aliases = new ArrayList<String>();
//            aliases.add("test.alias1");
//            aliases.add("test.alias2");
//            description.setHostAliases(aliases);
            String sshsubmissionId = addSSHJobSubmission();
            System.out.println("**** SSH Submission id ****** :" + sshsubmissionId);
//            String gsiSSHsubmissionId = addGSISSHJobSubmission();
//            System.out.println("**** GSISSH Submission id ****** :" + gsiSSHsubmissionId);
//            String globusSubmissionId = addGlobusJobSubmission();
//            System.out.println("**** Globus Submission id ****** :" + globusSubmissionId);
            JobSubmissionInterface sshSubmissionInt = new JobSubmissionInterface();
            sshSubmissionInt.setJobSubmissionInterfaceId(sshsubmissionId);
            sshSubmissionInt.setPriorityOrder(1);
            sshSubmissionInt.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);
//            JobSubmissionInterface globusSubInt = new JobSubmissionInterface();
//            globusSubInt.setJobSubmissionInterfaceId(globusSubmissionId);
//            globusSubInt.setPriorityOrder(2);
//            globusSubInt.setJobSubmissionProtocol(JobSubmissionProtocol.GLOBUS);
            List<JobSubmissionInterface> interfaceList = new ArrayList<JobSubmissionInterface>();
            interfaceList.add(sshSubmissionInt);
//            interfaceList.add(globusSubInt);
            description.setJobSubmissionInterfaces(interfaceList);

            String scpDataMoveId = addSCPDataMovement();
            System.out.println("**** SCP DataMoveId****** :" + scpDataMoveId);
            String gridFTPDataMoveId = addGridFTPDataMovement();
            System.out.println("**** grid FTP DataMoveId****** :" + gridFTPDataMoveId);

            List<DataMovementInterface> dataMovementInterfaces = new ArrayList<DataMovementInterface>();
            DataMovementInterface scpInterface = new DataMovementInterface();
            scpInterface.setDataMovementInterfaceId(scpDataMoveId);
            scpInterface.setDataMovementProtocol(DataMovementProtocol.SCP);
            scpInterface.setPriorityOrder(1);

            DataMovementInterface gridFTPMv = new DataMovementInterface();
            gridFTPMv.setDataMovementInterfaceId(gridFTPDataMoveId);
            gridFTPMv.setDataMovementProtocol(DataMovementProtocol.GridFTP);
            gridFTPMv.setPriorityOrder(2);

            description.setDataMovementInterfaces(dataMovementInterfaces);

            BatchQueue batchQueue1 = new BatchQueue();
            batchQueue1.setQueueName("queue1");
            batchQueue1.setQueueDescription("que1Desc1");
            batchQueue1.setMaxRunTime(10);
            batchQueue1.setMaxNodes(4);
            batchQueue1.setMaxJobsInQueue(1);

            BatchQueue batchQueue2 = new BatchQueue();
            batchQueue2.setQueueName("queue2");
            batchQueue2.setQueueDescription("que1Desc2");
            batchQueue2.setMaxRunTime(10);
            batchQueue2.setMaxNodes(4);
            batchQueue2.setMaxJobsInQueue(1);

            List<BatchQueue> batchQueueList = new ArrayList<BatchQueue>();
            batchQueueList.add(batchQueue1);
            batchQueueList.add(batchQueue2);
            description.setBatchQueues(batchQueueList);

            Map<FileSystems, String> fileSysMap = new HashMap<FileSystems, String>();
            fileSysMap.put(FileSystems.HOME, "/home");
            fileSysMap.put(FileSystems.SCRATCH, "/tmp");
            description.setFileSystems(fileSysMap);

            String resourceId = computeResource.addComputeResource(description);
            System.out.println("**********Resource id ************* : " +  resourceId);
            ComputeResourceDescription host = null;
            if (computeResource.isComputeResourceExists(resourceId)){
                host = computeResource.getComputeResource(resourceId);
                List<BatchQueue> batchQueues = host.getBatchQueues();
                for (BatchQueue queue : batchQueues){
                    System.out.println("%%%%%%%%%%%%%%%% queue description :  %%%%%%%%%%%%%%%%%%% : " + queue.getQueueDescription());
                }
                List<String> hostAliases = host.getHostAliases();
                if (hostAliases != null && !hostAliases.isEmpty()){
                    for (String alias : hostAliases){
                        System.out.println("%%%%%%%%%%%%%%%% alias value :  %%%%%%%%%%%%%%%%%%% : " + alias);
                    }
                }
                host.addToHostAliases("abc");
                computeResource.updateComputeResource(resourceId, host);
                List<String> hostAliases1 = computeResource.getComputeResource(resourceId).getHostAliases();
                for (String alias : hostAliases1){
                    System.out.println("%%%%%%%%%%%%%%%% alias value :  %%%%%%%%%%%%%%%%%%% : " + alias);
                }
                System.out.println("**********Resource name ************* : " +  host.getHostName());
            }

            SSHJobSubmission sshJobSubmission = computeResource.getSSHJobSubmission(sshsubmissionId);
            System.out.println("**********SSH Submission resource job manager ************* : " +  sshJobSubmission.getResourceJobManager().toString());
            System.out.println("**********Monitor mode ************* : " +  sshJobSubmission.getMonitorMode().toString());

//            GlobusJobSubmission globusJobSubmission = computeResource.get(globusSubmissionId);
//            System.out.println("**********Globus Submission resource job manager ************* : " + globusJobSubmission.getResourceJobManager().toString());

            SCPDataMovement scpDataMovement = computeResource.getSCPDataMovement(scpDataMoveId);
            System.out.println("**********SCP Data Move Security protocol ************* : " + scpDataMovement.getSecurityProtocol().toString());

            GridFTPDataMovement gridFTPDataMovement = computeResource.getGridFTPDataMovement(gridFTPDataMoveId);
            System.out.println("**********GRID FTP Data Move Security protocol ************* : " + gridFTPDataMovement.getSecurityProtocol().toString());

            description.setHostName("localhost2");
            computeResource.updateComputeResource(resourceId, description);
            if (computeResource.isComputeResourceExists(resourceId)){
                host = computeResource.getComputeResource(resourceId);
                System.out.println("**********Updated Resource name ************* : " +  host.getHostName());
            }

            Map<String, String> cfilters = new HashMap<String, String>();
            cfilters.put(AppCatAbstractResource.ComputeResourceConstants.HOST_NAME, "localhost2");
            List<ComputeResourceDescription> computeResourceList = computeResource.getComputeResourceList(cfilters);
            System.out.println("**********Size of compute resources ************* : " +  computeResourceList.size());

            List<ComputeResourceDescription> allComputeResourceList = computeResource.getAllComputeResourceList();
            System.out.println("**********Size of all compute resources ************* : " +  allComputeResourceList.size());

            Map<String, String> allComputeResourceIdList = computeResource.getAllComputeResourceIdList();
            System.out.println("**********Size of all compute resources ids ************* : " +  allComputeResourceIdList.size());

//            Map<String, String> globusfilters = new HashMap<String, String>();
//            globusfilters.put(AbstractResource.GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER, ResourceJobManager.PBS.toString());
//            List<GlobusJobSubmission> gList = computeResource.getGlobusJobSubmissionList(globusfilters);
//            System.out.println("**********Size of globus jobs ************* : " +  gList.size());

//            Map<String, String> sshfilters = new HashMap<String, String>();
//            sshfilters.put(AbstractResource.SSHSubmissionConstants.RESOURCE_JOB_MANAGER, ResourceJobManager.PBS.toString());
//            List<SSHJobSubmission> sshList = computeResource.getSS(sshfilters);
//            System.out.println("**********Size of SSH jobs ************* : " + sshList.size());

//            Map<String, String> gsishfilters = new HashMap<String, String>();
//            gsishfilters.put(AbstractResource.GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER, ResourceJobManager.PBS.toString());
//            List<GSISSHJobSubmission> gsisshList = computeResource.getGSISSHJobSubmissionList(gsishfilters);
//            System.out.println("**********Size of GSISSH jobs ************* : " + gsisshList.size());

//            Map<String, String> scpfilters = new HashMap<String, String>();
//            scpfilters.put(AbstractResource.SCPDataMovementConstants.SECURITY_PROTOCOL, SecurityProtocol.SSH_KEYS.toString());
//            List<SCPDataMovement> scpDataMovementList = computeResource.getSCPDataMovementList(scpfilters);
//            System.out.println("**********Size of SCP DM list ************* : " + scpDataMovementList.size());
//
//            Map<String, String> ftpfilters = new HashMap<String, String>();
//            ftpfilters.put(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL, SecurityProtocol.SSH_KEYS.toString());
//            List<GridFTPDataMovement> ftpDataMovementList = computeResource.getGridFTPDataMovementList(ftpfilters);
//            System.out.println("**********Size of FTP DM list ************* : " + ftpDataMovementList.size());

            assertTrue("Compute resource save successfully", host != null);
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String addSSHJobSubmission (){
        try {
            SSHJobSubmission jobSubmission = new SSHJobSubmission();
            jobSubmission.setSshPort(22);
            jobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
            ResourceJobManager jobManager = new ResourceJobManager();
            jobManager.setResourceJobManagerType(ResourceJobManagerType.PBS);
            jobManager.setPushMonitoringEndpoint("monitor ep");
            jobManager.setJobManagerBinPath("/bin");
            Map<JobManagerCommand, String> commands = new HashMap<JobManagerCommand, String>();
            commands.put(JobManagerCommand.SUBMISSION, "Sub command");
            commands.put(JobManagerCommand.SHOW_QUEUE, "show q command");
            jobManager.setJobManagerCommands(commands);
            jobSubmission.setMonitorMode(MonitorMode.POLL_JOB_MANAGER);
//            String jobManagerID = appcatalog.getComputeResource().addResourceJobManager(jobManager);
//            jobManager.setResourceJobManagerId(jobManagerID);
            jobSubmission.setResourceJobManager(jobManager);
            return appcatalog.getComputeResource().addSSHJobSubmission(jobSubmission);
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

//    public String addGlobusJobSubmission (){
//        try {
//            GlobusJobSubmission jobSubmission = new GlobusJobSubmission();
//            jobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
//            jobSubmission.setResourceJobManager(ResourceJobManager.PBS);
//            List<String> endPoints = new ArrayList<String>();
//            endPoints.add("222.33.43.444");
//            endPoints.add("23.344.44.454");
//            jobSubmission.setGlobusGateKeeperEndPoint(endPoints);
//            return appcatalog.getComputeResource().addGlobusJobSubmission(jobSubmission);
//        } catch (AppCatalogException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public String addSCPDataMovement (){
        try {
            SCPDataMovement dataMovement = new SCPDataMovement();
            dataMovement.setSshPort(22);
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            return appcatalog.getComputeResource().addScpDataMovement(dataMovement);
        }catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String addGridFTPDataMovement (){
        try {
            GridFTPDataMovement dataMovement = new GridFTPDataMovement();
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            List<String> endPoints = new ArrayList<String>();
            endPoints.add("222.33.43.444");
            endPoints.add("23.344.44.454");
            dataMovement.setGridFTPEndPoints(endPoints);
            return appcatalog.getComputeResource().addGridFTPDataMovement(dataMovement);
        }catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


}

