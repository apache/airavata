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

import org.apache.aiaravata.application.catalog.data.model.*;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
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
                    "Url=" + readServerProperties(APPCATALOG_JDBC_URL) + "," +
                    "Username=" + readServerProperties(APPCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(APPCATALOG_JDBC_PWD) +
                    ",validationQuery=" + readServerProperties(APPCATALOG_VALIDATION_QUERY);
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
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }

    private static Resource createComputeResource(ComputeResource o) {
        ComputeHostResource hostResource = new ComputeHostResource();
        hostResource.setResoureId(o.getResourceID());
        hostResource.setHostName(o.getHostName());
        hostResource.setDescription(o.getDescription());
        hostResource.setScratchLocation(o.getScratchLocation());
        hostResource.setPreferredJobSubmissionProtocol(o.getPreferredJobSubProtocol());
        return hostResource;
    }

    private static Resource createHostAlias(HostAlias o) {
        HostAliasResource aliasResource = new HostAliasResource();
        aliasResource.setResourceID(o.getResourceID());
        aliasResource.setAlias(o.getAlias());
        return aliasResource;
    }

    private static Resource createHostIPAddress(HostIPAddress o) {
        HostAliasResource aliasResource = new HostAliasResource();
        aliasResource.setResourceID(o.getResourceID());
        aliasResource.setAlias(o.getIpaddress());
        return aliasResource;
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
        resource.setDeploymentId(o.getDeployementID());
        resource.setAppDes(o.getApplicationDesc());
        resource.setAppModuleId(o.getAppModuleID());
        resource.setEnvModuleLoadCMD(o.getEnvModuleLoaString());
        resource.setHostId(o.getHostID());
        resource.setExecutablePath(o.getExecutablePath());
        resource.setModuleResource((AppModuleResource)createApplicationModule(o.getApplicationModule()));
        resource.setHostResource((ComputeHostResource)createComputeResource(o.getComputeResource()));
        return resource;
    }

    private static Resource createLibraryPrepPathResource(LibraryPrepandPath o) {
        LibraryPrepandPathResource resource = new LibraryPrepandPathResource();
        resource.setDeploymentId(o.getDeploymentID());
        resource.setName(o.getName());
        resource.setValue(o.getValue());
        resource.setAppDeploymentResource((AppDeploymentResource)createApplicationDeployment(o.getApplicationDeployment()));
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
}
