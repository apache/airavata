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

package org.apache.aiaravata.application.catalog.data.impl;

import java.util.*;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ComputeResource;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogUtils;
import org.apache.airavata.model.appcatalog.computeresource.*;
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
		ComputeResourceResource computeHostResource = saveComputeResource(description);
		saveHostAliases(description, computeHostResource);
		saveIpAddresses(description, computeHostResource);
		saveBatchQueues(description, computeHostResource);
		saveFileSystems(description, computeHostResource);
		saveJobSubmissionInterfaces(description, computeHostResource);
		saveDataMovementInterfaces(description, computeHostResource);
		return computeHostResource.getResourceId();
	}

	protected ComputeResourceResource saveComputeResource(
			ComputeResourceDescription description) throws AppCatalogException {
		ComputeResourceResource computeHostResource = AppCatalogThriftConversion.getComputeHostResource(description);
		computeHostResource.save();
		return computeHostResource;
	}

	protected void saveDataMovementInterfaces(
			ComputeResourceDescription description,
			ComputeResourceResource computeHostResource)
			throws AppCatalogException {
		List<DataMovementInterface> dataMovemenetInterfaces = description.getDataMovementInterfaces();
		if (dataMovemenetInterfaces != null && !dataMovemenetInterfaces.isEmpty()) {
		    for (DataMovementInterface dataMovementInterface : dataMovemenetInterfaces) {
		    	DataMovementInterfaceResource dmir = AppCatalogThriftConversion.getDataMovementInterface(dataMovementInterface);
		    	dmir.setComputeHostResource(computeHostResource);
		    	dmir.setComputeResourceId(computeHostResource.getResourceId());
				dmir.save();
		    }
		}
	}

	protected void saveJobSubmissionInterfaces(
			ComputeResourceDescription description,
			ComputeResourceResource computeHostResource)
			throws AppCatalogException {
		List<JobSubmissionInterface> jobSubmissionInterfaces = description.getJobSubmissionInterfaces();
		if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
		    for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
		    	JobSubmissionInterfaceResource jsir = AppCatalogThriftConversion.getJobSubmissionInterface(jobSubmissionInterface);
				jsir.setComputeHostResource(computeHostResource);
				jsir.setComputeResourceId(computeHostResource.getResourceId());
				jsir.save();
		    }
		}
	}

	protected void saveFileSystems(ComputeResourceDescription description,
			ComputeResourceResource computeHostResource)
			throws AppCatalogException {
		Map<FileSystems, String> fileSystems = description.getFileSystems();
		if (fileSystems != null && !fileSystems.isEmpty()) {
		    for (FileSystems key : fileSystems.keySet()) {
		    	ComputeResourceFileSystemResource computeResourceFileSystemResource = new ComputeResourceFileSystemResource();
		    	computeResourceFileSystemResource.setComputeHostResource(computeHostResource);
		    	computeResourceFileSystemResource.setComputeResourceId(computeHostResource.getResourceId());
		    	computeResourceFileSystemResource.setFileSystem(key.toString());
		    	computeResourceFileSystemResource.setPath(fileSystems.get(key));
		    	computeResourceFileSystemResource.save();
		    }
		}
	}

	protected void saveBatchQueues(ComputeResourceDescription description,
			ComputeResourceResource computeHostResource)
			throws AppCatalogException {
		List<BatchQueue> batchQueueList = description.getBatchQueues();
		if (batchQueueList != null && !batchQueueList.isEmpty()) {
		    for (BatchQueue batchQueue : batchQueueList) {
		    	BatchQueueResource bq = AppCatalogThriftConversion.getBatchQueue(batchQueue);
		    	bq.setComputeResourceId(computeHostResource.getResourceId());
		    	bq.setComputeHostResource(computeHostResource);
		        bq.save();
		    }
		}
	}

	protected void saveIpAddresses(ComputeResourceDescription description,
			ComputeResourceResource computeHostResource)
			throws AppCatalogException {
		Set<String> ipAddresses = description.getIpAddresses();
		if (ipAddresses != null && !ipAddresses.isEmpty()) {
		    for (String ipAddress : ipAddresses) {
		        HostIPAddressResource ipAddressResource = new HostIPAddressResource();
		        ipAddressResource.setComputeHostResource(computeHostResource);
		        ipAddressResource.setResourceID(computeHostResource.getResourceId());
		        ipAddressResource.setIpaddress(ipAddress);
		        ipAddressResource.save();
		    }
		}
	}

	protected void saveHostAliases(ComputeResourceDescription description,
			ComputeResourceResource computeHostResource)
			throws AppCatalogException {
		Set<String> hostAliases = description.getHostAliases();
		if (hostAliases != null && !hostAliases.isEmpty()) {
		    for (String alias : hostAliases) {
		        HostAliasResource aliasResource = new HostAliasResource();
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
        	sshJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("SSH"));
    		String resourceJobManagerId = addResourceJobManager(sshJobSubmission.getResourceJobManager());
    		SshJobSubmissionResource resource = AppCatalogThriftConversion.getSSHJobSubmission(sshJobSubmission);
    		resource.setResourceJobManagerId(resourceJobManagerId);
    		resource.getResourceJobManagerResource().setResourceJobManagerId(resourceJobManagerId);
    		resource.save();
        	return resource.getJobSubmissionInterfaceId();
        }catch (Exception e) {
            logger.error("Error while saving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addCloudJobSubmission(CloudJobSubmission sshJobSubmission) throws AppCatalogException {
        try {
            sshJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("Cloud"));
            CloudSubmissionResource resource = AppCatalogThriftConversion.getCloudJobSubmission(sshJobSubmission);
            resource.save();
            return resource.getJobSubmissionInterfaceId();
        }catch (Exception e) {
            logger.error("Error while saving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addJobSubmissionProtocol(String computeResourceId, JobSubmissionInterface jobSubmissionInterface) throws AppCatalogException {
        try {
        	JobSubmissionInterfaceResource jsi = AppCatalogThriftConversion.getJobSubmissionInterface(jobSubmissionInterface);
        	jsi.setComputeResourceId(computeResourceId);
        	ComputeResourceResource computeResourceResource = new ComputeResourceResource();
        	computeResourceResource=(ComputeResourceResource)computeResourceResource.get(computeResourceId);
        	jsi.setComputeHostResource(computeResourceResource);
            jsi.save();
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
//            resource.setSubmissionID(AppCatalogUtils.getID(hostName));
//            resource.setSshPort(resource.getSshPort());
//            resource.setResourceJobManager(gsisshJobSubmission.getResourceJobManager().toString());
//            resource.setInstalledPath(gsisshJobSubmission.getInstalledPath());
//            resource.setMonitorMode(gsisshJobSubmission.getMonitorMode());
//            resource.save();
//            gsisshJobSubmission.setJobSubmissionDataID(resource.getSubmissionID());
//
//            Set<String> exports = gsisshJobSubmission.getExports();
//            if (exports != null && !exports.isEmpty()){
//                for (String export : exports){
//                    GSISSHExportResource exportResource = new GSISSHExportResource();
//                    exportResource.setSubmissionID(resource.getSubmissionID());
//                    exportResource.setExport(export);
//                    exportResource.setGsisshSubmissionResource(resource);
//                    exportResource.save();
//                }
//            }
//
//            List<String> preJobCommands = gsisshJobSubmission.getPreJobCommands();
//            if (preJobCommands != null && !preJobCommands.isEmpty()){
//                for (String command : preJobCommands){
//                    GSISSHPreJobCommandResource commandResource = new GSISSHPreJobCommandResource();
//                    commandResource.setSubmissionID(resource.getSubmissionID());
//                    commandResource.setCommand(command);
//                    commandResource.setGsisshSubmissionResource(resource);
//                    commandResource.save();
//                }
//            }
//
//            List<String> postJobCommands = gsisshJobSubmission.getPostJobCommands();
//            if (postJobCommands != null && !postJobCommands.isEmpty()){
//                for (String command : postJobCommands){
//                    GSISSHPostJobCommandResource commandResource = new GSISSHPostJobCommandResource();
//                    commandResource.setSubmissionID(resource.getSubmissionID());
//                    commandResource.setCommand(command);
//                    commandResource.setGsisshSubmissionResource(resource);
//                    commandResource.save();
//                }
//            }
//            return resource.getSubmissionID();
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
//            resource.setSubmissionID(jobSubmissionId);
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
//            resource.setSubmissionID(AppCatalogUtils.getID(hostName));
//            resource.setSecurityProtocol(globusJobSubmission.getSecurityProtocol().toString());
//            resource.setResourceJobManager(globusJobSubmission.getResourceJobManager().toString());
//            resource.save();
//            globusJobSubmission.setJobSubmissionDataID(resource.getSubmissionID());
//            List<String> globusGateKeeperEndPoint = globusJobSubmission.getGlobusGateKeeperEndPoint();
//            if (globusGateKeeperEndPoint != null && !globusGateKeeperEndPoint.isEmpty()) {
//                for (String endpoint : globusGateKeeperEndPoint) {
//                    GlobusGKEndpointResource endpointResource = new GlobusGKEndpointResource();
//                    endpointResource.setSubmissionID(resource.getSubmissionID());
//                    endpointResource.setEndpoint(endpoint);
//                    endpointResource.setGlobusJobSubmissionResource(resource);
//                    endpointResource.save();
//                }
//            }
//            return resource.getSubmissionID();
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
        	ScpDataMovementResource resource = AppCatalogThriftConversion.getSCPDataMovementDescription(scpDataMovement);
            resource.save();
            return resource.getDataMovementInterfaceId();
        }catch (Exception e){
            logger.error("Error while saving SCP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addDataMovementProtocol(String computeResourceId, DataMovementInterface dataMovementInterface) throws AppCatalogException {
        try {
        	DataMovementInterfaceResource dmi = AppCatalogThriftConversion.getDataMovementInterface(dataMovementInterface);
        	dmi.setComputeResourceId(computeResourceId);
        	ComputeResourceResource computeResourceResource = new ComputeResourceResource();
        	computeResourceResource=(ComputeResourceResource)computeResourceResource.get(computeResourceId);
        	dmi.setComputeHostResource(computeResourceResource);
        	dmi.save();
        }catch (Exception e){
            logger.error("Error while saving "+dataMovementInterface.getDataMovementProtocol().toString()+" data movement Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        try {
        	gridFTPDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"));
        	GridftpDataMovementResource resource = AppCatalogThriftConversion.getGridFTPDataMovementDescription(gridFTPDataMovement);
            resource.save();
            List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFTPEndPoints();
            if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
                for (String endpoint : gridFTPEndPoint) {
                    GridftpEndpointResource endpointResource = new GridftpEndpointResource();
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
            ComputeResourceResource resource = new ComputeResourceResource();
            ComputeResourceResource computeResource = (ComputeResourceResource)resource.get(resourceId);
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
            ComputeResourceResource resource = new ComputeResourceResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.ComputeResourceConstants.HOST_NAME)){
                    List<Resource> resources = resource.get(AbstractResource.ComputeResourceConstants.HOST_NAME, filters.get(fieldName));
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
            ComputeResourceResource resource = new ComputeResourceResource();
            List<Resource> resources = resource.getAll();
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
            ComputeResourceResource resource = new ComputeResourceResource();
            List<Resource> allComputeResources = resource.getAll();
            if (allComputeResources != null && !allComputeResources.isEmpty()){
                for (Resource cm : allComputeResources){
                    ComputeResourceResource cmr = (ComputeResourceResource)cm;
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
            SshJobSubmissionResource resource = new SshJobSubmissionResource();
            resource = (SshJobSubmissionResource)resource.get(submissionId);
            return AppCatalogThriftConversion.getSSHJobSubmissionDescription(resource);
        }catch (Exception e){
            logger.error("Error while retrieving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public CloudJobSubmission getCloudJobSubmission(String submissionId) throws AppCatalogException {
        try {
            CloudSubmissionResource resource = new CloudSubmissionResource();
            resource = (CloudSubmissionResource)resource.get(submissionId);
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
            ScpDataMovementResource resource = new ScpDataMovementResource();
            ScpDataMovementResource dataMovementResource = (ScpDataMovementResource)resource.get(dataMoveId);
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
            GridftpDataMovementResource resource = new GridftpDataMovementResource();
            GridftpDataMovementResource dataMovementResource = (GridftpDataMovementResource)resource.get(dataMoveId);
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
            ComputeResourceResource resource = new ComputeResourceResource();
            return resource.isExists(resourceId);
        }catch (Exception e){
            logger.error("Error while retrieving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeComputeResource(String resourceId) throws AppCatalogException {
        try {
            ComputeResourceResource resource = new ComputeResourceResource();
            resource.remove(resourceId);
        }catch (Exception e){
            logger.error("Error while removing compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeJobSubmissionInterface(String jobSubmissionInterfaceId) throws AppCatalogException {
        try {
            JobSubmissionInterfaceResource resource = new JobSubmissionInterfaceResource();
            resource.remove(jobSubmissionInterfaceId);
        }catch (Exception e){
            logger.error("Error while removing job submission interface..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeDataMovementInterface(String dataMovementInterfaceId) throws AppCatalogException {
        try {
            DataMovementInterfaceResource resource = new DataMovementInterfaceResource();
            resource.remove(dataMovementInterfaceId);
        }catch (Exception e){
            logger.error("Error while removing data movement interface..", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
	public String addResourceJobManager(ResourceJobManager resourceJobManager)
			throws AppCatalogException {
		resourceJobManager.setResourceJobManagerId(AppCatalogUtils.getID("RJM"));
		ResourceJobManagerResource resource = AppCatalogThriftConversion.getResourceJobManager(resourceJobManager);
		resource.save();
		Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
		if (jobManagerCommands!=null) {
			for (JobManagerCommand commandType : jobManagerCommands.keySet()) {
				JobManagerCommandResource r = new JobManagerCommandResource();
				r.setCommandType(commandType.toString());
				r.setCommand(jobManagerCommands.get(commandType));
				r.setResourceJobManagerId(resource.getResourceJobManagerId());
				r.save();
			}
		}
		return resource.getResourceJobManagerId();
	}

	@Override
	public String addLocalJobSubmission(LOCALSubmission localSubmission)
			throws AppCatalogException {
		localSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("LOCAL"));
		String resourceJobManagerId = addResourceJobManager(localSubmission.getResourceJobManager());
		LocalSubmissionResource localJobSubmission = AppCatalogThriftConversion.getLocalJobSubmission(localSubmission);
		localJobSubmission.setResourceJobManagerId(resourceJobManagerId);
		localJobSubmission.getResourceJobManagerResource().setResourceJobManagerId(resourceJobManagerId);
    	localJobSubmission.save();
    	return localJobSubmission.getJobSubmissionInterfaceId();
	}

	@Override
	public String addLocalDataMovement(LOCALDataMovement localDataMovement)
			throws AppCatalogException {
		localDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("LOCAL"));
		LocalDataMovementResource ldm = AppCatalogThriftConversion.getLocalDataMovement(localDataMovement);
		ldm.save();
    	return ldm.getDataMovementInterfaceId();
	}

	@Override
	public LOCALSubmission getLocalJobSubmission(String submissionId)
			throws AppCatalogException {
		LocalSubmissionResource localSubmissionResource = new LocalSubmissionResource();
		localSubmissionResource= (LocalSubmissionResource)localSubmissionResource.get(submissionId);
		return AppCatalogThriftConversion.getLocalJobSubmission(localSubmissionResource);
	}

	@Override
	public LOCALDataMovement getLocalDataMovement(String datamovementId)
			throws AppCatalogException {
		LocalDataMovementResource localDataMovementResource = new LocalDataMovementResource();
		localDataMovementResource = (LocalDataMovementResource) localDataMovementResource.get(datamovementId);
		return AppCatalogThriftConversion.getLocalDataMovement(localDataMovementResource);
	}
}
