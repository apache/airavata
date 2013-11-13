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
package org.apache.airavata.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.jcr.RepositoryException;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataManager;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.client.api.ExecutionManager;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.client.api.UserManager;
import org.apache.airavata.client.api.WorkflowManager;
import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.impl.*;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataClient extends Observable implements AiravataAPI {

	private static final Logger log = LoggerFactory
			.getLogger(AiravataClient.class);
	public static final String REGISTRY = "JCR";
	public static final String GFAC = "gfac";
	public static final String PROXYSERVER = "proxyserver";
	public static final String MSGBOX = "msgbox";
	public static final String BROKER = "broker";
	public static final String DEFAULT_GFAC_URL = "gfac.url";
	public static final String DEFAULT_MYPROXY_SERVER = "myproxy.url";
	public static final String DEFAULT_MESSAGE_BOX_URL = "messagebox.url";
	public static final String DEFAULT_BROKER_URL = "messagebroker.url";
	public static final String MYPROXYUSERNAME = "myproxy.username";
	public static final String MYPROXYPASS = "myproxy.password";
	public static final String WITHLISTENER = "with.Listener";
	public static final String WORKFLOWSERVICEURL = "xbaya.service.url";
	public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
	private AiravataClientConfiguration clientConfiguration;
	private String currentUser;
	private URI regitryURI;
	private PasswordCallback callBack;

	private AiravataRegistry2 registry;

	private Map<String, String> configuration = new HashMap<String, String>();
	private AiravataManagerImpl airavataManagerImpl;
	private ApplicationManagerImpl applicationManagerImpl;
	private WorkflowManagerImpl workflowManagerImpl;
	private ProvenanceManagerImpl provenanceManagerImpl;
	private UserManagerImpl userManagerImpl;
//	private ExecutionManagerThriftImpl executionManagerImpl;
    private ExecutionManagerImpl executionManagerImpl;
	private String gateway;
	private boolean configCreated = false;

    private static volatile boolean registryServiceStarted = false;

    private static int WAIT_TIME_PERIOD = 4 * 1000;
    private static int WAIT_ITERATIONS = 15;

	private static final Version API_VERSION = new Version("Airavata", 0, 9,
			null, null, null);

	// FIXME: Need a constructor to set registry URL
	protected AiravataClient() {
	}

	private static HashMap<String, String> createConfig(URI registryUrl, String gateway,
			String username, String password) throws RepositoryException,
			RegistryException, AiravataConfigurationException {
		HashMap<String, String> config = new HashMap<String, String>();
		if (registryUrl != null) {
			config.put(AiravataClient.REGISTRY, registryUrl.toString());
		}
		AiravataRegistry2 registryObject = getRegistry(registryUrl, gateway,
				username, new PasswordCallBackImpl(username, password));
		if (registryObject != null) {
			URI uri = registryObject.getEventingServiceURI();
			config.put(
					AiravataClient.BROKER,
					uri == null ? "http://localhost:8080/airavata-server/services/EventingService"
							: uri.toString());
			uri = registryObject.getMessageBoxURI();
			config.put(
					AiravataClient.MSGBOX,
					uri == null ? "http://localhost:8080/airavata-server/services/MsgBoxService"
							: uri.toString());
			List<URI> URLList = registryObject.getWorkflowInterpreterURIs();
			config.put(
					AiravataClient.WORKFLOWSERVICEURL,
					URLList == null || URLList.size() == 0 ? "http://localhost:8080/airavata-server/services/WorkflowInterpretor?wsdl"
							: URLList.get(0).toString());
			List<URI> urlList = registryObject.getGFacURIs();
			config.put(
					AiravataClient.GFAC,
					urlList == null || urlList.size() == 0 ? "http://localhost:8080/airavata-server/services/GFacService"
							: urlList.get(0).toString());
			config.put(AiravataClient.WITHLISTENER, "true");
		}
		return config;
	}

	@Override
	public void initialize() throws AiravataAPIInvocationException {

//        if (AiravataUtils.isServer()) {
//            waitTillRegistryServiceStarts();
//        }

        try {
			if (!configCreated) {
				configuration = createConfig(getRegitryURI(), getGateway(), getCurrentUser(),
						getPassword());
				configCreated = true;
			}
			updateClientConfiguration(configuration);

			// TODO: At some point this should contain the current user the
			// airavata
			// client is
			// logged in to the Airavata system
			setCurrentUser(getClientConfiguration().getJcrUsername());
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(
					"Error while initializing the Airavata API", e);
		}
	}

    private void waitTillRegistryServiceStarts() throws AiravataAPIInvocationException{

        synchronized (API_VERSION) {
            if (!registryServiceStarted) {
                waitForRegistryServiceToStart(getRegistryWebServiceWSDLUrl());
                registryServiceStarted = true;
            }
        }
    }

    private String getRegistryWebServiceWSDLUrl() throws AiravataAPIInvocationException {
        String registryUrl = null;
        try {
            registryUrl = ApplicationSettings.getAbsoluteSetting("registry.service.wsdl");
        } catch (ApplicationSettingsException e) {
            String msg = "Configuration registry.service.wsdl is not specified in the configuration file";
            log.warn(msg);
            log.debug(msg, e);
        }

        if (registryUrl == null) {
            String hostName = getRegitryURI().getHost();
            int port = getRegitryURI().getPort();
            String protocol = null;
            try {
                protocol = getRegitryURI().toURL().getProtocol();
            } catch (MalformedURLException e) {
                String msg = "Error retrieving protocol from registry URI - "
                        + getRegitryURI().toString();
                log.error(msg, e);
                throw new AiravataAPIInvocationException(msg, e);
            }

            StringBuilder registryServiceUrlString = new StringBuilder(protocol);
            registryServiceUrlString.append("://").append(hostName).append(":").append(port);
            registryServiceUrlString.append("/axis2/services/RegistryService?wsdl");

            registryUrl = registryServiceUrlString.toString();
        }

        return registryUrl;
    }

    private void waitForRegistryServiceToStart(String url) throws AiravataAPIInvocationException {

        log.info("Registry service URL - " + url);

        int iterations = 0;
        Exception exception = null;

        while (!registryServiceStarted) {
            try {
                org.apache.airavata.registry.stub.RegistryServiceStub stub =
                        new org.apache.airavata.registry.stub.RegistryServiceStub(url);
                registryServiceStarted = stub.isRegistryServiceStarted().getIsRegistryServiceStartedResponse().
                        getReturn();
            } catch (Exception e) {
                exception = e;
            }

            if (!registryServiceStarted) {
                try {
                    if (iterations == WAIT_ITERATIONS) {
                        if (exception != null) {
                            throw new AiravataAPIInvocationException("Unable to connect to RegistryService. " +
                                    "RegistryService may not have started", exception);
                        } else {
                            throw new AiravataAPIInvocationException("Unable to connect to RegistryService. " +
                                    "RegistryService may not have started");
                        }

                    } else {
                        Thread.sleep(WAIT_TIME_PERIOD);
                    }
                } catch (InterruptedException e1) {
                    log.info("Received an interrupted exception.");
                }

                log.info("Attempting to contact registry service, iteration - " + iterations);

                ++iterations;
            }

        }


    }

	private void updateClientConfiguration(Map<String, String> configuration)
			throws MalformedURLException {
		AiravataClientConfiguration clientConfiguration = getClientConfiguration();
		if (configuration.get(GFAC) != null) {
			clientConfiguration.setGfacURL(new URL(configuration.get(GFAC)));
		}
		if (configuration.get(MSGBOX) != null) {
			clientConfiguration.setMessageboxURL(new URL(configuration
					.get(MSGBOX)));
		}
		if (configuration.get(BROKER) != null) {
			clientConfiguration.setMessagebrokerURL(new URL(configuration
					.get(BROKER)));
		}
		if (configuration.get(WORKFLOWSERVICEURL) != null) {
			clientConfiguration.setXbayaServiceURL(new URL(configuration
					.get(WORKFLOWSERVICEURL)));
		}
		if (configuration.get(MSGBOX) != null) {
			clientConfiguration.setMessageboxURL(new URL(configuration
					.get(MSGBOX)));
		}

		if (clientConfiguration.getRegistryURL() != null
				&& clientConfiguration.getGfacURL() == null) {
			try {
				clientConfiguration.setGfacURL(getRegistryClient()
						.getGFacURIs().get(0).toURL());
				configuration.put(GFAC, clientConfiguration.getGfacURL()
						.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public AiravataRegistry2 getRegistryClient()
			throws AiravataConfigurationException, RegistryException {
		if (registry == null) {
			registry = getRegistry(getRegitryURI(), getGateway(),
					getCurrentUser(), getCallBack());
		}
		return registry;
	}

	public static AiravataRegistry2 getRegistry(URI registryURI,
			String gateway, String username, PasswordCallback callback)
			throws RegistryException, AiravataConfigurationException {
		return AiravataRegistryFactory.getRegistry(registryURI, new Gateway(
				gateway), new AiravataUser(username), callback);
	}

	public AiravataClientConfiguration getClientConfiguration() {
		if (clientConfiguration == null) {
			clientConfiguration = new AiravataClientConfiguration();
		}
		return clientConfiguration;
	}

	public AiravataManager getAiravataManager() {
		if (airavataManagerImpl == null) {
			airavataManagerImpl = new AiravataManagerImpl(this);
		}
		return airavataManagerImpl;
	}

	public ApplicationManager getApplicationManager() {
		if (applicationManagerImpl == null) {
			applicationManagerImpl = new ApplicationManagerImpl(this);
		}
		return applicationManagerImpl;
	}

	public WorkflowManager getWorkflowManager() {
		if (workflowManagerImpl == null) {
			workflowManagerImpl = new WorkflowManagerImpl(this);
		}
		return workflowManagerImpl;
	}

	public ProvenanceManager getProvenanceManager() {
		if (provenanceManagerImpl == null) {
			provenanceManagerImpl = new ProvenanceManagerImpl(this);
		}
		return provenanceManagerImpl;
	}

	public UserManager getUserManager() {
		if (userManagerImpl == null) {
			userManagerImpl = new UserManagerImpl(this);
		}
		return userManagerImpl;
	}

	public ExecutionManager getExecutionManager() {
		if (executionManagerImpl == null) {
//			executionManagerImpl = new ExecutionManagerThriftImpl(this);
			executionManagerImpl = new ExecutionManagerImpl(this);
		}
		return executionManagerImpl;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public Version getVersion() {
		return API_VERSION;
	}

	@Override
	public DescriptorBuilder getDescriptorBuilder() {
		return new DescriptorBuilder();
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public void setRegitryURI(URI regitryURI) {
		this.regitryURI = regitryURI;
	}

	public String getPassword() {
		if (getCallBack() != null) {
			return getCallBack().getPassword(getCurrentUser());
		}
		return null;
	}

	public URI getRegitryURI() {
		return regitryURI;
	}

	public PasswordCallback getCallBack() {
		return callBack;
	}

	public void setCallBack(PasswordCallback callBack) {
		this.callBack = callBack;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	
	public static void main(String[] args) throws Exception {
		AiravataAPI api = AiravataAPIFactory.getAPI(new URI("http://localhost:8080/airavata/services/registry"), "default", "admin", new PasswordCallBackImpl("admin", "admin"));
		ExperimentAdvanceOptions options = api.getExecutionManager().createExperimentAdvanceOptions();
		options.getCustomWorkflowSchedulingSettings().addNewNodeSettings("data1", "comma_app", 1, 1);
		String workflow = "Workflow3";
		List<WorkflowInput> inputs = api.getWorkflowManager().getWorkflowInputs(workflow);
		System.out.println(api.getExecutionManager().runExperiment(workflow, inputs,options));
	}
}
