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

package org.apache.airavata.migrator.registry;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.xmlbeans.XmlException;
import org.ogce.schemas.gfac.beans.ApplicationBean;
import org.ogce.schemas.gfac.beans.HostBean;
import org.ogce.schemas.gfac.beans.ServiceBean;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.client.XRegistryClientUtil;
import org.ogce.xregistry.utils.XRegistryClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xregistry.generated.FindAppDescResponseDocument;
import xregistry.generated.HostDescData;
import xregistry.generated.ServiceDescData;

import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XRegistryMigrationManager {
    private static String migrationPropertiesFile = null;
    private static AiravataJCRRegistry jcrRegistry = null;
    private static String jcrRegistryURL = null;
    private static String jcrUsername = null;
    private static String jcrPassword = null;
    private static Logger log = LoggerFactory.getLogger(XRegistryMigrationManager.class);

    public XRegistryMigrationManager(String propertyFile) throws XRegistryMigrationException {
        migrationPropertiesFile = propertyFile;
        loadProperties(propertyFile);
    }

    public static void main(String[] args) {
        XRegistryMigrationManager manager;
        try {
            manager = new XRegistryMigrationManager(args[0]);
            manager.migrate();
        } catch (XRegistryMigrationException e) {
            log.info(e.getMessage());
        }
    }

    /**
     * Migrates the the resources from XRegistry to the Airavata Registry.
     *
     * @throws XRegistryMigrationException XRegistryMigrationException
     */
    public void migrate() throws XRegistryMigrationException {
        Map<String,String> config = new HashMap<String,String>();
        URI uri;
        try {
            uri = new URI(jcrRegistryURL);
        } catch (URISyntaxException e) {
            throw new XRegistryMigrationException("Invalid JCR Registry URL " + e.getMessage(), e);
        }
        config.put("org.apache.jackrabbit.repository.uri", uri.toString());

        try {
            jcrRegistry = new AiravataJCRRegistry(uri,
                    "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory",
                    jcrUsername, jcrPassword, config);
        } catch (RepositoryException e) {
            throw new XRegistryMigrationException("Issue creating the JCR Registry instance " +
                    e.getMessage(), e);
        }

        XRegistryClient client;
        try {
            client = XRegistryClientUtil.CreateGSISecureRegistryInstance(migrationPropertiesFile);
        } catch (XRegistryClientException e) {
            throw new XRegistryMigrationException("Issue instantiating the XRegistry instance. " +
                    "Check property file " + e.getMessage(), e);
        }

        saveAllHostDescriptions(client);
        saveAllServiceDescriptions(client);

        log.info("DONE!");
    }

    private static void loadProperties(String file) throws XRegistryMigrationException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream propertyStream = classLoader.getResourceAsStream(file);
        Properties properties = new Properties();
        if (propertyStream == null) {
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(new File(file));
            } catch (FileNotFoundException e) {
                throw new XRegistryMigrationException("Migration properties file not found " +
                        e.getMessage(), e);
            }
            try {
                properties.load(fileInputStream);
            } catch (IOException e) {
                throw new XRegistryMigrationException("Issue occurred while loading the migration " +
                        "properties from file " + e.getMessage(), e);
            }
        } else {
            try {
                properties.load(propertyStream);
            } catch (IOException e) {
                throw new XRegistryMigrationException("Issue occurred while loading the " +
                        "migration properties from file " + e.getMessage(), e);
            }
        }

        jcrRegistryURL = properties.getProperty(MigrationConstants.JCR_URL);
        jcrUsername = properties.getProperty(MigrationConstants.JCR_USERNAME);
        jcrPassword = properties.getProperty(MigrationConstants.JCR_PASSWORD);
    }

    /**
     * Saves all the host descriptions to the Airavata Registry from the the given XRegistry.
     *
     * @param client client to access the XRegistry
     * @throws XRegistryMigrationException XRegistryMigrationException
     */
    private static void saveAllHostDescriptions(XRegistryClient client)
            throws XRegistryMigrationException {
        HostDescription host;
        HostDescData[] hostDescData;
        try {
            hostDescData = client.findHosts("");
        } catch (XRegistryClientException e) {
            throw new XRegistryMigrationException("Issue searching hosts in XRegistry instance " +
                    e.getMessage(), e);
        }
        Map<QName, HostDescData> val = new HashMap<QName, HostDescData>();
        for (HostDescData hostDesc : hostDescData) {
            val.put(hostDesc.getName(), hostDesc);
            String hostDescStr;
            try {
                hostDescStr = client.getHostDesc(hostDesc.getName().getLocalPart());
            } catch (XRegistryClientException e) {
                throw new XRegistryMigrationException("Issue getting the host description with " +
                        "name " + hostDesc.getName().getLocalPart() + " from XRegistry instance " +
                        e.getMessage(), e);
            }
            HostBean hostBean;
            try {
                hostBean = org.ogce.schemas.gfac.beans.
                        utils.HostUtils.simpleHostBeanRequest(hostDescStr);
                log.info("Host : " + hostBean.getHostName());
                log.info(hostDescStr);

            } catch (XmlException e) {
                throw new XRegistryMigrationException("Issue creating the OGCE Schema Host Bean " +
                        e.getMessage(), e);
            }

            host = MigrationUtil.createHostDescription(hostBean);

            try {
                jcrRegistry.saveHostDescription(host);
            } catch (RegistryException e) {
                throw new XRegistryMigrationException("Issue occurred when saving the Host " +
                        "Description " + host.getType().getHostName() + " to JCR Registry" +
                        e.getMessage(), e);
            }

        }

        log.info("All Hosts are saved!");
    }

    /**
     * Saves all the host service descriptions to the Airavata Registry from the the given XRegistry.
     *
     * @param client client to access the XRegistry
     * @throws XRegistryMigrationException XRegistryMigrationException
     */
    private static void saveAllServiceDescriptions(XRegistryClient client)
            throws XRegistryMigrationException {
        ServiceDescription service;
        ServiceDescData[] serviceDescData;
        try {
            serviceDescData = client.findServiceDesc("");
        } catch (XRegistryClientException e) {
            throw new XRegistryMigrationException("Issue accessing XRegistry " + e.getMessage(), e);
        }
        Map<QName, ServiceDescData> val3 = new HashMap<QName, ServiceDescData>();
        int count = 0;

        for (ServiceDescData serviceDesc : serviceDescData) {
            val3.put(serviceDesc.getName(), serviceDesc);
            String serviceDescStr;
            try {
                serviceDescStr = client.getServiceDesc(serviceDesc.getName());
            } catch (XRegistryClientException e) {
                throw new XRegistryMigrationException("Issue retrieving Service Description form " +
                        "XRegistry instance " + e.getMessage(), e);
            }

            ServiceBean serviceBean;
            String applicationName;
            try {
                serviceBean = org.ogce.schemas.gfac.beans.
                        utils.ServiceUtils.serviceBeanRequest(serviceDescStr);
                applicationName = serviceBean.getApplicationName();
                log.info("Service : " + serviceBean.getServiceName());
                log.info(serviceDescStr);
            } catch (XmlException e) {
                throw new XRegistryMigrationException("Issue creating the OGCE Schema Service " +
                        "Bean " + e.getMessage(), e);
            } catch (IOException e) {
                throw new XRegistryMigrationException("Issue creating the OGCE Schema Service " +
                        "Bean " + e.getMessage(), e);
            }

            try {
                String serviceName = serviceBean.getServiceName();
                ServiceDescription serviceDescription = jcrRegistry.getServiceDesc(serviceName);
                if(serviceDescription == null) {
                    service = MigrationUtil.createServiceDescription(serviceBean);
                    jcrRegistry.saveServiceDescription(service);
                    ApplicationBean appBean =
                            saveApplicationDescriptionWithName(client, applicationName, service);
                    if (appBean != null){
                        jcrRegistry.deployServiceOnHost(service.getType().
                                getName(), appBean.getHostName());
                    }
                } else {
                    serviceName = serviceName + "_" + count++;
                    service = MigrationUtil.createServiceDescription(serviceName,serviceBean);
                    log.info("Service Description named " +
                            service.getType().getName() +
                            " exists in the registry. Therefore, saving it as " +
                            serviceName + " in the registry.");

                    jcrRegistry.saveServiceDescription(service);
                    ApplicationBean appBean = saveApplicationDescriptionWithName(client,
                            applicationName, service);
                    if (appBean != null){
                        jcrRegistry.deployServiceOnHost(service.getType().getName(),
                                appBean.getHostName());
                    }

                }
            } catch (RegistryException e) {
                throw new XRegistryMigrationException("Issue accessing the JCR Registry " +
                        e.getMessage(), e);
            }

        }
        log.info("All Service/Application descriptors are saved!");
    }

    /**
     * Saves the application description to the Airavata Registry from the the given XRegistry.
     *
     * @param client client to access the XRegistry
     * @param applicationName name of the application to be saved
     * @param service service name
     * @return ApplicationBean
     * @throws XRegistryMigrationException XRegistryMigrationException
     */
    private static ApplicationBean saveApplicationDescriptionWithName(XRegistryClient client,
                                                                      String applicationName,
                                                                      ServiceDescription service)
            throws XRegistryMigrationException {
        FindAppDescResponseDocument.FindAppDescResponse.AppData[] appData;
        try {
            appData = client.findAppDesc(applicationName);
        } catch (XRegistryClientException e) {
            throw new XRegistryMigrationException("Issue accessing XRegistry " + e.getMessage(), e);
        }
        Map<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData> val2 =
                new HashMap<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData>();
        ApplicationBean appBean = null;
        int count = 0;
        for (FindAppDescResponseDocument.FindAppDescResponse.AppData appDesc : appData) {
            val2.put(appDesc.getName(), appDesc);
            String appDescStr;
            try {
                appDescStr = client.getAppDesc(appDesc.getName().toString(),appDesc.getHostName());
            } catch (XRegistryClientException e) {
                throw new XRegistryMigrationException("Issue retrieving Application " +
                        "Description form XRegistry instance " + e.getMessage(), e);
            }
            try {
                appBean = org.ogce.schemas.gfac.beans.
                        utils.ApplicationUtils.simpleApplicationBeanRequest(appDescStr);
                log.info("Application : " + appBean.getApplicationName());
                log.info(appDescStr);

            } catch (XmlException e) {
                throw new XRegistryMigrationException("Issue creating the OGCE Schema " +
                        "Application Bean " + e.getMessage(), e);
            }

            try {
                String name = service.getType().getName();
                String hostName = appBean.getHostName();
                Thread.sleep(500);
                ApplicationDeploymentDescription appDepDesc =
                        jcrRegistry.getDeploymentDescription(name, hostName);
                if(appDepDesc == null) {
                    if(log.isDebugEnabled()) {
                        log.debug("name    : " + name);
                        log.debug("hostName: " + hostName);
                    }
                    jcrRegistry.saveDeploymentDescription(name, hostName,
                            MigrationUtil.createAppDeploymentDescription(appBean));
                } else {
                    //Creating a new name for the the duplicated item
                    name = name + "_" + count++;
                    if(log.isDebugEnabled()) {
                        log.debug("name    : " + name);
                        log.debug("hostName: " + hostName);
                    }
                    log.info("Application Deployment Description named " +
                            service.getType().getName() + " with host " + hostName +
                            " exists in the registry. Therefore, saving it as " +
                            name + " in the registry.");
                    jcrRegistry.saveDeploymentDescription(name, hostName,
                            MigrationUtil.createAppDeploymentDescription(name,appBean));
                }
            } catch (RegistryException e) {
                throw new XRegistryMigrationException("Issue using the Airavata Registry API " +
                        e.getMessage(), e);
            } catch (InterruptedException e) {
                throw new XRegistryMigrationException(e.getMessage(), e);
            }


        }

        return appBean;
    }

}

