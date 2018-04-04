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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.registry.core.repositories.appcatalog.util.Initialize;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ComputeResourceRepositoryTest {

    private static Initialize initialize;
    private ComputeResourceRepository computeResourceRepository;
    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            computeResourceRepository = new ComputeResourceRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void ComputeResourceRepositoryTest() throws AppCatalogException {
        ComputeResourceDescription description = new ComputeResourceDescription();

        description.setHostName("localhost");
        description.setResourceDescription("test compute resource");
        description.setGatewayUsageReporting(true);
        List<String> ipdaresses = new ArrayList<String>();
        ipdaresses.add("222.33.43.444");
        ipdaresses.add("23.344.44.454");
        description.setIpAddresses(ipdaresses);
        String sshsubmissionId = addSSHJobSubmission();

        // Verify SSHJobSubmission
        SSHJobSubmission getSSHJobSubmission = computeResourceRepository.getSSHJobSubmission(sshsubmissionId);
        assertTrue(sshsubmissionId.equals(getSSHJobSubmission.getJobSubmissionInterfaceId()));
        assertTrue(MonitorMode.POLL_JOB_MANAGER.toString().equals(getSSHJobSubmission.getMonitorMode().toString()));
        assertTrue(getSSHJobSubmission.getResourceJobManager().getJobManagerCommands().size() == 2);

        JobSubmissionInterface sshSubmissionInt = new JobSubmissionInterface();
        sshSubmissionInt.setJobSubmissionInterfaceId(sshsubmissionId);
        sshSubmissionInt.setPriorityOrder(1);
        sshSubmissionInt.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);
        List<JobSubmissionInterface> interfaceList = new ArrayList<JobSubmissionInterface>();
        interfaceList.add(sshSubmissionInt);
        description.setJobSubmissionInterfaces(interfaceList);

        // Verify SCP Datamovement
        String scpDataMoveId = addSCPDataMovement();
        SCPDataMovement scpDataMovement = computeResourceRepository.getSCPDataMovement(scpDataMoveId);
        System.out.println("**********SCP Data Move Security protocol ************* : " + scpDataMovement.getSecurityProtocol().toString());
        assertTrue(scpDataMoveId.equals(scpDataMovement.getDataMovementInterfaceId()));
        assertTrue(SecurityProtocol.SSH_KEYS.toString().equals(scpDataMovement.getSecurityProtocol().toString()));

        // Verify Grid FTP
        String gridFTPDataMoveId = addGridFTPDataMovement();
        GridFTPDataMovement gridFTPDataMovement = computeResourceRepository.getGridFTPDataMovement(gridFTPDataMoveId);
        System.out.println("**********GRID FTP Data Move Security protocol ************* : " + gridFTPDataMovement.getSecurityProtocol().toString());
        assertTrue(gridFTPDataMoveId.equals(gridFTPDataMovement.getDataMovementInterfaceId()));
        assertTrue(gridFTPDataMovement.getSecurityProtocol().toString().equals(SecurityProtocol.SSH_KEYS.toString()));

        List<DataMovementInterface> dataMovementInterfaces = new ArrayList<DataMovementInterface>();
        DataMovementInterface scpInterface = new DataMovementInterface();
        scpInterface.setDataMovementInterfaceId(scpDataMoveId);
        scpInterface.setDataMovementProtocol(DataMovementProtocol.SCP);
        scpInterface.setPriorityOrder(1);

        DataMovementInterface gridFTPMv = new DataMovementInterface();
        gridFTPMv.setDataMovementInterfaceId(gridFTPDataMoveId);
        gridFTPMv.setDataMovementProtocol(DataMovementProtocol.GridFTP);
        gridFTPMv.setPriorityOrder(2);

        dataMovementInterfaces.add(scpInterface);
        dataMovementInterfaces.add(gridFTPMv);

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


        // Verify add/update compute resource
        String resourceId = computeResourceRepository.addComputeResource(description);
        ComputeResourceDescription host = null;

        if (computeResourceRepository.isComputeResourceExists(resourceId)){
            host = computeResourceRepository.getComputeResource(resourceId);
            List<BatchQueue> batchQueues = host.getBatchQueues();
            // check batch queue size
            assertTrue(batchQueues.size() == 2);
            for (BatchQueue queue : batchQueues){
                System.out.println("%%%%%%%%%%%%%%%% queue description :  %%%%%%%%%%%%%%%%%%% : " + queue.getQueueDescription());
            }

            // verify host aliases
            List<String> hostAliases = host.getHostAliases();
            assertTrue(hostAliases.size() == 0);
            if (hostAliases != null && !hostAliases.isEmpty()){
                for (String alias : hostAliases){
                    System.out.println("%%%%%%%%%%%%%%%% alias value :  %%%%%%%%%%%%%%%%%%% : " + alias);
                }
            }
            host.addToHostAliases("abc");
            computeResourceRepository.updateComputeResource(resourceId, host);
            List<String> hostAliases1 = computeResourceRepository.getComputeResource(resourceId).getHostAliases();
            assertTrue(hostAliases1.size() == 1);
            for (String alias : hostAliases1){
                System.out.println("%%%%%%%%%%%%%%%% alias value :  %%%%%%%%%%%%%%%%%%% : " + alias);
            }
            System.out.println("**********Resource name ************* : " +  host.getHostName());
            assertTrue(host.getHostName().equals("localhost"));
            assertTrue(host.isGatewayUsageReporting());
        }

        // Verify updating compute resource
        description.setHostName("localhost2");
        computeResourceRepository.updateComputeResource(resourceId, description);
        if (computeResourceRepository.isComputeResourceExists(resourceId)){
            host = computeResourceRepository.getComputeResource(resourceId);
            assertTrue(host.getHostName().equals("localhost2"));
            System.out.println("**********Updated Resource name ************* : " +  host.getHostName());
        }

        Map<String, String> cfilters = new HashMap<String, String>();
        cfilters.put(DBConstants.ComputeResource.HOST_NAME, "localhost2");
        List<ComputeResourceDescription> computeResourceList = computeResourceRepository.getComputeResourceList(cfilters);
        assertTrue(computeResourceList.size() == 1);
        System.out.println("**********Size of compute resources ************* : " +  computeResourceList.size());

        List<ComputeResourceDescription> allComputeResourceList = computeResourceRepository.getAllComputeResourceList();
        assertTrue(allComputeResourceList.size() == 1);
        System.out.println("**********Size of all compute resources ************* : " +  allComputeResourceList.size());

        Map<String, String> allComputeResourceIdList = computeResourceRepository.getAllComputeResourceIdList();
        assertTrue(allComputeResourceIdList.size() == 1);
        System.out.println("**********Size of all compute resources ids ************* : " +  allComputeResourceIdList.size());

        assertTrue("Compute resource save successfully", host != null);
    }

    public String addSSHJobSubmission() throws AppCatalogException {
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
        String jobManagerID = computeResourceRepository.addResourceJobManager(jobManager);
        jobManager.setResourceJobManagerId(jobManagerID);
        jobSubmission.setMonitorMode(MonitorMode.POLL_JOB_MANAGER);
        jobSubmission.setResourceJobManager(jobManager);

        return computeResourceRepository.addSSHJobSubmission(jobSubmission);
    }

    public String addSCPDataMovement (){
        try {
            SCPDataMovement dataMovement = new SCPDataMovement();
            dataMovement.setSshPort(22);
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            return computeResourceRepository.addScpDataMovement(dataMovement);
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
            return computeResourceRepository.addGridFTPDataMovement(dataMovement);
        }catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
