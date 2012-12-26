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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.util.WebServiceUtil;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRComponentRegistry extends ComponentRegistry {

    private static final Logger log = LoggerFactory.getLogger(JCRComponentRegistry.class);
    private static final String NAME = "Application Services";
//    public static final String REPOSITORY_PROPERTIES = "airavata-server.properties";

    private AiravataAPI airavataAPI;

    public JCRComponentRegistry(AiravataAPI airavataAPI) {
        this.setAiravataAPI(airavataAPI);
    }

//    public JCRComponentRegistry(String username, String password) throws RegistryException {
//        String gatewayName=null;
//        String registryURL = null;
//        AiravataRegistryConnectionDataProvider provider = AiravataRegistryFactory.getRegistryConnectionDataProvider();
//		if (provider==null){
//	        URL configURL = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
//	        if(configURL != null){
//		        try {
//			        Properties properties = new Properties();
//		            properties.load(configURL.openStream());
//		            if (username==null){
//			            if(properties.get(RegistryConstants.KEY_DEFAULT_REGISTRY_USER) != null){
//			                username = (String)properties.get(RegistryConstants.KEY_DEFAULT_REGISTRY_USER);
//			            }
//		            }
//		            gatewayName = (String)properties.get(RegistryConstants.KEY_DEFAULT_GATEWAY_ID);
//                    registryURL =  properties.getProperty(RegistryConstants.KEY_DEFAULT_REGISTRY_URL);
//		        } catch (MalformedURLException e) {
//		            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		        } catch (IOException e) {
//		            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		        }
//	        }
//        }else{
//        	try {
//				if (username==null){
//					username=provider.getValue(RegistryConstants.KEY_DEFAULT_REGISTRY_USER).toString();
//				}
//				gatewayName = provider.getValue(RegistryConstants.KEY_DEFAULT_GATEWAY_ID).toString();
//                registryURL = provider.getValue(RegistryConstants.KEY_DEFAULT_REGISTRY_URL).toString();
//			} catch (Exception e) {
//				log.warn(e.getMessage());
//			}
//        }
//        if (username==null){
//        	username="admin";
//        }
//        if (gatewayName==null){
//        	gatewayName="default";
//        }
//        try {
//            URI baseUri = new URI(registryURL);
//            //TODO callback class
////            PasswordCallBackImpl passwordCallBack = new PasswordCallBackImpl(username, password);
//            this.airavataAPI = AiravataAPIFactory.getAPI(baseUri, gatewayName, username, (PasswordCallback)null);
//        }  catch (URISyntaxException e) {
//            log.error("Error initializing Airavata Client");
//        } catch (AiravataAPIInvocationException e) {
//            log.error("Error initializing Airavata Client");
//        }
//
//    }

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
            List<ServiceDescription> services = this.getAiravataAPI().getApplicationManager().getAllServiceDescriptions();
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

//    public String saveApplicationDescription(String service, String host, ApplicationDeploymentDescription app) {
//        // deploy the service on host
//        registry.deployServiceOnHost(service, host);
//
//        // save deployment description
//        return registry.saveApplicationDescription(service, host, app);
//    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

	public void setAiravataAPI(AiravataAPI airavataAPI) {
		this.airavataAPI = airavataAPI;
	}
}