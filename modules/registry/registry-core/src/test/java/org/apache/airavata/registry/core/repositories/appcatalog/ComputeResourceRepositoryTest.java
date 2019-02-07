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
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ComputeResourceRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceRepositoryTest.class);

    private ComputeResourceRepository computeResourceRepository;

    public ComputeResourceRepositoryTest() {
        super(Database.APP_CATALOG);
        computeResourceRepository = new ComputeResourceRepository();
    }

    @Test
    public void removeBatchQueueTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);

        ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String savedComputeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);

        List<BatchQueue> batchQueues = computeResourceDescription.getBatchQueues();
        Assert.assertTrue(batchQueues.size() > 0);

        computeResourceRepository.removeBatchQueue(savedComputeResourceId, batchQueues.get(0).getQueueName());

        ComputeResourceDescription updatedComputeResource = computeResourceRepository.getComputeResource(savedComputeResourceId);

        List<BatchQueue> updatedBatchQueues = updatedComputeResource.getBatchQueues();

        Assert.assertEquals(batchQueues.size(), updatedBatchQueues.size() + 1);
        Optional<BatchQueue> searchedInterfaceResult = updatedBatchQueues.stream()
                .filter(queue -> queue.getQueueName().equals(batchQueues.get(0).getQueueName())).findFirst();

        Assert.assertFalse(searchedInterfaceResult.isPresent());
    }

    @Test
    public void removeDataMovementInterfaceTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);

        ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String savedComputeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);

        List<DataMovementInterface> dataMovementInterfaces = computeResourceDescription.getDataMovementInterfaces();
        Assert.assertTrue(dataMovementInterfaces.size() > 0);

        computeResourceRepository.removeDataMovementInterface(savedComputeResourceId, dataMovementInterfaces.get(0).getDataMovementInterfaceId());

        ComputeResourceDescription updatedComputeResource = computeResourceRepository.getComputeResource(savedComputeResourceId);

        List<DataMovementInterface> updatedDataMovementInterfaces = updatedComputeResource.getDataMovementInterfaces();

        Assert.assertEquals(dataMovementInterfaces.size(), updatedDataMovementInterfaces.size() + 1);
        Optional<DataMovementInterface> searchedInterfaceResult = updatedDataMovementInterfaces.stream()
                .filter(iface -> iface.getDataMovementInterfaceId().equals(dataMovementInterfaces.get(0).getDataMovementInterfaceId())).findFirst();

        Assert.assertFalse(searchedInterfaceResult.isPresent());
    }

    @Test
    public void removeJobSubmissionInterfaceTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);

        ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String savedComputeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);

        List<JobSubmissionInterface> jobSubmissionInterfaces = computeResourceDescription.getJobSubmissionInterfaces();
        Assert.assertTrue(jobSubmissionInterfaces.size() > 0);

        computeResourceRepository.removeJobSubmissionInterface(savedComputeResourceId, jobSubmissionInterfaces.get(0).getJobSubmissionInterfaceId());

        ComputeResourceDescription updatedComputeResource = computeResourceRepository.getComputeResource(savedComputeResourceId);

        List<JobSubmissionInterface> updatedJobSubmissionInterfaces = updatedComputeResource.getJobSubmissionInterfaces();

        Assert.assertEquals(jobSubmissionInterfaces.size(), updatedJobSubmissionInterfaces.size() + 1);
        Optional<JobSubmissionInterface> searchedInterfaceResult = updatedJobSubmissionInterfaces.stream()
                .filter(iface -> iface.getJobSubmissionInterfaceId().equals(jobSubmissionInterfaces.get(0).getJobSubmissionInterfaceId())).findFirst();

        Assert.assertFalse(searchedInterfaceResult.isPresent());
    }

    @Test
    public void listComputeResourcesTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);

        List<String> allIds = new ArrayList<>();
        List<ComputeResourceDescription> allComputeResources = new ArrayList<>();
        Map<String, String> allComputeResourceMap = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);
            computeResourceDescription.setHostName("Host" + i);
            computeResourceDescription.setEnabled((i%2 == 0));
            String savedId = computeResourceRepository.addComputeResource(computeResourceDescription);
            allIds.add(savedId);
            allComputeResources.add(computeResourceDescription);
            allComputeResourceMap.put(savedId, computeResourceDescription.getHostName());
        }

        List<ComputeResourceDescription> allSavedComputeResources = computeResourceRepository.getAllComputeResourceList();

        Assert.assertEquals(5, allSavedComputeResources.size());
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(deepCompareComputeResourceDescription(allComputeResources.get(i), allSavedComputeResources.get(i)));
        }

        Map<String, String> allSavedComputeResourceIds = computeResourceRepository.getAllComputeResourceIdList();

        Assert.assertEquals(5, allSavedComputeResourceIds.size());

        for (String id : allIds) {
            String host = allSavedComputeResourceIds.get(id);
            Assert.assertNotNull(host);
            Assert.assertEquals(allComputeResourceMap.get(id), host);
        }

        Map<String, String> allAvailableIds = computeResourceRepository.getAvailableComputeResourceIdList();

        Assert.assertEquals(3, allAvailableIds.size());
        Assert.assertNotNull(allAvailableIds.get(allIds.get(0)));
        Assert.assertNotNull(allAvailableIds.get(allIds.get(2)));
        Assert.assertNotNull(allAvailableIds.get(allIds.get(4)));
    }

    @Test
    public void filterComputeResourcesTest() throws AppCatalogException {

        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);
        ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);


        Map<String, String> cfilters = new HashMap<String, String>();
        cfilters.put(DBConstants.ComputeResource.HOST_NAME, "localhost");
        List<ComputeResourceDescription> computeResourceList = computeResourceRepository.getComputeResourceList(cfilters);

        Assert.assertEquals(0, computeResourceList.size());

        String computeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);
        computeResourceList = computeResourceRepository.getComputeResourceList(cfilters);

        Assert.assertEquals(1, computeResourceList.size());

        Assert.assertEquals(computeResourceId, computeResourceList.get(0).getComputeResourceId());

        try {
            cfilters = new HashMap<String, String>();
            cfilters.put("Invalid_filter", "localhost");
            computeResourceRepository.getComputeResourceList(cfilters);
            Assert.fail();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void updateComputeResourceTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);
        ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String computeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);

        ComputeResourceDescription savedComputeResource = computeResourceRepository.getComputeResource(computeResourceId);
        savedComputeResource.getHostAliases().add("New Alias");

        BatchQueue batchQueue = new BatchQueue();
        batchQueue.setQueueName("queue new ");
        batchQueue.setQueueDescription("que1Desc new");
        batchQueue.setMaxRunTime(16);
        batchQueue.setMaxNodes(10);
        batchQueue.setMaxProcessors(11);
        batchQueue.setMaxJobsInQueue(5);
        batchQueue.setMaxMemory(2005);
        batchQueue.setCpuPerNode(7);
        batchQueue.setDefaultNodeCount(11);
        batchQueue.setDefaultCPUCount(3);
        batchQueue.setDefaultWalltime(34);
        batchQueue.setQueueSpecificMacros("Macros new");
        batchQueue.setIsDefaultQueue(true);

        savedComputeResource.getBatchQueues().add(batchQueue);
        savedComputeResource.setCpusPerNode(43);
        savedComputeResource.setDefaultWalltime(4343);

        computeResourceRepository.updateComputeResource(computeResourceId, savedComputeResource);

        ComputeResourceDescription updatedComputeResource = computeResourceRepository.getComputeResource(computeResourceId);
        Assert.assertTrue(deepCompareComputeResourceDescription(savedComputeResource, updatedComputeResource));
    }

    @Test
    public void addComputeResourceTest() throws AppCatalogException {

        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement);
        ComputeResourceDescription computeResourceDescription = prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        computeResourceDescription.setComputeResourceId("manually-entered-id");

        Assert.assertNull(computeResourceRepository.getComputeResource("manually-entered-id"));
        String computeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);
        Assert.assertEquals("manually-entered-id", computeResourceId);
        Assert.assertTrue(computeResourceRepository.isComputeResourceExists(computeResourceId));
        ComputeResourceDescription savedComputeResource = computeResourceRepository.getComputeResource("manually-entered-id");
        Assert.assertNotNull(savedComputeResource);

        Assert.assertTrue(deepCompareComputeResourceDescription(computeResourceDescription, savedComputeResource));
    }


    @Test
    public void addResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceRepository.addResourceJobManager(resourceJobManager);
        ResourceJobManager savedJobManager = computeResourceRepository.getResourceJobManager(jobManagerId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(resourceJobManager, savedJobManager, "__isset_bitfield"));

    }

    @Test
    public void deleteResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceRepository.addResourceJobManager(resourceJobManager);

        Assert.assertNotNull(computeResourceRepository.getResourceJobManager(jobManagerId));
        computeResourceRepository.deleteResourceJobManager(jobManagerId);
        Assert.assertNull(computeResourceRepository.getResourceJobManager(jobManagerId));

    }
    @Test
    public void updateResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceRepository.addResourceJobManager(resourceJobManager);
        ResourceJobManager savedJobManager = computeResourceRepository.getResourceJobManager(jobManagerId);

        savedJobManager.setJobManagerBinPath("/new bin");
        savedJobManager.getJobManagerCommands().put(JobManagerCommand.SHOW_START, "New Command Value");
        savedJobManager.getParallelismPrefix().put(ApplicationParallelismType.MPI, "MPI Type");

        computeResourceRepository.updateResourceJobManager(jobManagerId, savedJobManager);

        ResourceJobManager updatedJobManager = computeResourceRepository.getResourceJobManager(jobManagerId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(savedJobManager, updatedJobManager, "__isset_bitfield"));
    }

    @Test
    public void addUnicoreJobSubmissionTest() throws AppCatalogException {
        UnicoreJobSubmission unicoreJobSubmission = prepareUnicoreJobSubmission();
        String savedSubmissionId = computeResourceRepository.addUNICOREJobSubmission(unicoreJobSubmission);
        UnicoreJobSubmission savedSubmission = computeResourceRepository.getUNICOREJobSubmission(savedSubmissionId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(unicoreJobSubmission, savedSubmission, "__isset_bitfield"));
    }

    @Test
    public void addCloudJobSubmissionTest() throws AppCatalogException {
        CloudJobSubmission cloudJobSubmission = prepareCloudJobSubmission();
        String savedSubmissionId = computeResourceRepository.addCloudJobSubmission(cloudJobSubmission);
        CloudJobSubmission savedSubmission = computeResourceRepository.getCloudJobSubmission(savedSubmissionId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(cloudJobSubmission, savedSubmission, "__isset_bitfield"));
    }

    @Test
    public void addLocalJobSubmissionTest() throws AppCatalogException {
        ResourceJobManager jobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(jobManager);

        LOCALSubmission localSubmission = prepareLocalJobSubmission(jobManager);
        String savedSubmissionId = computeResourceRepository.addLocalJobSubmission(localSubmission);
        LOCALSubmission savedSubmission = computeResourceRepository.getLocalJobSubmission(savedSubmissionId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(localSubmission, savedSubmission, "__isset_bitfield"));
    }

    @Test
    public void addSSHJobSubmissionTest() throws AppCatalogException {
        ResourceJobManager jobManager = prepareResourceJobManager();
        computeResourceRepository.addResourceJobManager(jobManager);

        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(jobManager);
        String jobSubmissionId = computeResourceRepository.addSSHJobSubmission(sshJobSubmission);
        SSHJobSubmission savedJobSubmission = computeResourceRepository.getSSHJobSubmission(jobSubmissionId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(sshJobSubmission, savedJobSubmission, "__isset_bitfield"));
    }

    @Test
    public void addSCPDataMovementTest() throws AppCatalogException {
        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String dataMovementId = computeResourceRepository.addScpDataMovement(scpDataMovement);

        SCPDataMovement savedDataMovement = computeResourceRepository.getSCPDataMovement(dataMovementId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(scpDataMovement, savedDataMovement, "__isset_bitfield"));
    }

    @Test
    public void addLocalDataMovementTest() throws AppCatalogException {
        LOCALDataMovement localDataMovement = prepareLocalDataMovement();
        String dataMovementId = computeResourceRepository.addLocalDataMovement(localDataMovement);

        LOCALDataMovement savedDataMovement = computeResourceRepository.getLocalDataMovement(dataMovementId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(localDataMovement, savedDataMovement, "__isset_bitfield"));
    }

    @Test
    public void addUnicoreDataMovementTest() throws AppCatalogException {
        UnicoreDataMovement unicoreDataMovement = prepareUnicoreDataMovement();
        String dataMovementId = computeResourceRepository.addUnicoreDataMovement(unicoreDataMovement);

        UnicoreDataMovement savedDataMovement = computeResourceRepository.getUNICOREDataMovement(dataMovementId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(unicoreDataMovement, savedDataMovement, "__isset_bitfield"));
    }

    @Test
    public void addGridFTPDataMovementTest() throws AppCatalogException {
        GridFTPDataMovement gridFTPDataMovement1 = prepareGridFTPDataMovement("222.33.43.444", "23.344.44.454");
        String dataMovementId1 = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement1);
        GridFTPDataMovement savedDataMovement1 = computeResourceRepository.getGridFTPDataMovement(dataMovementId1);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(gridFTPDataMovement1, savedDataMovement1, "__isset_bitfield"));

        GridFTPDataMovement gridFTPDataMovement2 = prepareGridFTPDataMovement("222.33.43.445", "23.344.44.400");
        String dataMovementId2 = computeResourceRepository.addGridFTPDataMovement(gridFTPDataMovement2);
        GridFTPDataMovement savedDataMovement2 = computeResourceRepository.getGridFTPDataMovement(dataMovementId2);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(gridFTPDataMovement2, savedDataMovement2, "__isset_bitfield"));

    }

    @Test
    public void fetchNotAvailableResourceTest() throws AppCatalogException {
        Assert.assertNull(computeResourceRepository.getResourceJobManager("INVALID ID"));
        Assert.assertNull(computeResourceRepository.getComputeResource("INVALID ID"));
        Assert.assertNull(computeResourceRepository.getCloudJobSubmission("INVALID ID"));
        Assert.assertEquals(0, computeResourceRepository.getFileSystems("INVALID ID").size());
        Assert.assertNull(computeResourceRepository.getGridFTPDataMovement("INVALID ID"));
        Assert.assertNull(computeResourceRepository.getLocalDataMovement("INVALID ID"));
        Assert.assertNull(computeResourceRepository.getLocalJobSubmission("INVALID ID"));
        Assert.assertNull(computeResourceRepository.getSCPDataMovement("INVALID ID"));
        Assert.assertNull(computeResourceRepository.getUNICOREDataMovement("INVALID ID"));
    }

    private ComputeResourceDescription prepareComputeResource(String sshSubmissionId, String scpDataMoveId, String gridFTPDataMoveId, int batchQueueCount) {
        ComputeResourceDescription description = new ComputeResourceDescription();

        description.setHostName("localhost");
        description.setResourceDescription("test compute resource");
        description.setGatewayUsageReporting(true);
        List<String> ipdaresses = new ArrayList<String>();
        ipdaresses.add("222.33.43.444");
        ipdaresses.add("23.344.44.454");
        description.setIpAddresses(ipdaresses);

        JobSubmissionInterface sshSubmissionInt = new JobSubmissionInterface();
        sshSubmissionInt.setJobSubmissionInterfaceId(sshSubmissionId);
        sshSubmissionInt.setPriorityOrder(1);
        sshSubmissionInt.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);
        List<JobSubmissionInterface> interfaceList = new ArrayList<JobSubmissionInterface>();
        interfaceList.add(sshSubmissionInt);
        description.setJobSubmissionInterfaces(interfaceList);

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

        List<BatchQueue> batchQueueList = new ArrayList<BatchQueue>();

        for (int i = 0; i < batchQueueCount; i++) {
            BatchQueue batchQueue = new BatchQueue();
            batchQueue.setQueueName("queue" + i);
            batchQueue.setQueueDescription("que1Desc" + i);
            batchQueue.setMaxRunTime(10 + i);
            batchQueue.setMaxNodes(4 + i);
            batchQueue.setMaxProcessors(5 + i);
            batchQueue.setMaxJobsInQueue(i);
            batchQueue.setMaxMemory(2000 + i);
            batchQueue.setCpuPerNode(1 + i);
            batchQueue.setDefaultNodeCount(3 + i);
            batchQueue.setDefaultCPUCount(15 + i);
            batchQueue.setDefaultWalltime(2 + i);
            batchQueue.setQueueSpecificMacros("Macros " + i);
            batchQueue.setIsDefaultQueue(i == 0);
            batchQueueList.add(batchQueue);
        }
        description.setBatchQueues(batchQueueList);

        Map<FileSystems, String> fileSysMap = new HashMap<FileSystems, String>();
        fileSysMap.put(FileSystems.HOME, "/home");
        fileSysMap.put(FileSystems.SCRATCH, "/tmp");
        description.setFileSystems(fileSysMap);

        description.setHostAliases(new ArrayList<>());

        return description;
    }

    private ResourceJobManager prepareResourceJobManager() {
        ResourceJobManager jobManager = new ResourceJobManager();
        jobManager.setResourceJobManagerType(ResourceJobManagerType.PBS);
        jobManager.setPushMonitoringEndpoint("monitor ep");
        jobManager.setJobManagerBinPath("/bin");

        Map<ApplicationParallelismType, String> parallelismPrefix =  new HashMap<>();
        parallelismPrefix.put(ApplicationParallelismType.CCM, "ccm parallel");
        jobManager.setParallelismPrefix(parallelismPrefix);

        Map<JobManagerCommand, String> commands = new HashMap<JobManagerCommand, String>();
        commands.put(JobManagerCommand.SUBMISSION, "Sub command");
        commands.put(JobManagerCommand.SHOW_QUEUE, "show q command");
        jobManager.setJobManagerCommands(commands);
        return jobManager;
    }

    private UnicoreJobSubmission prepareUnicoreJobSubmission() {
        UnicoreJobSubmission unicoreJobSubmission = new UnicoreJobSubmission();
        unicoreJobSubmission.setSecurityProtocol(SecurityProtocol.KERBEROS);
        unicoreJobSubmission.setUnicoreEndPointURL("http://endpoint");
        return unicoreJobSubmission;
    }

    private CloudJobSubmission prepareCloudJobSubmission() {
        CloudJobSubmission cloudJobSubmission = new CloudJobSubmission();
        cloudJobSubmission.setExecutableType("Executable");
        cloudJobSubmission.setProviderName(ProviderName.EC2);
        cloudJobSubmission.setNodeId("ec2 node");
        cloudJobSubmission.setSecurityProtocol(SecurityProtocol.KERBEROS);
        cloudJobSubmission.setUserAccountName("user1");
        return cloudJobSubmission;
    }

    private LOCALSubmission prepareLocalJobSubmission(ResourceJobManager jobManager) {
        LOCALSubmission localSubmission = new LOCALSubmission();
        localSubmission.setResourceJobManager(jobManager);
        localSubmission.setSecurityProtocol(SecurityProtocol.KERBEROS);
        return localSubmission;
    }

    private SSHJobSubmission prepareSSHJobSubmission(ResourceJobManager jobManager) {
        SSHJobSubmission jobSubmission = new SSHJobSubmission();
        jobSubmission.setSshPort(22);
        jobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
        jobSubmission.setMonitorMode(MonitorMode.POLL_JOB_MANAGER);
        jobSubmission.setResourceJobManager(jobManager);
        return jobSubmission;
    }

    private LOCALDataMovement prepareLocalDataMovement() {
        return new LOCALDataMovement();
    }

    private SCPDataMovement prepareScpDataMovement() {
        SCPDataMovement dataMovement = new SCPDataMovement();
        dataMovement.setSshPort(22);
        dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        return dataMovement;
    }

    private UnicoreDataMovement prepareUnicoreDataMovement() {
        UnicoreDataMovement dataMovement = new UnicoreDataMovement();
        dataMovement.setSecurityProtocol(SecurityProtocol.KERBEROS);
        dataMovement.setUnicoreEndPointURL("http://endpoint");
        return dataMovement;
    }

    private GridFTPDataMovement prepareGridFTPDataMovement(String... endpoints) {
        GridFTPDataMovement dataMovement = new GridFTPDataMovement();
        dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        List<String> endPoints = new ArrayList<String>();
        endPoints.addAll(endPoints);
        dataMovement.setGridFTPEndPoints(endPoints);
        return dataMovement;
    }

    private boolean deepCompareComputeResourceDescription(ComputeResourceDescription expected, ComputeResourceDescription actual) {
        boolean equals = EqualsBuilder.reflectionEquals(expected, actual,
                "__isset_bitfield", "batchQueues", "fileSystems", "jobSubmissionInterfaces", "dataMovementInterfaces", "ipAddresses", "hostAliases");

        equals = equals & deepCompareArrayList(expected.getBatchQueues(), actual.getBatchQueues(), false);
        equals = equals & deepCompareArrayList(expected.getJobSubmissionInterfaces(), actual.getJobSubmissionInterfaces(), false);
        equals = equals & deepCompareArrayList(expected.getDataMovementInterfaces(), actual.getDataMovementInterfaces(), false);
        equals = equals & deepCompareArrayList(expected.getIpAddresses(), actual.getIpAddresses(), false);
        equals = equals & deepCompareArrayList(expected.getHostAliases(), actual.getHostAliases(), false);
        return equals;
    }

    private boolean deepCompareArrayList(List expected, List actual, boolean preferOrder) {
        if ((expected == null) == (actual == null)) {
            if (expected == null) {
                return true;
            }

            if (expected.size() != actual.size()) {
                return false;
            }

            boolean equals = true;
            if (preferOrder) {
                for (int i = 0; i < expected.size(); i++) {
                    equals = equals & EqualsBuilder.reflectionEquals(expected.get(i), actual.get(i), "__isset_bitfield");
                }
            } else {
                boolean checked[] = new boolean[expected.size()];
                for (int i = 0; i < expected.size(); i++) {
                    equals = false;
                    for (int j = 0; j < expected.size(); j++) {
                        if (checked[j]) {
                            continue;
                        }
                        equals = equals | EqualsBuilder.reflectionEquals(expected.get(i), actual.get(j), "__isset_bitfield");
                        if (equals) {
                            checked[j] = true;
                            break;
                        }
                    }

                    if (!equals) {
                        break;
                    }
                }
            }
            return equals;
        } else {
            return false;
        }
    }
}
