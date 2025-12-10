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

import java.util.*;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, ComputeResourceRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ComputeResourceRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}

    private final ComputeResourceService computeResourceService;

    public ComputeResourceRepositoryTest(ComputeResourceService computeResourceService) {
        super(Database.APP_CATALOG);
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
        Map<String, String> allComputeResourceMap = new HashMap<>();
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

        List<ComputeResourceDescription> allSavedComputeResources = computeResourceService.getAllComputeResourceList();

        Assertions.assertEquals(5, allSavedComputeResources.size());
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(
                    deepCompareComputeResourceDescription(allComputeResources.get(i), allSavedComputeResources.get(i)));
        }

        Map<String, String> allSavedComputeResourceIds = computeResourceService.getAllComputeResourceIdList();

        Assertions.assertEquals(5, allSavedComputeResourceIds.size());

        for (String id : allIds) {
            String host = allSavedComputeResourceIds.get(id);
            Assertions.assertNotNull(host);
            Assertions.assertEquals(allComputeResourceMap.get(id), host);
        }

        Map<String, String> allAvailableIds = computeResourceService.getAvailableComputeResourceIdList();

        Assertions.assertEquals(3, allAvailableIds.size());
        Assertions.assertNotNull(allAvailableIds.get(allIds.get(0)));
        Assertions.assertNotNull(allAvailableIds.get(allIds.get(2)));
        Assertions.assertNotNull(allAvailableIds.get(allIds.get(4)));
    }

    @Test
    public void filterComputeResourcesTest() throws AppCatalogException {

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

        Map<String, String> cfilters = new HashMap<String, String>();
        cfilters.put(DBConstants.ComputeResource.HOST_NAME, "localhost");
        List<ComputeResourceDescription> computeResourceList = computeResourceService.getComputeResourceList(cfilters);

        Assertions.assertEquals(0, computeResourceList.size());

        String computeResourceId = computeResourceService.addComputeResource(computeResourceDescription);
        computeResourceList = computeResourceService.getComputeResourceList(cfilters);

        Assertions.assertEquals(1, computeResourceList.size());

        Assertions.assertEquals(computeResourceId, computeResourceList.get(0).getComputeResourceId());

        try {
            cfilters = new HashMap<String, String>();
            cfilters.put("Invalid_filter", "localhost");
            computeResourceService.getComputeResourceList(cfilters);
            Assertions.fail();
        } catch (Exception e) {
            // ignore
        }
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

        computeResourceDescription.setComputeResourceId("manually-entered-id");

        Assertions.assertNull(computeResourceService.getComputeResource("manually-entered-id"));
        String computeResourceId = computeResourceService.addComputeResource(computeResourceDescription);
        Assertions.assertEquals("manually-entered-id", computeResourceId);
        Assertions.assertTrue(computeResourceService.isComputeResourceExists(computeResourceId));
        ComputeResourceDescription savedComputeResource =
                computeResourceService.getComputeResource("manually-entered-id");
        Assertions.assertNotNull(savedComputeResource);

        Assertions.assertTrue(deepCompareComputeResourceDescription(computeResourceDescription, savedComputeResource));
    }

    @Test
    public void addResourceJobManagerTest() throws AppCatalogException {
        ResourceJobManager resourceJobManager = prepareResourceJobManager();
        String jobManagerId = computeResourceService.addResourceJobManager(resourceJobManager);
        ResourceJobManager savedJobManager = computeResourceService.getResourceJobManager(jobManagerId);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(resourceJobManager, savedJobManager, "__isset_bitfield"));
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

        Assertions.assertTrue(EqualsBuilder.reflectionEquals(savedJobManager, updatedJobManager, "__isset_bitfield"));
    }

    @Test
    public void addCloudJobSubmissionTest() throws AppCatalogException {
        // Test AWS CloudJobSubmission (EC2)
        CloudJobSubmission cloudJobSubmission = prepareCloudJobSubmission();
        String savedSubmissionId = computeResourceService.addCloudJobSubmission(cloudJobSubmission);
        CloudJobSubmission savedSubmission = computeResourceService.getCloudJobSubmission(savedSubmissionId);

        Assertions.assertTrue(EqualsBuilder.reflectionEquals(cloudJobSubmission, savedSubmission, "__isset_bitfield"));
    }

    @Test
    public void addSSHJobSubmissionTest() throws AppCatalogException {
        ResourceJobManager jobManager = prepareResourceJobManager();
        computeResourceService.addResourceJobManager(jobManager);

        SSHJobSubmission sshJobSubmission = prepareSSHJobSubmission(jobManager);
        String jobSubmissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);
        SSHJobSubmission savedJobSubmission = computeResourceService.getSSHJobSubmission(jobSubmissionId);

        Assertions.assertTrue(EqualsBuilder.reflectionEquals(sshJobSubmission, savedJobSubmission, "__isset_bitfield"));
    }

    @Test
    public void addSCPDataMovementTest() throws AppCatalogException {
        SCPDataMovement scpDataMovement = prepareScpDataMovement();
        String dataMovementId = computeResourceService.addScpDataMovement(scpDataMovement);

        SCPDataMovement savedDataMovement = computeResourceService.getSCPDataMovement(dataMovementId);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(scpDataMovement, savedDataMovement, "__isset_bitfield"));
    }

    @Test
    public void addGridFTPDataMovementTest() throws AppCatalogException {
        GridFTPDataMovement gridFTPDataMovement1 = prepareGridFTPDataMovement("222.33.43.444", "23.344.44.454");
        String dataMovementId1 = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement1);
        GridFTPDataMovement savedDataMovement1 = computeResourceService.getGridFTPDataMovement(dataMovementId1);
        Assertions.assertTrue(
                EqualsBuilder.reflectionEquals(gridFTPDataMovement1, savedDataMovement1, "__isset_bitfield"));

        GridFTPDataMovement gridFTPDataMovement2 = prepareGridFTPDataMovement("222.33.43.445", "23.344.44.400");
        String dataMovementId2 = computeResourceService.addGridFTPDataMovement(gridFTPDataMovement2);
        GridFTPDataMovement savedDataMovement2 = computeResourceService.getGridFTPDataMovement(dataMovementId2);
        Assertions.assertTrue(
                EqualsBuilder.reflectionEquals(gridFTPDataMovement2, savedDataMovement2, "__isset_bitfield"));
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
        // Changed from PBS to SLURM as per requirements
        jobManager.setResourceJobManagerType(ResourceJobManagerType.SLURM);
        jobManager.setPushMonitoringEndpoint("monitor ep");
        jobManager.setJobManagerBinPath("/usr/bin");

        Map<ApplicationParallelismType, String> parallelismPrefix = new HashMap<>();
        parallelismPrefix.put(ApplicationParallelismType.CCM, "ccm parallel");
        jobManager.setParallelismPrefix(parallelismPrefix);

        Map<JobManagerCommand, String> commands = new HashMap<JobManagerCommand, String>();
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
        endPoints.addAll(endPoints);
        dataMovement.setGridFTPEndPoints(endPoints);
        return dataMovement;
    }

    private boolean deepCompareComputeResourceDescription(
            ComputeResourceDescription expected, ComputeResourceDescription actual) {
        boolean equals = EqualsBuilder.reflectionEquals(
                expected,
                actual,
                "__isset_bitfield",
                "batchQueues",
                "fileSystems",
                "jobSubmissionInterfaces",
                "dataMovementInterfaces",
                "ipAddresses",
                "hostAliases");

        equals = equals & deepCompareArrayList(expected.getBatchQueues(), actual.getBatchQueues(), false);
        equals = equals
                & deepCompareArrayList(
                        expected.getJobSubmissionInterfaces(), actual.getJobSubmissionInterfaces(), false);
        equals = equals
                & deepCompareArrayList(expected.getDataMovementInterfaces(), actual.getDataMovementInterfaces(), false);
        equals = equals & deepCompareArrayList(expected.getIpAddresses(), actual.getIpAddresses(), false);
        equals = equals & deepCompareArrayList(expected.getHostAliases(), actual.getHostAliases(), false);
        return equals;
    }

    private boolean deepCompareArrayList(List<?> expected, List<?> actual, boolean preferOrder) {
        if ((expected == null) == (actual == null)) {
            if (expected == null) {
                return true;
            }

            if (actual == null || expected.size() != actual.size()) {
                return false;
            }

            boolean equals = true;
            if (preferOrder) {
                for (int i = 0; i < expected.size(); i++) {
                    equals =
                            equals & EqualsBuilder.reflectionEquals(expected.get(i), actual.get(i), "__isset_bitfield");
                }
            } else {
                boolean checked[] = new boolean[expected.size()];
                for (int i = 0; i < expected.size(); i++) {
                    equals = false;
                    for (int j = 0; j < expected.size(); j++) {
                        if (checked[j]) {
                            continue;
                        }
                        equals = equals
                                | EqualsBuilder.reflectionEquals(expected.get(i), actual.get(j), "__isset_bitfield");
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
