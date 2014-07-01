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

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ComputeResource;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogUtils;
import org.apache.airavata.model.computehost.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComputeResourceImpl implements ComputeResource {
    private final static Logger logger = LoggerFactory.getLogger(ComputeResourceImpl.class);

    @Override
    public String addComputeResource(ComputeResourceDescription description) throws AppCatalogException {
        try {
            ComputeHostResource computeHostResource = new ComputeHostResource();
            computeHostResource.setHostName(description.getHostName());
            computeHostResource.setResoureId(AppCatalogUtils.getID(description.getHostName()));
            description.setResourceId(computeHostResource.getResoureId());
            computeHostResource.setPreferredJobSubmissionProtocol(description.getPreferredJobSubmissionProtocol());
            computeHostResource.setDescription(description.getResourceDescription());
            computeHostResource.save();
            description.setResourceId(computeHostResource.getResoureId());

            Set<String> hostAliases = description.getHostAliases();
            if (hostAliases != null && !hostAliases.isEmpty()) {
                for (String alias : hostAliases) {
                    HostAliasResource aliasResource = new HostAliasResource();
                    aliasResource.setComputeHostResource(computeHostResource);
                    aliasResource.setResourceID(computeHostResource.getResoureId());
                    aliasResource.setAlias(alias);
                    aliasResource.save();
                }
            }

            Set<String> ipAddresses = description.getIpAddresses();
            if (ipAddresses != null && !ipAddresses.isEmpty()) {
                for (String ipAddress : ipAddresses) {
                    HostIPAddressResource ipAddressResource = new HostIPAddressResource();
                    ipAddressResource.setComputeHostResource(computeHostResource);
                    ipAddressResource.setResourceID(computeHostResource.getResoureId());
                    ipAddressResource.setIpaddress(ipAddress);
                    ipAddressResource.save();
                }
            }

            Map<String, JobSubmissionProtocol> jobSubmissionProtocols = description.getJobSubmissionProtocols();
            if (jobSubmissionProtocols != null && !jobSubmissionProtocols.isEmpty()) {
                for (String key : jobSubmissionProtocols.keySet()) {
                    JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionProtocols.get(key);
                    JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
                    resource.setResourceID(computeHostResource.getResoureId());
                    resource.setComputeHostResource(computeHostResource);
                    resource.setSubmissionID(key);
                    resource.setJobType(jobSubmissionProtocol.toString());
                    resource.save();
                }
            }
            Map<String, DataMovementProtocol> movementProtocols = description.getDataMovementProtocols();
            if (movementProtocols != null && !movementProtocols.isEmpty()) {
                for (String key : movementProtocols.keySet()) {
                    DataMovementProtocol dataMovementProtocol = movementProtocols.get(key);
                    DataMovementProtocolResource resource = new DataMovementProtocolResource();
                    resource.setResourceID(computeHostResource.getResoureId());
                    resource.setComputeHostResource(computeHostResource);
                    resource.setDataMoveID(key);
                    resource.setDataMoveType(dataMovementProtocol.toString());
                    resource.save();
                }
            }
            return computeHostResource.getResoureId();
        } catch (Exception e) {
            logger.error("Error while saving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource) throws AppCatalogException{
        try {
            ComputeHostResource computeHostResource = new ComputeHostResource();
            ComputeHostResource existingComputeResouce = (ComputeHostResource)computeHostResource.get(computeResourceId);
            existingComputeResouce.setHostName(updatedComputeResource.getHostName());
            existingComputeResouce.setPreferredJobSubmissionProtocol(updatedComputeResource.getPreferredJobSubmissionProtocol());
            existingComputeResouce.setDescription(updatedComputeResource.getResourceDescription());
            existingComputeResouce.save();

            Set<String> hostAliases = updatedComputeResource.getHostAliases();
            if (hostAliases != null && !hostAliases.isEmpty()) {
                for (String alias : hostAliases) {
                    HostAliasResource aliasResource = new HostAliasResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.HostAliasConstants.RESOURCE_ID, computeResourceId);
                    ids.put(AbstractResource.HostAliasConstants.ALIAS, alias);
                    HostAliasResource existingAlias = (HostAliasResource)aliasResource.get(ids);
                    existingAlias.setComputeHostResource(existingComputeResouce);
                    existingAlias.setAlias(alias);
                    existingAlias.save();
                }
            }

            Set<String> ipAddresses = updatedComputeResource.getIpAddresses();
            if (ipAddresses != null && !ipAddresses.isEmpty()) {
                for (String ipAddress : ipAddresses) {
                    HostIPAddressResource ipAddressResource = new HostIPAddressResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.HostIPAddressConstants.RESOURCE_ID, computeResourceId);
                    ids.put(AbstractResource.HostIPAddressConstants.IP_ADDRESS, ipAddress);
                    HostIPAddressResource existingIpAddress = (HostIPAddressResource)ipAddressResource.get(ids);
                    existingIpAddress.setComputeHostResource(existingComputeResouce);
                    existingIpAddress.setResourceID(computeResourceId);
                    existingIpAddress.setIpaddress(ipAddress);
                    existingIpAddress.save();
                }
            }
            Map<String, JobSubmissionProtocol> jobSubmissionProtocols = updatedComputeResource.getJobSubmissionProtocols();
            if (jobSubmissionProtocols != null && !jobSubmissionProtocols.isEmpty()) {
                for (String submissionId : jobSubmissionProtocols.keySet()) {
                    JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionProtocols.get(submissionId);
                    JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.JobSubmissionProtocolConstants.RESOURCE_ID, computeResourceId);
                    ids.put(AbstractResource.JobSubmissionProtocolConstants.SUBMISSION_ID, submissionId);
                    ids.put(AbstractResource.JobSubmissionProtocolConstants.JOB_TYPE, jobSubmissionProtocol.toString());
                    JobSubmissionProtocolResource existingJobProtocol = (JobSubmissionProtocolResource)resource.get(ids);
                    existingJobProtocol.setResourceID(computeResourceId);
                    existingJobProtocol.setComputeHostResource(existingComputeResouce);
                    existingJobProtocol.setSubmissionID(submissionId);
                    existingJobProtocol.setJobType(jobSubmissionProtocol.toString());
                    existingJobProtocol.save();
                }
            }
            Map<String, DataMovementProtocol> movementProtocols = updatedComputeResource.getDataMovementProtocols();
            if (movementProtocols != null && !movementProtocols.isEmpty()) {
                for (String dataMoveId : movementProtocols.keySet()) {
                    DataMovementProtocol dataMovementProtocol = movementProtocols.get(dataMoveId);
                    DataMovementProtocolResource resource = new DataMovementProtocolResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.DataMoveProtocolConstants.RESOURCE_ID, computeResourceId);
                    ids.put(AbstractResource.DataMoveProtocolConstants.DATA_MOVE_ID, dataMoveId);
                    ids.put(AbstractResource.DataMoveProtocolConstants.JOB_TYPE, dataMovementProtocol.toString());
                    DataMovementProtocolResource existingDMP = (DataMovementProtocolResource)resource.get(ids);
                    existingDMP.setResourceID(computeResourceId);
                    existingDMP.setComputeHostResource(existingComputeResouce);
                    existingDMP.setDataMoveID(dataMoveId);
                    existingDMP.setDataMoveType(dataMovementProtocol.toString());
                    existingDMP.save();
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating compute resource...", e);
            throw new AppCatalogException(e);
        } 
    }

    @Override
    public String addSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        try {
            SSHSubmissionResource resource = new SSHSubmissionResource();
            String hostName = "SSH";
            resource.setSubmissionID(AppCatalogUtils.getID(hostName));
            resource.setSshPort(resource.getSshPort());
            resource.setResourceJobManager(sshJobSubmission.getResourceJobManager().toString());
            resource.save();
            sshJobSubmission.setJobSubmissionDataID(resource.getSubmissionID());
            return resource.getSubmissionID();
        }catch (Exception e) {
            logger.error("Error while saving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addSSHJobSubmissionProtocol(String computeResourceId, String jobSubmissionId) throws AppCatalogException {
        try {
            JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
            resource.setResourceID(computeResourceId);
            resource.setSubmissionID(jobSubmissionId);
            ComputeResourceDescription computeResource = getComputeResource(computeResourceId);
            resource.setComputeHostResource(AppCatalogThriftConversion.getComputeHostResource(computeResource));
            resource.setJobType(JobSubmissionProtocol.SSH.toString());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving SSH Job Submission Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addGSISSHJobSubmission(GSISSHJobSubmission gsisshJobSubmission) throws AppCatalogException {
        try {
            GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
            String hostName = "GSISSH";
            resource.setSubmissionID(AppCatalogUtils.getID(hostName));
            resource.setSshPort(resource.getSshPort());
            resource.setResourceJobManager(gsisshJobSubmission.getResourceJobManager().toString());
            resource.setInstalledPath(gsisshJobSubmission.getInstalledPath());
            resource.setMonitorMode(gsisshJobSubmission.getMonitorMode());
            resource.save();
            gsisshJobSubmission.setJobSubmissionDataID(resource.getSubmissionID());

            Set<String> exports = gsisshJobSubmission.getExports();
            if (exports != null && !exports.isEmpty()){
                for (String export : exports){
                    GSISSHExportResource exportResource = new GSISSHExportResource();
                    exportResource.setSubmissionID(resource.getSubmissionID());
                    exportResource.setExport(export);
                    exportResource.setGsisshSubmissionResource(resource);
                    exportResource.save();
                }
            }

            List<String> preJobCommands = gsisshJobSubmission.getPreJobCommands();
            if (preJobCommands != null && !preJobCommands.isEmpty()){
                for (String command : preJobCommands){
                    GSISSHPreJobCommandResource commandResource = new GSISSHPreJobCommandResource();
                    commandResource.setSubmissionID(resource.getSubmissionID());
                    commandResource.setCommand(command);
                    commandResource.setGsisshSubmissionResource(resource);
                    commandResource.save();
                }
            }

            List<String> postJobCommands = gsisshJobSubmission.getPostJobCommands();
            if (postJobCommands != null && !postJobCommands.isEmpty()){
                for (String command : postJobCommands){
                    GSISSHPostJobCommandResource commandResource = new GSISSHPostJobCommandResource();
                    commandResource.setSubmissionID(resource.getSubmissionID());
                    commandResource.setCommand(command);
                    commandResource.setGsisshSubmissionResource(resource);
                    commandResource.save();
                }
            }
            return resource.getSubmissionID();
        }catch (Exception e) {
            logger.error("Error while saving GSISSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addGSISSHJobSubmissionProtocol(String computeResourceId, String jobSubmissionId) throws AppCatalogException {
        try {
            JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
            resource.setResourceID(computeResourceId);
            resource.setSubmissionID(jobSubmissionId);
            ComputeResourceDescription computeResource = getComputeResource(computeResourceId);
            resource.setComputeHostResource(AppCatalogThriftConversion.getComputeHostResource(computeResource));
            resource.setJobType(JobSubmissionProtocol.GSISSH.toString());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving GSISSH Job Submission Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addGlobusJobSubmission(GlobusJobSubmission globusJobSubmission) throws AppCatalogException {
        try {
            GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
            String hostName = "GLOBUS";
            resource.setSubmissionID(AppCatalogUtils.getID(hostName));
            resource.setSecurityProtocol(globusJobSubmission.getSecurityProtocol().toString());
            resource.setResourceJobManager(globusJobSubmission.getResourceJobManager().toString());
            resource.save();
            globusJobSubmission.setJobSubmissionDataID(resource.getSubmissionID());
            List<String> globusGateKeeperEndPoint = globusJobSubmission.getGlobusGateKeeperEndPoint();
            if (globusGateKeeperEndPoint != null && !globusGateKeeperEndPoint.isEmpty()) {
                for (String endpoint : globusGateKeeperEndPoint) {
                    GlobusGKEndpointResource endpointResource = new GlobusGKEndpointResource();
                    endpointResource.setSubmissionID(resource.getSubmissionID());
                    endpointResource.setEndpoint(endpoint);
                    endpointResource.setGlobusJobSubmissionResource(resource);
                    endpointResource.save();
                }
            }
            return resource.getSubmissionID();
        } catch (Exception e) {
            logger.error("Error while saving Globus Job Submission...", e);
            throw new AppCatalogException(e);
        }

    }

    @Override
    public void addGlobusJobSubmissionProtocol(String computeResourceId, String jobSubmissionId) throws AppCatalogException {
        try {
            JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
            resource.setResourceID(computeResourceId);
            resource.setSubmissionID(jobSubmissionId);
            ComputeResourceDescription computeResource = getComputeResource(computeResourceId);
            resource.setComputeHostResource(AppCatalogThriftConversion.getComputeHostResource(computeResource));
            resource.setJobType(JobSubmissionProtocol.GRAM.toString());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving Globus Job Submission Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        try {
            SCPDataMovementResource resource = new SCPDataMovementResource();
            String hostName = "SCP";
            resource.setDataMoveID(AppCatalogUtils.getID(hostName));
            resource.setSecurityProtocol(scpDataMovement.getSecurityProtocol().toString());
            resource.setSshPort(scpDataMovement.getSshPort());
            resource.save();
            scpDataMovement.setDataMovementDataID(resource.getDataMoveID());
            return resource.getDataMoveID();
        }catch (Exception e){
            logger.error("Error while saving SCP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addScpDataMovementProtocol(String computeResourceId, String dataMoveId) throws AppCatalogException {
        try {
            DataMovementProtocolResource resource = new DataMovementProtocolResource();
            resource.setResourceID(computeResourceId);
            resource.setDataMoveID(dataMoveId);
            ComputeResourceDescription computeResource = getComputeResource(computeResourceId);
            resource.setComputeHostResource(AppCatalogThriftConversion.getComputeHostResource(computeResource));
            resource.setDataMoveType(DataMovementProtocol.SCP.toString());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving SCP data movement Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        try {
            GridFTPDataMovementResource resource = new GridFTPDataMovementResource();
            String hostName = "GRID_FTP";
            resource.setDataMoveID(AppCatalogUtils.getID(hostName));
            resource.setSecurityProtocol(gridFTPDataMovement.getSecurityProtocol().toString());
            resource.save();
            gridFTPDataMovement.setDataMovementDataID(resource.getDataMoveID());

            List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFTPEndPoint();
            if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
                for (String endpoint : gridFTPEndPoint) {
                    GridFTPDMEndpointResource endpointResource = new GridFTPDMEndpointResource();
                    endpointResource.setDataMoveId(resource.getDataMoveID());
                    endpointResource.setEndpoint(endpoint);
                    endpointResource.setGridFTPDataMovementResource(resource);
                    endpointResource.save();
                }
            }
            return resource.getDataMoveID();
        }catch (Exception e){
            logger.error("Error while saving GridFTP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addGridFTPDataMovementProtocol(String computeResourceId, String dataMoveId) throws AppCatalogException {
        try {
            DataMovementProtocolResource resource = new DataMovementProtocolResource();
            resource.setResourceID(computeResourceId);
            resource.setDataMoveID(dataMoveId);
            ComputeResourceDescription computeResource = getComputeResource(computeResourceId);
            resource.setComputeHostResource(AppCatalogThriftConversion.getComputeHostResource(computeResource));
            resource.setDataMoveType(DataMovementProtocol.GridFTP.toString());
            resource.save();
        }catch (Exception e){
            logger.error("Error while saving GridFTP data movement Protocol...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ComputeResourceDescription getComputeResource(String resourceId) throws AppCatalogException {
        try {
            ComputeHostResource resource = new ComputeHostResource();
            ComputeHostResource computeResource = (ComputeHostResource)resource.get(resourceId);
            return AppCatalogThriftConversion.getComputeHostDescription(computeResource);
        }catch (Exception e){
            logger.error("Error while retrieving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters) throws AppCatalogException {
        try {
            ComputeHostResource resource = new ComputeHostResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.ComputeResourceConstants.HOST_NAME)){
                    List<Resource> resources = resource.get(AbstractResource.ComputeResourceConstants.HOST_NAME, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getComputeDescriptionList(resources);
                    }
                }else if (fieldName.equals(AbstractResource.ComputeResourceConstants.PREFERED_SUBMISSION_PROTOCOL)){
                    List<Resource> resources = resource.get(AbstractResource.ComputeResourceConstants.PREFERED_SUBMISSION_PROTOCOL, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getComputeDescriptionList(resources);
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
        return null;
    }

    @Override
    public GSISSHJobSubmission getGSISSHJobSubmission(String submissionId) throws AppCatalogException {
        try {
            GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
            GSISSHSubmissionResource submissionResource = (GSISSHSubmissionResource)resource.get(submissionId);
            return AppCatalogThriftConversion.getGSISSHSubmissionDescription(submissionResource);
        }catch (Exception e){
            logger.error("Error while retrieving GSISSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<GSISSHJobSubmission> getGSISSHJobSubmissionList(Map<String, String> filters) throws AppCatalogException {
        try {
            GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER)){
                    List<Resource> resources = resource.get(AbstractResource.GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getGSISSHSubmissionList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for GSISSH Submission.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for GSISSH Submission.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving GSISSH Submission list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public GlobusJobSubmission getGlobusJobSubmission(String submissionId) throws AppCatalogException {
        try {
            GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
            GlobusJobSubmissionResource submissionResource = (GlobusJobSubmissionResource)resource.get(submissionId);
            return AppCatalogThriftConversion.getGlobusJobSubmissionDescription(submissionResource);
        }catch (Exception e){
            logger.error("Error while retrieving Globus Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<GlobusJobSubmission> getGlobusJobSubmissionList(Map<String, String> filters) throws AppCatalogException {
        try {
            GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER)){
                    List<Resource> resources = resource.get(AbstractResource.GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getGlobusSubmissionList(resources);
                    }
                }else if (fieldName.equals(AbstractResource.GlobusJobSubmissionConstants.SECURITY_PROTOCAL)){
                    List<Resource> resources = resource.get(AbstractResource.GlobusJobSubmissionConstants.SECURITY_PROTOCAL, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getGlobusSubmissionList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for Globus Submission.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for Globus Submission.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving Globus Submission list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        try {
            SSHSubmissionResource resource = new SSHSubmissionResource();
            SSHSubmissionResource submissionResource = (SSHSubmissionResource)resource.get(submissionId);
            return AppCatalogThriftConversion.getSSHJobSubmissionDescription(submissionResource);
        }catch (Exception e){
            logger.error("Error while retrieving SSH Job Submission...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<SSHJobSubmission> getSSHJobSubmissionList(Map<String, String> filters) throws AppCatalogException {
        try {
            SSHSubmissionResource resource = new SSHSubmissionResource();
            for (String fieldName : filters.keySet() ){
               if (fieldName.equals(AbstractResource.SSHSubmissionConstants.RESOURCE_JOB_MANAGER)){
                    List<Resource> resources = resource.get(AbstractResource.SSHSubmissionConstants.RESOURCE_JOB_MANAGER, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getSSHSubmissionList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for SSH Submission.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for SSH Submission.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving SSH Submission list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws AppCatalogException {
        try {
            SCPDataMovementResource resource = new SCPDataMovementResource();
            SCPDataMovementResource dataMovementResource = (SCPDataMovementResource)resource.get(dataMoveId);
            return AppCatalogThriftConversion.getSCPDataMovementDescription(dataMovementResource);
        }catch (Exception e){
            logger.error("Error while retrieving SCP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<SCPDataMovement> getSCPDataMovementList(Map<String, String> filters) throws AppCatalogException {
        try {
            SCPDataMovementResource resource = new SCPDataMovementResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.SCPDataMovementConstants.SECURITY_PROTOCOL)){
                    List<Resource> resources = resource.get(AbstractResource.SCPDataMovementConstants.SECURITY_PROTOCOL, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getSCPDataMovementList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for SCP Data movement.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for SCP Data movement.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving SCP Data movement list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMoveId) throws AppCatalogException {
        try {
            GridFTPDataMovementResource resource = new GridFTPDataMovementResource();
            GridFTPDataMovementResource dataMovementResource = (GridFTPDataMovementResource)resource.get(dataMoveId);
            return AppCatalogThriftConversion.getGridFTPDataMovementDescription(dataMovementResource);
        }catch (Exception e){
            logger.error("Error while retrieving Grid FTP Data Movement...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<GridFTPDataMovement> getGridFTPDataMovementList(Map<String, String> filters) throws AppCatalogException {
        try {
            GridFTPDataMovementResource resource = new GridFTPDataMovementResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL)){
                    List<Resource> resources = resource.get(AbstractResource.GridFTPDataMovementConstants.SECURITY_PROTOCOL, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getGridFTPDataMovementList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for GridFTP Data movement.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for GridFTP Data movement.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving GridFTP Data movement list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public boolean isComputeResourceExists(String resourceId) throws AppCatalogException {
        try {
            ComputeHostResource resource = new ComputeHostResource();
            return resource.isExists(resourceId);
        }catch (Exception e){
            logger.error("Error while retrieving compute resource...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeComputeResource(String resourceId) throws AppCatalogException {
        try {
            ComputeHostResource resource = new ComputeHostResource();
            resource.remove(resourceId);
        }catch (Exception e){
            logger.error("Error while removing compute resource...", e);
            throw new AppCatalogException(e);
        }
    }
}
