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

package org.apache.aiaravata.application.catalog.data.util;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.airavata.model.Workflow;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.*;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreDataMovement;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;

import java.util.*;

public class AppCatalogThriftConversion {
    public static ComputeResourceResource getComputeHostResource (ComputeResourceDescription description){
        ComputeResourceResource resource = new ComputeResourceResource();
        resource.setHostName(description.getHostName());
        resource.setResourceDescription(description.getResourceDescription());
        resource.setResourceId(description.getComputeResourceId());
        resource.setMaxMemoryPerNode(description.getMaxMemoryPerNode());
        resource.setEnabled(description.isEnabled());
        return resource;
    }

    public static ComputeResourceDescription getComputeHostDescription (ComputeResourceResource resource) throws AppCatalogException {
        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setComputeResourceId(resource.getResourceId());
        description.setHostName(resource.getHostName());
        description.setResourceDescription(resource.getResourceDescription());
        description.setMaxMemoryPerNode(resource.getMaxMemoryPerNode());
        description.setEnabled(resource.isEnabled());
        HostAliasResource aliasResource = new HostAliasResource();
        List<Resource> resources = aliasResource.get(AbstractResource.HostAliasConstants.RESOURCE_ID, resource.getResourceId());
        if (resources != null && !resources.isEmpty()){
            description.setHostAliases(getHostAliases(resources));
        }
        HostIPAddressResource ipAddressResource = new HostIPAddressResource();
        List<Resource> ipAddresses = ipAddressResource.get(AbstractResource.HostIPAddressConstants.RESOURCE_ID, resource.getResourceId());
        if (ipAddresses != null && !ipAddresses.isEmpty()){
            description.setIpAddresses(getIpAddresses(ipAddresses));
        }

        BatchQueueResource bqResource = new BatchQueueResource();
        List<Resource> batchQueues = bqResource.get(AbstractResource.BatchQueueConstants.COMPUTE_RESOURCE_ID, resource.getResourceId());
        if (batchQueues != null && !batchQueues.isEmpty()){
            description.setBatchQueues(getBatchQueues(batchQueues));
        }
        
        ComputeResourceFileSystemResource fsResource = new ComputeResourceFileSystemResource();
        List<Resource> fsList = fsResource.get(AbstractResource.ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID, resource.getResourceId());
        description.setFileSystems(new HashMap<FileSystems,String>());
        if (fsList != null && !fsList.isEmpty()){
        	for (Resource r : fsList) {
        		ComputeResourceFileSystemResource rr=(ComputeResourceFileSystemResource)r;
        		description.getFileSystems().put(FileSystems.valueOf(rr.getFileSystem()), rr.getPath());
			}
        }
        
        JobSubmissionInterfaceResource jsiResource = new JobSubmissionInterfaceResource();
        List<Resource> hsiList = jsiResource.get(AbstractResource.JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID, resource.getResourceId());
        if (hsiList != null && !hsiList.isEmpty()){
            description.setJobSubmissionInterfaces(getJobSubmissionInterfaces(hsiList));
        }
        
        DataMovementInterfaceResource dmiResource = new DataMovementInterfaceResource();
        List<Resource> dmiList = dmiResource.get(AbstractResource.DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID, resource.getResourceId());
        if (dmiList != null && !dmiList.isEmpty()){
            description.setDataMovementInterfaces(getDataMovementInterfaces(dmiList));
        }
        return description;
    }

    public static  List<ComputeResourceDescription> getComputeDescriptionList (List<Resource> resources) throws AppCatalogException {
        List<ComputeResourceDescription> list = new ArrayList<ComputeResourceDescription>();
        for (Resource resource : resources){
            list.add(getComputeHostDescription((ComputeResourceResource)resource));
        }
        return list;
    }

    public static List<String> getHostAliases (List<Resource> resources){
        List<String> hostAliases = new ArrayList<String>();
        for (Resource alias : resources){
            hostAliases.add(((HostAliasResource)alias).getAlias());
        }
        return hostAliases;
    }

    public static List<String> getIpAddresses (List<Resource> resources){
        List<String> hostIpAddresses = new ArrayList<String>();
        for (Resource resource : resources){
            hostIpAddresses.add(((HostIPAddressResource)resource).getIpaddress());
        }
        return hostIpAddresses;
    }
    
    public static List<BatchQueue> getBatchQueues (List<Resource> resources){
    	List<BatchQueue> batchQueues = new ArrayList<BatchQueue>();
        for (Resource resource : resources){
        	batchQueues.add(getBatchQueue((BatchQueueResource)resource));
        }
        return batchQueues;
    }
    
    public static List<DataMovementInterface> getDataMovementInterfaces(List<Resource> resources){
    	List<DataMovementInterface> dataMovementInterfaces = new ArrayList<DataMovementInterface>();
        for (Resource resource : resources){
        	dataMovementInterfaces.add(getDataMovementInterface((DataMovementInterfaceResource)resource));
        }
        return dataMovementInterfaces;
    }
    
    public static DataMovementInterface getDataMovementInterface(DataMovementInterfaceResource resource){
    	DataMovementInterface dmi = new DataMovementInterface();
    	dmi.setDataMovementInterfaceId(resource.getDataMovementInterfaceId());
    	dmi.setDataMovementProtocol(DataMovementProtocol.valueOf(resource.getDataMovementProtocol()));
    	dmi.setPriorityOrder(resource.getPriorityOrder());
        return dmi;
    }
    
    public static DataMovementInterfaceResource getDataMovementInterface(DataMovementInterface resource){
    	DataMovementInterfaceResource dmi = new DataMovementInterfaceResource();
    	dmi.setDataMovementInterfaceId(resource.getDataMovementInterfaceId());
    	dmi.setDataMovementProtocol(resource.getDataMovementProtocol().toString());
    	dmi.setPriorityOrder(resource.getPriorityOrder());
        return dmi;
    }
    
    public static List<JobSubmissionInterface> getJobSubmissionInterfaces(List<Resource> resources){
    	List<JobSubmissionInterface> jobSubmissionInterfaces = new ArrayList<JobSubmissionInterface>();
        for (Resource resource : resources){
        	jobSubmissionInterfaces.add(getJobSubmissionInterface((JobSubmissionInterfaceResource)resource));
        }
        return jobSubmissionInterfaces;
    }
    
    public static JobSubmissionInterface getJobSubmissionInterface(JobSubmissionInterfaceResource resource){
    	JobSubmissionInterface jsi = new JobSubmissionInterface();
    	jsi.setJobSubmissionInterfaceId(resource.getJobSubmissionInterfaceId());
    	jsi.setJobSubmissionProtocol(JobSubmissionProtocol.valueOf(resource.getJobSubmissionProtocol()));
    	jsi.setPriorityOrder(resource.getPriorityOrder());
        return jsi;
    }
    
    public static JobSubmissionInterfaceResource getJobSubmissionInterface(JobSubmissionInterface resource){
    	JobSubmissionInterfaceResource jsi = new JobSubmissionInterfaceResource();
    	jsi.setJobSubmissionInterfaceId(resource.getJobSubmissionInterfaceId());
    	jsi.setJobSubmissionProtocol(resource.getJobSubmissionProtocol().toString());
    	jsi.setPriorityOrder(resource.getPriorityOrder());
        return jsi;
    }
    
    public static BatchQueue getBatchQueue(BatchQueueResource resource){
    	BatchQueue batchQueue = new BatchQueue();
    	batchQueue.setMaxJobsInQueue(resource.getMaxJobInQueue());
    	batchQueue.setMaxNodes(resource.getMaxNodes());
    	batchQueue.setMaxProcessors(resource.getMaxProcessors());
    	batchQueue.setMaxRunTime(resource.getMaxRuntime());
    	batchQueue.setMaxMemory(resource.getMaxMemory());
    	batchQueue.setQueueDescription(resource.getQueueDescription());
    	batchQueue.setQueueName(resource.getQueueName());
        return batchQueue;
    }

    public static BatchQueueResource getBatchQueue(BatchQueue resource){
    	BatchQueueResource batchQueue = new BatchQueueResource();
    	batchQueue.setMaxJobInQueue(resource.getMaxJobsInQueue());
    	batchQueue.setMaxNodes(resource.getMaxNodes());
    	batchQueue.setMaxProcessors(resource.getMaxProcessors());
    	batchQueue.setMaxRuntime(resource.getMaxRunTime());
    	batchQueue.setQueueDescription(resource.getQueueDescription());
    	batchQueue.setQueueName(resource.getQueueName());
    	batchQueue.setMaxMemory(resource.getMaxMemory());
        return batchQueue;
    }
    
//    public static Map<String, JobSubmissionProtocol> getJobSubmissionProtocolList(List<Resource> resources){
//       Map<String, JobSubmissionProtocol> protocols = new HashMap<String, JobSubmissionProtocol>();
//        for (Resource resource : resources){
//            JobSubmissionProtocolResource submission = (JobSubmissionProtocolResource) resource;
//            protocols.put(submission.getSubmissionID(), JobSubmissionProtocol.valueOf(submission.getJobType()));
//        }
//        return protocols;
//    }

//    public static Map<String, DataMovementProtocol> getDataMoveProtocolList(List<Resource> resources){
//        Map<String, DataMovementProtocol> protocols = new HashMap<String, DataMovementProtocol>();
//        for (Resource resource : resources){
//            DataMovementProtocolResource protocolResource = (DataMovementProtocolResource) resource;
//            protocols.put(protocolResource.getDataMoveID(), DataMovementProtocol.valueOf(protocolResource.getDataMoveType()));
//        }
//        return protocols;
//    }

    public static SshJobSubmissionResource getSSHJobSubmission (SSHJobSubmission submission){
    	SshJobSubmissionResource resource = new SshJobSubmissionResource();
        resource.setAlternativeSshHostname(submission.getAlternativeSSHHostName());
        resource.setJobSubmissionInterfaceId(submission.getJobSubmissionInterfaceId());
        ResourceJobManagerResource resourceJobManager = getResourceJobManager(submission.getResourceJobManager());
//        resourceJobManager.setResourceJobManagerId(submission.getJobSubmissionInterfaceId());
        resource.setResourceJobManagerId(resourceJobManager.getResourceJobManagerId());
        if (submission.getMonitorMode() != null){
            resource.setMonitorMode(submission.getMonitorMode().toString());
        }
        resource.setResourceJobManagerResource(resourceJobManager);
        if (submission.getSecurityProtocol() != null){
            resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
        }
        resource.setSshPort(submission.getSshPort());
        return resource;
    }
    
    
    public static UnicoreJobSubmissionResource getUnicoreJobSubmission (UnicoreJobSubmission submission){
    	UnicoreJobSubmissionResource resource = new UnicoreJobSubmissionResource();
        resource.setjobSubmissionInterfaceId(submission.getJobSubmissionInterfaceId());
        if (submission.getSecurityProtocol() != null){
            resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
        }
        resource.setUnicoreEndpointUrl(submission.getUnicoreEndPointURL());
        return resource;
    }

    public static UnicoreDataMovementResource getUnicoreDMResource (UnicoreDataMovement dataMovement){
        UnicoreDataMovementResource resource = new UnicoreDataMovementResource();
        resource.setDataMovementId(dataMovement.getDataMovementInterfaceId());
        if (dataMovement.getSecurityProtocol() != null){
            resource.setSecurityProtocol(dataMovement.getSecurityProtocol().toString());
        }
        resource.setUnicoreEndpointUrl(dataMovement.getUnicoreEndPointURL());
        return resource;
    }

    
    public static CloudSubmissionResource getCloudJobSubmission (CloudJobSubmission submission){
        CloudSubmissionResource resource = new CloudSubmissionResource();
        resource.setJobSubmissionInterfaceId(submission.getJobSubmissionInterfaceId());
        if (submission.getSecurityProtocol() != null){
            resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
        }
        if(submission.getProviderName() != null){
            resource.setProviderName(submission.getProviderName().toString());
        }
        resource.setUserAccountName(submission.getUserAccountName());
        resource.setNodeId(submission.getNodeId());
        resource.setExecutableType(submission.getExecutableType());
        return resource;
    }

    public static LocalDataMovementResource getLocalDataMovement(LOCALDataMovement localSubmission)throws AppCatalogException {
    	LocalDataMovementResource submission = new LocalDataMovementResource();
    	submission.setDataMovementInterfaceId(localSubmission.getDataMovementInterfaceId());
    	return submission;
    }
    
    public static LOCALDataMovement getLocalDataMovement(LocalDataMovementResource localSubmission)throws AppCatalogException {
    	LOCALDataMovement submission = new LOCALDataMovement();
    	submission.setDataMovementInterfaceId(localSubmission.getDataMovementInterfaceId());
    	return submission;
    }
    
    
    public static LocalSubmissionResource getLocalJobSubmission(LOCALSubmission localSubmission)throws AppCatalogException {
    	LocalSubmissionResource submission = new LocalSubmissionResource();
    	submission.setJobSubmissionInterfaceId(localSubmission.getJobSubmissionInterfaceId());
    	ResourceJobManagerResource resourceJobManager = getResourceJobManager(localSubmission.getResourceJobManager());
    	submission.setResourceJobManagerId(resourceJobManager.getResourceJobManagerId());
    	submission.setResourceJobManagerResource(resourceJobManager);
    	return submission;
    }
    
    public static LOCALSubmission getLocalJobSubmission(LocalSubmissionResource localSubmission)throws AppCatalogException {
    	LOCALSubmission submission = new LOCALSubmission();
    	submission.setJobSubmissionInterfaceId(localSubmission.getJobSubmissionInterfaceId());
    	submission.setResourceJobManager(getResourceJobManager(localSubmission.getResourceJobManagerResource()));
    	return submission;
    }
    
    public static ResourceJobManagerResource getResourceJobManager(ResourceJobManager manager){
    	ResourceJobManagerResource r = new ResourceJobManagerResource();
    	r.setResourceJobManagerId(manager.getResourceJobManagerId());
    	r.setJobManagerBinPath(manager.getJobManagerBinPath());
    	r.setPushMonitoringEndpoint(manager.getPushMonitoringEndpoint());
    	r.setResourceJobManagerType(manager.getResourceJobManagerType().toString());
    	return r;
    }
    
    public static ResourceJobManager getResourceJobManager(ResourceJobManagerResource manager) throws AppCatalogException {
    	ResourceJobManager r = new ResourceJobManager();
    	r.setResourceJobManagerId(manager.getResourceJobManagerId());
    	r.setJobManagerBinPath(manager.getJobManagerBinPath());
    	r.setPushMonitoringEndpoint(manager.getPushMonitoringEndpoint());
    	r.setResourceJobManagerType(ResourceJobManagerType.valueOf(manager.getResourceJobManagerType()));
    	r.setJobManagerCommands(new HashMap<JobManagerCommand, String>());
    	JobManagerCommandResource jmcr=new JobManagerCommandResource();
        List<Resource> jmcrList = jmcr.get(AbstractResource.JobManagerCommandConstants.RESOURCE_JOB_MANAGER_ID, manager.getResourceJobManagerId());
        if (jmcrList != null && !jmcrList.isEmpty()){
        	for (Resource rrr : jmcrList) {
        		JobManagerCommandResource rr=(JobManagerCommandResource)rrr;
        		r.getJobManagerCommands().put(JobManagerCommand.valueOf(rr.getCommandType()), rr.getCommand());
			}
        }
    	return r;
    }
    
    
    public static SSHJobSubmission getSSHJobSubmissionDescription (SshJobSubmissionResource submission) throws AppCatalogException {
    	SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
    	sshJobSubmission.setAlternativeSSHHostName(submission.getAlternativeSshHostname());
    	sshJobSubmission.setJobSubmissionInterfaceId(submission.getJobSubmissionInterfaceId());
    	sshJobSubmission.setResourceJobManager(getResourceJobManager(submission.getResourceJobManagerResource()));
    	sshJobSubmission.setSecurityProtocol(SecurityProtocol.valueOf(submission.getSecurityProtocol()));
    	sshJobSubmission.setSshPort(submission.getSshPort());
        if (submission.getMonitorMode() != null){
            sshJobSubmission.setMonitorMode(MonitorMode.valueOf(submission.getMonitorMode()));
        }
        return sshJobSubmission;
    }

    public static UnicoreJobSubmission getUnicoreJobSubmissionDescription (UnicoreJobSubmissionResource submission) throws AppCatalogException {
    	UnicoreJobSubmission unicoreJobSubmission = new UnicoreJobSubmission();
    	unicoreJobSubmission.setUnicoreEndPointURL(submission.getUnicoreEndpointUrl());
    	unicoreJobSubmission.setJobSubmissionInterfaceId(submission.getjobSubmissionInterfaceId());
        if (submission.getSecurityProtocol() != null){
            unicoreJobSubmission.setSecurityProtocol(SecurityProtocol.valueOf(submission.getSecurityProtocol()));
        }
        return unicoreJobSubmission;
    }

    public static UnicoreDataMovement getUnicoreDMDescription (UnicoreDataMovementResource resource) throws AppCatalogException {
        UnicoreDataMovement dataMovement = new UnicoreDataMovement();
        dataMovement.setUnicoreEndPointURL(resource.getUnicoreEndpointUrl());
        dataMovement.setDataMovementInterfaceId(resource.getDataMovementId());
        if (resource.getSecurityProtocol() != null){
            dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(resource.getSecurityProtocol()));
        }
        return dataMovement;
    }

    
    public static CloudJobSubmission getCloudJobSubmissionDescription (CloudSubmissionResource submission) throws AppCatalogException {
        CloudJobSubmission cloudJobSubmission = new CloudJobSubmission();
        cloudJobSubmission.setJobSubmissionInterfaceId(submission.getJobSubmissionInterfaceId());
        cloudJobSubmission.setExecutableType(submission.getExecutableType());
        cloudJobSubmission.setSecurityProtocol(SecurityProtocol.valueOf(submission.getSecurityProtocol()));
        cloudJobSubmission.setNodeId(submission.getNodeId());
        cloudJobSubmission.setUserAccountName(submission.getUserAccountName());
        cloudJobSubmission.setProviderName(ProviderName.valueOf(submission.getProviderName()));
        return cloudJobSubmission;
    }

//    public static GlobusJobSubmission getGlobusJobSubmissionDescription (GlobusJobSubmissionResource submission) throws AppCatalogException {
//        GlobusJobSubmission globusJobSubmission = new GlobusJobSubmission();
//        globusJobSubmission.setJobSubmissionInterfaceId(submission.getSubmissionID());
//        globusJobSubmission.setResourceJobManager(ResourceJobManager.valueOf(submission.getResourceJobManager()));
//        globusJobSubmission.setSecurityProtocol(SecurityProtocol.valueOf(submission.getSecurityProtocol()));
//
//        GlobusGKEndpointResource endpointResource = new GlobusGKEndpointResource();
//        List<Resource> endpoints = endpointResource.get(AbstractResource.GlobusEPConstants.SUBMISSION_ID, submission.getSubmissionID());
//        if (endpoints != null && !endpoints.isEmpty()){
//            globusJobSubmission.setGlobusGateKeeperEndPoint(getGlobusGateKeeperEndPointList(endpoints));
//        }
//
//        return globusJobSubmission;
//    }

    public static SCPDataMovement getSCPDataMovementDescription (ScpDataMovementResource dataMovementResource) throws AppCatalogException {
        SCPDataMovement dataMovement = new SCPDataMovement();
        dataMovement.setDataMovementInterfaceId(dataMovementResource.getDataMovementInterfaceId());
        dataMovement.setAlternativeSCPHostName(dataMovementResource.getAlternativeScpHostname());
        dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(dataMovementResource.getSecurityProtocol()));
        dataMovement.setSshPort(dataMovementResource.getSshPort());
        return dataMovement;
    }
    
    public static ScpDataMovementResource getSCPDataMovementDescription (SCPDataMovement dataMovementResource) throws AppCatalogException {
    	ScpDataMovementResource dataMovement = new ScpDataMovementResource();
        dataMovement.setDataMovementInterfaceId(dataMovementResource.getDataMovementInterfaceId());
        dataMovement.setAlternativeScpHostname(dataMovementResource.getAlternativeSCPHostName());
        dataMovement.setSecurityProtocol(dataMovementResource.getSecurityProtocol().toString());
        dataMovement.setSshPort(dataMovementResource.getSshPort());
        return dataMovement;
    }

    public static GridFTPDataMovement getGridFTPDataMovementDescription (GridftpDataMovementResource dataMovementResource) throws AppCatalogException {
        GridFTPDataMovement dataMovement = new GridFTPDataMovement();
        dataMovement.setDataMovementInterfaceId(dataMovementResource.getDataMovementInterfaceId());
        dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(dataMovementResource.getSecurityProtocol()));
        GridftpEndpointResource endpointResource = new GridftpEndpointResource();
        List<Resource> endpoints = endpointResource.get(AbstractResource.GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID, dataMovementResource.getDataMovementInterfaceId());
        if (endpoints != null && !endpoints.isEmpty()){
            dataMovement.setGridFTPEndPoints(getGridFTPDMEPList(endpoints));
        }
        return dataMovement;
    }

    public static GridftpDataMovementResource getGridFTPDataMovementDescription (GridFTPDataMovement dataMovementResource) throws AppCatalogException {
    	GridftpDataMovementResource dataMovement = new GridftpDataMovementResource();
        dataMovement.setDataMovementInterfaceId(dataMovementResource.getDataMovementInterfaceId());
        dataMovement.setSecurityProtocol(dataMovementResource.getSecurityProtocol().toString());
        return dataMovement;
    }
    
    public static List<String> getGridFTPDMEPList (List<Resource> endpoints){
        List<String> list = new ArrayList<String>();
        for (Resource resource : endpoints){
            list.add(((GridftpEndpointResource) resource).getEndpoint());
        }
        return list;
    }

    public static List<String> getGlobusGateKeeperEndPointList (List<Resource> resources) throws AppCatalogException {
        List<String> list = new ArrayList<String>();
        for (Resource resource : resources){
            list.add(((GlobusGKEndpointResource) resource).getEndpoint());
        }
        return list;
    }
//
//    public static List<GSISSHJobSubmission> getGSISSHSubmissionList (List<Resource> resources) throws AppCatalogException {
//        List<GSISSHJobSubmission> list = new ArrayList<GSISSHJobSubmission>();
//        for (Resource resource : resources){
//            list.add(getGSISSHSubmissionDescription((GSISSHSubmissionResource) resource));
//        }
//        return list;
//    }
//
//    public static List<GlobusJobSubmission> getGlobusSubmissionList (List<Resource> resources) throws AppCatalogException {
//        List<GlobusJobSubmission> list = new ArrayList<GlobusJobSubmission>();
//        for (Resource resource : resources){
//            list.add(getGlobusJobSubmissionDescription((GlobusJobSubmissionResource) resource));
//        }
//        return list;
//    }
//
//    public static List<SSHJobSubmission> getSSHSubmissionList (List<Resource> resources) throws AppCatalogException {
//        List<SSHJobSubmission> list = new ArrayList<SSHJobSubmission>();
//        for (Resource resource : resources){
//            list.add(getSSHJobSubmissionDescription((SshJobSubmissionResource) resource));
//        }
//        return list;
//    }
//
//    public static List<GridFTPDataMovement> getGridFTPDataMovementList (List<Resource> resources) throws AppCatalogException {
//        List<GridFTPDataMovement> list = new ArrayList<GridFTPDataMovement>();
//        for (Resource resource : resources){
//            list.add(getGridFTPDataMovementDescription((GridftpDataMovementResource) resource));
//        }
//        return list;
//    }
//
//    public static List<SCPDataMovement> getSCPDataMovementList (List<Resource> resources) throws AppCatalogException {
//        List<SCPDataMovement> list = new ArrayList<SCPDataMovement>();
//        for (Resource resource : resources){
//            list.add(getSCPDataMovementDescription((ScpDataMovementResource) resource));
//        }
//        return list;
//    }
//
//    public static Set<String> getGSISSHExports (List<Resource> gsisshExportResources){
//        Set<String> exports = new HashSet<String>();
//        for (Resource resource : gsisshExportResources){
//            exports.add(((GSISSHExportResource) resource).getExport());
//        }
//        return exports;
//    }
//
//    public static List<String> getGSISSHPreJobCommands (List<Resource> gsisshPreJobCommandResources){
//        List<String> list = new ArrayList<String>();
//        for (Resource resource : gsisshPreJobCommandResources){
//            list.add(((GSISSHPreJobCommandResource) resource).getCommand());
//        }
//        return list;
//    }
//
//    public static List<String> getGSISSHPostJobCommands (List<Resource> gsisshPostJobCommandResources){
//        List<String> list = new ArrayList<String>();
//        for (Resource resource : gsisshPostJobCommandResources){
//            list.add(((GSISSHPostJobCommandResource) resource).getCommand());
//        }
//        return list;
//    }
//
//    public static GlobusJobSubmissionResource getGlobusJobSubmission (GlobusJobSubmission submission){
//        GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
//        resource.setSubmissionID(submission.getJobSubmissionDataID());
//        resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
//        resource.setResourceJobManager(submission.getResourceJobManager().toString());
//        return resource;
//    }

    public static ApplicationModule getApplicationModuleDesc (AppModuleResource resource){
        ApplicationModule module = new ApplicationModule();
        module.setAppModuleId(resource.getModuleId());
        module.setAppModuleDescription(resource.getModuleDesc());
        module.setAppModuleName(resource.getModuleName());
        module.setAppModuleVersion(resource.getModuleVersion());
        return module;
    }

    public static ApplicationInterfaceDescription getApplicationInterfaceDescription (AppInterfaceResource resource) throws AppCatalogException {
        ApplicationInterfaceDescription description = new ApplicationInterfaceDescription();
        description.setApplicationInterfaceId(resource.getInterfaceId());
        description.setApplicationName(resource.getAppName());
        description.setApplicationDescription(resource.getAppDescription());

        AppModuleMappingResource appModuleMappingResource = new AppModuleMappingResource();
        List<Resource> appModules = appModuleMappingResource.get(AbstractResource.AppModuleMappingConstants.INTERFACE_ID, resource.getInterfaceId());
        if (appModules != null && !appModules.isEmpty()){
            description.setApplicationModules(getAppModuleIds(appModules));
        }

        ApplicationInputResource inputResource = new ApplicationInputResource();
        List<Resource> appInputs = inputResource.get(AbstractResource.AppInputConstants.INTERFACE_ID, resource.getInterfaceId());
        if (appInputs != null && !appInputs.isEmpty()){
            description.setApplicationInputs(getAppInputs(appInputs));
        }

        ApplicationOutputResource outputResource = new ApplicationOutputResource();
        List<Resource> appOutputs = outputResource.get(AbstractResource.AppOutputConstants.INTERFACE_ID, resource.getInterfaceId());
        if (appOutputs != null && !appOutputs.isEmpty()){
            description.setApplicationOutputs(getAppOutputs(appOutputs));
        }
        return description;
    }

    public static List<String> getAppModuleIds (List<Resource> appModuleMappings){
        List<String> modules = new ArrayList<String>();
        for (Resource resource : appModuleMappings){
            modules.add(((AppModuleMappingResource)resource).getModuleId());
        }
        return modules;
    }

    public static List<ApplicationModule> getAppModules (List<Resource> appModules){
        List<ApplicationModule> modules = new ArrayList<ApplicationModule>();
        for (Resource resource : appModules){
            modules.add(getApplicationModuleDesc((AppModuleResource) resource));
        }
        return modules;
    }

    public static List<ApplicationInterfaceDescription> getAppInterfaceDescList (List<Resource> appInterfaces) throws AppCatalogException {
        List<ApplicationInterfaceDescription> interfaceDescriptions = new ArrayList<ApplicationInterfaceDescription>();
        for (Resource resource : appInterfaces){
            interfaceDescriptions.add(getApplicationInterfaceDescription((AppInterfaceResource) resource));
        }
        return interfaceDescriptions;
    }

    public static List<InputDataObjectType> getAppInputs (List<Resource> resources){
        List<InputDataObjectType> inputs = new ArrayList<InputDataObjectType>();
        for (Resource resource : resources){
            inputs.add(getInputDataObjType((ApplicationInputResource) resource));
        }
        return inputs;
    }

    public static InputDataObjectType getInputDataObjType (ApplicationInputResource input){
        InputDataObjectType inputDataObjectType = new InputDataObjectType();
        inputDataObjectType.setName(input.getInputKey());
        inputDataObjectType.setValue(input.getInputVal());
        inputDataObjectType.setApplicationArgument(input.getAppArgument());
        inputDataObjectType.setInputOrder(input.getInputOrder());
        inputDataObjectType.setMetaData(input.getMetadata());
        inputDataObjectType.setType(DataType.valueOf(input.getDataType()));
        inputDataObjectType.setStandardInput(input.isStandardInput());
        inputDataObjectType.setUserFriendlyDescription(input.getUserFriendlyDesc());
        inputDataObjectType.setIsRequired(input.getRequired());
        inputDataObjectType.setRequiredToAddedToCommandLine(input.getRequiredToCMD());
        inputDataObjectType.setDataStaged(input.isDataStaged());
        return inputDataObjectType;
    }

    public static List<OutputDataObjectType> getAppOutputs (List<Resource> resources){
        List<OutputDataObjectType> outputs = new ArrayList<OutputDataObjectType>();
        for (Resource resource : resources){
            outputs.add(getOutputDataObjType((ApplicationOutputResource) resource));
        }
        return outputs;
    }
    public static OutputDataObjectType getOutputDataObjType (ApplicationOutputResource output){
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        outputDataObjectType.setName(output.getOutputKey());
        outputDataObjectType.setValue(output.getOutputVal());
        outputDataObjectType.setType(DataType.valueOf(output.getDataType()));
        outputDataObjectType.setIsRequired(output.getRequired());
        outputDataObjectType.setRequiredToAddedToCommandLine(output.getRequiredToCMD());
        outputDataObjectType.setDataMovement(output.isDataMovement());
        outputDataObjectType.setLocation(output.getDataNameLocation());
        outputDataObjectType.setSearchQuery(output.getSearchQuery());
        outputDataObjectType.setApplicationArgument(output.getAppArgument());
        return outputDataObjectType;
    }

    public static ApplicationDeploymentDescription getApplicationDeploymentDescription (AppDeploymentResource resource) throws AppCatalogException {
        ApplicationDeploymentDescription description = new ApplicationDeploymentDescription();
        description.setAppDeploymentId(resource.getDeploymentId());
        description.setAppModuleId(resource.getAppModuleId());
        description.setComputeHostId(resource.getHostId());
        description.setExecutablePath(resource.getExecutablePath());
        if (resource.getParallelism() != null){
            description.setParallelism(ApplicationParallelismType.valueOf(resource.getParallelism()));
        }
        description.setAppDeploymentDescription(resource.getAppDes());
        ModuleLoadCmdResource cmdResource = new ModuleLoadCmdResource();
        List<Resource> moduleLoadCmds = cmdResource.get(AbstractResource.ModuleLoadCmdConstants.APP_DEPLOYMENT_ID, resource.getDeploymentId());
        if (moduleLoadCmds != null && !moduleLoadCmds.isEmpty()){
            for (Resource moduleLoadCmd : moduleLoadCmds){
                description.addToModuleLoadCmds(((ModuleLoadCmdResource) moduleLoadCmd).getCmd());
            }
        }
        LibraryPrepandPathResource prepandPathResource = new LibraryPrepandPathResource();
        List<Resource> libPrepandPaths = prepandPathResource.get(AbstractResource.LibraryPrepandPathConstants.DEPLOYMENT_ID, resource.getDeploymentId());
        if (libPrepandPaths != null && !libPrepandPaths.isEmpty()){
            description.setLibPrependPaths(getLibPrepandPaths(libPrepandPaths));
        }

        LibraryApendPathResource apendPathResource = new LibraryApendPathResource();
        List<Resource> libApendPaths = apendPathResource.get(AbstractResource.LibraryPrepandPathConstants.DEPLOYMENT_ID, resource.getDeploymentId());
        if (libApendPaths != null && !libApendPaths.isEmpty()){
            description.setLibAppendPaths(getLibApendPaths(libApendPaths));
        }

        AppEnvironmentResource appEnvironmentResource = new AppEnvironmentResource();
        List<Resource> appEnvList = appEnvironmentResource.get(AbstractResource.LibraryPrepandPathConstants.DEPLOYMENT_ID, resource.getDeploymentId());
        if (appEnvList != null && !appEnvList.isEmpty()){
            description.setSetEnvironment(getAppEnvPaths(appEnvList));
        }
        PreJobCommandResource preJobCommandResource = new PreJobCommandResource();
        List<Resource> preJobCommands = preJobCommandResource.get(AbstractResource.PreJobCommandConstants.DEPLOYMENT_ID, resource.getDeploymentId());
        if (preJobCommands != null && !preJobCommands.isEmpty()){
            for (Resource prejobCommand : preJobCommands){
                description.addToPreJobCommands(((PreJobCommandResource) prejobCommand).getCommand());
            }
        }
        PostJobCommandResource postJobCommandResource = new PostJobCommandResource();
        List<Resource> postJobCommands = postJobCommandResource.get(AbstractResource.PostJobCommandConstants.DEPLOYMENT_ID, resource.getDeploymentId());
        if (postJobCommands != null && !postJobCommands.isEmpty()){
            for (Resource postjobCommand : postJobCommands){
                description.addToPostJobCommands(((PostJobCommandResource) postjobCommand).getCommand());
            }
        }
        return description;
    }

    public static List<ApplicationDeploymentDescription> getAppDepDescList (List<Resource> resources) throws AppCatalogException {
        List<ApplicationDeploymentDescription> appList = new ArrayList<ApplicationDeploymentDescription>();
        for (Resource resource : resources){
            appList.add(getApplicationDeploymentDescription((AppDeploymentResource)resource));
        }
        return appList;
    }

    public static SetEnvPaths getSetEnvPath(Resource resource){
        SetEnvPaths envPaths = new SetEnvPaths();
        if (resource instanceof LibraryPrepandPathResource){
            envPaths.setName(((LibraryPrepandPathResource) resource).getName());
            envPaths.setValue(((LibraryPrepandPathResource) resource).getValue());
            return envPaths;
        }else if (resource instanceof LibraryApendPathResource){
            envPaths.setName(((LibraryApendPathResource) resource).getName());
            envPaths.setValue(((LibraryApendPathResource) resource).getValue());
            return envPaths;
        }else if (resource instanceof AppEnvironmentResource){
            envPaths.setName(((AppEnvironmentResource) resource).getName());
            envPaths.setValue(((AppEnvironmentResource) resource).getValue());
            return envPaths;
        }else {
            return null;
        }
    }

    public static List<SetEnvPaths> getLibPrepandPaths (List<Resource> prepandPaths){
        List<SetEnvPaths> pathList = new ArrayList<SetEnvPaths>();
        for (Resource resource : prepandPaths){
            pathList.add(getSetEnvPath(resource));
        }
        return pathList;
    }

    public static List<SetEnvPaths> getLibApendPaths (List<Resource> appendPaths){
        List<SetEnvPaths> pathList = new ArrayList<SetEnvPaths>();
        for (Resource resource : appendPaths){
            pathList.add(getSetEnvPath(resource));
        }
        return pathList;
    }

    public static List<SetEnvPaths> getAppEnvPaths (List<Resource> appEnvPaths){
        List<SetEnvPaths> pathList = new ArrayList<SetEnvPaths>();
        for (Resource resource : appEnvPaths){
            pathList.add(getSetEnvPath(resource));
        }
        return pathList;
    }

    public static ComputeResourcePreference getComputeResourcePreference (ComputeHostPreferenceResource resource){
        ComputeResourcePreference preference = new ComputeResourcePreference();
        preference.setComputeResourceId(resource.getResourceId());
        preference.setOverridebyAiravata(resource.getOverrideByAiravata());
        if (resource.getPreferredJobProtocol() != null){
            preference.setPreferredJobSubmissionProtocol(JobSubmissionProtocol.valueOf(resource.getPreferredJobProtocol()));
        }
        if (resource.getPreferedDMProtocol() != null){
            preference.setPreferredDataMovementProtocol(DataMovementProtocol.valueOf(resource.getPreferedDMProtocol()));
        }
        preference.setPreferredBatchQueue(resource.getBatchQueue());
        preference.setScratchLocation(resource.getScratchLocation());
        preference.setAllocationProjectNumber(resource.getProjectNumber());
        preference.setLoginUserName(resource.getLoginUserName());
        return preference;
    }

    public static List<ComputeResourcePreference> getComputeResourcePreferences (List<Resource> resources){
        List<ComputeResourcePreference> preferences = new ArrayList<ComputeResourcePreference>();
        if (resources != null && !resources.isEmpty()){
            for (Resource resource : resources){
                 preferences.add(getComputeResourcePreference((ComputeHostPreferenceResource)resource));
            }
        }
        return preferences;
    }

    public static InputDataObjectType getWorkflowInput (WorkflowInputResource resource){
        InputDataObjectType input = new InputDataObjectType();
        input.setName(resource.getInputKey());
        input.setApplicationArgument(resource.getAppArgument());
        input.setInputOrder(resource.getInputOrder());
        input.setType(DataType.valueOf(resource.getDataType()));
        input.setMetaData(resource.getMetadata());
        input.setUserFriendlyDescription(resource.getUserFriendlyDesc());
        input.setIsRequired(resource.getRequired());
        input.setRequiredToAddedToCommandLine(resource.getRequiredToCMD());
        input.setDataStaged(resource.isDataStaged());
        return input;
    }

    public static List<InputDataObjectType> getWFInputs(List<Resource> resources){
        List<InputDataObjectType> inputResources = new ArrayList<InputDataObjectType>();
        if (resources != null && !resources.isEmpty()){
            for (Resource resource : resources){
                inputResources.add(getWorkflowInput((WorkflowInputResource) resource));
            }
        }
        return inputResources;
    }

    public static GatewayResourceProfile getGatewayResourceProfile(GatewayProfileResource gw, List<ComputeResourcePreference> preferences){
        GatewayResourceProfile gatewayProfile = new GatewayResourceProfile();
        gatewayProfile.setGatewayID(gw.getGatewayID());
        gatewayProfile.setComputeResourcePreferences(preferences);
        return gatewayProfile;
    }

    public static Workflow getWorkflow (WorkflowResource resource) throws AppCatalogException {
        Workflow workflow = new Workflow();
        workflow.setTemplateId(resource.getWfTemplateId());
        workflow.setGraph(resource.getGraph());
        workflow.setName(resource.getWfName());
        if (resource.getImage() != null){
            workflow.setImage(resource.getImage().getBytes());
        }
        WorkflowInputResource inputResource = new WorkflowInputResource();
        List<Resource> resources = inputResource.get(AbstractResource.WFInputConstants.WF_TEMPLATE_ID, resource.getWfTemplateId());
        workflow.setWorkflowInputs(getWFInputs(resources));

        return workflow;
    }
}
