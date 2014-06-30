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
import org.apache.airavata.model.computehost.*;

import java.util.*;

public class AppCatalogThriftConversion {
    public static ComputeHostResource getComputeHostResource (ComputeResourceDescription description){
        ComputeHostResource resource = new ComputeHostResource();
        resource.setHostName(description.getHostName());
        resource.setDescription(description.getResourceDescription());
        resource.setPreferredJobSubmissionProtocol(description.getPreferredJobSubmissionProtocol());
        resource.setPreferredJobSubmissionProtocol(description.getResourceId());
        return resource;
    }

    public static ComputeResourceDescription getComputeHostDescription (ComputeHostResource resource) throws AppCatalogException{
        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setResourceId(resource.getResoureId());
        description.setHostName(resource.getHostName());
        description.setResourceDescription(resource.getDescription());
        description.setPreferredJobSubmissionProtocol(resource.getPreferredJobSubmissionProtocol());
        HostAliasResource aliasResource = new HostAliasResource();
        List<Resource> resources = aliasResource.get(AbstractResource.HostAliasConstants.RESOURCE_ID, resource.getResoureId());
        if (resources != null && !resources.isEmpty()){
            description.setHostAliases(getHostAliases(resources));
        }
        HostIPAddressResource ipAddressResource = new HostIPAddressResource();
        List<Resource> ipAddresses = ipAddressResource.get(AbstractResource.HostIPAddressConstants.RESOURCE_ID, resource.getResoureId());
        if (ipAddresses != null && !ipAddresses.isEmpty()){
            description.setIpAddresses(getIpAddresses(ipAddresses));
        }

        JobSubmissionProtocolResource submissionProtocolResource = new JobSubmissionProtocolResource();
        List<Resource> submissionProtocols = submissionProtocolResource.get(AbstractResource.JobSubmissionProtocolConstants.RESOURCE_ID, resource.getResoureId());
        if (submissionProtocols != null && !submissionProtocols.isEmpty()){
            description.setJobSubmissionProtocols(getJobSubmissionProtocolList(submissionProtocols));
        }

        DataMovementProtocolResource movementProtocolResource = new DataMovementProtocolResource();
        List<Resource> dataMoveProtocols = movementProtocolResource.get(AbstractResource.DataMoveProtocolConstants.RESOURCE_ID, resource.getResoureId());
        if (dataMoveProtocols != null && !dataMoveProtocols.isEmpty()){
            description.setDataMovementProtocols(getDataMoveProtocolList(dataMoveProtocols));
        }
        return description;
    }

    public static  List<ComputeResourceDescription> getComputeDescriptionList (List<Resource> resources) throws AppCatalogException {
        List<ComputeResourceDescription> list = new ArrayList<ComputeResourceDescription>();
        for (Resource resource : resources){
            list.add(getComputeHostDescription((ComputeHostResource)resource));
        }
        return list;
    }

    public static Set<String> getHostAliases (List<Resource> resources){
        Set<String> hostAliases = new HashSet<String>();
        for (Resource alias : resources){
            hostAliases.add(((HostAliasResource)alias).getAlias());
        }
        return hostAliases;
    }

    public static Set<String> getIpAddresses (List<Resource> resources){
        Set<String> hostIpAddresses = new HashSet<String>();
        for (Resource resource : resources){
            hostIpAddresses.add(((HostIPAddressResource)resource).getIpaddress());
        }
        return hostIpAddresses;
    }

    public static Map<String, JobSubmissionProtocol> getJobSubmissionProtocolList(List<Resource> resources){
       Map<String, JobSubmissionProtocol> protocols = new HashMap<String, JobSubmissionProtocol>();
        for (Resource resource : resources){
            JobSubmissionProtocolResource submission = (JobSubmissionProtocolResource) resource;
            protocols.put(submission.getSubmissionID(), JobSubmissionProtocol.valueOf(submission.getJobType()));
        }
        return protocols;
    }

    public static Map<String, DataMovementProtocol> getDataMoveProtocolList(List<Resource> resources){
        Map<String, DataMovementProtocol> protocols = new HashMap<String, DataMovementProtocol>();
        for (Resource resource : resources){
            DataMovementProtocolResource protocolResource = (DataMovementProtocolResource) resource;
            protocols.put(protocolResource.getDataMoveID(), DataMovementProtocol.valueOf(protocolResource.getDataMoveType()));
        }
        return protocols;
    }


    public static GSISSHSubmissionResource getGSISSHSubmission (ComputeHostResource hostResource, GSISSHJobSubmission submission){
        GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
        resource.setComputeHostResource(hostResource);
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setMonitorMode(submission.getMonitorMode());
        resource.setInstalledPath(submission.getInstalledPath());
        resource.setResourceID(hostResource.getResoureId());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        resource.setSshPort(submission.getSshPort());
        return resource;
    }

    public static GSISSHJobSubmission getGSISSHSubmissionDescription (GSISSHSubmissionResource submission) throws AppCatalogException {
        GSISSHJobSubmission gsisshJobSubmission = new GSISSHJobSubmission();
        gsisshJobSubmission.setJobSubmissionDataID(submission.getSubmissionID());
        gsisshJobSubmission.setResourceJobManager(ResourceJobManager.valueOf(submission.getResourceJobManager()));
        gsisshJobSubmission.setSshPort(submission.getSshPort());
        gsisshJobSubmission.setInstalledPath(submission.getInstalledPath());
        gsisshJobSubmission.setMonitorMode(submission.getMonitorMode());

        GSISSHExportResource exportResource = new GSISSHExportResource();
        List<Resource> exports = exportResource.get(AbstractResource.GSISSHExportConstants.SUBMISSION_ID, submission.getSubmissionID());
        if (exports != null && !exports.isEmpty()){
            gsisshJobSubmission.setExports(getGSISSHExports(exports));
        }

        GSISSHPostJobCommandResource postJobCommandResource = new GSISSHPostJobCommandResource();
        List<Resource> resources = postJobCommandResource.get(AbstractResource.GSISSHPostJobCommandConstants.SUBMISSION_ID, submission.getSubmissionID());
        if (resources != null && !resources.isEmpty()){
            gsisshJobSubmission.setPostJobCommands(getGSISSHPostJobCommands(resources));
        }

        GSISSHPreJobCommandResource preJobCommandResource = new GSISSHPreJobCommandResource();
        List<Resource> preJobCommands = preJobCommandResource.get(AbstractResource.GSISSHPreJobCommandConstants.SUBMISSION_ID, submission.getSubmissionID());
        if (preJobCommands != null && !preJobCommands.isEmpty()){
            gsisshJobSubmission.setPreJobCommands(getGSISSHPreJobCommands(preJobCommands));
        }

        return gsisshJobSubmission;
    }

    public static GlobusJobSubmission getGlobusJobSubmissionDescription (GlobusJobSubmissionResource submission) throws AppCatalogException {
        GlobusJobSubmission globusJobSubmission = new GlobusJobSubmission();
        globusJobSubmission.setJobSubmissionDataID(submission.getSubmissionID());
        globusJobSubmission.setResourceJobManager(ResourceJobManager.valueOf(submission.getResourceJobManager()));
        globusJobSubmission.setSecurityProtocol(SecurityProtocol.valueOf(submission.getSecurityProtocol()));

        GlobusGKEndpointResource endpointResource = new GlobusGKEndpointResource();
        List<Resource> endpoints = endpointResource.get(AbstractResource.GlobusEPConstants.SUBMISSION_ID, submission.getSubmissionID());
        if (endpoints != null && !endpoints.isEmpty()){
            globusJobSubmission.setGlobusGateKeeperEndPoint(getGlobusGateKeeperEndPointList(endpoints));
        }

        return globusJobSubmission;
    }

    public static SCPDataMovement getSCPDataMovementDescription (SCPDataMovementResource dataMovementResource) throws AppCatalogException {
        SCPDataMovement dataMovement = new SCPDataMovement();
        dataMovement.setDataMovementDataID(dataMovementResource.getDataMoveID());
        dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(dataMovementResource.getSecurityProtocol()));
        dataMovement.setSshPort(dataMovementResource.getSshPort());
        return dataMovement;
    }

    public static GridFTPDataMovement getGridFTPDataMovementDescription (GridFTPDataMovementResource dataMovementResource) throws AppCatalogException {
        GridFTPDataMovement dataMovement = new GridFTPDataMovement();
        dataMovement.setDataMovementDataID(dataMovementResource.getDataMoveID());
        dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(dataMovementResource.getSecurityProtocol()));
        GridFTPDMEndpointResource endpointResource = new GridFTPDMEndpointResource();
        List<Resource> endpoints = endpointResource.get(AbstractResource.GridFTPDMEPConstants.DATA_MOVE_ID, dataMovementResource.getDataMoveID());
        if (endpoints != null && !endpoints.isEmpty()){
            dataMovement.setGridFTPEndPoint(getGridFTPDMEPList(endpoints));
        }
        return dataMovement;
    }

    public static List<String> getGridFTPDMEPList (List<Resource> endpoints){
        List<String> list = new ArrayList<String>();
        for (Resource resource : endpoints){
            list.add(((GridFTPDMEndpointResource) resource).getEndpoint());
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

    public static List<GSISSHJobSubmission> getGSISSHSubmissionList (List<Resource> resources) throws AppCatalogException {
        List<GSISSHJobSubmission> list = new ArrayList<GSISSHJobSubmission>();
        for (Resource resource : resources){
            list.add(getGSISSHSubmissionDescription((GSISSHSubmissionResource) resource));
        }
        return list;
    }

    public static List<GlobusJobSubmission> getGlobusSubmissionList (List<Resource> resources) throws AppCatalogException {
        List<GlobusJobSubmission> list = new ArrayList<GlobusJobSubmission>();
        for (Resource resource : resources){
            list.add(getGlobusJobSubmissionDescription((GlobusJobSubmissionResource) resource));
        }
        return list;
    }

    public static List<GridFTPDataMovement> getGridFTPDataMovementList (List<Resource> resources) throws AppCatalogException {
        List<GridFTPDataMovement> list = new ArrayList<GridFTPDataMovement>();
        for (Resource resource : resources){
            list.add(getGridFTPDataMovementDescription((GridFTPDataMovementResource) resource));
        }
        return list;
    }

    public static List<SCPDataMovement> getSCPDataMovementList (List<Resource> resources) throws AppCatalogException {
        List<SCPDataMovement> list = new ArrayList<SCPDataMovement>();
        for (Resource resource : resources){
            list.add(getSCPDataMovementDescription((SCPDataMovementResource) resource));
        }
        return list;
    }

    public static Set<String> getGSISSHExports (List<Resource> gsisshExportResources){
        Set<String> exports = new HashSet<String>();
        for (Resource resource : gsisshExportResources){
            exports.add(((GSISSHExportResource) resource).getExport());
        }
        return exports;
    }

    public static List<String> getGSISSHPreJobCommands (List<Resource> gsisshPreJobCommandResources){
        List<String> list = new ArrayList<String>();
        for (Resource resource : gsisshPreJobCommandResources){
            list.add(((GSISSHPreJobCommandResource) resource).getCommand());
        }
        return list;
    }

    public static List<String> getGSISSHPostJobCommands (List<Resource> gsisshPostJobCommandResources){
        List<String> list = new ArrayList<String>();
        for (Resource resource : gsisshPostJobCommandResources){
            list.add(((GSISSHPostJobCommandResource) resource).getCommand());
        }
        return list;
    }

    public static GlobusJobSubmissionResource getGlobusJobSubmission (ComputeHostResource hostResource, GlobusJobSubmission submission){
        GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
        resource.setComputeHostResource(hostResource);
        resource.setResourceID(hostResource.getResoureId());
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        return resource;
    }

    public static SSHSubmissionResource getSSHJobSubmission (ComputeHostResource hostResource, SSHJobSubmission submission){
        SSHSubmissionResource resource = new SSHSubmissionResource();
        resource.setComputeHostResource(hostResource);
        resource.setResourceID(hostResource.getResoureId());
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        return resource;
    }



}
