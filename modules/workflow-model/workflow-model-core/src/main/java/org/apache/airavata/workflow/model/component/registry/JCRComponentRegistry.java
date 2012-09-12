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

package org.apache.airavata.workflow.model.component.registry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.util.WebServiceUtil;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRComponentRegistry extends ComponentRegistry {

    private static final Logger log = LoggerFactory.getLogger(JCRComponentRegistry.class);
    private static final String NAME = "Application Services";
    public static final String REPOSITORY_PROPERTIES = "repository.properties";
    public static final String GATEWAY_ID = "gateway.id";
    public static final String REGISTRY_USER = "registry.user";

    private AiravataRegistry2 registry;

    public JCRComponentRegistry(String username, String password) throws RegistryException {
        HashMap<String, String> map = new HashMap<String, String>();
        URL configURL = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
        Properties properties = new Properties();
        if(configURL != null){
        try {
            properties.load(configURL.openStream());
            if(properties.get(REGISTRY_USER) != null){
                username = (String)properties.get(REGISTRY_USER);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        }else {
            // provide a way to get gatewayid from xbaya gui
            properties.setProperty(GATEWAY_ID, "default");
        }
        try {
            this.registry = AiravataRegistryFactory.getRegistry(new Gateway((String)properties.get(GATEWAY_ID)),
                    new AiravataUser(username));
        } catch (AiravataConfigurationException e) {
            log.error("Error initializing AiravataRegistry2");
        }

    }

    static {
        registerUserManagers();
    }

    /**
     * to manually trigger user manager registrations
     */
    private static void registerUserManagers() {
        try {
            Class.forName("org.apache.airavata.xbaya.component.registry.jackrabbit.user.JackRabbitUserManagerWrap");
        } catch (ClassNotFoundException e) {
            // error in registering user managers
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getComponentReferenceList()
     */
    @Override
    public List<ComponentReference> getComponentReferenceList() {
        List<ComponentReference> tree = new ArrayList<ComponentReference>();
        try {
            List<ServiceDescription> services = this.registry.getServiceDescriptors();
            for (ServiceDescription serviceDescription : services) {
                String serviceName = serviceDescription.getType().getName();
                JCRComponentReference jcr = new JCRComponentReference(serviceName,
                        WebServiceUtil.getWSDL(serviceDescription));
                tree.add(jcr);
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
		}

        return tree;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

//    public String saveDeploymentDescription(String service, String host, ApplicationDeploymentDescription app) {
//        // deploy the service on host
//        registry.deployServiceOnHost(service, host);
//
//        // save deployment description
//        return registry.saveDeploymentDescription(service, host, app);
//    }

    public AiravataRegistry2 getRegistry() {
        return registry;
    }
}