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

package org.apache.airavata.registry.core.app.catalog.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.app.catalog.model.*;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

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
                    "Url=" + readServerProperties(APPCATALOG_JDBC_URL) + "?autoReconnect=true," +
                    "Username=" + readServerProperties(APPCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(APPCATALOG_JDBC_PWD) +
                    ",validationQuery=" + readServerProperties(APPCATALOG_VALIDATION_QUERY);
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("persistenceXmlLocation", "META-INF/persistence.xml");
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
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
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
    public static AppCatalogResource getResource(AppCatalogResourceType type, Object o) {
        switch (type){
	        case COMPUTE_RESOURCE:
				if (o instanceof ComputeResource){
					return createComputeResource((ComputeResource) o);
				}else{
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
            case UNICORE_JOB_SUBMISSION:
                if (o instanceof UnicoreJobSubmission){
                    return createUnicoreJobSubmission((UnicoreJobSubmission) o);
                }else {
                    logger.error("Object should be a GSISSH Submission", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GSISSH Submission.");
                }
            case UNICORE_DATA_MOVEMENT:
                if (o instanceof UnicoreDataMovement){
                    return createUnicoreDataMovement((UnicoreDataMovement) o);
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
            case PRE_JOBCOMMAND:
                if (o instanceof PreJobCommand){
                    return createPreJobCommand((PreJobCommand) o);
                }else {
                    logger.error("Object should be a GSISSHPreJobCommand.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a GSISSHPreJobCommand.");
                }
            case POST_JOBCOMMAND:
                if (o instanceof PostJobCommand){
                    return createPostJObCommand((PostJobCommand) o);
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
            case SSH_JOB_SUBMISSION:
				if (o instanceof SshJobSubmission){
					return createSshJobSubmission((SshJobSubmission) o);
				}else{
					logger.error("Object should be a Ssh Job Submission.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Ssh Job Submission.");
				}
            case SCP_DATA_MOVEMENT:
				if (o instanceof ScpDataMovement){
					return createScpDataMovement((ScpDataMovement) o);
				}else{
					logger.error("Object should be a Scp Data Movement.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Scp Data Movement.");
				}
            case GRIDFTP_DATA_MOVEMENT:
				if (o instanceof GridftpDataMovement){
					return createGridftpDataMovement((GridftpDataMovement) o);
				}else{
					logger.error("Object should be a Gridftp Data Movement.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Gridftp Data Movement.");
				}
            case GRIDFTP_ENDPOINT:
				if (o instanceof GridftpEndpoint){
					return createGridftpEndpoint((GridftpEndpoint) o);
				}else{
					logger.error("Object should be a Gridftp Endpoint.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Gridftp Endpoint.");
				}
//            case JOB_SUBMISSION_PROTOCOL:
//                if (o instanceof JobSubmissionProtocol){
//                    return createJobSubmissionProtocol((JobSubmissionProtocol) o);
//                }else {
//                    logger.error("Object should be a JobSubmissionProtocol.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Object should be a JobSubmissionProtocol.");
//                }
//            case DATA_MOVEMENT_PROTOCOL:
//                if (o instanceof DataMovementProtocol){
//                    return createDataMovementProtocol((DataMovementProtocol) o);
//                }else {
//                    logger.error("Object should be a DataMovementProtocol.", new IllegalArgumentException());
//                    throw new IllegalArgumentException("Object should be a DataMovementProtocol.");
//                }
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
            case DATA_MOVEMENT_INTERFACE:
				if (o instanceof DataMovementInterface){
					return createDataMovementInterface((DataMovementInterface) o);
				}else{
					logger.error("Object should be a Data Movement Interface.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Data Movement Interface.");
				}
            case RESOURCE_JOB_MANAGER:
				if (o instanceof ResourceJobManager){
					return createResourceJobManager((ResourceJobManager) o);
				}else{
					logger.error("Object should be a Resource Job Manager.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Resource Job Manager.");
				}
            case JOB_MANAGER_COMMAND:
				if (o instanceof JobManagerCommand){
					return createJobManagerCommand((JobManagerCommand) o);
				}else{
					logger.error("Object should be a Job Manager Command.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Job Manager Command.");
				}
			case LOCAL_SUBMISSION:
				if (o instanceof LocalSubmission){
					return createLocalSubmission((LocalSubmission) o);
				}else{
					logger.error("Object should be a Local Submission.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Local Submission.");
				}
			case LOCAL_DATA_MOVEMENT:
				if (o instanceof LocalDataMovement){
					return createLocalDataMovement((LocalDataMovement) o);
				}else{
					logger.error("Object should be a Local Data Movement.", new IllegalArgumentException());
					throw new IllegalArgumentException("Object should be a Local Data Movement.");
				}
            case MODULE_LOAD_CMD:
                if (o instanceof ModuleLoadCmd) {
                    return createModuleLoadCmd((ModuleLoadCmd) o);
                } else {
                    logger.error("Object should be a Module Load Cmd.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Module Load Cmd.");
                }
            case WORKFLOW:
                if (o instanceof Workflow) {
                    return createWorkflow((Workflow) o);
                } else {
                    logger.error("Object should be a Workflow.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow.");
                }
            case WORKFLOW_INPUT:
                if (o instanceof WorkflowInput){
                    return createWorflowInput((WorkflowInput) o);
                }else {
                    logger.error("Object should be a Workflow Input.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow Input.");
                }
            case WORKFLOW_OUTPUT:
                if (o instanceof WorkflowOutput){
                    return createWorkflowOutput((WorkflowOutput) o);
                }else {
                    logger.error("Object should be a Workflow Output.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow Output.");
                }
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }
	
	private static AppCatalogResource createLocalDataMovement(LocalDataMovement o) {
		LocalDataMovementAppCatalogResourceAppCat localDataMovementResource = new LocalDataMovementAppCatalogResourceAppCat();
        if (o != null){
            localDataMovementResource.setDataMovementInterfaceId(o.getDataMovementInterfaceId());
        }
		return localDataMovementResource;
	}
	
    private static AppCatalogResource createLocalSubmission(LocalSubmission o) {
		LocalSubmissionAppCatalogResourceAppCat localSubmissionResource = new LocalSubmissionAppCatalogResourceAppCat();
        if (o != null){
            localSubmissionResource.setResourceJobManagerId(o.getResourceJobManagerId());
            localSubmissionResource.setResourceJobManagerResource((ResourceJobManagerAppCatalogResourceAppCat)createResourceJobManager(o.getResourceJobManager()));
            localSubmissionResource.setJobSubmissionInterfaceId(o.getJobSubmissionInterfaceId());
            localSubmissionResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                localSubmissionResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return localSubmissionResource;
	}
    
    private static AppCatalogResource createJobManagerCommand(JobManagerCommand o) {
		JobManagerCommandAppCatalogResourceAppCat jobManagerCommandResource = new JobManagerCommandAppCatalogResourceAppCat();
        if (o != null){
            jobManagerCommandResource.setResourceJobManagerId(o.getResourceJobManagerId());
            jobManagerCommandResource.setResourceJobManagerResource((ResourceJobManagerAppCatalogResourceAppCat)createResourceJobManager(o.getResourceJobManager()));
            jobManagerCommandResource.setCommandType(o.getCommandType());
            jobManagerCommandResource.setCommand(o.getCommand());
        }
		return jobManagerCommandResource;
	}
    
    private static AppCatalogResource createResourceJobManager(ResourceJobManager o) {
		ResourceJobManagerAppCatalogResourceAppCat resourceJobManagerResource = new ResourceJobManagerAppCatalogResourceAppCat();
        if (o != null) {
            resourceJobManagerResource.setResourceJobManagerId(o.getResourceJobManagerId());
            resourceJobManagerResource.setPushMonitoringEndpoint(o.getPushMonitoringEndpoint());
            resourceJobManagerResource.setJobManagerBinPath(o.getJobManagerBinPath());
            resourceJobManagerResource.setResourceJobManagerType(o.getResourceJobManagerType());
            resourceJobManagerResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                resourceJobManagerResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return resourceJobManagerResource;
	}
    
    private static AppCatalogResource createDataMovementInterface(DataMovementInterface o) {
		DataMovementInterfaceAppCatalogResourceAppCat dataMovementInterfaceResource = new DataMovementInterfaceAppCatalogResourceAppCat();
        if (o != null) {
            dataMovementInterfaceResource.setComputeResourceId(o.getComputeResourceId());
            dataMovementInterfaceResource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat)createComputeResource(o.getComputeResource()));
            dataMovementInterfaceResource.setDataMovementProtocol(o.getDataMovementProtocol());
            dataMovementInterfaceResource.setDataMovementInterfaceId(o.getDataMovementInterfaceId());
            dataMovementInterfaceResource.setPriorityOrder(o.getPriorityOrder());
            dataMovementInterfaceResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                dataMovementInterfaceResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return dataMovementInterfaceResource;
	}
    
    private static AppCatalogResource createJobSubmissionInterface(JobSubmissionInterface o) {
		JobSubmissionInterfaceAppCatalogResourceAppCat jobSubmissionInterfaceResource = new JobSubmissionInterfaceAppCatalogResourceAppCat();
        if (o != null) {
            jobSubmissionInterfaceResource.setJobSubmissionInterfaceId(o.getJobSubmissionInterfaceId());
            jobSubmissionInterfaceResource.setComputeResourceId(o.getComputeResourceId());
            jobSubmissionInterfaceResource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat)createComputeResource(o.getComputeResource()));
            jobSubmissionInterfaceResource.setJobSubmissionProtocol(o.getJobSubmissionProtocol());
            jobSubmissionInterfaceResource.setPriorityOrder(o.getPriorityOrder());
            jobSubmissionInterfaceResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                jobSubmissionInterfaceResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return jobSubmissionInterfaceResource;
	}
    
    private static AppCatalogResource createComputeResourceFileSystem(ComputeResourceFileSystem o) {
		ComputeResourceFileSystemAppCatalogResourceAppCat computeResourceFileSystemResource = new ComputeResourceFileSystemAppCatalogResourceAppCat();
        if (o != null){
            computeResourceFileSystemResource.setComputeResourceId(o.getComputeResourceId());
            computeResourceFileSystemResource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat)createComputeResource(o.getComputeResource()));
            computeResourceFileSystemResource.setPath(o.getPath());
            computeResourceFileSystemResource.setFileSystem(o.getFileSystem());
        }
		return computeResourceFileSystemResource;
	}
    
    private static AppCatalogResource createBatchQueue(BatchQueue o) {
		BatchQueueAppCatalogResourceAppCat batchQueueResource = new BatchQueueAppCatalogResourceAppCat();
        if (o != null){
            batchQueueResource.setComputeResourceId(o.getComputeResourceId());
            batchQueueResource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat)createComputeResource(o.getComputeResource()));
            batchQueueResource.setMaxRuntime(o.getMaxRuntime());
            batchQueueResource.setMaxJobInQueue(o.getMaxJobInQueue());
            batchQueueResource.setQueueDescription(o.getQueueDescription());
            batchQueueResource.setQueueName(o.getQueueName());
            batchQueueResource.setMaxProcessors(o.getMaxProcessors());
            batchQueueResource.setMaxNodes(o.getMaxNodes());
            batchQueueResource.setMaxMemory(o.getMaxMemory());
        }
		return batchQueueResource;
	}
    private static AppCatalogResource createComputeResource(ComputeResource o) {
		ComputeResourceAppCatalogResourceAppCat computeResourceResource = new ComputeResourceAppCatalogResourceAppCat();
        if (o != null) {
            computeResourceResource.setResourceDescription(o.getResourceDescription());
            computeResourceResource.setResourceId(o.getResourceId());
            computeResourceResource.setHostName(o.getHostName());
            computeResourceResource.setCreatedTime(o.getCreationTime());
            computeResourceResource.setMaxMemoryPerNode(o.getMaxMemoryPerNode());
            if (o.getUpdateTime() != null){
                computeResourceResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return computeResourceResource;
	}

    private static AppCatalogResource createHostAlias(HostAlias o) {
        HostAliasAppCatalogResourceAppCat aliasResource = new HostAliasAppCatalogResourceAppCat();
        if (o != null){
            aliasResource.setResourceID(o.getResourceID());
            aliasResource.setAlias(o.getAlias());
            aliasResource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat)createComputeResource(o.getComputeResource()));
        }
        return aliasResource;
    }

    private static AppCatalogResource createHostIPAddress(HostIPAddress o) {
        HostIPAddressAppCatalogResourceAppCat ipAddressResource = new HostIPAddressAppCatalogResourceAppCat();
        if (o != null){
            ipAddressResource.setResourceID(o.getResourceID());
            ipAddressResource.setIpaddress(o.getIpaddress());
            ipAddressResource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat)createComputeResource(o.getComputeResource()));
        }
        return ipAddressResource;
    }

    private static AppCatalogResource createGSSISSHSubmission(GSISSHSubmission o) {
        GSISSHSubmissionAppCatalogResourceAppCat submissionResource = new GSISSHSubmissionAppCatalogResourceAppCat();
        if (o != null) {
            submissionResource.setSubmissionID(o.getSubmissionID());
            submissionResource.setResourceJobManager(o.getResourceJobManager());
            submissionResource.setSshPort(o.getSshPort());
            submissionResource.setInstalledPath(o.getInstalledPath());
            submissionResource.setMonitorMode(o.getMonitorMode());
        }
        return submissionResource;
    }
    
    
    private static AppCatalogResource createUnicoreJobSubmission(UnicoreJobSubmission o) {
        UnicoreJobSubmissionAppCatalogResourceAppCat submissionResource = new UnicoreJobSubmissionAppCatalogResourceAppCat();
        if (o != null) {
            submissionResource.setjobSubmissionInterfaceId(o.getSubmissionID());
            submissionResource.setUnicoreEndpointUrl(o.getUnicoreEndpointUrl());
            submissionResource.setSecurityProtocol(o.getSecurityProtocol());
        }
        return submissionResource;
    }

    private static AppCatalogResource createUnicoreDataMovement(UnicoreDataMovement o) {
        UnicoreDataMovementAppCatalogResourceAppCat dataMovementResource = new UnicoreDataMovementAppCatalogResourceAppCat();
        if (o != null) {
            dataMovementResource.setDataMovementId(o.getDataMovementId());
            dataMovementResource.setUnicoreEndpointUrl(o.getUnicoreEndpointUrl());
            dataMovementResource.setSecurityProtocol(o.getSecurityProtocol());
        }
        return dataMovementResource;
    }
    
    private static AppCatalogResource createGSISSHExport(GSISSHExport o){
        GSISSHExportAppCatalogResourceAppCat resource = new GSISSHExportAppCatalogResourceAppCat();
        if (o != null) {
            resource.setSubmissionID(o.getSubmissionID());
            resource.setExport(o.getExport());
            resource.setGsisshSubmissionResource((GSISSHSubmissionAppCatalogResourceAppCat)createGSSISSHSubmission(o.getGsisshJobSubmission()));
        }
        return resource;
    }

    private static AppCatalogResource createPreJobCommand(PreJobCommand o){
        PreJobCommandAppCatalogResourceAppCat resource = new PreJobCommandAppCatalogResourceAppCat();
        if (o != null) {
            resource.setAppDeploymentId(o.getDeploymentId());
            resource.setCommand(o.getCommand());
            resource.setAppDeploymentResource((AppDeploymentAppCatalogResourceAppCat) createApplicationDeployment(o.getApplicationDeployment()));
        }
        return resource;
    }

    private static AppCatalogResource createPostJObCommand(PostJobCommand o){
        PostJobCommandAppCatalogResourceAppCat resource = new PostJobCommandAppCatalogResourceAppCat();
        if (o != null){
            resource.setAppDeploymentId(o.getDeploymentId());
            resource.setCommand(o.getCommand());
            resource.setAppDeploymentResource((AppDeploymentAppCatalogResourceAppCat) createApplicationDeployment(o.getDeployment()));
        }
        return resource;
    }

    private static AppCatalogResource createGlobusJobSubmission(GlobusJobSubmission o) {
        GlobusJobSubmissionAppCatalogResourceAppCat resource = new GlobusJobSubmissionAppCatalogResourceAppCat();
        if (o != null){
            resource.setSubmissionID(o.getSubmissionID());
            resource.setResourceJobManager(o.getResourceJobManager());
            resource.setSecurityProtocol(o.getSecurityProtocol());
        }
        return resource;
    }

    private static AppCatalogResource createGlobusEndpoint(GlobusGKEndpoint o) {
        GlobusGKEndpointAppCatalogResourceAppCat resource = new GlobusGKEndpointAppCatalogResourceAppCat();
        if (o != null){
            resource.setSubmissionID(o.getSubmissionID());
            resource.setEndpoint(o.getEndpoint());
            resource.setGlobusJobSubmissionResource((GlobusJobSubmissionAppCatalogResourceAppCat)createGlobusJobSubmission(o.getGlobusSubmission()));
        }
        return resource;
    }
	
	private static AppCatalogResource createSshJobSubmission(SshJobSubmission o) {
        SshJobSubmissionAppCatalogResourceAppCat sshJobSubmissionResource = new SshJobSubmissionAppCatalogResourceAppCat();
        if (o != null) {
            sshJobSubmissionResource.setResourceJobManagerId(o.getResourceJobManagerId());
            sshJobSubmissionResource.setResourceJobManagerResource((ResourceJobManagerAppCatalogResourceAppCat) createResourceJobManager(o.getResourceJobManager()));
            sshJobSubmissionResource.setJobSubmissionInterfaceId(o.getJobSubmissionInterfaceId());
            sshJobSubmissionResource.setAlternativeSshHostname(o.getAlternativeSshHostname());
            sshJobSubmissionResource.setSecurityProtocol(o.getSecurityProtocol());
            sshJobSubmissionResource.setSshPort(o.getSshPort());
            sshJobSubmissionResource.setMonitorMode(o.getMonitorMode());
            sshJobSubmissionResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                sshJobSubmissionResource.setUpdatedTime(o.getUpdateTime());
            }
        }
        return sshJobSubmissionResource;
    }

    private static AppCatalogResource createScpDataMovement(ScpDataMovement o) {
		ScpDataMovementAppCatalogResourceAppCat scpDataMovementResource = new ScpDataMovementAppCatalogResourceAppCat();
        if (o != null){
            scpDataMovementResource.setQueueDescription(o.getQueueDescription());
            scpDataMovementResource.setDataMovementInterfaceId(o.getDataMovementInterfaceId());
            scpDataMovementResource.setSecurityProtocol(o.getSecurityProtocol());
            scpDataMovementResource.setAlternativeScpHostname(o.getAlternativeScpHostname());
            scpDataMovementResource.setSshPort(o.getSshPort());
            scpDataMovementResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                scpDataMovementResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return scpDataMovementResource;
	}

    private static AppCatalogResource createGridftpDataMovement(GridftpDataMovement o) {
		GridftpDataMovementAppCatalogResourceAppCat gridftpDataMovementResource = new GridftpDataMovementAppCatalogResourceAppCat();
        if (o != null){
            gridftpDataMovementResource.setDataMovementInterfaceId(o.getDataMovementInterfaceId());
            gridftpDataMovementResource.setSecurityProtocol(o.getSecurityProtocol());
            gridftpDataMovementResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                gridftpDataMovementResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return gridftpDataMovementResource;
	}

    private static AppCatalogResource createGridftpEndpoint(GridftpEndpoint o) {
		GridftpEndpointAppCatalogResourceAppCat gridftpEndpointResource = new GridftpEndpointAppCatalogResourceAppCat();
        if (o != null){
            gridftpEndpointResource.setEndpoint(o.getEndpoint());
            gridftpEndpointResource.setDataMovementInterfaceId(o.getDataMovementInterfaceId());
            gridftpEndpointResource.setGridftpDataMovementResource((GridftpDataMovementAppCatalogResourceAppCat)createGridftpDataMovement(o.getGridftpDataMovement()));
            gridftpEndpointResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                gridftpEndpointResource.setUpdatedTime(o.getUpdateTime());
            }
        }
		return gridftpEndpointResource;
	}

//    private static Resource createJobSubmissionProtocol(JobSubmissionProtocol o) {
//        JobSubmissionProtocolResource resource = new JobSubmissionProtocolResource();
//        if (o != null){
//            resource.setResourceID(o.getResourceID());
//            resource.setSubmissionID(o.getSubmissionID());
//            resource.setJobType(o.getJobType());
//            resource.setComputeHostResource((ComputeResourceResource)createComputeResource(o.getComputeResource()));
//        }
//        return resource;
//    }
//
//    private static Resource createDataMovementProtocol(DataMovementProtocol o) {
//        DataMovementProtocolResource resource = new DataMovementProtocolResource();
//        if (o != null) {
//            resource.setResourceID(o.getResourceID());
//            resource.setDataMoveID(o.getDataMoveID());
//            resource.setDataMoveType(o.getDataMoveType());
//            resource.setComputeHostResource((ComputeResourceResource)createComputeResource(o.getComputeResource()));
//        }
//        return resource;
//    }

    private static AppCatalogResource createApplicationModule(ApplicationModule o) {
        AppModuleAppCatalogResourceAppCat moduleResource = new AppModuleAppCatalogResourceAppCat();
        if (o != null){
            moduleResource.setModuleId(o.getModuleID());
            moduleResource.setModuleDesc(o.getModuleDesc());
            moduleResource.setGatewayId(o.getGatewayId());
            moduleResource.setModuleName(o.getModuleName());
            moduleResource.setModuleVersion(o.getModuleVersion());
            moduleResource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                moduleResource.setUpdatedTime(o.getUpdateTime());
            }
        }
        return moduleResource;
    }

    private static AppCatalogResource createApplicationDeployment(ApplicationDeployment o) {
        AppDeploymentAppCatalogResourceAppCat resource = new AppDeploymentAppCatalogResourceAppCat();
        if (o != null){
            resource.setDeploymentId(o.getDeploymentID());
            resource.setAppDes(o.getApplicationDesc());
            resource.setAppModuleId(o.getAppModuleID());
            resource.setHostId(o.getHostID());
            resource.setExecutablePath(o.getExecutablePath());
            resource.setGatewayId(o.getGatewayId());
            resource.setParallelism(o.getParallelism());
            resource.setModuleResource((AppModuleAppCatalogResourceAppCat) createApplicationModule(o.getApplicationModule()));
            resource.setHostResource((ComputeResourceAppCatalogResourceAppCat) createComputeResource(o.getComputeResource()));
            resource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                resource.setUpdatedTime(o.getUpdateTime());
            }
        }
        return resource;
    }

    private static AppCatalogResource createLibraryPrepPathResource(LibraryPrepandPath o) {
        LibraryPrepandPathAppCatalogResourceAppCat resource = new LibraryPrepandPathAppCatalogResourceAppCat();
        if (o != null){
            resource.setDeploymentId(o.getDeploymentID());
            resource.setName(o.getName());
            resource.setValue(o.getValue());
            resource.setAppDeploymentResource((AppDeploymentAppCatalogResourceAppCat) createApplicationDeployment(o.getApplicationDeployment()));
        }
        return resource;
    }

    private static AppCatalogResource createLibraryApendPathResource(LibraryApendPath o) {
        LibraryApendPathAppCatalogResourceAppCat resource = new LibraryApendPathAppCatalogResourceAppCat();
        if (o != null){
            resource.setDeploymentId(o.getDeploymentID());
            resource.setName(o.getName());
            resource.setValue(o.getValue());
            resource.setAppDeploymentResource((AppDeploymentAppCatalogResourceAppCat)createApplicationDeployment(o.getApplicationDeployment()));
        }
        return resource;
    }

    private static AppCatalogResource createAppEnvironmentResource(AppEnvironment o) {
        AppEnvironmentAppCatalogResourceAppCat resource = new AppEnvironmentAppCatalogResourceAppCat();
        if (o != null){
            resource.setDeploymentId(o.getDeploymentID());
            resource.setName(o.getName());
            resource.setValue(o.getValue());
            resource.setAppDeploymentResource((AppDeploymentAppCatalogResourceAppCat)createApplicationDeployment(o.getApplicationDeployment()));
        }
        return resource;
    }

    private static AppCatalogResource createAppInterfaceResource(ApplicationInterface o) {
        AppInterfaceAppCatalogResourceAppCat resource = new AppInterfaceAppCatalogResourceAppCat();
        if (o != null){
            resource.setInterfaceId(o.getInterfaceID());
            resource.setAppName(o.getAppName());
            resource.setAppDescription(o.getAppDescription());
            resource.setCreatedTime(o.getCreationTime());
            resource.setGatewayId(o.getGatewayId());
            if (o.getUpdateTime() != null){
                resource.setUpdatedTime(o.getUpdateTime());
            }
        }
        return resource;
    }

    private static AppCatalogResource createAppModMappingResource(AppModuleMapping o) {
        AppModuleMappingAppCatalogResourceAppCat resource = new AppModuleMappingAppCatalogResourceAppCat();
        if (o != null){
            resource.setInterfaceId(o.getInterfaceID());
            resource.setModuleId(o.getModuleID());
        }
        return resource;
    }

    private static AppCatalogResource createApplicationInput(ApplicationInput o) {
        ApplicationInputAppCatalogResourceAppCat resource = new ApplicationInputAppCatalogResourceAppCat();
        if (o != null){
            resource.setInterfaceID(o.getInterfaceID());
            resource.setInputKey(o.getInputKey());
            resource.setInputVal(o.getInputVal());
            resource.setDataType(o.getDataType());
            resource.setMetadata(o.getMetadata());
            resource.setAppArgument(o.getAppArgument());
            resource.setUserFriendlyDesc(o.getUserFriendlyDesc());
            resource.setStandardInput(o.isStandardInput());
            resource.setInputOrder(o.getInputOrder());
            resource.setRequired(o.isRequired());
            resource.setRequiredToCMD(o.isRequiredToCMD());
            resource.setDataStaged(o.isDataStaged());
            resource.setAppInterfaceResource((AppInterfaceAppCatalogResourceAppCat)createAppInterfaceResource(o.getApplicationInterface()));
        }
        return resource;
    }

    private static AppCatalogResource createWorflowInput(WorkflowInput o) {
        WorkflowInputAppCatalogResourceAppCat resource = new WorkflowInputAppCatalogResourceAppCat();
        if (o != null){
            resource.setWfTemplateId(o.getWfTemplateId());
            resource.setInputKey(o.getInputKey());
            if (o.getInputVal() != null){
                resource.setInputVal(new String(o.getInputVal()));
            }
            resource.setDataType(o.getDataType());
            resource.setMetadata(o.getMetadata());
            resource.setAppArgument(o.getAppArgument());
            resource.setInputOrder(o.getInputOrder());
            resource.setUserFriendlyDesc(o.getUserFriendlyDesc());
            resource.setStandardInput(o.isStandardInput());
            resource.setRequired(o.isRequired());
            resource.setRequiredToCMD(o.isRequiredToCMD());
            resource.setDataStaged(o.isDataStaged());
            resource.setWorkflowResource((WorkflowAppCatalogResourceAppCat)createWorkflow(o.getWorkflow()));
        }
        return resource;
    }

    private static AppCatalogResource createApplicationOutput(ApplicationOutput o) {
        ApplicationOutputAppCatalogResourceAppCat resource = new ApplicationOutputAppCatalogResourceAppCat();
        if (o != null){
            resource.setInterfaceID(o.getInterfaceID());
            resource.setOutputKey(o.getOutputKey());
            resource.setOutputVal(o.getOutputVal());
            resource.setDataType(o.getDataType());
            resource.setRequired(o.isRequired());
            resource.setRequiredToCMD(o.isRequiredToCMD());
            resource.setDataMovement(o.isDataMovement());
            resource.setDataNameLocation(o.getDataNameLocation());
            resource.setSearchQuery(o.getSearchQuery());
            resource.setAppArgument(o.getApplicationArgument());
            resource.setAppInterfaceResource((AppInterfaceAppCatalogResourceAppCat)createAppInterfaceResource(o.getApplicationInterface()));
        }
        return resource;
    }

    private static AppCatalogResource createWorkflowOutput(WorkflowOutput o) {
        WorkflowOutputAppCatalogResourceAppCat resource = new WorkflowOutputAppCatalogResourceAppCat();
        if (o != null){
            resource.setWfTemplateId(o.getWfTemplateId());
            resource.setOutputKey(o.getOutputKey());
            if (o.getOutputVal() != null){
                resource.setOutputVal(new String(o.getOutputVal()));
            }
            resource.setDataType(o.getDataType());
            resource.setValidityType(o.getValidityType());
            resource.setDataMovement(o.isDataMovement());
            resource.setDataNameLocation(o.getDataNameLocation());
            resource.setWorkflowResource((WorkflowAppCatalogResourceAppCat)createWorkflow(o.getWorkflow()));
        }
        return resource;
    }

    private static AppCatalogResource createGatewayProfile(GatewayProfile o) {
        GatewayProfileAppCatalogResourceAppCat resource = new GatewayProfileAppCatalogResourceAppCat();
        if (o != null) {
            resource.setGatewayID(o.getGatewayID());
            resource.setCreatedTime(o.getCreationTime());
            if (o.getUpdateTime() != null){
                resource.setUpdatedTime(o.getUpdateTime());
            }
        }
        return resource;
    }

    private static AppCatalogResource createComputeResourcePref(ComputeResourcePreference o) {
        ComputeHostPreferenceAppCatalogResourceAppCat resource = new ComputeHostPreferenceAppCatalogResourceAppCat();
        if (o != null) {
            resource.setGatewayId(o.getGatewayId());
            resource.setResourceId(o.getResourceId());
            resource.setOverrideByAiravata(o.isOverrideByAiravata());
            resource.setPreferredJobProtocol(o.getPreferedJobSubmissionProtocol());
            resource.setPreferedDMProtocol(o.getPreferedDataMoveProtocol());
            resource.setBatchQueue(o.getBatchQueue());
            resource.setScratchLocation(o.getScratchLocation());
            resource.setProjectNumber(o.getProjectNumber());
            resource.setLoginUserName(o.getLoginUserName());
            resource.setComputeHostResource((ComputeResourceAppCatalogResourceAppCat) createComputeResource(o.getComputeHostResource()));
            resource.setGatewayProfile((GatewayProfileAppCatalogResourceAppCat) createGatewayProfile(o.getGatewayProfile()));
        }
        return resource;
    }

    private static AppCatalogResource createModuleLoadCmd(ModuleLoadCmd o) {
        ModuleLoadCmdAppCatalogResourceAppCat moduleLoadCmdResource = new ModuleLoadCmdAppCatalogResourceAppCat();
        if (o != null){
            moduleLoadCmdResource.setCmd(o.getCmd());
            moduleLoadCmdResource.setAppDeploymentId(o.getAppDeploymentId());
            moduleLoadCmdResource.setAppDeploymentResource((AppDeploymentAppCatalogResourceAppCat)createApplicationDeployment(o.getApplicationDeployment()));
        }
        return moduleLoadCmdResource;
    }

    private static AppCatalogResource createWorkflow(Workflow o) {
        WorkflowAppCatalogResourceAppCat workflowResource = new WorkflowAppCatalogResourceAppCat();
        workflowResource.setWfName(o.getWfName());
        workflowResource.setCreatedUser(o.getCreatedUser());
        if (o.getGraph() != null){
            workflowResource.setGraph(new String(o.getGraph()));
        }
        if (o.getImage() != null){
            workflowResource.setImage(new String(o.getImage()));
        }
        workflowResource.setCreatedTime(o.getCreationTime());
        if (o.getUpdateTime() != null){
            workflowResource.setUpdatedTime(o.getUpdateTime());
        }
        workflowResource.setWfTemplateId(o.getWfTemplateId());
        workflowResource.setGatewayId(o.getGatewayId());
        return workflowResource;
    }
}
