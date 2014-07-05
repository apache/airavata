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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.aiaravata.application.catalog.data.model.AppEnvironment;
import org.apache.aiaravata.application.catalog.data.model.AppModuleMapping;
import org.apache.aiaravata.application.catalog.data.model.ApplicationDeployment;
import org.apache.aiaravata.application.catalog.data.model.ApplicationInput;
import org.apache.aiaravata.application.catalog.data.model.ApplicationInterface;
import org.apache.aiaravata.application.catalog.data.model.ApplicationModule;
import org.apache.aiaravata.application.catalog.data.model.ApplicationOutput;
import org.apache.aiaravata.application.catalog.data.model.BatchQueue;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.ComputeResourceFileSystem;
import org.apache.aiaravata.application.catalog.data.model.ComputeResourcePreference;
import org.apache.aiaravata.application.catalog.data.model.DataMovementProtocol;
import org.apache.aiaravata.application.catalog.data.model.GSISSHExport;
import org.apache.aiaravata.application.catalog.data.model.GSISSHPostJobCommand;
import org.apache.aiaravata.application.catalog.data.model.GSISSHPreJobCommand;
import org.apache.aiaravata.application.catalog.data.model.GSISSHSubmission;
import org.apache.aiaravata.application.catalog.data.model.GatewayProfile;
import org.apache.aiaravata.application.catalog.data.model.GlobusGKEndpoint;
import org.apache.aiaravata.application.catalog.data.model.GlobusJobSubmission;
import org.apache.aiaravata.application.catalog.data.model.GridFTPDMEndpoint;
import org.apache.aiaravata.application.catalog.data.model.GridFTPDataMovement;
import org.apache.aiaravata.application.catalog.data.model.HostAlias;
import org.apache.aiaravata.application.catalog.data.model.HostIPAddress;
import org.apache.aiaravata.application.catalog.data.model.JobSubmissionInterface;
import org.apache.aiaravata.application.catalog.data.model.JobSubmissionProtocol;
import org.apache.aiaravata.application.catalog.data.model.LibraryApendPath;
import org.apache.aiaravata.application.catalog.data.model.LibraryPrepandPath;
import org.apache.aiaravata.application.catalog.data.model.SCPDataMovement;
import org.apache.aiaravata.application.catalog.data.model.SSHSubmission;
import org.apache.aiaravata.application.catalog.data.resources.AppDeploymentResource;
import org.apache.aiaravata.application.catalog.data.resources.AppEnvironmentResource;
import org.apache.aiaravata.application.catalog.data.resources.AppInterfaceResource;
import org.apache.aiaravata.application.catalog.data.resources.AppModuleMappingResource;
import org.apache.aiaravata.application.catalog.data.resources.AppModuleResource;
import org.apache.aiaravata.application.catalog.data.resources.ApplicationInputResource;
import org.apache.aiaravata.application.catalog.data.resources.ApplicationOutputResource;
import org.apache.aiaravata.application.catalog.data.resources.BatchQueueResource;
import org.apache.aiaravata.application.catalog.data.resources.ComputeHostPreferenceResource;
import org.apache.aiaravata.application.catalog.data.resources.ComputeHostResource;
import org.apache.aiaravata.application.catalog.data.resources.ComputeResourceFileSystemResource;
import org.apache.aiaravata.application.catalog.data.resources.DataMovementProtocolResource;
import org.apache.aiaravata.application.catalog.data.resources.GSISSHExportResource;
import org.apache.aiaravata.application.catalog.data.resources.GSISSHPostJobCommandResource;
import org.apache.aiaravata.application.catalog.data.resources.GSISSHPreJobCommandResource;
import org.apache.aiaravata.application.catalog.data.resources.GSISSHSubmissionResource;
import org.apache.aiaravata.application.catalog.data.resources.GatewayProfileResource;
import org.apache.aiaravata.application.catalog.data.resources.GlobusGKEndpointResource;
import org.apache.aiaravata.application.catalog.data.resources.GlobusJobSubmissionResource;
import org.apache.aiaravata.application.catalog.data.resources.GridFTPDMEndpointResource;
import org.apache.aiaravata.application.catalog.data.resources.GridFTPDataMovementResource;
import org.apache.aiaravata.application.catalog.data.resources.HostAliasResource;
import org.apache.aiaravata.application.catalog.data.resources.HostIPAddressResource;
import org.apache.aiaravata.application.catalog.data.resources.JobSubmissionInterfaceResource;
import org.apache.aiaravata.application.catalog.data.resources.JobSubmissionProtocolResource;
import org.apache.aiaravata.application.catalog.data.resources.LibraryApendPathResource;
import org.apache.aiaravata.application.catalog.data.resources.LibraryPrepandPathResource;
import org.apache.aiaravata.application.catalog.data.resources.Resource;
import org.apache.aiaravata.application.catalog.data.resources.SCPDataMovementResource;
import org.apache.aiaravata.application.catalog.data.resources.SSHSubmissionResource;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppCatalogJPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(AppCatalogJPAUtils.class);
    private static final String PERSISTENCE_UNIT_NAME = "appcatalog_data";
    private static final String APPCATALOG_JDBC_DRIVER = "appcatalog.jdbc.driver";
    private static final String APPCATALOG_JDBC_URL = "appcatalog.jdbc.url";
    private static final String APPCATALOG_JDBC_USER = "appcatalog.jdbc.user";
    private static final String APPCATALOG_JDBC_PWD = "appcatalog.jdbc.password";
    private static final String APPCATALOG_VALIDATION_QUERY = "appcatalog.validationQuery";
    private static final String JPA_CACHE_SIZE = "jpa.cache.size";
    protected static EntityManagerFactory factory;

    public static EntityManager getEntityManager() throws ApplicationSettingsException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(APPCATALOG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(APPCATALOG_JDBC_URL) + "," +
                    "Username=" + readServerProperties(APPCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(APPCATALOG_JDBC_PWD);
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            properties.put("openjpa.DataCache","true(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE))  + ", SoftReferenceSize=0)");
            properties.put("openjpa.QueryCache","true(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE))  + ", SoftReferenceSize=0)");
            properties.put("openjpa.RemoteCommitProvider","sjvm");
            properties.put("openjpa.Log","DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        return factory.createEntityManager();
    }

    private static String readServerProperties (String propertyName) throws ApplicationSettingsException {
        try {
            return ServerSettings.getSetting(propertyName);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server.properties...", e);
            throw new ApplicationSettingsException("Unable to read airavata-server.properties...");
        }
    }

    /**
     *
     * @param type model type
     * @param o model type instance
     * @return corresponding resource object
     */
    public static Resource getResource(AppCatalogResourceType type, Object o) {
        switch (type){
            case COMPUTE_RESOURCE:
                if (o instanceof ComputeResource){
                    return createComputeResource((ComputeResource) o);
                }else {
                    logger.error("Object should be a Compute Resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Compute Resource.");
                }
            case HOST_ALIAS:
                if (o instanceof HostAlias){
                    return createHostAlias((HostAlias) o);
                }else {
                    logger.error("Object should be a Host Alias.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Host Alias.");
                }
            case HOST_IPADDRESS:
                if (o instanceof HostIPAddress){
                    return createHostIPAddress((HostIPAddress) o);
                }else {
                    logger.error("Object should be a Host IPAdress.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Host IPAdress.");
                }
            case GSISSH_SUBMISSION:
                if (o instanceof GSISSHSubmission){
                    return createGSSISSHSubmission((GSISSHSubmission) o);
                }else {
                    logger.error("Object should be a GSISSH Submission", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GSISSH Submission.");
                }
            case GSISSH_EXPORT:
                if (o instanceof GSISSHExport){
                    return createGSISSHExport((GSISSHExport) o);
                }else {
                    logger.error("Object should be a GSISSH Export.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GSISSH Export.");
                }
            case GSISSH_PREJOBCOMMAND:
                if (o instanceof GSISSHPreJobCommand){
                    return createGSISSHPreJObCommand((GSISSHPreJobCommand) o);
                }else {
                    logger.error("Object should be a GSISSHPreJobCommand.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GSISSHPreJobCommand.");
                }
            case GSISSH_POSTJOBCOMMAND:
                if (o instanceof GSISSHPostJobCommand){
                    return createGSISSHPostJObCommand((GSISSHPostJobCommand) o);
                }else {
                    logger.error("Object should be a GSISSHPostJobCommand.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GSISSHPostJobCommand.");
                }
            case GLOBUS_SUBMISSION:
                if (o instanceof GlobusJobSubmission){
                    return createGlobusJobSubmission((GlobusJobSubmission) o);
                }else {
                    logger.error("Object should be a GlobusJobSubmission.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GlobusJobSubmission.");
                }
            case GLOBUS_GK_ENDPOINT:
                if (o instanceof GlobusGKEndpoint){
                    return createGlobusEndpoint((GlobusGKEndpoint) o);
                }else {
                    logger.error("Object should be a GlobusJobSubmission.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GlobusJobSubmission.");
                }
            case SSH_SUBMISSION:
                if (o instanceof SSHSubmission){
                    return createSSHSubmission((SSHSubmission) o);
                }else {
                    logger.error("Object should be a SSHSubmission.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a SSHSubmission.");
                }
            case SCP_DATAMOVEMENT:
                if (o instanceof SCPDataMovement){
                    return createSCPDataMovement((SCPDataMovement) o);
                }else {
                    logger.error("Object should be a SCPDataMovement.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a SCPDataMovement.");
                }
            case GRID_FTP_DATAMOVEMENT:
                if (o instanceof GridFTPDataMovement){
                    return createGridFTPDataMovement((GridFTPDataMovement) o);
                }else {
                    logger.error("Object should be a GridFTPDataMovement.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GridFTPDataMovement.");
                }
            case GRID_FTP_DM_ENDPOINT:
                if (o instanceof GridFTPDMEndpoint){
                    return createGridFTPDMEP((GridFTPDMEndpoint) o);
                }else {
                    logger.error("Object should be a GridFTPDataMovement.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GridFTPDataMovement.");
                }
            case JOB_SUBMISSION_PROTOCOL:
                if (o instanceof JobSubmissionProtocol){
                    return createJobSubmissionProtocol((JobSubmissionProtocol) o);
                }else {
                    logger.error("Object should be a JobSubmissionProtocol.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a JobSubmissionProtocol.");
                }
            case DATA_MOVEMENT_PROTOCOL:
                if (o instanceof DataMovementProtocol){
                    return createDataMovementProtocol((DataMovementProtocol) o);
                }else {
                    logger.error("Object should be a DataMovementProtocol.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a DataMovementProtocol.");
                }
            case APPLICATION_MODULE:
                if (o instanceof ApplicationModule){
                    return createApplicationModule((ApplicationModule) o);
                }else {
                    logger.error("Object should be a Application Module.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Application Module.");
                }
            case APPLICATION_DEPLOYMENT:
                if (o instanceof ApplicationDeployment){
                    return createApplicationDeployment((ApplicationDeployment) o);
                }else {
                    logger.error("Object should be a Application Deployment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Application Deployment.");
                }
            case LIBRARY_PREPAND_PATH:
                if (o instanceof LibraryPrepandPath){
                    return createLibraryPrepPathResource((LibraryPrepandPath) o);
                }else {
                    logger.error("Object should be a Library Prepand path.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Library Prepand path.");
                }
            case LIBRARY_APEND_PATH:
                if (o instanceof LibraryApendPath){
                    return createLibraryApendPathResource((LibraryApendPath) o);
                }else {
                    logger.error("Object should be a Library Apend path.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Library Apend.");
                }
            case APP_ENVIRONMENT:
                if (o instanceof AppEnvironment){
                    return createAppEnvironmentResource((AppEnvironment) o);
                }else {
                    logger.error("Object should be a AppEnvironment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a AppEnvironment.");
                }
            case APPLICATION_INTERFACE:
                if (o instanceof ApplicationInterface){
                    return createAppInterfaceResource((ApplicationInterface) o);
                }else {
                    logger.error("Object should be a ApplicationInterface.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ApplicationInterface.");
                }
            case APP_MODULE_MAPPING:
                if (o instanceof AppModuleMapping){
                    return createAppModMappingResource((AppModuleMapping) o);
                }else {
                    logger.error("Object should be a AppModuleMapping.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a AppModuleMapping.");
                }
            case APPLICATION_OUTPUT:
                if (o instanceof ApplicationOutput){
                    return createApplicationOutput((ApplicationOutput) o);
                }else {
                    logger.error("Object should be a ApplicationOutput.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ApplicationOutput.");
                }
            case GATEWAY_PROFILE:
                if (o instanceof GatewayProfile){
                    return createGatewayProfile((GatewayProfile) o);
                }else {
                    logger.error("Object should be a GatewayProfile.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GatewayProfile.");
                }
            case COMPUTE_RESOURCE_PREFERENCE:
                if (o instanceof ComputeResourcePreference){
                    return createComputeResourcePref((ComputeResourcePreference) o);
                }else {
                    logger.error("Object should be a Compute Resource Preference.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Compute Resource Preference.");
                }
            case APPLICATION_INPUT:
                if (o instanceof ApplicationInput){
                    return createApplicationInput((ApplicationInput) o);
                }else {
                    logger.error("Object should be a ApplicationInput.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ApplicationInput.");
                }
            case BATCH_QUEUE:
				if (o instanceof BatchQueue){
					return createBatchQueue((BatchQueue) o);
				}else{
					logger.error("Object should be a Batch Queue.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Batch Queue.");
				}
            case COMPUTE_RESOURCE_FILE_SYSTEM:
				if (o instanceof ComputeResourceFileSystem){
					return createComputeResourceFileSystem((ComputeResourceFileSystem) o);
				}else{
					logger.error("Object should be a Compute Resource File System.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Compute Resource File System.");
				}
            case JOB_SUBMISSION_INTERFACE:
				if (o instanceof JobSubmissionInterface){
					return createJobSubmissionInterface((JobSubmissionInterface) o);
				}else{
					logger.error("Object should be a Job Submission Interface.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Job Submission Interface.");
				}
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }
    
    private static Resource createJobSubmissionInterface(JobSubmissionInterface o) {
		JobSubmissionInterfaceResource jobSubmissionInterfaceResource = new JobSubmissionInterfaceResource();
		jobSubmissionInterfaceResource.setJobSubmissionInterfaceId(o.getJobSubmissionInterfaceId());
		jobSubmissionInterfaceResource.setComputeResourceId(o.getComputeResourceId());
		jobSubmissionInterfaceResource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
		jobSubmissionInterfaceResource.setJobSubmissionProtocol(o.getJobSubmissionProtocol());
		jobSubmissionInterfaceResource.setPriorityOrder(o.getPriorityOrder());
		return jobSubmissionInterfaceResource;
	}
    
    private static Resource createComputeResourceFileSystem(ComputeResourceFileSystem o) {
		ComputeResourceFileSystemResource computeResourceFileSystemResource = new ComputeResourceFileSystemResource();
		computeResourceFileSystemResource.setComputeResourceId(o.getComputeResourceId());
		computeResourceFileSystemResource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
		computeResourceFileSystemResource.setPath(o.getPath());
		computeResourceFileSystemResource.setFileSystem(o.getFileSystem());
		return computeResourceFileSystemResource;
	}
    
    private static Resource createBatchQueue(BatchQueue o) {
		BatchQueueResource batchQueueResource = new BatchQueueResource();
		batchQueueResource.setComputeResourceId(o.getComputeResourceId());
		batchQueueResource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
		batchQueueResource.setMaxRuntime(o.getMaxRuntime());
		batchQueueResource.setMaxJobInQueue(o.getMaxJobInQueue());
		batchQueueResource.setQueueDescription(o.getQueueDescription());
		batchQueueResource.setQueueName(o.getQueueName());
		batchQueueResource.setMaxProcessors(o.getMaxProcessors());
		batchQueueResource.setMaxNodes(o.getMaxNodes());
		return batchQueueResource;
	}
    private static Resource createComputeResource(ComputeResource o) {
        ComputeHostResource hostResource = new ComputeHostResource();
        hostResource.setResoureId(o.getResourceID());
        hostResource.setHostName(o.getHostName());
        hostResource.setDescription(o.getDescription());
        hostResource.setPreferredJobSubmissionProtocol(o.getPreferredJobSubProtocol());
        return hostResource;
    }

    private static Resource createHostAlias(HostAlias o) {
        HostAliasResource aliasResource = new HostAliasResource();
        aliasResource.setResourceID(o.getResourceID());
        aliasResource.setAlias(o.getAlias());
        aliasResource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
        return aliasResource;
    }

    private static Resource createHostIPAddress(HostIPAddress o) {
        HostIPAddressResource ipAddressResource = new HostIPAddressResource();
        ipAddressResource.setResourceID(o.getResourceID());
        ipAddressResource.setIpaddress(o.getIpaddress());
        ipAddressResource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
        return ipAddressResource;
    }

    private static Resource createGSSISSHSubmission(GSISSHSubmission o) {
        GSISSHSubmissionResource submissionResource = new GSISSHSubmissionResource();
        submissionResource.setSubmissionID(o.getSubmissionID());
        submissionResource.setResourceJobManager(o.getResourceJobManager());
        submissionResource.setSshPort(o.getSshPort());
        submissionResource.setInstalledPath(o.getInstalledPath());
        submissionResource.setMonitorMode(o.getMonitorMode());
        return submissionResource;
    }

    private static Resource createGSISSHExport(GSISSHExport o){
        GSISSHExportResource resource = new GSISSHExportResource();
        resource.setSubmissionID(o.getSubmissionID());
        resource.setExport(o.getExport());
        resource.setGsisshSubmissionResource((GSISSHSubmissionResource)createGSSISSHSubmission(o.getGsisshJobSubmission()));
        return resource;
    }

    private static Resource createGSISSHPreJObCommand(GSISSHPreJobCommand o){
        GSISSHPreJobCommandResource resource = new GSISSHPreJobCommandResource();
        resource.setSubmissionID(o.getSubmissionID());
        resource.setCommand(o.getCommand());
        resource.setGsisshSubmissionResource((GSISSHSubmissionResource)createGSSISSHSubmission(o.getGsisshSubmission()));
        return resource;
    }

    private static Resource createGSISSHPostJObCommand(GSISSHPostJobCommand o){
        GSISSHPostJobCommandResource resource = new GSISSHPostJobCommandResource();
        resource.setSubmissionID(o.getSubmissionID());
        resource.setCommand(o.getCommand());
        resource.setGsisshSubmissionResource((GSISSHSubmissionResource)createGSSISSHSubmission(o.getGsisshSubmission()));
        return resource;
    }

    private static Resource createGlobusJobSubmission(GlobusJobSubmission o) {
        GlobusJobSubmissionResource resource = new GlobusJobSubmissionResource();
        resource.setSubmissionID(o.getSubmissionID());
        resource.setResourceJobManager(o.getResourceJobManager());
        resource.setSecurityProtocol(o.getSecurityProtocol());
        return resource;
    }

    private static Resource createGlobusEndpoint(GlobusGKEndpoint o) {
        GlobusGKEndpointResource resource = new GlobusGKEndpointResource();
        resource.setSubmissionID(o.getSubmissionID());
        resource.setEndpoint(o.getEndpoint());
        resource.setGlobusJobSubmissionResource((GlobusJobSubmissionResource)createGlobusJobSubmission(o.getGlobusSubmission()));
        return resource;
    }

    private static Resource createSSHSubmission(SSHSubmission o) {
        SSHSubmissionResource resource = new SSHSubmissionResource();
        resource.setSubmissionID(o.getSubmissionID());
        resource.setResourceJobManager(o.getResourceJobManager());
        resource.setSshPort(o.getSshPort());
        return resource;
    }

    private static Resource createSCPDataMovement(SCPDataMovement o) {
        SCPDataMovementResource resource = new SCPDataMovementResource();
        resource.setDataMoveID(o.getDataMoveID());
        resource.setSecurityProtocol(o.getSecurityProtocol());
        resource.setSshPort(o.getSshPort());
        return resource;
    }

    private static Resource createGridFTPDataMovement(GridFTPDataMovement o) {
        GridFTPDataMovementResource resource = new GridFTPDataMovementResource();
        resource.setDataMoveID(o.getDataMoveID());
        resource.setSecurityProtocol(o.getSecurityProtocol());
        resource.setGridFTPEP(o.getGridFTPEP());
        return resource;
    }

    private static Resource createGridFTPDMEP(GridFTPDMEndpoint o) {
        GridFTPDMEndpointResource resource = new GridFTPDMEndpointResource();
        resource.setDataMoveId(o.getDataMoveId());
        resource.setEndpoint(o.getEndpoint());
        resource.setGridFTPDataMovementResource((GridFTPDataMovementResource) createGridFTPDataMovement(o.getGridFTPDataMovement()));
        return resource;
    }

    private static Resource createJobSubmissionProtocol(JobSubmissionProtocol o) {
        JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
        resource.setResourceID(o.getResourceID());
        resource.setSubmissionID(o.getSubmissionID());
        resource.setJobType(o.getJobType());
        resource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
        return resource;
    }

    private static Resource createDataMovementProtocol(DataMovementProtocol o) {
        DataMovementProtocolResource resource = new DataMovementProtocolResource();
        resource.setResourceID(o.getResourceID());
        resource.setDataMoveID(o.getDataMoveID());
        resource.setDataMoveType(o.getDataMoveType());
        resource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
        return resource;
    }

    private static Resource createApplicationModule(ApplicationModule o) {
        AppModuleResource moduleResource = new AppModuleResource();
        moduleResource.setModuleId(o.getModuleID());
        moduleResource.setModuleDesc(o.getModuleDesc());
        moduleResource.setModuleName(o.getModuleName());
        moduleResource.setModuleVersion(o.getModuleVersion());
        return moduleResource;
    }

    private static Resource createApplicationDeployment(ApplicationDeployment o) {
        AppDeploymentResource resource = new AppDeploymentResource();
        resource.setDeploymentId(o.getDeploymentID());
        resource.setAppDes(o.getApplicationDesc());
        resource.setAppModuleId(o.getAppModuleID());
        resource.setEnvModuleLoadCMD(o.getEnvModuleLoaString());
        resource.setHostId(o.getHostID());
        resource.setExecutablePath(o.getExecutablePath());
        resource.setModuleResource((AppModuleResource) createApplicationModule(o.getApplicationModule()));
        resource.setHostResource((ComputeHostResource) createComputeResource(o.getComputeResource()));
        return resource;
    }

    private static Resource createLibraryPrepPathResource(LibraryPrepandPath o) {
        LibraryPrepandPathResource resource = new LibraryPrepandPathResource();
        resource.setDeploymentId(o.getDeploymentID());
        resource.setName(o.getName());
        resource.setValue(o.getValue());
        resource.setAppDeploymentResource((AppDeploymentResource) createApplicationDeployment(o.getApplicationDeployment()));
        return resource;
    }

    private static Resource createLibraryApendPathResource(LibraryApendPath o) {
        LibraryApendPathResource resource = new LibraryApendPathResource();
        resource.setDeploymentId(o.getDeploymentID());
        resource.setName(o.getName());
        resource.setValue(o.getValue());
        resource.setAppDeploymentResource((AppDeploymentResource)createApplicationDeployment(o.getApplicationDeployment()));
        return resource;
    }

    private static Resource createAppEnvironmentResource(AppEnvironment o) {
        AppEnvironmentResource resource = new AppEnvironmentResource();
        resource.setDeploymentId(o.getDeploymentID());
        resource.setName(o.getName());
        resource.setValue(o.getValue());
        resource.setAppDeploymentResource((AppDeploymentResource)createApplicationDeployment(o.getApplicationDeployment()));
        return resource;
    }

    private static Resource createAppInterfaceResource(ApplicationInterface o) {
        AppInterfaceResource resource = new AppInterfaceResource();
        resource.setInterfaceId(o.getInterfaceID());
        resource.setAppName(o.getAppName());
        return resource;
    }

    private static Resource createAppModMappingResource(AppModuleMapping o) {
        AppModuleMappingResource resource = new AppModuleMappingResource();
        resource.setInterfaceId(o.getInterfaceID());
        resource.setModuleId(o.getModuleID());
        return resource;
    }

    private static Resource createApplicationInput(ApplicationInput o) {
        ApplicationInputResource resource = new ApplicationInputResource();
        resource.setInterfaceID(o.getInterfaceID());
        resource.setInputKey(o.getInputKey());
        resource.setInputVal(o.getInputVal());
        resource.setDataType(o.getDataType());
        resource.setMetadata(o.getMetadata());
        resource.setAppArgument(o.getAppArgument());
        resource.setUserFriendlyDesc(o.getUserFriendlyDesc());
        resource.setStandareInput(o.isStandardInput());
        resource.setAppInterfaceResource((AppInterfaceResource)createAppInterfaceResource(o.getApplicationInterface()));
        return resource;
    }

    private static Resource createApplicationOutput(ApplicationOutput o) {
        ApplicationOutputResource resource = new ApplicationOutputResource();
        resource.setInterfaceID(o.getInterfaceID());
        resource.setOutputKey(o.getOutputKey());
        resource.setOutputVal(o.getOutputVal());
        resource.setDataType(o.getDataType());
        resource.setAppInterfaceResource((AppInterfaceResource)createAppInterfaceResource(o.getApplicationInterface()));
        return resource;
    }

    private static Resource createGatewayProfile(GatewayProfile o) {
        GatewayProfileResource resource = new GatewayProfileResource();
        resource.setGatewayID(o.getGatewayID());
        resource.setGatewayName(o.getGatewayName());
        resource.setGatewayDesc(o.getGatewayDesc());
        return resource;
    }

    private static Resource createComputeResourcePref(ComputeResourcePreference o) {
        ComputeHostPreferenceResource resource = new ComputeHostPreferenceResource();
        resource.setGatewayId(o.getGatewayId());
        resource.setResourceId(o.getResourceId());
        resource.setOverrideByAiravata(o.isOverrideByAiravata());
        resource.setPreferredJobProtocol(o.getPreferedJobSubmissionProtocol());
        resource.setPreferedDMProtocol(o.getPreferedDataMoveProtocol());
        resource.setBatchQueue(o.getBatchQueue());
        resource.setScratchLocation(o.getScratchLocation());
        resource.setProjectNumber(o.getProjectNumber());
        resource.setComputeHostResource((ComputeHostResource)createComputeResource(o.getComputeHostResource()));
        resource.setGatewayProfile((GatewayProfileResource)createGatewayProfile(o.getGatewayProfile()));
        return resource;
    }
}
