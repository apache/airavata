/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.repositories.appcatalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.BatchQueue;
import org.apache.airavata.common.model.CloudJobSubmission;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.DataMovementInterface;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.FileSystems;
import org.apache.airavata.common.model.GridFTPDataMovement;
import org.apache.airavata.common.model.JobManagerCommand;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.MonitorMode;
import org.apache.airavata.common.model.ProviderName;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.common.model.SCPDataMovement;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.SecurityProtocol;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.test.util.ReflectionEquals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ComputeResourceRepositoryTest extends TestBase {

    @PersistenceContext
    private EntityManager entityManager;

    private final ComputeResourceService computeResourceService;

    public ComputeResourceRepositoryTest(ComputeResourceService computeResourceService) {
        this.computeResourceService = computeResourceService;
    }

    @Test
    public void removeBatchQueueTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);

        ComputeResourceDescription computeResourceDescription =
                prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String savedComputeResourceId = computeResourceService.addComputeResource(computeResourceDescription);

        List<BatchQueue> batchQueues = computeResourceDescription.getBatchQueues();
        Assertions.assertTrue(batchQueues.size() > 0);

        computeResourceService.removeBatchQueue(
                savedComputeResourceId, batchQueues.get(0).getQueueName());

        ComputeResourceDescription updatedComputeResource =
                computeResourceService.getComputeResource(savedComputeResourceId);

        List<BatchQueue> updatedBatchQueues = updatedComputeResource.getBatchQueues();

        if (updatedBatchQueues == null) {
            updatedBatchQueues = new java.util.ArrayList<>();
        }
        Assertions.assertEquals(batchQueues.size(), updatedBatchQueues.size() + 1);
        Optional<BatchQueue> searchedInterfaceResult = updatedBatchQueues.stream()
                .filter(queue -> queue.getQueueName().equals(batchQueues.get(0).getQueueName()))
                .findFirst();

        Assertions.assertFalse(searchedInterfaceResult.isPresent());
    }

    @Test
    public void removeDataMovementInterfaceTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);

        ComputeResourceDescription computeResourceDescription =
                prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String savedComputeResourceId = computeResourceService.addComputeResource(computeResourceDescription);

        List<DataMovementInterface> dataMovementInterfaces = computeResourceDescription.getDataMovementInterfaces();
        Assertions.assertTrue(dataMovementInterfaces.size() > 0);

        computeResourceService.removeDataMovementInterface(
                savedComputeResourceId, dataMovementInterfaces.get(0).getDataMovementInterfaceId());

        ComputeResourceDescription updatedComputeResource =
                computeResourceService.getComputeResource(savedComputeResourceId);

        List<DataMovementInterface> updatedDataMovementInterfaces = updatedComputeResource.getDataMovementInterfaces();

        if (updatedDataMovementInterfaces == null) {
            updatedDataMovementInterfaces = new java.util.ArrayList<>();
        }
        Assertions.assertEquals(dataMovementInterfaces.size(), updatedDataMovementInterfaces.size() + 1);
        Optional<DataMovementInterface> searchedInterfaceResult = updatedDataMovementInterfaces.stream()
                .filter(iface -> iface.getDataMovementInterfaceId()
                        .equals(dataMovementInterfaces.get(0).getDataMovementInterfaceId()))
                .findFirst();

        Assertions.assertFalse(searchedInterfaceResult.isPresent());
    }

    @Test
    public void removeJobSubmissionInterfaceTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);

        ComputeResourceDescription computeResourceDescription =
                prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String savedComputeResourceId = computeResourceService.addComputeResource(computeResourceDescription);

        List<JobSubmissionInterface> jobSubmissionInterfaces = computeResourceDescription.getJobSubmissionInterfaces();
        Assertions.assertTrue(jobSubmissionInterfaces.size() > 0);

        computeResourceService.removeJobSubmissionInterface(
                savedComputeResourceId, jobSubmissionInterfaces.get(0).getJobSubmissionInterfaceId());

        ComputeResourceDescription updatedComputeResource =
                computeResourceService.getComputeResource(savedComputeResourceId);

        List<JobSubmissionInterface> updatedJobSubmissionInterfaces =
                updatedComputeResource.getJobSubmissionInterfaces();

        if (updatedJobSubmissionInterfaces == null) {
            updatedJobSubmissionInterfaces = new java.util.ArrayList<>();
        }
        Assertions.assertEquals(jobSubmissionInterfaces.size(), updatedJobSubmissionInterfaces.size() + 1);
        Optional<JobSubmissionInterface> searchedInterfaceResult = updatedJobSubmissionInterfaces.stream()
                .filter(iface -> iface.getJobSubmissionInterfaceId()
                        .equals(jobSubmissionInterfaces.get(0).getJobSubmissionInterfaceId()))
                .findFirst();

        Assertions.assertFalse(searchedInterfaceResult.isPresent());
    }

    @Test
    public void listComputeResourcesTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);

        List<String> allIds = new ArrayList<>();
        List<ComputeResourceDescription> allComputeResources = new ArrayList<>();
        var allComputeResourceMap = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            ComputeResourceDescription computeResourceDescription =
                    prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);
            computeResourceDescription.setHostName("Host" + i);
            computeResourceDescription.setEnabled((i % 2 == 0));
            String savedId = computeResourceService.addComputeResource(computeResourceDescription);
            allIds.add(savedId);
            allComputeResources.add(computeResourceDescription);
            allComputeResourceMap.put(savedId, computeResourceDescription.getHostName());
        }
        entityManager.flush();

        List<ComputeResourceDescription> allSavedComputeResources = computeResourceService.getAllComputeResourceList();

        Assertions.assertTrue(
                allSavedComputeResources.size() >= 5,
                "Expected at least 5 resources, but got " + allSavedComputeResources.size());

        Map<String, ComputeResourceDescription> savedMap = new HashMap<>();
        for (ComputeResourceDescription saved : allSavedComputeResources) {
            savedMap.put(saved.getComputeResourceId(), saved);
        }

        for (int i = 0; i < 5; i++) {
            ComputeResourceDescription saved = savedMap.get(allIds.get(i));
            Assertions.assertNotNull(saved, "Resource " + allIds.get(i) + " not found in saved list");
            Assertions.assertTrue(
                    deepCompareComputeResourceDescription(allComputeResources.get(i), saved),
                    "Resource " + allIds.get(i) + " does not match expected");
        }

        var allSavedComputeResourceIds = computeResourceService.getAllComputeResourceIdList();

        Assertions.assertTrue(
                allSavedComputeResourceIds.size() >= 5,
                "Expected at least 5 resource IDs, but got " + allSavedComputeResourceIds.size());

        for (String id : allIds) {
            String host = allSavedComputeResourceIds.get(id);
            Assertions.assertNotNull(host, "Resource ID " + id + " not found in saved map");
            Assertions.assertEquals(allComputeResourceMap.get(id), host, "Host name mismatch for resource " + id);
        }

        var allAvailableIds = computeResourceService.getAvailableComputeResourceIdList();

        Assertions.assertTrue(
                allAvailableIds.size() >= 3,
                "Expected at least 3 available resources, but got " + allAvailableIds.size());
        Assertions.assertNotNull(
                allAvailableIds.get(allIds.get(0)), "Resource " + allIds.get(0) + " should be available");
        Assertions.assertNotNull(
                allAvailableIds.get(allIds.get(2)), "Resource " + allIds.get(2) + " should be available");
        Assertions.assertNotNull(
                allAvailableIds.get(allIds.get(4)), "Resource " + allIds.get(4) + " should be available");
    }

    @Test
    public void filterComputeResourcesTest() throws AppCatalogException {
        // Use unique hostname to avoid conflicts with other tests
        String uniqueHostname =
                "filter-test-host-" + java.util.UUID.randomUUID().toString();

        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);
        ComputeResourceDescription computeResourceDescription =
                prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4, uniqueHostname);

        var cfilters = new HashMap<String, String>();
        cfilters.put(DBConstants.ComputeResource.HOST_NAME, uniqueHostname);
        List<ComputeResourceDescription> computeResourceList = computeResourceService.getComputeResourceList(cfilters);

        // Should be empty before we add the resource with this unique hostname
        Assertions.assertEquals(0, computeResourceList.size());

        String computeResourceId = computeResourceService.addComputeResource(computeResourceDescription);
        computeResourceList = computeResourceService.getComputeResourceList(cfilters);

        Assertions.assertEquals(1, computeResourceList.size());
        Assertions.assertEquals(computeResourceId, computeResourceList.get(0).getComputeResourceId());

        // Expect exception when using invalid filter
        cfilters.clear();
        cfilters.put("Invalid_filter", uniqueHostname);
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> computeResourceService.getComputeResourceList(cfilters),
                "Should throw exception for invalid filter");
    }

    @Test
    public void updateComputeResourceTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);
        ComputeResourceDescription computeResourceDescription =
                prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4);

        String computeResourceId = computeResourceService.addComputeResource(computeResourceDescription);

        ComputeResourceDescription savedComputeResource = computeResourceService.getComputeResource(computeResourceId);
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

        if (savedComputeResource.getBatchQueues() == null) {
            savedComputeResource.setBatchQueues(new java.util.ArrayList<>());
        }
        savedComputeResource.getBatchQueues().add(batchQueue);
        savedComputeResource.setCpusPerNode(43);
        savedComputeResource.setDefaultWalltime(4343);

        computeResourceService.updateComputeResource(computeResourceId, savedComputeResource);

        ComputeResourceDescription updatedComputeResource =
                computeResourceService.getComputeResource(computeResourceId);
        Assertions.assertTrue(deepCompareComputeResourceDescription(savedComputeResource, updatedComputeResource));
    }

    @Test
    public void addComputeResourceTest() throws AppCatalogException {
        // Use unique ID to avoid conflicts with other test runs
        String uniqueResourceId =
                "compute-resource-" + java.util.UUID.randomUUID().toString();
        String uniqueHostname = "add-test-host-" + java.util.UUID.randomUUID().toString();

        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(resourceJobManager);
        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(resourceJobManager);
        String sshSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String scpDataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        GridFTPDataMovement gridFTPDataMovement = prepareGridFTPDataMovement("192.156.33.44");
        String gridFTPDataMovementId = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement);
        ComputeResourceDescription computeResourceDescription =
                prepareComputeResource(sshSubmissionId, scpDataMovementId, gridFTPDataMovementId, 4, uniqueHostname);

        computeResourceDescription.setComputeResourceId(uniqueResourceId);

        // Should not exist before adding
        Assertions.assertNull(computeResourceService.getComputeResource(uniqueResourceId));
        String computeResourceId = computeResourceService.addComputeResource(computeResourceDescription);
        Assertions.assertEquals(uniqueResourceId, computeResourceId);
        Assertions.assertTrue(computeResourceService.isComputeResourceExists(computeResourceId));
        ComputeResourceDescription savedComputeResource = computeResourceService.getComputeResource(uniqueResourceId);
        Assertions.assertNotNull(savedComputeResource);

        Assertions.assertTrue(deepCompareComputeResourceDescription(computeResourceDescription, savedComputeResource));
    }

    @Test
    public void addResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceService.addResourceJobManager(resourceJobManager);
        ResourceJobManager savedJobManager = computeResourceService.getResourceJobManager(jobManagerId);
        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(resourceJobManager, savedJobManager, "__isset_bitfield"));
    }

    @Test
    public void deleteResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceService.addResourceJobManager(resourceJobManager);

        Assertions.assertNotNull(computeResourceService.getResourceJobManager(jobManagerId));
        computeResourceService.deleteResourceJobManager(jobManagerId);
        Assertions.assertNull(computeResourceService.getResourceJobManager(jobManagerId));
    }

    @Test
    public void updateResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceService.addResourceJobManager(resourceJobManager);
        ResourceJobManager savedJobManager = computeResourceService.getResourceJobManager(jobManagerId);

        savedJobManager.setJobManagerBinPath("/new bin");
        savedJobManager.getJobManagerCommands().put(JobManagerCommand.SHOW_START, "New Command Value");
        savedJobManager.getParallelismPrefix().put(ApplicationParallelismType.MPI, "MPI Type");

        computeResourceService.updateResourceJobManager(jobManagerId, savedJobManager);

        ResourceJobManager updatedJobManager = computeResourceService.getResourceJobManager(jobManagerId);

        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(savedJobManager, updatedJobManager, "__isset_bitfield"));
    }

    @Test
    public void addCloudJobSubmissionTest() throws AppCatalogException {

        CloudJobSubmission cloudJobSubmission = prepareCloudJobSubmission();
        String savedSubmissionId = computeResourceService.addCloudJobSubmission(cloudJobSubmission);
        CloudJobSubmission savedSubmission = computeResourceService.getCloudJobSubmission(savedSubmissionId);

        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(cloudJobSubmission, savedSubmission, "__isset_bitfield"));
    }

    @Test
    public void addSSHJobSubmissionTest() throws AppCatalogException {
        ResourceJobManager jobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(jobManager);

        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(jobManager);
        String jobSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);
        SSHJobSubmission savedJobSubmission = computeResourceService.getSSHJobSubmission(jobSubmissionId);

        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(sshJobSubmission, savedJobSubmission, "__isset_bitfield"));
    }

    @Test
    public void addSCPDataMovementTest() throws AppCatalogException {
        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String dataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        SCPDataMovement savedDataMovement = computeResourceService.getSCPDataMovement(dataMovementId);
        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(scpDataMovement, savedDataMovement, "__isset_bitfield"));
    }

    @Test
    public void addGridFTPDataMovementTest() throws AppCatalogException {
        GridFTPDataMovement gridFTPDataMovement1 = prepareGridFTPDataMovement("222.33.43.444", "23.344.44.454");
        String dataMovementId1 = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement1);
        GridFTPDataMovement savedDataMovement1 = computeResourceService.getGridFTPDataMovement(dataMovementId1);
        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(gridFTPDataMovement1, savedDataMovement1, "__isset_bitfield"));

        GridFTPDataMovement gridFTPDataMovement2 = prepareGridFTPDataMovement("222.33.43.445", "23.344.44.400");
        String dataMovementId2 = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement2);
        GridFTPDataMovement savedDataMovement2 = computeResourceService.getGridFTPDataMovement(dataMovementId2);
        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(gridFTPDataMovement2, savedDataMovement2, "__isset_bitfield"));
    }

    @Test
    public void fetchNotAvailableResourceTest() throws AppCatalogException {
        Assertions.assertNull(computeResourceService.getResourceJobManager("INVALID ID"));
        Assertions.assertNull(computeResourceService.getComputeResource("INVALID ID"));
        Assertions.assertNull(computeResourceService.getCloudJobSubmission("INVALID ID"));
        Assertions.assertEquals(
                0, computeResourceService.getFileSystems("INVALID ID").size());
        Assertions.assertNull(computeResourceService.getGridFTPDataMovement("INVALID ID"));
        Assertions.assertNull(computeResourceService.getSCPDataMovement("INVALID ID"));
    }

    private ComputeResourceDescription prepareComputeResource(
            String sshSubmissionId, String scpDataMoveId, String gridFTPDataMoveId, int batchQueueCount) {
        // Default hostname with UUID for test isolation
        return prepareComputeResource(
                sshSubmissionId,
                scpDataMoveId,
                gridFTPDataMoveId,
                batchQueueCount,
                "test-host-" + java.util.UUID.randomUUID().toString());
    }

    private ComputeResourceDescription prepareComputeResource(
            String sshSubmissionId,
            String scpDataMoveId,
            String gridFTPDataMoveId,
            int batchQueueCount,
            String hostName) {
        ComputeResourceDescription description = new ComputeResourceDescription();

        description.setHostName(hostName);
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

        var fileSysMap = new HashMap<FileSystems, String>();
        fileSysMap.put(FileSystems.HOME, "/home");
        fileSysMap.put(FileSystems.SCRATCH, "/tmp");
        description.setFileSystems(fileSysMap);

        description.setHostAliases(new ArrayList<>());

        return description;
    }

    private ResourceJobManager prepareResourceJobManager() {
        ResourceJobManager jobManager = new ResourceJobManager();

        jobManager.setResourceJobManagerType(ResourceJobManagerType.SLURM);
        jobManager.setPushMonitoringEndpoint("monitor ep");
        jobManager.setJobManagerBinPath("/usr/bin");

        var parallelismPrefix = new HashMap<ApplicationParallelismType, String>();
        parallelismPrefix.put(ApplicationParallelismType.CCM, "ccm parallel");
        jobManager.setParallelismPrefix(parallelismPrefix);

        var commands = new HashMap<JobManagerCommand, String>();
        commands.put(JobManagerCommand.SUBMISSION, "sbatch");
        commands.put(JobManagerCommand.JOB_MONITORING, "squeue");
        commands.put(JobManagerCommand.DELETION, "scancel");
        commands.put(JobManagerCommand.SHOW_QUEUE, "squeue");
        jobManager.setJobManagerCommands(commands);
        return jobManager;
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

    private SSHJobSubmission prepareSSHJobSubmission(ResourceJobManager jobManager) {
        SSHJobSubmission jobSubmission = new SSHJobSubmission();
        jobSubmission.setSshPort(22);
        jobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
        jobSubmission.setMonitorMode(MonitorMode.POLL_JOB_MANAGER);
        jobSubmission.setResourceJobManager(jobManager);
        return jobSubmission;
    }

    private SCPDataMovement prepareScpDataMovement() {
        SCPDataMovement dataMovement = new SCPDataMovement();
        dataMovement.setSshPort(22);
        dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        return dataMovement;
    }

    private GridFTPDataMovement prepareGridFTPDataMovement(String... endpoints) {
        GridFTPDataMovement dataMovement = new GridFTPDataMovement();
        dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        List<String> endPoints = new ArrayList<String>();
        endPoints.addAll(Arrays.asList(endpoints));
        dataMovement.setGridFTPEndPoints(endPoints);
        return dataMovement;
    }

    private boolean deepCompareComputeResourceDescription(
            ComputeResourceDescription expected, ComputeResourceDescription actual) {
        boolean equals = ReflectionEquals.reflectionEquals(
                expected,
                actual,
                "__isset_bitfield",
                "computeResourceId",
                "linkedStorageResourceId",
                "projects",
                "batchQueues",
                "fileSystems",
                "jobSubmissionInterfaces",
                "dataMovementInterfaces",
                "ipAddresses",
                "hostAliases",
                "creationTime",
                "updateTime");

        equals = equals & deepCompareArrayList(expected.getBatchQueues(), actual.getBatchQueues(), false);
        equals = equals
                & deepCompareArrayList(
                        expected.getJobSubmissionInterfaces(), actual.getJobSubmissionInterfaces(), false);
        equals = equals
                & deepCompareArrayList(expected.getDataMovementInterfaces(), actual.getDataMovementInterfaces(), false);
        equals = equals & deepCompareStringList(expected.getIpAddresses(), actual.getIpAddresses());
        equals = equals & deepCompareStringList(expected.getHostAliases(), actual.getHostAliases());
        return equals;
    }

    private boolean deepCompareStringList(List<String> expected, List<String> actual) {
        if ((expected == null) == (actual == null)) {
            if (expected == null) {
                return true;
            }
            if (actual == null || expected.size() != actual.size()) {
                return false;
            }

            return new java.util.HashSet<>(expected).equals(new java.util.HashSet<>(actual));
        }
        return false;
    }

    private boolean deepCompareArrayList(List<?> expected, List<?> actual, boolean preferOrder) {
        if ((expected == null) == (actual == null)) {
            if (expected == null) {
                return true;
            }

            if (actual == null || expected.size() != actual.size()) {
                return false;
            }

            String[] excludeFields =
                    new String[] {"__isset_bitfield", "creationTime", "updateTime", "storageResourceId"};

            boolean equals = true;
            if (preferOrder) {
                for (int i = 0; i < expected.size(); i++) {
                    Object expectedItem = expected.get(i);
                    Object actualItem = actual.get(i);

                    if (expectedItem instanceof org.apache.airavata.common.model.BatchQueue) {

                        org.apache.airavata.common.model.BatchQueue expectedBq =
                                (org.apache.airavata.common.model.BatchQueue) expectedItem;
                        org.apache.airavata.common.model.BatchQueue actualBq =
                                (org.apache.airavata.common.model.BatchQueue) actualItem;

                        boolean bqEquals = Objects.equals(expectedBq.getQueueName(), actualBq.getQueueName())
                                && Objects.equals(expectedBq.getQueueDescription(), actualBq.getQueueDescription())
                                && Objects.equals(expectedBq.getMaxNodes(), actualBq.getMaxNodes())
                                && Objects.equals(expectedBq.getMaxProcessors(), actualBq.getMaxProcessors())
                                && Objects.equals(expectedBq.getMaxJobsInQueue(), actualBq.getMaxJobsInQueue())
                                && Objects.equals(expectedBq.getMaxMemory(), actualBq.getMaxMemory())
                                && Objects.equals(expectedBq.getCpuPerNode(), actualBq.getCpuPerNode())
                                && Objects.equals(expectedBq.getDefaultNodeCount(), actualBq.getDefaultNodeCount())
                                && Objects.equals(expectedBq.getDefaultCPUCount(), actualBq.getDefaultCPUCount())
                                && Objects.equals(expectedBq.getDefaultWalltime(), actualBq.getDefaultWalltime())
                                && Objects.equals(
                                        expectedBq.getQueueSpecificMacros(), actualBq.getQueueSpecificMacros())
                                && Objects.equals(expectedBq.getIsDefaultQueue(), actualBq.getIsDefaultQueue());

                        equals = equals & bqEquals;
                    } else if (expectedItem instanceof org.apache.airavata.common.model.JobSubmissionInterface
                            && actualItem instanceof org.apache.airavata.common.model.JobSubmissionInterface) {

                        org.apache.airavata.common.model.JobSubmissionInterface expectedJsi =
                                (org.apache.airavata.common.model.JobSubmissionInterface) expectedItem;
                        org.apache.airavata.common.model.JobSubmissionInterface actualJsi =
                                (org.apache.airavata.common.model.JobSubmissionInterface) actualItem;
                        boolean jsiEquals = Objects.equals(
                                        expectedJsi.getJobSubmissionInterfaceId(),
                                        actualJsi.getJobSubmissionInterfaceId())
                                && Objects.equals(expectedJsi.getPriorityOrder(), actualJsi.getPriorityOrder())
                                && Objects.equals(
                                        expectedJsi.getJobSubmissionProtocol(), actualJsi.getJobSubmissionProtocol());
                        equals = equals & jsiEquals;
                    } else if (expectedItem instanceof org.apache.airavata.common.model.DataMovementInterface
                            && actualItem instanceof org.apache.airavata.common.model.DataMovementInterface) {

                        org.apache.airavata.common.model.DataMovementInterface expectedDmi =
                                (org.apache.airavata.common.model.DataMovementInterface) expectedItem;
                        org.apache.airavata.common.model.DataMovementInterface actualDmi =
                                (org.apache.airavata.common.model.DataMovementInterface) actualItem;
                        boolean dmiEquals = Objects.equals(
                                        expectedDmi.getDataMovementInterfaceId(),
                                        actualDmi.getDataMovementInterfaceId())
                                && Objects.equals(expectedDmi.getPriorityOrder(), actualDmi.getPriorityOrder())
                                && Objects.equals(
                                        expectedDmi.getDataMovementProtocol(), actualDmi.getDataMovementProtocol());
                        equals = equals & dmiEquals;
                    } else {
                        equals = equals & ReflectionEquals.reflectionEquals(expectedItem, actualItem, excludeFields);
                    }
                }
            } else {
                boolean checked[] = new boolean[actual.size()];
                for (int i = 0; i < expected.size(); i++) {
                    equals = false;
                    Object expectedItem = expected.get(i);
                    for (int j = 0; j < actual.size(); j++) {
                        if (checked[j]) {
                            continue;
                        }
                        Object actualItem = actual.get(j);

                        if (expectedItem instanceof org.apache.airavata.common.model.BatchQueue) {

                            org.apache.airavata.common.model.BatchQueue expectedBq =
                                    (org.apache.airavata.common.model.BatchQueue) expectedItem;
                            org.apache.airavata.common.model.BatchQueue actualBq =
                                    (org.apache.airavata.common.model.BatchQueue) actualItem;

                            boolean bqEquals = Objects.equals(expectedBq.getQueueName(), actualBq.getQueueName())
                                    && Objects.equals(expectedBq.getQueueDescription(), actualBq.getQueueDescription())
                                    && Objects.equals(expectedBq.getMaxNodes(), actualBq.getMaxNodes())
                                    && Objects.equals(expectedBq.getMaxProcessors(), actualBq.getMaxProcessors())
                                    && Objects.equals(expectedBq.getMaxJobsInQueue(), actualBq.getMaxJobsInQueue())
                                    && Objects.equals(expectedBq.getMaxMemory(), actualBq.getMaxMemory())
                                    && Objects.equals(expectedBq.getCpuPerNode(), actualBq.getCpuPerNode())
                                    && Objects.equals(expectedBq.getDefaultNodeCount(), actualBq.getDefaultNodeCount())
                                    && Objects.equals(expectedBq.getDefaultCPUCount(), actualBq.getDefaultCPUCount())
                                    && Objects.equals(expectedBq.getDefaultWalltime(), actualBq.getDefaultWalltime())
                                    && Objects.equals(
                                            expectedBq.getQueueSpecificMacros(), actualBq.getQueueSpecificMacros())
                                    && Objects.equals(expectedBq.getIsDefaultQueue(), actualBq.getIsDefaultQueue());

                            equals = bqEquals;
                        } else if (expectedItem instanceof org.apache.airavata.common.model.JobSubmissionInterface
                                && actualItem instanceof org.apache.airavata.common.model.JobSubmissionInterface) {

                            org.apache.airavata.common.model.JobSubmissionInterface expectedJsi =
                                    (org.apache.airavata.common.model.JobSubmissionInterface) expectedItem;
                            org.apache.airavata.common.model.JobSubmissionInterface actualJsi =
                                    (org.apache.airavata.common.model.JobSubmissionInterface) actualItem;
                            equals = Objects.equals(
                                            expectedJsi.getJobSubmissionInterfaceId(),
                                            actualJsi.getJobSubmissionInterfaceId())
                                    && Objects.equals(expectedJsi.getPriorityOrder(), actualJsi.getPriorityOrder())
                                    && Objects.equals(
                                            expectedJsi.getJobSubmissionProtocol(),
                                            actualJsi.getJobSubmissionProtocol());
                        } else if (expectedItem instanceof org.apache.airavata.common.model.DataMovementInterface
                                && actualItem instanceof org.apache.airavata.common.model.DataMovementInterface) {

                            org.apache.airavata.common.model.DataMovementInterface expectedDmi =
                                    (org.apache.airavata.common.model.DataMovementInterface) expectedItem;
                            org.apache.airavata.common.model.DataMovementInterface actualDmi =
                                    (org.apache.airavata.common.model.DataMovementInterface) actualItem;
                            equals = Objects.equals(
                                            expectedDmi.getDataMovementInterfaceId(),
                                            actualDmi.getDataMovementInterfaceId())
                                    && Objects.equals(expectedDmi.getPriorityOrder(), actualDmi.getPriorityOrder())
                                    && Objects.equals(
                                            expectedDmi.getDataMovementProtocol(), actualDmi.getDataMovementProtocol());
                        } else {
                            equals = ReflectionEquals.reflectionEquals(expectedItem, actualItem, excludeFields);
                        }
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
