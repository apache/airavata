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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.AiravataManager;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.client.api.ExecutionManager;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.client.api.UserManager;
import org.apache.airavata.client.api.WorkflowManager;
import org.apache.airavata.client.impl.*;
import org.apache.airavata.client.stub.interpretor.NameValue;
import org.apache.airavata.client.stub.interpretor.WorkflowInterpretorStub;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.rest.client.*;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.registry.JCRComponentRegistry;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.Monitor;
import org.apache.airavata.ws.monitor.MonitorConfiguration;
import org.apache.airavata.ws.monitor.MonitorEventListener;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataClient implements AiravataAPI {

	private static final Logger log = LoggerFactory
			.getLogger(AiravataClient.class);
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
	private MonitorConfiguration monitorConfiguration;
	private static String workflow = "";
	private static WorkflowContextHeaderBuilder builder;
	private String currentUser;
    private String password;
    private URI regitryURI;
    private PasswordCallBackImpl passwordCallBack;

	private AiravataRegistry2 registry;

	private Map<String, String> configuration = new HashMap<String, String>();
	private AiravataManagerImpl airavataManagerImpl;
	private ApplicationManagerImpl applicationManagerImpl;
	private WorkflowManagerImpl workflowManagerImpl;
	private ProvenanceManagerImpl provenanceManagerImpl;
	private UserManagerImpl userManagerImpl;
	private ExecutionManagerImpl executionManagerImpl;

	// private NameValue[] configurations = new NameValue[7];

	private static final Version API_VERSION = new Version("Airavata", 0, 5,
			null, null, null);

	protected AiravataClient(Map<String, String> configuration)
			throws MalformedURLException {
		this.configuration = configuration;
		initialize();
	}

	// FIXME: Need a constructor to set registry URL
	protected AiravataClient() {
	}

	protected AiravataClient(String fileName) throws RegistryException,
			MalformedURLException, IOException {
		URL url = this.getClass().getClassLoader().getResource(fileName);
		if (url == null) {
			url = (new File(fileName)).toURL();
		}
		Properties properties = new Properties();
		properties.load(url.openStream());
		configuration.put(GFAC,
				validateAxisService(properties.getProperty(DEFAULT_GFAC_URL)));
		configuration.put(MSGBOX, validateAxisService(properties
				.getProperty(DEFAULT_MESSAGE_BOX_URL)));
		configuration
				.put(BROKER, validateAxisService(properties
						.getProperty(DEFAULT_BROKER_URL)));
		configuration
				.put(WORKFLOWSERVICEURL, validateAxisService(properties
						.getProperty(WORKFLOWSERVICEURL)));
		configuration.put(WITHLISTENER, properties.getProperty(WITHLISTENER));

		initialize();
	}

	// protected AiravataClient(URI registryUrl, String username, String
	// password) throws MalformedURLException, RepositoryException,
	// RegistryException {
	// this(createConfig(registryUrl, username, password));
	// }
	//
	// private static HashMap<String, String> createConfig(URI registryUrl,
	// String username, String password) throws RepositoryException,
	// RegistryException {
	// HashMap<String, String> config = new HashMap<String,String>();
	// config.put(AiravataClient.JCR,registryUrl.toString());
	// config.put(AiravataClient.JCR_USERNAME,username);
	// config.put(AiravataClient.JCR_PASSWORD,password);
	// AiravataRegistry2 registryObject = getRegistryObject(username, password);
	// if (registryObject!=null){
	// URI uri = registryObject.getEventingServiceURI();
	// config.put(AiravataClient.BROKER,uri==null?
	// "http://localhost:8080/axis2/services/EventingService":uri.toString());
	// uri = registryObject.getMessageBoxURI();
	// config.put(AiravataClient.MSGBOX,uri==null?
	// "http://localhost:8080/axis2/services/MsgBoxService":uri.toString());
	// List<URI> URLList = registryObject.getWorkflowInterpreterURIs();
	// config.put(AiravataClient.WORKFLOWSERVICEURL,URLList==null ||
	// URLList.size()==0?
	// "http://localhost:8080/axis2/services/WorkflowInterpretor?wsdl":URLList.get(0).toString());
	// List<URI> urlList = registryObject.getGFacURIs();
	// config.put(AiravataClient.GFAC,urlList==null || urlList.size()==0?
	// "http://localhost:8080/axis2/services/GFacService":urlList.get(0).toString());
	// config.put(AiravataClient.WITHLISTENER,"true");
	// }
	// return config;
	// }
	private void initialize() throws MalformedURLException {
		updateClientConfiguration(configuration);

		// At this point we do not know the workflowExperimentId
		// FIXME: Registry URL is set null as its not used. Set this when we
		// have rest services
		builder = new WorkflowContextHeaderBuilder(configuration.get(BROKER),
				configuration.get(GFAC), null, null, null,
				configuration.get(MSGBOX));

		// TODO: At some point this should contain the current user the airavata
		// client is
		// logged in to the Airavata system
		setCurrentUser(getClientConfiguration().getJcrUsername());
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
		// if (configuration.get(JCR)!= null) {
		// clientConfiguration
		// .setJcrURL(new URL(configuration.get(JCR)));
		// }
		if (configuration.get(WORKFLOWSERVICEURL) != null) {
			clientConfiguration.setXbayaServiceURL(new URL(configuration
					.get(WORKFLOWSERVICEURL)));
		}
		if (configuration.get(MSGBOX) != null) {
			clientConfiguration.setMessageboxURL(new URL(configuration
					.get(MSGBOX)));
		}

		if (clientConfiguration.getJcrURL() != null
				&& clientConfiguration.getGfacURL() == null) {
			try {
				clientConfiguration.setGfacURL(getConfigurationResourceClient().getGFacURIs()
						.get(0).toURL());
				configuration.put(GFAC, clientConfiguration.getGfacURL()
						.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void loadWorkflowFromaFile(String workflowFile)
			throws URISyntaxException, IOException {
		File workflow = null;
		URL url = AiravataClient.class.getClassLoader().getResource(
				workflowFile);
		if (url == null) {
			url = (new File(workflowFile)).toURL();
		}
		try {
			workflow = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		FileInputStream stream = new FileInputStream(workflow);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			this.workflow = Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

	public void loadWorkflowasaString(String workflowAsaString) {
		this.workflow = workflowAsaString;
	}

	public static void updateWorkflowInputValuesFromProperties(
			List<WorkflowInput> inputs, String fileName) throws IOException {
		URL url = AiravataClient.class.getClassLoader().getResource(fileName);
		if (url == null) {
			url = (new File(fileName)).toURL();
		}
		Properties properties = new Properties();
		properties.load(url.openStream());
		for (WorkflowInput workflowInput : inputs) {
			if (properties.containsKey(workflowInput.getName())) {
				workflowInput.setValue(properties.get(workflowInput.getName()));
			}
		}
	}

	public NameValue[] setInputs(String fileName) throws IOException {
		URL url = this.getClass().getClassLoader().getResource(fileName);
		if (url == null) {
			url = (new File(fileName)).toURL();
		}
		Properties properties = new Properties();
		properties.load(url.openStream());
		try {
			Workflow workflow = new Workflow(this.workflow);
			List<NodeImpl> inputs = workflow.getGraph().getNodes();
			int inputSize = 0;
			for (NodeImpl input : inputs) {
				if (input instanceof InputNode) {
					inputSize++;
				}
			}
			NameValue[] inputNameVals = new NameValue[inputSize];
			int index = 0;
			for (NodeImpl input : inputs) {
				if (input instanceof InputNode) {
					inputNameVals[index] = new NameValue();
					String name = input.getName();
					inputNameVals[index].setName(name);
					inputNameVals[index].setValue(properties.getProperty(name));
					((InputNode) input).setDefaultValue(properties
							.getProperty(name));
					index++;
				}
			}
			// setWorkflow(XMLUtil.xmlElementToString((workflow.toXML()));
			return inputNameVals;
		} catch (GraphException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (ComponentException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		return null;
	}

	public void setInputs(Properties inputList) {
		try {
			Workflow workflow = new Workflow(this.workflow);
			List<WSComponentPort> inputs = workflow.getInputs();
			for (WSComponentPort input : inputs) {
				input.setValue(inputList.getProperty(input.getName()));
			}
		} catch (GraphException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (ComponentException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
	}

	public String runWorkflow(String topic) {
		return runWorkflow(topic, (String) null);
	}

	public String runWorkflow(String topic, String user) {
		return runWorkflow(topic, user, null, topic);
	}

	public String runWorkflow(String topic, String user, String metadata,
			String workflowInstanceName) {
		return runWorkflow(topic, user, metadata, workflowInstanceName, builder);
	}

	public String runWorkflow(String topic, String user, String metadata,
			String workflowInstanceName, WorkflowContextHeaderBuilder builder) {
		String worflowoutput = null;
		try {
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(
					getClientConfiguration().getXbayaServiceURL().toString());
			OMElement omElement = AXIOMUtil.stringToOM(XMLUtil
					.xmlElementToString(builder.getXml()));
			stub._getServiceClient().addHeader(omElement);
			worflowoutput = stub.launchWorkflow(workflow, topic, null);
			runPreWorkflowExecutionTasks(worflowoutput, user, metadata,
					workflowInstanceName);

		} catch (AxisFault e) {

		} catch (RemoteException e) {
			// log.fine(e.getMessage(), e);
		} catch (RegistryException e) {
			// log.fine(e.getMessage(), e);
		} catch (XMLStreamException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		// log.info("Workflow output : " + worflowoutput);
		return worflowoutput;
	}

	public Monitor getWorkflowExecutionMonitor(String topic) {
		return getWorkflowExecutionMonitor(topic, null);
	}

	public Monitor getWorkflowExecutionMonitor(String topic,
			MonitorEventListener listener) {
		final String fTopic = topic;
		try {
			monitorConfiguration = new MonitorConfiguration(new URI(
					configuration.get(BROKER)), fTopic, true, new URI(
					configuration.get(MSGBOX)));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		final Monitor monitor = new Monitor(monitorConfiguration);
		monitor.setPrint(true);
		monitor.getEventData().registerEventListener(listener);
		return monitor;
	}

	private void runPreWorkflowExecutionTasks(String topic, String user,
			String metadata, String experimentName) throws RegistryException {
		if (user != null) {
			getProvenanceResouceClient().updateExperimentExecutionUser(topic, user);
		}
		if (metadata != null) {
			getProvenanceResouceClient().updateExperimentMetadata(topic, metadata);
		}
		if (experimentName == null) {
			experimentName = topic;
		}
		getProvenanceResouceClient().updateExperimentName(topic, experimentName);
	}

	public String runWorkflow(String topic, NameValue[] inputs)
			throws Exception {
		return runWorkflow(topic, inputs, null);
	}

	public String runWorkflow(String topic, NameValue[] inputs, String user)
			throws Exception {
		return runWorkflow(topic, inputs, user, null, topic);
	}

	public String runWorkflow(final String topic, final NameValue[] inputs,
			final String user, final String metadata,
			final String experimentName) throws Exception {
		return runWorkflow(topic, inputs, user, metadata, experimentName,
				builder);
	}

	public String runWorkflow(final String topic, final NameValue[] inputs,
			final String user, final String metadata,
			final String experimentName,
			final WorkflowContextHeaderBuilder builder) throws AiravataAPIInvocationException {
		return runWorkflow(topic, inputs, user, metadata, experimentName,
				builder, true);
	}

	private static int TIMEOUT_STEP = 1000;
	private static int MAX_TIMEOUT = 60000;

	public String runWorkflow(final String topic, final NameValue[] inputs,
			final String user, final String metadata,
			final String experimentName,
			final WorkflowContextHeaderBuilder builder, boolean launchOnThread)
			throws AiravataAPIInvocationException {
		try {
			runPreWorkflowExecutionTasks(topic, user, metadata, experimentName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
		if (launchOnThread) {
			new Thread(new Runnable() {
				public void run() {
					launchWorkflow(topic, inputs, builder);
				}
			}).start();
			int timeout = 0;
			try {
				while (!getExperimentResourceClient().isExperimentExists(topic)
						&& timeout < MAX_TIMEOUT) {
					Thread.sleep(TIMEOUT_STEP);
					timeout += MAX_TIMEOUT;
				}
			} catch (InterruptedException e) {
				throw new AiravataAPIInvocationException(e);
			}
		} else {
			launchWorkflow(topic, inputs, builder);
		}
		return topic;
	}

	public List<ExperimentData> getWorkflowExecutionDataByUser(String user)
			throws RegistryException {
		return getProvenanceResouceClient().getExperimentByUser(user);
	}

	public ExperimentData getWorkflowExecutionData(String topic)
			throws RegistryException {
		return getProvenanceResouceClient().getExperiment(topic);
	}

	public List<ExperimentData> getWorkflowExecutionData(String user,
			int pageSize, int PageNo) throws RegistryException {
        return null;
//		return getProvenanceResouceClient().getExperimentByUser(user, pageSize, PageNo);
	}

	public static String getWorkflow() {
		return workflow;
	}

	public static void setWorkflow(String workflow) {
		AiravataClient.workflow = workflow;
	}

	public ConfigurationResourceClient getConfigurationResourceClient (){
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        ConfigurationResourceClient configurationResourceClient = new ConfigurationResourceClient(getCurrentUser(), passwordCallBack);
        return configurationResourceClient;
    }

    public DescriptorResourceClient getDescriptorResourceClient (){
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        DescriptorResourceClient descriptorResourceClient = new DescriptorResourceClient(getCurrentUser(), passwordCallBack);
        return descriptorResourceClient;
    }

    public ExperimentResourceClient getExperimentResourceClient (){
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        ExperimentResourceClient experimentResourceClient = new ExperimentResourceClient(getCurrentUser(), passwordCallBack);
        return experimentResourceClient;
    }

    public ProvenanceResourceClient getProvenanceResouceClient (){
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        ProvenanceResourceClient provenanceResourceClient = new ProvenanceResourceClient(getCurrentUser(), passwordCallBack);
        return provenanceResourceClient;
    }

    public ProjectResourceClient getProjectResourceClient (){
        PasswordCallBackImpl passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        ProjectResourceClient projectResourceClient = new ProjectResourceClient(getCurrentUser(), passwordCallBack);
        return projectResourceClient;
    }

    public PublishedWorkflowResourceClient getPublishedWFResourceClient (){
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        PublishedWorkflowResourceClient publishedWorkflowResourceClient = new PublishedWorkflowResourceClient(getCurrentUser(), passwordCallBack);
        return publishedWorkflowResourceClient;
    }

    public UserWorkflowResourceClient getUserWFResourceClient () {
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        UserWorkflowResourceClient userWorkflowResourceClient = new UserWorkflowResourceClient(getCurrentUser(), passwordCallBack);
        return userWorkflowResourceClient;
    }

    public BasicRegistryResourceClient getBasicResourceClient (){
        passwordCallBack = new PasswordCallBackImpl(getCurrentUser(), getPassword());
        BasicRegistryResourceClient basicRegistryResourceClient = new BasicRegistryResourceClient(getCurrentUser(), passwordCallBack);
        return basicRegistryResourceClient;
    }

//    public AiravataRegistry2 getRegistry() throws RegistryException {
//		if (registry == null) {
//			String jcrUsername = getClientConfiguration().getJcrUsername();
//			String jcrPassword = getClientConfiguration().getJcrPassword();
//			registry = getRegistryObject(jcrUsername, jcrPassword);
//		}
//		return registry;
//	}

	private static AiravataRegistry2 getRegistryObject(String jcrUsername,
			String jcrPassword) throws RegistryException {
		AiravataRegistry2 registry = new JCRComponentRegistry(jcrUsername,
				jcrPassword).getRegistry();
		return registry;
	}

	public AiravataClientConfiguration getClientConfiguration() {
		if (clientConfiguration == null) {
			clientConfiguration = new AiravataClientConfiguration();
		}
		return clientConfiguration;
	}

	private String validateAxisService(String urlString)
			throws RegistryException {
		String originalURL = urlString;
		if (!urlString.endsWith("?wsdl")) {
			urlString = urlString + "?wsdl";
		}
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.connect();
		} catch (MalformedURLException e) {
			// the URL is not in a valid form
			throw new RegistryException("Given Axis2 Service URL : "
					+ urlString + " is Invalid", e);
		} catch (IOException e) {
			// the connection couldn't be established
			throw new RegistryException("Given Axis2 Service URL : "
					+ urlString + " is Invalid", e);
		}
		return originalURL;
	}

	private String validateURL(String urlString) throws RegistryException {
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.connect();
		} catch (MalformedURLException e) {
			// the URL is not in a valid form
			throw new RegistryException("Given URL: " + urlString
					+ " is Invalid", e);
		} catch (IOException e) {
			// the connection couldn't be established
			throw new RegistryException("Given URL: " + urlString
					+ " is Invalid", e);
		}
		return urlString;
	}

	public List<String> getWorkflowTemplateIds() {
		List<String> workflowList = new ArrayList<String>();
		Map<String, String> workflows;
		try {
			workflows = getUserWFResourceClient().getWorkflows();
			for (String name : workflows.keySet()) {
				workflowList.add(name);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workflowList;
	}

	public String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs) throws Exception {
		return runWorkflow(workflowTemplateId, inputs, getBasicResourceClient().getUser()
				.getUserName(), null, workflowTemplateId + "_"
				+ Calendar.getInstance().getTime().toString());
	}

	public String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs, String workflowInstanceName)
			throws Exception {
		return runWorkflow(workflowTemplateId, inputs, getBasicResourceClient().getUser()
				.getUserName(), null, workflowInstanceName);
	}

	public String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata,
			String workflowInstanceName) throws Exception {
		Workflow workflowObj = getWorkflow(workflowTemplateId);
		return runWorkflow(workflowObj, inputs, user, metadata,
				workflowInstanceName, builder);
	}

	public String runWorkflow(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata,
			String workflowInstanceName, WorkflowContextHeaderBuilder builder)
			throws Exception {
		Workflow workflowObj = getWorkflow(workflowTemplateId);
		return runWorkflow(workflowObj, inputs, user, metadata,
				workflowInstanceName, builder);
	}

	public String runWorkflow(Workflow workflow, List<WorkflowInput> inputs,
			String workflowInstanceName) throws GraphException,
			ComponentException, Exception {
		return runWorkflow(workflow, inputs, null, null, workflowInstanceName,
				builder);
	}

	public String runWorkflow(Workflow workflowObj, List<WorkflowInput> inputs,
			String user, String metadata, String workflowInstanceName)
			throws GraphException, ComponentException, Exception {
		return runWorkflow(workflowObj, inputs, user, metadata,
				workflowInstanceName, builder);
	}

	public String runWorkflow(Workflow workflowObj, List<WorkflowInput> inputs,
			String user, String metadata, String workflowInstanceName,
			WorkflowContextHeaderBuilder builder) throws AiravataAPIInvocationException{
		try {
			String workflowString = XMLUtil.xmlElementToString(workflowObj
					.toXML());
			List<WSComponentPort> ports = getWSComponentPortInputs(workflowObj);
			for (WorkflowInput input : inputs) {
				WSComponentPort port = getWSComponentPort(input.getName(),
						ports);
				if (port != null) {
					port.setValue(input.getValue());
				}
			}
			List<NameValue> inputValues = new ArrayList<NameValue>();
			for (WSComponentPort port : ports) {
				NameValue nameValue = new NameValue();
				nameValue.setName(port.getName());
				if (port.getValue() == null) {
					nameValue.setValue(port.getDefaultValue());
				} else {
					nameValue.setValue(port.getValue().toString());
				}
				inputValues.add(nameValue);
			}
			workflow = workflowString;
			String topic = workflowObj.getName() + "_" + UUID.randomUUID();
			getProvenanceResouceClient().setWorkflowInstanceTemplateName(topic,
                    workflowObj.getName());
			return runWorkflow(topic, inputValues.toArray(new NameValue[] {}),
					user, metadata, workflowInstanceName, builder);
		} catch (GraphException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (ComponentException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
	}

	public String runWorkflow(String workflowName, List<WorkflowInput> inputs,
			String user, String metadata, String workflowInstanceName,
			String experimentID) throws AiravataAPIInvocationException {
		try {
			Workflow workflowObj = getWorkflow(workflowName);
			String workflowString = XMLUtil.xmlElementToString(workflowObj
					.toXML());
			List<WSComponentPort> ports;
			ports = getWSComponentPortInputs(workflowObj);
			for (WorkflowInput input : inputs) {
				WSComponentPort port = getWSComponentPort(input.getName(),
						ports);
				if (port != null) {
					port.setValue(input.getValue());
				}
			}
			List<NameValue> inputValues = new ArrayList<NameValue>();
			for (WSComponentPort port : ports) {
				NameValue nameValue = new NameValue();
				nameValue.setName(port.getName());
				if (port.getValue() == null) {
					nameValue.setValue(port.getDefaultValue());
				} else {
					nameValue.setValue(port.getValue().toString());
				}
				inputValues.add(nameValue);
			}
			workflow = workflowString;
			if (experimentID == null || experimentID.isEmpty()) {
				experimentID = workflowObj.getName() + "_" + UUID.randomUUID();
			}
			getProvenanceResouceClient().setWorkflowInstanceTemplateName(experimentID,
                    workflowObj.getName());
			return runWorkflow(experimentID,
					inputValues.toArray(new NameValue[] {}), user, metadata,
					workflowInstanceName, this.builder);
		}  catch (GraphException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (ComponentException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (Exception e) {
            throw new AiravataAPIInvocationException(
                    "Error working with Airavata Registry: "
                            + e.getLocalizedMessage(), e);
        }
	}

	public List<WorkflowInput> getWorkflowInputs(String workflowTemplateId)
			throws AiravataAPIInvocationException {
		try {
			Workflow workflowTemplate = getWorkflow(workflowTemplateId);
			List<WSComponentPort> inputs = getWSComponentPortInputs(workflowTemplate);
			List<InputNode> inputNodes = getInputNodes(workflowTemplate);
			List<WorkflowInput> results = new ArrayList<WorkflowInput>();
			for (InputNode port : inputNodes) {
				Object value = null;
				WSComponentPort wsComponentPort = getWSComponentPort(
						port.getName(), inputs);
				if (wsComponentPort != null) {
					value = wsComponentPort.getValue();
				}
				results.add(new WorkflowInput(port.getName(), port
						.getParameterType().getLocalPart(), port
						.getDefaultValue(), value, !port.isVisibility()));
			}
			return results;
		} catch (AiravataAPIInvocationException e) {
			throw e;
		}catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	public String getWorkflowAsString(String workflowTemplateId)
			throws AiravataAPIInvocationException {
		try {
			Map<String, String> workflows = getUserWFResourceClient().getWorkflows();
			for (String name : workflows.keySet()) {
				if (name.equals(workflowTemplateId)) {
					return workflows.get(name);
				}
			}
			return null;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}

	}

	private List<WSComponentPort> getWSComponentPortInputs(
			String workflowTemplateId) throws AiravataAPIInvocationException{
		Workflow workflow = getWorkflow(workflowTemplateId);
		try {
			return getWSComponentPortInputs(workflow);
		} catch (GraphException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (ComponentException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	private Workflow getWorkflow(String workflowTemplateId)
			throws AiravataAPIInvocationException {
		try {
			Workflow workflow = new Workflow(
					getWorkflowAsString(workflowTemplateId));
			return workflow;
		} catch (GraphException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (ComponentException e) {
			throw new AiravataAPIInvocationException(e);
		}

	}

	private List<WSComponentPort> getWSComponentPortInputs(Workflow workflow)
			throws GraphException, ComponentException {
		workflow.createScript();
		List<WSComponentPort> inputs = workflow.getInputs();
		return inputs;
	}

	private List<InputNode> getInputNodes(String workflowTemplateId)
			throws AiravataAPIInvocationException {
		Workflow workflow = getWorkflow(workflowTemplateId);
		return getInputNodes(workflow);
	}

	private List<InputNode> getInputNodes(Workflow workflow) {
		List<InputNode> inputNodes = GraphUtil.getInputNodes(workflow
				.getGraph());
		return inputNodes;
	}

	private WSComponentPort getWSComponentPort(String name,
			List<WSComponentPort> ports) {
		for (WSComponentPort port : ports) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	public static WorkflowContextHeaderBuilder getBuilder() {
		return builder;
	}

	private static void addNode(Node parentNode, Node childNode)
			throws RepositoryException {
		Node node;
		String childNodeName = childNode.getName();
		if (!parentNode.hasNode(childNodeName)) {
			node = parentNode.addNode(childNodeName);
		} else {
			node = parentNode.getNode(childNodeName);
		}
		System.out.println(node.getPath());
		PropertyIterator childProperties = childNode.getProperties();
		while (childProperties.hasNext()) {
			Property childProperty = childProperties.nextProperty();
			if (!(childProperty.getName().startsWith("jcr:") || childProperty
					.getName().startsWith("rep:"))) {
				if (childProperty.isMultiple()) {
					node.setProperty(childProperty.getName(),
							childProperty.getValues());
				} else {
					node.setProperty(childProperty.getName(),
							childProperty.getValue());
				}

			}
		}
		NodeIterator children = childNode.getNodes();
		while (children.hasNext()) {
			Node c = children.nextNode();
			addNode(node, c);
		}
	}

	// private static void migrateRespositoryData(
	// AiravataJCRRegistry sourceRegistry,
	// AiravataJCRRegistry targetRegistry) throws Exception {
	// Session session1 = null;
	// Session session2 = null;
	// try {
	// session1 = sourceRegistry.getRepository().login(new
	// SimpleCredentials(sourceRegistry.getUsername(), new
	// String(sourceRegistry.getPassword()).toCharArray()));
	// session2 = targetRegistry.getRepository().login(new
	// SimpleCredentials(targetRegistry.getUsername(), new
	// String(targetRegistry.getPassword()).toCharArray()));
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// }
	//
	//
	// NodeIterator nodes = session1.getRootNode().getNodes();
	// Node rootNode = session2.getRootNode();
	// List<String> ignoreRoots=Arrays.asList(new
	// String[]{"/AIRAVATA_CONFIGURATION_DATA"});
	// while(nodes.hasNext()){
	// Node nextNode = nodes.nextNode();
	// String path = nextNode.getPath();
	// if (!(path.equals("/jcr:system")||path.equals("/rep:policy") ||
	// ignoreRoots.contains(path))) {
	// addNode(rootNode,nextNode);
	// System.out.println();
	// }
	// }
	// System.out.print("Saving session.");
	// session1.logout();
	// System.out.print(".");
	// session2.save();
	// System.out.print(".");
	// session2.logout();
	// System.out.println(".done");
	// }

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
			executionManagerImpl = new ExecutionManagerImpl(this);
		}
		return executionManagerImpl;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public List<String> getWorkflowServiceNodeIDs(String templateID) throws AiravataAPIInvocationException {
		try {
			Workflow workflow = new Workflow(getWorkflowAsString(templateID));
			return workflow.getWorkflowServiceNodeIDs();
		}  catch (GraphException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (ComponentException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (AiravataAPIInvocationException e) {
			throw e;
		}
	}

	public Version getVersion() {
		return API_VERSION;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	private void launchWorkflow(final String topic, final NameValue[] inputs,
			final WorkflowContextHeaderBuilder builder) {
		try {
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(
					getClientConfiguration().getXbayaServiceURL().toString());
			stub._getServiceClient().addHeader(
					AXIOMUtil.stringToOM(XMLUtil.xmlElementToString(builder
							.getXml())));
			stub.launchWorkflow(workflow, topic, inputs);
			// log.info("Workflow output : " + worflowoutput);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRegitryURI(URI regitryURI) {
        this.regitryURI = regitryURI;
    }

    public String getPassword() {
        return password;
    }

    public URI getRegitryURI() {
        return regitryURI;
    }
}
