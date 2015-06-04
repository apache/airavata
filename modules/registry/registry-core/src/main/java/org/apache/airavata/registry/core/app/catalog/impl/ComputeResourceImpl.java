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

package org.apache.airavata.registry.core.app.catalog.impl;

import java.util.*;

import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogUtils;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeResourceImpl implements ComputeResource {
    private final static Logger logger = LoggerFactory.getLogger(ComputeResourceImpl.class);

    @Override
    public String addComputeResource(ComputeResourceDescription description) throws AppCatalogException {
        try {
            if (description.getComputeResourceId().equals("") || description.getComputeResourceId().equals(computeResourceModelConstants.DEFAULT_ID)){
                description.setComputeResourceId(AppCatalogUtils.getID(description.getHostName()));
            }
        	return saveComputeResourceDescriptorData(description);
        } catch (Exception e) {
            logger.error("Error while saving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

	protected String saveComputeResourceDescriptorData(
			ComputeResourceDescription description) throws AppCatalogException {
		//TODO remove existing one
		ComputeResourceAppCatalogResourceAppCat computeHostResource = saveComputeResource(description);
		saveHostAliases(description, computeHostResource);
		saveIpAddresses(description, computeHostResource);
		saveBatchQueues(description, computeHostResource);
		saveFileSystems(description, computeHostResource);
		saveJobSubmissionInterfaces(description, computeHostResource);
		saveDataMovementInterfaces(description, computeHostResource);
		return computeHostResource.getResourceId();
	}

	protected ComputeResourceAppCatalogResourceAppCat saveComputeResource(
			ComputeResourceDescription description) throws AppCatalogException {
		ComputeResourceAppCatalogResourceAppCat computeHostResource = AppCatalogThriftConversion.getComputeHostResource(description);
		computeHostResource.save();
		return computeHostResource;
	}

	protected void saveDataMovementInterfaces(
			ComputeResourceDescription description,
			ComputeResourceAppCatalogResourceAppCat computeHostResource)
			throws AppCatalogException {
		List<DataMovementInterface> dataMovemenetInterfaces = description.getDataMovementInterfaces();
		if (dataMovemenetInterfaces != null && !dataMovemenetInterfaces.isEmpty()) {
		    for (DataMovementInterface dataMovementInterface : dataMovemenetInterfaces) {
		    	DataMovementInterfaceAppCatalogResourceAppCat dmir = AppCatalogThriftConversion.getDataMovementInterface(dataMovementInterface);
		    	dmir.setComputeHostResource(computeHostResource);
		    	dmir.setComputeResourceId(computeHostResource.getResourceId());
				dmir.save();
		    }
		}
	}

	protected void saveJobSubmissionInterfaces(
			ComputeResourceDescription description,
			ComputeResourceAppCatalogResourceAppCat computeHostResource)
			throws AppCatalogException {
		List<JobSubmissionInterface> jobSubmissionInterfaces = description.getJobSubmissionInterfaces();
		if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
		    for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
		    	JobSubmissionInterfaceAppCatalogResourceAppCat jsir = AppCatalogThriftConversion.getJobSubmissionInterface(jobSubmissionInterface);
				jsir.setComputeHostResource(computeHostResource);
				jsir.setComputeResourceId(computeHostResource.getResourceId());
				jsir.save();
		    }
		}
	}

	protected void saveFileSystems(ComputeResourceDescription description,
			ComputeResourceAppCatalogResourceAppCat computeHostResource)
			throws AppCatalogException {
		Map<FileSystems, String> fileSystems = description.getFileSystems();
		if (fileSystems != null && !fileSystems.isEmpty()) {
		    for (FileSystems key : fileSystems.keySet()) {
		    	ComputeResourceFileSystemAppCatalogResourceAppCat computeResourceFileSystemResource = new ComputeResourceFileSystemAppCatalogResourceAppCat();
		    	computeResourceFileSystemResource.setComputeHostResource(computeHostResource);
		    	computeResourceFileSystemResource.setComputeResourceId(computeHostResource.getResourceId());
		    	computeResourceFileSystemResource.setFileSystem(key.toString());
		    	computeResourceFileSystemResource.setPath(fileSystems.get(key));
		    	computeResourceFileSystemResource.save();
		    }
		}
	}

	protected void saveBatchQueues(ComputeResourceDescription description,
			ComputeResourceAppCatalogResourceAppCat computeHostResource)
			throws AppCatalogException {
		List<BatchQueue> batchQueueList = description.getBatchQueues();
		if (batchQueueList != null && !batchQueueList.isEmpty()) {
		    for (BatchQueue batchQueue : batchQueueList) {
		    	BatchQueueAppCatalogResourceAppCat bq = AppCatalogThriftConversion.getBatchQueue(batchQueue);
		    	bq.setComputeResourceId(computeHostResource.getResourceId());
		    	bq.setComputeHostResource(computeHostResource);
		        bq.save();
		    }
		}
	}

	protected void saveIpAddresses(ComputeResourceDescription description,
			ComputeResourceAppCatalogResourceAppCat computeHostResource)
			throws AppCatalogException {
		List<String> ipAddresses = description.getIpAddresses();
        HostIPAddressAppCatalogResourceAppCat resource = new HostIPAddressAppCatalogResourceAppCat();
        resource.remove(description.getComputeResourceId());
		if (ipAddresses != null && !ipAddresses.isEmpty()) {
		    for (String ipAddress : ipAddresses) {
		        HostIPAddressAppCatalogResourceAppCat ipAddressResource = new HostIPAddressAppCatalogResourceAppCat();
		        ipAddressResource.setComputeHostResource(computeHostResource);
		        ipAddressResource.setResourceID(computeHostResource.getResourceId());
		        ipAddressResource.setIpaddress(ipAddress);
		        ipAddressResource.save();
		    }
		}
	}

	protected void saveHostAliases(ComputeResourceDescription description,
			ComputeResourceAppCatalogResourceAppCat computeHostResource)
			throws AppCatalogException {
		List<String> hostAliases = description.getHostAliases();
        // delete previous host aliases
        HostAliasAppCatalogResourceAppCat resource = new HostAliasAppCatalogResourceAppCat();
        resource.remove(description.getComputeResourceId());
		if (hostAliases != null && !hostAliases.isEmpty()) {
		    for (String alias : hostAliases) {
		        HostAliasAppCatalogResourceAppCat aliasResource = new HostAliasAppCatalogResourceAppCat();
		        aliasResource.setComputeHostResource(computeHostResource);
		        aliasResource.setResourceID(computeHostResource.getResourceId());
                aliasResource.setAlias(alias);
		        aliasResource.save();
		    }
		}
	}

    @Override
    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource) throws AppCatalogException{
        try {
        	saveComputeResourceDescriptorData(updatedComputeResource);
        } catch (Exception e) {
            logger.error("Error while updating compute resource...", e);
            throw new AppCatalogException(e);
        } 
    }

    @Override
    public String addSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        try {
            String submissionId = AppCatalogUtils.getID("SSH");
            sshJobSubmission.setJobSubmissionInterfaceId(submissionId);
    		String resourceJobManagerId = addResourceJobManager(sshJobSubmission.getResourceJobManager());
    		SshJobSubmissionAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getSSHJobSubmission(sshJobSubmission);
    		resource.setResourceJobManagerId(resourceJobManagerId);
    		resource.getResourceJobManagerResource().setResourceJobManagerId(resourceJobManagerId);
            if (sshJobSubmission.getMonitorMode() != null){
                resource.setMonitorMode(sshJobSubmission.getMonitorMode().toString());
            }
            resource.save();
        	return submissionId;
        }catch (Exception e) {
            logger.error("Error while saving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addCloudJobSubmission(CloudJobSubmission sshJobSubmission) throws AppCatalogException {
        try {
            sshJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("Cloud"));
            CloudSubmissionAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getCloudJobSubmission(sshJobSubmission);
            resource.save();
            return resource.getJobSubmissionInterfaceId();
        }catch (Exception e) {
            logger.error("Error while saving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }
    
	@Override
	public String addUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission)
			throws AppCatalogException {
		 try {
             unicoreJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("UNICORE"));
             UnicoreJobSubmissionAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getUnicoreJobSubmission(unicoreJobSubmission);
             resource.setUnicoreEndpointUrl(unicoreJobSubmission.getUnicoreEndPointURL());
             if (unicoreJobSubmission.getSecurityProtocol() !=  null){
                 resource.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol().toString());
             }
             resource.save();
             return resource.getjobSubmissionInterfaceId();
         }catch (Exception e){
	            logger.error("Error while retrieving SSH Job Submission...", e);
	            throw new AppCatalogException(e);
	        }
		 
	}

    @Override
    public String addJobSubmissionProtocol(String computeResourceId, JobSubmissionInterface jobSubmissionInterface) throws AppCatalogException {
        try {
        	JobSubmissionInterfaceAppCatalogResourceAppCat jsi = AppCatalogThriftConversion.getJobSubmissionInterface(jobSubmissionInterface);
        	jsi.setComputeResourceId(computeResourceId);
        	ComputeResourceAppCatalogResourceAppCat computeResourceResource = new ComputeResourceAppCatalogResourceAppCat();
        	computeResourceResource=(ComputeResourceAppCatalogResourceAppCat)computeResourceResource.get(computeResourceId);
        	jsi.setComputeHostResource(computeResourceResource);
            jsi.save();
            return jsi.getJobSubmissionInterfaceId();
        }catch (Exception e){
            logger.error("Error while saving "+jobSubmissionInterface.getJobSubmissionProtocol().toString()+" Job Submission Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

//    @Override
//    public String addGSISSHJobSubmission(GSISSHJobSubmission gsisshJobSubmission) throws AppCatalogException {
//        try {
//            GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
//            String hostName = "GSISSH";
//            resource.setDeploymentId(AppCatalogUtils.getID(hostName));
//            resource.setSshPort(resource.getSshPort());
//            resource.setResourceJobManager(gsisshJobSubmission.getResourceJobManager().toString());
//            resource.setInstalledPath(gsisshJobSubmission.getInstalledPath());
//            resource.setMonitorMode(gsisshJobSubmission.getMonitorMode());
//            resource.save();
//            gsisshJobSubmission.setJobSubmissionDataID(resource.getDeploymentId());
//
//            Set<String> exports = gsisshJobSubmission.getExports();
//            if (exports != null && !exports.isEmpty()){
//                for (String export : exports){
//                    GSISSHExportResource exportResource = new GSISSHExportResource();
//                    exportResource.setDeploymentId(resource.getDeploymentId());
//                    exportResource.setExport(export);
//                    exportResource.setAppDeploymentResource(resource);
//                    exportResource.save();
//                }
//            }
//
//            List<String> preJobCommands = gsisshJobSubmission.getPreJobCommands();
//            if (preJobCommands != null && !preJobCommands.isEmpty()){
//                for (String command : preJobCommands){
//                    GSISSHPreJobCommandResource commandResource = new GSISSHPreJobCommandResource();
//                    commandResource.setDeploymentId(resource.getDeploymentId());
//                    commandResource.setCommand(command);
//                    commandResource.setAppDeploymentResource(resource);
//                    commandResource.save();
//                }
//            }
//
//            List<String> postJobCommands = gsisshJobSubmission.getPostJobCommands();
//            if (postJobCommands != null && !postJobCommands.isEmpty()){
//                for (String command : postJobCommands){
//                    GSISSHPostJobCommandResource commandResource = new GSISSHPostJobCommandResource();
//                    commandResource.setDeploymentId(resource.getDeploymentId());
//                    commandResource.setCommand(command);
//                    commandResource.setAppDeploymentResource(resource);
//                    commandResource.save();
//                }
//            }
//            return resource.getDeploymentId();
//        }catch (Exception e) {
//            logger.error("Error while saving GSISSH Job Submission...", e);
//            throw new AppCatalogException(e);
//        }
//    }
//
//    @Override
//    public void addGSISSHJobSubmissionProtocol(String computeResourceId, String jobSubmissionId) throws AppCatalogException {
//        try {
//            JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
//            resource.setResourceID(computeResourceId);
//            resource.setDeploymentId(jobSubmissionId);
//            ComputeResourceDescription computeResource = getComputeResource(computeResourceId);
//            resource.setComputeHostResource(AppCatalogThriftConversion.getComputeHostResource(computeResource));
//            resource.setJobType(JobSubmissionProtocol.GSISSH.toString());
//            resource.save();
//        }catch (Exception e){
//            logger.error("Error while saving GSISSH Job Submission Protocol...", e);
//            throw new AppCatalogException(e);
//        }
//    }

    @Override
    public String addGlobusJobSubmission(GlobusJobSubmission globusJobSubmission) throws AppCatalogException {
//        try {
//            GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
//            String hostName = "GLOBUS";
//            resource.setDeploymentId(AppCatalogUtils.getID(hostName));
//            resource.setSecurityProtocol(globusJobSubmission.getSecurityProtocol().toString());
//            resource.setResourceJobManager(globusJobSubmission.getResourceJobManager().toString());
//            resource.save();
//            globusJobSubmission.setJobSubmissionDataID(resource.getDeploymentId());
//            List<String> globusGateKeeperEndPoint = globusJobSubmission.getGlobusGateKeeperEndPoint();
//            if (globusGateKeeperEndPoint != null && !globusGateKeeperEndPoint.isEmpty()) {
//                for (String endpoint : globusGateKeeperEndPoint) {
//                    GlobusGKEndpointResource endpointResource = new GlobusGKEndpointResource();
//                    endpointResource.setDeploymentId(resource.getDeploymentId());
//                    endpointResource.setEndpoint(endpoint);
//                    endpointResource.setGlobusJobSubmissionResource(resource);
//                    endpointResource.save();
//                }
//            }
//            return resource.getDeploymentId();
//        } catch (Exception e) {
//            logger.error("Error while saving Globus Job Submission...", e);
//            throw new AppCatalogException(e);
//        }
    	return null;
    }

    @Override
    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        try {
        	scpDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"));
        	ScpDataMovementAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getSCPDataMovementDescription(scpDataMovement);
            resource.save();
            return resource.getDataMovementInterfaceId();
        }catch (Exception e){
            logger.error("Error while saving SCP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addUnicoreDataMovement(UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        try {
            unicoreDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("UNICORE"));
            UnicoreDataMovementAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getUnicoreDMResource(unicoreDataMovement);
            resource.save();
            return resource.getDataMovementId();
        }catch (Exception e){
            logger.error("Error while saving UNICORE Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addDataMovementProtocol(String computeResourceId, DataMovementInterface dataMovementInterface) throws AppCatalogException {
        try {
        	DataMovementInterfaceAppCatalogResourceAppCat dmi = AppCatalogThriftConversion.getDataMovementInterface(dataMovementInterface);
        	dmi.setComputeResourceId(computeResourceId);
        	ComputeResourceAppCatalogResourceAppCat computeResourceResource = new ComputeResourceAppCatalogResourceAppCat();
        	computeResourceResource=(ComputeResourceAppCatalogResourceAppCat)computeResourceResource.get(computeResourceId);
        	dmi.setComputeHostResource(computeResourceResource);
        	dmi.save();
            return dmi.getDataMovementInterfaceId();
        }catch (Exception e){
            logger.error("Error while saving "+dataMovementInterface.getDataMovementProtocol().toString()+" data movement Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        try {
        	gridFTPDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("GRIDFTP"));
        	GridftpDataMovementAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getGridFTPDataMovementDescription(gridFTPDataMovement);
            resource.save();
            List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFTPEndPoints();
            if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
                for (String endpoint : gridFTPEndPoint) {
                    GridftpEndpointAppCatalogResourceAppCat endpointResource = new GridftpEndpointAppCatalogResourceAppCat();
                    endpointResource.setDataMovementInterfaceId(resource.getDataMovementInterfaceId());
                    endpointResource.setEndpoint(endpoint);
                    endpointResource.setGridftpDataMovementResource(resource);
                    endpointResource.save();
                }
            }
            return resource.getDataMovementInterfaceId();
        }catch (Exception e){
            logger.error("Error while saving GridFTP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ComputeResourceDescription getComputeResource(String resourceId) throws AppCatalogException {
        try {
            ComputeResourceAppCatalogResourceAppCat resource = new ComputeResourceAppCatalogResourceAppCat();
            ComputeResourceAppCatalogResourceAppCat computeResource = (ComputeResourceAppCatalogResourceAppCat)resource.get(resourceId);
            return AppCatalogThriftConversion.getComputeHostDescription(computeResource);
        }catch (Exception e){
            logger.error("Error while retrieving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters) throws AppCatalogException {
        List<ComputeResourceDescription> computeResourceDescriptions = new ArrayList<ComputeResourceDescription>();
        try {
        	//TODO check if this is correct way to do this
            ComputeResourceAppCatalogResourceAppCat resource = new ComputeResourceAppCatalogResourceAppCat();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AppCatAbstractResource.ComputeResourceConstants.HOST_NAME)){
                    List<AppCatalogResource> resources = resource.get(AppCatAbstractResource.ComputeResourceConstants.HOST_NAME, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        computeResourceDescriptions = AppCatalogThriftConversion.getComputeDescriptionList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for compute resource.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving compute resource list...", e);
            throw new AppCatalogException(e);
        }
        return computeResourceDescriptions;
    }

    @Override
    public List<ComputeResourceDescription> getAllComputeResourceList() throws AppCatalogException {
        List<ComputeResourceDescription> computeResourceDescriptions = new ArrayList<ComputeResourceDescription>();
        try {
            ComputeResourceAppCatalogResourceAppCat resource = new ComputeResourceAppCatalogResourceAppCat();
            List<AppCatalogResource> resources = resource.getAll();
            if (resources != null && !resources.isEmpty()){
                computeResourceDescriptions = AppCatalogThriftConversion.getComputeDescriptionList(resources);
            }
        }catch (Exception e){
            logger.error("Error while retrieving compute resource list...", e);
            throw new AppCatalogException(e);
        }
        return computeResourceDescriptions;
    }

    @Override
    public Map<String, String> getAllComputeResourceIdList() throws AppCatalogException {
        try {
            Map<String, String> computeResourceMap = new HashMap<String, String>();
            ComputeResourceAppCatalogResourceAppCat resource = new ComputeResourceAppCatalogResourceAppCat();
            List<AppCatalogResource> allComputeResources = resource.getAll();
            if (allComputeResources != null && !allComputeResources.isEmpty()){
                for (AppCatalogResource cm : allComputeResources){
                    ComputeResourceAppCatalogResourceAppCat cmr = (ComputeResourceAppCatalogResourceAppCat)cm;
                    computeResourceMap.put(cmr.getResourceId(), cmr.getHostName());
                }
            }
            return computeResourceMap;
        }catch (Exception e){
            logger.error("Error while retrieving compute resource list...", e);
            throw new AppCatalogException(e);
        }
    }

//    @Override
//    public GSISSHJobSubmission getGSISSHJobSubmission(String submissionId) throws AppCatalogException {
//        try {
//            GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
//            GSISSHSubmissionResource submissionResource = (GSISSHSubmissionResource)resource.get(submissionId);
//            return AppCatalogThriftConversion.getGSISSHSubmissionDescription(submissionResource);
//        }catch (Exception e){
//            logger.error("Error while retrieving GSISSH Job Submission...", e);
//            throw new AppCatalogException(e);
//        }
//    }
//
//    @Override
//    public List<GSISSHJobSubmission> getGSISSHJobSubmissionList(Map<String, String> filters) throws AppCatalogException {
//        try {
//            GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
//            for (String fieldName : filters.keySet() ){
//                if (fieldName.equals(AbstractResource.GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER)){
//                    List<Resource> resources = resource.get(AbstractResource.GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER, filters.get(fieldName));
//                    if (resources != null && !resources.isEmpty()){
//                        return AppCatalogThriftConversion.getGSISSHSubmissionList(resources);
//                    }
//                }else {
//                    logger.error("Unsupported field name for GSISSH Submission.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Unsupported field name for GSISSH Submission.");
//                }
//            }
//        }catch (Exception e){
//            logger.error("Error while retrieving GSISSH Submission list...", e);
//            throw new AppCatalogException(e);
//        }
//        return null;
//    }
//
//    @Override
//    public GlobusJobSubmission getGlobusJobSubmission(String submissionId) throws AppCatalogException {
//        try {
//        	GlobusJobSubmissionResource globusJobSubmissionResource = new GlobusJobSubmissionResource();
//        	globusJobSubmissionResource=(GlobusJobSubmissionResource)globusJobSubmissionResource.get(submissionId);
//        	AppCatalogThriftConversion.getglo
//            GlobusJobSubmissionResource resource = globusJobSubmissionResource;
//            GlobusJobSubmissionResource submissionResource = (GlobusJobSubmissionResource)resource.get(submissionId);
//            return AppCatalogThriftConversion.getGlobusJobSubmissionDescription(submissionResource);
//        }catch (Exception e){
//            logger.error("Error while retrieving Globus Job Submission...", e);
//            throw new AppCatalogException(e);
//        }
//    }
//
//    @Override
//    public List<GlobusJobSubmission> getGlobusJobSubmissionList(Map<String, String> filters) throws AppCatalogException {
//        try {
//            GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
//            for (String fieldName : filters.keySet() ){
//                if (fieldName.equals(AbstractResource.GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER)){
//                    List<Resource> resources = resource.get(AbstractResource.GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER, filters.get(fieldName));
//                    if (resources != null && !resources.isEmpty()){
//                        return AppCatalogThriftConversion.getGlobusSubmissionList(resources);
//                    }
//                }else if (fieldName.equals(AbstractResource.GlobusJobSubmissionConstants.SECURITY_PROTOCAL)){
//                    List<Resource> resources = resource.get(AbstractResource.GlobusJobSubmissionConstants.SECURITY_PROTOCAL, filters.get(fieldName));
//                    if (resources != null && !resources.isEmpty()){
//                        return AppCatalogThriftConversion.getGlobusSubmissionList(resources);
//                    }
//                }else {
//                    logger.error("Unsupported field name for Globus Submission.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Unsupported field name for Globus Submission.");
//                }
//            }
//        }catch (Exception e){
//            logger.error("Error while retrieving Globus Submission list...", e);
//            throw new AppCatalogException(e);
//        }
//        return null;
//    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        try {
            SshJobSubmissionAppCatalogResourceAppCat resource = new SshJobSubmissionAppCatalogResourceAppCat();
            resource = (SshJobSubmissionAppCatalogResourceAppCat)resource.get(submissionId);
            return AppCatalogThriftConversion.getSSHJobSubmissionDescription(resource);
        }catch (Exception e){
            logger.error("Error while retrieving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    //    @Override
	//    public List<GridFTPDataMovement> getGridFTPDataMovementList(Map<String, String> filters) throws AppCatalogException {
	//        try {
	//            GridftpDataMovementResource resource = new GridftpDataMovementResource();
	//            for (String fieldName : filters.keySet() ){
	//                if (fieldName.equals(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL)){
	//                    List<Resource> resources = resource.get(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL, filters.get(fieldName));
	//                    if (resources != null && !resources.isEmpty()){
	//                        return AppCatalogThriftConversion.getGridFTPDataMovementList(resources);
	//                    }
	//                }else {
	//                    logger.error("Unsupported field name for GridFTP Data movement.", new IllegalArgumentException());
	//                    throw new IllegalArgumentException("Unsupported field name for GridFTP Data movement.");
	//                }
	//            }
	//        }catch (Exception e){
	//            logger.error("Error while retrieving GridFTP Data movement list...", e);
	//            throw new AppCatalogException(e);
	//        }
	//        return null;
	//    }
	
	    @Override
		public UnicoreJobSubmission getUNICOREJobSubmission(String submissionId)
				throws AppCatalogException {
	    	try {
	            UnicoreJobSubmissionAppCatalogResourceAppCat resource = new UnicoreJobSubmissionAppCatalogResourceAppCat();
	            resource = (UnicoreJobSubmissionAppCatalogResourceAppCat)resource.get(submissionId);
	            return AppCatalogThriftConversion.getUnicoreJobSubmissionDescription(resource);
	        }catch (Exception e){
	            logger.error("Error while retrieving UNICORE Job Submission model instance...", e);
	            throw new AppCatalogException(e);
	        }
		}

    @Override
    public UnicoreDataMovement getUNICOREDataMovement(String dataMovementId)
            throws AppCatalogException {
        try {
            UnicoreDataMovementAppCatalogResourceAppCat resource = new UnicoreDataMovementAppCatalogResourceAppCat();
            resource = (UnicoreDataMovementAppCatalogResourceAppCat)resource.get(dataMovementId);
            return AppCatalogThriftConversion.getUnicoreDMDescription(resource);
        }catch (Exception e){
            logger.error("Error while retrieving UNICORE data movement...", e);
            throw new AppCatalogException(e);
        }
    }

	@Override
    public CloudJobSubmission getCloudJobSubmission(String submissionId) throws AppCatalogException {
        try {
            CloudSubmissionAppCatalogResourceAppCat resource = new CloudSubmissionAppCatalogResourceAppCat();
            resource = (CloudSubmissionAppCatalogResourceAppCat)resource.get(submissionId);
            return AppCatalogThriftConversion.getCloudJobSubmissionDescription(resource);
        }catch (Exception e){
            logger.error("Error while retrieving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }
//
//    @Override
//    public List<SSHJobSubmission> getSSHJobSubmissionList(Map<String, String> filters) throws AppCatalogException {
//        try {
//            SshJobSubmissionResource resource = new SshJobSubmissionResource();
//            for (String fieldName : filters.keySet() ){
//               if (fieldName.equals(AbstractResource.SSHSubmissionConstants.RESOURCE_JOB_MANAGER)){
//                    List<Resource> resources = resource.get(AbstractResource.SSHSubmissionConstants.RESOURCE_JOB_MANAGER, filters.get(fieldName));
//                    if (resources != null && !resources.isEmpty()){
//                        return AppCatalogThriftConversion.getSSHSubmissionList(resources);
//                    }
//                }else {
//                    logger.error("Unsupported field name for SSH Submission.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Unsupported field name for SSH Submission.");
//                }
//            }
//        }catch (Exception e){
//            logger.error("Error while retrieving SSH Submission list...", e);
//            throw new AppCatalogException(e);
//        }
//        return null;
//    }

    @Override
    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws AppCatalogException {
        try {
            ScpDataMovementAppCatalogResourceAppCat resource = new ScpDataMovementAppCatalogResourceAppCat();
            ScpDataMovementAppCatalogResourceAppCat dataMovementResource = (ScpDataMovementAppCatalogResourceAppCat)resource.get(dataMoveId);
            return AppCatalogThriftConversion.getSCPDataMovementDescription(dataMovementResource);
        }catch (Exception e){
            logger.error("Error while retrieving SCP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

//    @Override
//    public List<SCPDataMovement> getSCPDataMovementList(Map<String, String> filters) throws AppCatalogException {
//        try {
//            ScpDataMovementResource resource = new ScpDataMovementResource();
//            for (String fieldName : filters.keySet() ){
//                if (fieldName.equals(AbstractResource.SCPDataMovementConstants.SECURITY_PROTOCOL)){
//                    List<Resource> resources = resource.get(AbstractResource.SCPDataMovementConstants.SECURITY_PROTOCOL, filters.get(fieldName));
//                    if (resources != null && !resources.isEmpty()){
//                        return AppCatalogThriftConversion.getSCPDataMovementList(resources);
//                    }
//                }else {
//                    logger.error("Unsupported field name for SCP Data movement.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Unsupported field name for SCP Data movement.");
//                }
//            }
//        }catch (Exception e){
//            logger.error("Error while retrieving SCP Data movement list...", e);
//            throw new AppCatalogException(e);
//        }
//        return null;
//    }

    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMoveId) throws AppCatalogException {
        try {
            GridftpDataMovementAppCatalogResourceAppCat resource = new GridftpDataMovementAppCatalogResourceAppCat();
            GridftpDataMovementAppCatalogResourceAppCat dataMovementResource = (GridftpDataMovementAppCatalogResourceAppCat)resource.get(dataMoveId);
            return AppCatalogThriftConversion.getGridFTPDataMovementDescription(dataMovementResource);
        }catch (Exception e){
            logger.error("Error while retrieving Grid FTP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

//    @Override
//    public List<GridFTPDataMovement> getGridFTPDataMovementList(Map<String, String> filters) throws AppCatalogException {
//        try {
//            GridftpDataMovementResource resource = new GridftpDataMovementResource();
//            for (String fieldName : filters.keySet() ){
//                if (fieldName.equals(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL)){
//                    List<Resource> resources = resource.get(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL, filters.get(fieldName));
//                    if (resources != null && !resources.isEmpty()){
//                        return AppCatalogThriftConversion.getGridFTPDataMovementList(resources);
//                    }
//                }else {
//                    logger.error("Unsupported field name for GridFTP Data movement.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Unsupported field name for GridFTP Data movement.");
//                }
//            }
//        }catch (Exception e){
//            logger.error("Error while retrieving GridFTP Data movement list...", e);
//            throw new AppCatalogException(e);
//        }
//        return null;
//    }

    @Override
    public boolean isComputeResourceExists(String resourceId) throws AppCatalogException {
        try {
            ComputeResourceAppCatalogResourceAppCat resource = new ComputeResourceAppCatalogResourceAppCat();
            return resource.isExists(resourceId);
        }catch (Exception e){
            logger.error("Error while retrieving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeComputeResource(String resourceId) throws AppCatalogException {
        try {
            ComputeResourceAppCatalogResourceAppCat resource = new ComputeResourceAppCatalogResourceAppCat();
            resource.remove(resourceId);
        }catch (Exception e){
            logger.error("Error while removing compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId) throws AppCatalogException {
        try {
            JobSubmissionInterfaceAppCatalogResourceAppCat resource = new JobSubmissionInterfaceAppCatalogResourceAppCat();
            Map<String, String> ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID, computeResourceId);
            ids.put(AppCatAbstractResource.JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID, jobSubmissionInterfaceId);
            resource.remove(ids);
        }catch (Exception e){
            logger.error("Error while removing job submission interface..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeDataMovementInterface(String computeResourceId, String dataMovementInterfaceId) throws AppCatalogException {
        try {
            DataMovementInterfaceAppCatalogResourceAppCat resource = new DataMovementInterfaceAppCatalogResourceAppCat();
            Map<String, String> ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID, computeResourceId);
            ids.put(AppCatAbstractResource.DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID, dataMovementInterfaceId);
            resource.remove(ids);
        }catch (Exception e){
            logger.error("Error while removing data movement interface..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        try {
            BatchQueueAppCatalogResourceAppCat resource = new BatchQueueAppCatalogResourceAppCat();
            Map<String, String> ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.BatchQueueConstants.COMPUTE_RESOURCE_ID, computeResourceId);
            ids.put(AppCatAbstractResource.BatchQueueConstants.QUEUE_NAME, queueName);
            resource.remove(ids);
        }catch (Exception e){
            logger.error("Error while removing batch queue..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
	public String addResourceJobManager(ResourceJobManager resourceJobManager)
			throws AppCatalogException {
		resourceJobManager.setResourceJobManagerId(AppCatalogUtils.getID("RJM"));
		ResourceJobManagerAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getResourceJobManager(resourceJobManager);
		resource.save();
		Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
		if (jobManagerCommands!=null && jobManagerCommands.size() != 0) {
			for (JobManagerCommand commandType : jobManagerCommands.keySet()) {
				JobManagerCommandAppCatalogResourceAppCat r = new JobManagerCommandAppCatalogResourceAppCat();
				r.setCommandType(commandType.toString());
				r.setCommand(jobManagerCommands.get(commandType));
				r.setResourceJobManagerId(resource.getResourceJobManagerId());
				r.save();
			}
		}
		return resource.getResourceJobManagerId();
	}

    @Override
    public void updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager) throws AppCatalogException {
        try {
            ResourceJobManagerAppCatalogResourceAppCat resource = AppCatalogThriftConversion.getResourceJobManager(updatedResourceJobManager);
            resource.setResourceJobManagerId(resourceJobManagerId);
            resource.save();
            Map<JobManagerCommand, String> jobManagerCommands = updatedResourceJobManager.getJobManagerCommands();
            if (jobManagerCommands!=null && jobManagerCommands.size() != 0) {
                for (JobManagerCommand commandType : jobManagerCommands.keySet()) {
                    JobManagerCommandAppCatalogResourceAppCat r = new JobManagerCommandAppCatalogResourceAppCat();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AppCatAbstractResource.JobManagerCommandConstants.RESOURCE_JOB_MANAGER_ID, resourceJobManagerId);
                    ids.put(AppCatAbstractResource.JobManagerCommandConstants.COMMAND_TYPE, commandType.toString());
                    JobManagerCommandAppCatalogResourceAppCat existingCommand;
                    if (r.isExists(ids)){
                        existingCommand = (JobManagerCommandAppCatalogResourceAppCat)r.get(ids);
                    }else {
                        existingCommand = new JobManagerCommandAppCatalogResourceAppCat();
                    }
                    existingCommand.setCommandType(commandType.toString());
                    existingCommand.setCommand(jobManagerCommands.get(commandType));
                    existingCommand.setResourceJobManagerId(resource.getResourceJobManagerId());
                    existingCommand.save();
                }
            }
        }catch (Exception e){
            logger.error("Error while updating resource job manager..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        try {
            ResourceJobManagerAppCatalogResourceAppCat resource = new ResourceJobManagerAppCatalogResourceAppCat();
            ResourceJobManagerAppCatalogResourceAppCat jobManagerResource = (ResourceJobManagerAppCatalogResourceAppCat)resource.get(resourceJobManagerId);
            return AppCatalogThriftConversion.getResourceJobManager(jobManagerResource);
        }catch (Exception e){
            logger.error("Error while retrieving resource job manager..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void deleteResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        try {
            ResourceJobManagerAppCatalogResourceAppCat resource = new ResourceJobManagerAppCatalogResourceAppCat();
            resource.remove(resourceJobManagerId);
        }catch (Exception e){
            logger.error("Error while deleting resource job manager..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
	public String addLocalJobSubmission(LOCALSubmission localSubmission)
			throws AppCatalogException {
		localSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("LOCAL"));
		String resourceJobManagerId = addResourceJobManager(localSubmission.getResourceJobManager());
		LocalSubmissionAppCatalogResourceAppCat localJobSubmission = AppCatalogThriftConversion.getLocalJobSubmission(localSubmission);
		localJobSubmission.setResourceJobManagerId(resourceJobManagerId);
		localJobSubmission.getResourceJobManagerResource().setResourceJobManagerId(resourceJobManagerId);
    	localJobSubmission.save();
    	return localJobSubmission.getJobSubmissionInterfaceId();
	}

	@Override
	public String addLocalDataMovement(LOCALDataMovement localDataMovement)
			throws AppCatalogException {
		localDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("LOCAL"));
		LocalDataMovementAppCatalogResourceAppCat ldm = AppCatalogThriftConversion.getLocalDataMovement(localDataMovement);
		ldm.save();
    	return ldm.getDataMovementInterfaceId();
	}

	@Override
	public LOCALSubmission getLocalJobSubmission(String submissionId)
			throws AppCatalogException {
		LocalSubmissionAppCatalogResourceAppCat localSubmissionResource = new LocalSubmissionAppCatalogResourceAppCat();
		localSubmissionResource= (LocalSubmissionAppCatalogResourceAppCat)localSubmissionResource.get(submissionId);
		return AppCatalogThriftConversion.getLocalJobSubmission(localSubmissionResource);
	}

	@Override
	public LOCALDataMovement getLocalDataMovement(String datamovementId)
			throws AppCatalogException {
		LocalDataMovementAppCatalogResourceAppCat localDataMovementResource = new LocalDataMovementAppCatalogResourceAppCat();
		localDataMovementResource = (LocalDataMovementAppCatalogResourceAppCat) localDataMovementResource.get(datamovementId);
		return AppCatalogThriftConversion.getLocalDataMovement(localDataMovementResource);
	}

}
