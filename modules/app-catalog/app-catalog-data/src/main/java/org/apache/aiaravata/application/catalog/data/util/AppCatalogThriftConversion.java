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
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayProfile;

import java.util.*;

public class AppCatalogThriftConversion {
    public static ComputeHostResource getComputeHostResource (ComputeResourceDescription description){
        ComputeHostResource resource = new ComputeHostResource();
        resource.setHostName(description.getHostName());
        resource.setDescription(description.getComputeResourceDescription());
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


    public static GSISSHSubmissionResource getGSISSHSubmission (GSISSHJobSubmission submission){
        GSISSHSubmissionResource resource = new GSISSHSubmissionResource();
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setMonitorMode(submission.getMonitorMode());
        resource.setInstalledPath(submission.getInstalledPath());
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

    public static SSHJobSubmission getSSHJobSubmissionDescription (SshJobSubmissionResource submission) throws AppCatalogException {
        SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
        sshJobSubmission.setJobSubmissionDataID(submission.getSubmissionID());
        sshJobSubmission.setResourceJobManager(ResourceJobManager.valueOf(submission.getResourceJobManager()));
        sshJobSubmission.setSshPort(submission.getSshPort());
        return sshJobSubmission;
    }

    public static SCPDataMovement getSCPDataMovementDescription (ScpDataMovementResource dataMovementResource) throws AppCatalogException {
        SCPDataMovement dataMovement = new SCPDataMovement();
        dataMovement.setDataMovementDataID(dataMovementResource.getDataMoveID());
        dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(dataMovementResource.getSecurityProtocol()));
        dataMovement.setSshPort(dataMovementResource.getSshPort());
        return dataMovement;
    }

    public static GridFTPDataMovement getGridFTPDataMovementDescription (GridftpDataMovementResource dataMovementResource) throws AppCatalogException {
        GridFTPDataMovement dataMovement = new GridFTPDataMovement();
        dataMovement.setDataMovementDataID(dataMovementResource.getDataMoveID());
        dataMovement.setSecurityProtocol(SecurityProtocol.valueOf(dataMovementResource.getSecurityProtocol()));
        GridftpEndpointResource endpointResource = new GridftpEndpointResource();
        List<Resource> endpoints = endpointResource.get(AbstractResource.GridFTPDMEPConstants.DATA_MOVE_ID, dataMovementResource.getDataMoveID());
        if (endpoints != null && !endpoints.isEmpty()){
            dataMovement.setGridFTPEndPoint(getGridFTPDMEPList(endpoints));
        }
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

    public static List<SSHJobSubmission> getSSHSubmissionList (List<Resource> resources) throws AppCatalogException {
        List<SSHJobSubmission> list = new ArrayList<SSHJobSubmission>();
        for (Resource resource : resources){
            list.add(getSSHJobSubmissionDescription((SshJobSubmissionResource) resource));
        }
        return list;
    }

    public static List<GridFTPDataMovement> getGridFTPDataMovementList (List<Resource> resources) throws AppCatalogException {
        List<GridFTPDataMovement> list = new ArrayList<GridFTPDataMovement>();
        for (Resource resource : resources){
            list.add(getGridFTPDataMovementDescription((GridftpDataMovementResource) resource));
        }
        return list;
    }

    public static List<SCPDataMovement> getSCPDataMovementList (List<Resource> resources) throws AppCatalogException {
        List<SCPDataMovement> list = new ArrayList<SCPDataMovement>();
        for (Resource resource : resources){
            list.add(getSCPDataMovementDescription((ScpDataMovementResource) resource));
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

    public static GlobusJobSubmissionResource getGlobusJobSubmission (GlobusJobSubmission submission){
        GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setSecurityProtocol(submission.getSecurityProtocol().toString());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        return resource;
    }

    public static SshJobSubmissionResource getSSHJobSubmission (SSHJobSubmission submission){
        SshJobSubmissionResource resource = new SshJobSubmissionResource();
        resource.setSubmissionID(submission.getJobSubmissionDataID());
        resource.setResourceJobManager(submission.getResourceJobManager().toString());
        return resource;
    }

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
        inputDataObjectType.setMetaData(input.getMetadata());
        inputDataObjectType.setType(DataType.valueOf(input.getDataType()));
        inputDataObjectType.setStandardInput(input.isStandareInput());
        inputDataObjectType.setUserFriendlyDescription(input.getUserFriendlyDesc());
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
        return outputDataObjectType;
    }

    public static ApplicationDeploymentDescription getApplicationDeploymentDescription (AppDeploymentResource resource) throws AppCatalogException {
        ApplicationDeploymentDescription description = new ApplicationDeploymentDescription();
        description.setAppDeploymentId(resource.getDeploymentId());
        description.setAppModuleId(resource.getAppModuleId());
        description.setComputeHostId(resource.getHostId());
        description.setExecutablePath(resource.getExecutablePath());
        description.setAppDeploymentDescription(resource.getAppDes());
        description.setModuleLoadCmd(resource.getEnvModuleLoadCMD());

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
        preference.setPreferredJobSubmissionProtocol(resource.getPreferredJobProtocol());
        preference.setPreferredDataMovementProtocol(resource.getPreferedDMProtocol());
        preference.setPreferredBatchQueue(resource.getBatchQueue());
        preference.setScratchLocation(resource.getScratchLocation());
        preference.setAllocationProjectNumber(resource.getProjectNumber());
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

    public static GatewayProfile getGatewayProfile (GatewayProfileResource gw, List<ComputeResourcePreference> preferences){
        GatewayProfile gatewayProfile = new GatewayProfile();
        gatewayProfile.setGatewayID(gw.getGatewayID());
        gatewayProfile.setGatewayDescription(gw.getGatewayDesc());
        gatewayProfile.setGatewayName(gw.getGatewayName());
        gatewayProfile.setComputeResourcePreferences(preferences);
        return gatewayProfile;
    }
}
