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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFormatException;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataManager;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.client.api.ExecutionManager;
import org.apache.airavata.client.api.ProvenanceManager;
import org.apache.airavata.client.api.UserManager;
import org.apache.airavata.client.api.WorkflowManager;
import org.apache.airavata.client.impl.AiravataManagerImpl;
import org.apache.airavata.client.impl.ApplicationManagerImpl;
import org.apache.airavata.client.impl.ExecutionManagerImpl;
import org.apache.airavata.client.impl.ProvenanceManagerImpl;
import org.apache.airavata.client.impl.UserManagerImpl;
import org.apache.airavata.client.impl.WorkflowManagerImpl;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.registry.JCRComponentRegistry;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorStub;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorEventListener;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;

public class AiravataClient implements AiravataAPI {
//	private static final MLogger log = MLogger.getLogger();

	public static final String GFAC = "gfac";
	public static final String JCR = "jcr";
	public static final String PROXYSERVER = "proxyserver";
	public static final String MSGBOX = "msgbox";
	public static final String BROKER = "broker";
	public static final String DEFAULT_GFAC_URL = "gfac.url";
	public static final String DEFAULT_MYPROXY_SERVER = "myproxy.url";
	public static final String DEFAULT_MESSAGE_BOX_URL = "messagebox.url";
	public static final String DEFAULT_BROKER_URL = "messagebroker.url";
	public static final String DEFAULT_JCR_URL = "jcr.url";
	public static final String JCR_USERNAME = "jcr.username";
	public static final String JCR_PASSWORD = "jcr.password";
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
    
	private AiravataRegistry2 registry;

    private Map<String, String> configuration = new HashMap<String, String>();
	private AiravataManagerImpl airavataManagerImpl;
	private ApplicationManagerImpl applicationManagerImpl;
	private WorkflowManagerImpl workflowManagerImpl;
	private ProvenanceManagerImpl provenanceManagerImpl;
	private UserManagerImpl userManagerImpl;
	private ExecutionManagerImpl executionManagerImpl;

	// private NameValue[] configurations = new NameValue[7];

	private static final Version API_VERSION=new Version("Airavata",0,5,null,null,null);
	
	protected AiravataClient(Map<String,String> configuration)
			throws MalformedURLException {
		this.configuration = configuration;
		initialize();
	}

	protected AiravataClient(String fileName) throws RegistryException,
			MalformedURLException, IOException {
		URL url = this. getClass().getClassLoader().getResource(fileName);
		if (url == null) {
			url = (new File(fileName)).toURL();
		}
		Properties properties = new Properties();
		properties.load(url.openStream());

		configuration.put(GFAC,validateAxisService(properties
				.getProperty(DEFAULT_GFAC_URL)));
		configuration.put(MSGBOX, validateAxisService(properties
                .getProperty(DEFAULT_MESSAGE_BOX_URL)));
		configuration.put(BROKER,validateAxisService(properties
				.getProperty(DEFAULT_BROKER_URL)));
		configuration.put(WORKFLOWSERVICEURL,validateAxisService(properties
				.getProperty(WORKFLOWSERVICEURL)));
		configuration.put(JCR,properties
				.getProperty(DEFAULT_JCR_URL));
		configuration.put(JCR_USERNAME,properties.getProperty(JCR_USERNAME));

		configuration.put(JCR_PASSWORD,properties.getProperty(JCR_PASSWORD));

		configuration.put(WITHLISTENER,properties.getProperty(WITHLISTENER));

		initialize();        
	}

	protected AiravataClient(URI registryUrl, String username, String password) throws MalformedURLException, RepositoryException, RegistryException {
		this(createConfig(registryUrl, username, password));
	}

	private static HashMap<String, String> createConfig(URI registryUrl, String username, String password) throws RepositoryException, RegistryException {
		HashMap<String, String> config = new HashMap<String,String>();
		config.put(AiravataClient.JCR,registryUrl.toString());
		config.put(AiravataClient.JCR_USERNAME,username);
		config.put(AiravataClient.JCR_PASSWORD,password);
		AiravataRegistry2 registryObject = getRegistryObject(username, password);
		if (registryObject!=null){
			URI uri = registryObject.getEventingServiceURI();
			config.put(AiravataClient.BROKER,uri==null? "http://localhost:8080/axis2/services/EventingService":uri.toString());
			uri = registryObject.getMessageBoxURI();
			config.put(AiravataClient.MSGBOX,uri==null? "http://localhost:8080/axis2/services/MsgBoxService":uri.toString());
			List<URI> URLList = registryObject.getWorkflowInterpreterURIs();
			config.put(AiravataClient.WORKFLOWSERVICEURL,URLList==null || URLList.size()==0? "http://localhost:8080/axis2/services/WorkflowInterpretor?wsdl":URLList.get(0).toString());
			List<URI> urlList = registryObject.getGFacURIs();
			config.put(AiravataClient.GFAC,urlList==null || urlList.size()==0? "http://localhost:8080/axis2/services/GFacService":urlList.get(0).toString());
			config.put(AiravataClient.WITHLISTENER,"true");
		}
		return config;
	}
	private void initialize() throws MalformedURLException {
		updateClientConfiguration(configuration);
		
        // At this point we do not know the workflowExperimentId
		builder = new WorkflowContextHeaderBuilder(configuration.get(BROKER),
        		configuration.get(GFAC),configuration.get(JCR),null,null,
        		configuration.get(MSGBOX));
		
		//TODO: At some point this should contain the current user the airavata client is 
		//logged in to the Airavata system
		setCurrentUser(getClientConfiguration().getJcrUsername());
	}

	private void updateClientConfiguration(Map<String,String> configuration)
			throws MalformedURLException {
		AiravataClientConfiguration clientConfiguration = getClientConfiguration();
			if (configuration.get(GFAC) != null) {
				clientConfiguration
						.setGfacURL(new URL(configuration.get(GFAC)));
			}
			if (configuration.get(MSGBOX)!= null) {
				clientConfiguration.setMessageboxURL(new URL(configuration.get(MSGBOX)));
			}
			if (configuration.get(BROKER)!= null) {
				clientConfiguration.setMessagebrokerURL(new URL(configuration.get(BROKER)));
			}
			if (configuration.get(JCR)!= null) {
				clientConfiguration
						.setJcrURL(new URL(configuration.get(JCR)));
			}
            if (configuration.get(WORKFLOWSERVICEURL)!= null) {
				clientConfiguration.setXbayaServiceURL(new URL(configuration.get(WORKFLOWSERVICEURL)));
			}
           if (configuration.get(MSGBOX)!= null) {
				clientConfiguration.setMessageboxURL(new URL(configuration.get(MSGBOX)));
			}
		
		if (clientConfiguration.getJcrURL()!=null && clientConfiguration.getGfacURL()==null){
			try {
				clientConfiguration.setGfacURL(getRegistry().getGFacURIs().get(0).toURL());
				configuration.put(GFAC,clientConfiguration.getGfacURL().toString());
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

	public static void updateWorkflowInputValuesFromProperties(List<WorkflowInput> inputs, String fileName) throws IOException{
		URL url = AiravataClient.class.getClassLoader().getResource(fileName);
		if (url == null) {
			url = (new File(fileName)).toURL();
		}
		Properties properties = new Properties();
		properties.load(url.openStream());
		for (WorkflowInput workflowInput : inputs) {
			if (properties.containsKey(workflowInput.getName())){
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

	public String runWorkflow(String topic, String user, String metadata, String workflowInstanceName) {
		return runWorkflow(topic, user, metadata, workflowInstanceName, builder);
	}
	
	public String runWorkflow(String topic, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder) {
        String worflowoutput = null;
        try {
            WorkflowInterpretorStub stub = new WorkflowInterpretorStub(
                    getClientConfiguration().getXbayaServiceURL().toString());
            OMElement omElement = AXIOMUtil.stringToOM(XMLUtil.xmlElementToString(builder.getXml()));
            stub._getServiceClient().addHeader(omElement);
            worflowoutput = stub.launchWorkflow(workflow, topic,null);
            runPreWorkflowExecutionTasks(worflowoutput, user, metadata, workflowInstanceName);

        } catch (AxisFault e) {
		} catch (RemoteException e) {
//			log.fine(e.getMessage(), e);
		} catch (RegistryException e) {
//			log.fine(e.getMessage(), e);
		} catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
//			log.info("Workflow output : " + worflowoutput);
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
    		String metadata,String experimentName) throws RegistryException {
		if (user != null) {
			getRegistry().updateExperimentExecutionUser(topic, user);
		}
		if (metadata != null) {
			getRegistry().updateExperimentMetadata(topic, metadata);
		}
		if (experimentName==null) {
			experimentName=topic;
		}
		getRegistry().updateExperimentName(topic, experimentName);
	}

    public String runWorkflow(String topic, NameValue[] inputs) throws Exception {
		return runWorkflow(topic, inputs, null);
	}

	public String runWorkflow(String topic, NameValue[] inputs, String user) throws Exception {
		return runWorkflow(topic, inputs, user, null,topic);
	}

	public String runWorkflow(final String topic, final NameValue[] inputs, final String user,
			final String metadata, final String experimentName) throws Exception{
		return runWorkflow(topic, inputs, user, metadata, experimentName, builder);
	}
	public String runWorkflow(final String topic, final NameValue[] inputs, final String user,
			final String metadata, final String experimentName, final WorkflowContextHeaderBuilder builder) throws Exception{
		return runWorkflow(topic, inputs, user, metadata, experimentName, builder, false);
	}
	
	public String runWorkflow(final String topic, final NameValue[] inputs, final String user,
			final String metadata, final String experimentName, final WorkflowContextHeaderBuilder builder, boolean launchOnThread) throws Exception{
		if (launchOnThread) {
			new Thread(new Runnable() {
				public void run() {
					launchWorkflow(topic, inputs, user, metadata, experimentName, builder);
				}
			}).start();
		}else{
			launchWorkflow(topic, inputs, user, metadata, experimentName, builder);
		}
		return topic;
	}

	public List<ExperimentData> getWorkflowExecutionDataByUser(String user)
			throws RegistryException {
		return getRegistry().getExperimentByUser(user);
	}

	public ExperimentData getWorkflowExecutionData(String topic)
			throws RegistryException {
		return getRegistry().getExperiment(topic);
	}

	public List<ExperimentData> getWorkflowExecutionData(String user,
			int pageSize, int PageNo) throws RegistryException {
		return getRegistry().getExperimentByUser(user, pageSize, PageNo);
	}

	public static String getWorkflow() {
		return workflow;
	}

	public static void setWorkflow(String workflow) {
		AiravataClient.workflow = workflow;
	}

	public AiravataRegistry2 getRegistry() throws RegistryException {
		if (registry == null) {
				String jcrUsername = getClientConfiguration().getJcrUsername();
				String jcrPassword = getClientConfiguration().getJcrPassword();
				registry = getRegistryObject(jcrUsername, jcrPassword);
        }
		return registry;
	}

	private static AiravataRegistry2 getRegistryObject(
                                                      String jcrUsername,
                                                      String jcrPassword)
            throws RegistryException {
        AiravataRegistry2 registry = new JCRComponentRegistry(jcrUsername,jcrPassword).getRegistry();
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
		String originalURL=urlString;
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
			workflows = getRegistry().getWorkflows();
			for (String name : workflows.keySet()) {
				workflowList.add(name);
			}
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workflowList;
	}
	
	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs) throws Exception{
		return runWorkflow(workflowTemplateId,inputs,getRegistry().getUser().getUserName(),null,workflowTemplateId+"_"+Calendar.getInstance().getTime().toString());
	}
	
	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs,String workflowInstanceName) throws Exception{
		return runWorkflow(workflowTemplateId,inputs,getRegistry().getUser().getUserName(),null,workflowInstanceName);
	}
	
	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName) throws Exception{
		Workflow workflowObj = getWorkflow(workflowTemplateId);
		return runWorkflow(workflowObj, inputs, user, metadata,workflowInstanceName,builder);
	}

	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder) throws Exception{
		Workflow workflowObj = getWorkflow(workflowTemplateId);
		return runWorkflow(workflowObj, inputs, user, metadata,workflowInstanceName,builder);
	}
	
	public String runWorkflow(Workflow workflow,
			List<WorkflowInput> inputs, String workflowInstanceName)
			throws GraphException, ComponentException, Exception {
		return runWorkflow(workflow, inputs, null, null, workflowInstanceName,builder);
	}

	public String runWorkflow(Workflow workflowObj,
			List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName)
			throws GraphException, ComponentException, Exception {
		return runWorkflow(workflowObj, inputs, user, metadata, workflowInstanceName,builder);
	}
	
	public String runWorkflow(Workflow workflowObj,
			List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName, WorkflowContextHeaderBuilder builder)
			throws GraphException, ComponentException, Exception {
		try {
			String workflowString=XMLUtil.xmlElementToString(workflowObj.toXML());
			List<WSComponentPort> ports = getWSComponentPortInputs(workflowObj);
			for (WorkflowInput input : inputs) {
				WSComponentPort port = getWSComponentPort(input.getName(), ports);
				if (port!=null){
					port.setValue(input.getValue());
				}
			}
			List<NameValue> inputValues=new ArrayList<NameValue>();
			for (WSComponentPort port : ports) {
				NameValue nameValue = new NameValue();
				nameValue.setName(port.getName());
				if (port.getValue()==null){
					nameValue.setValue(port.getDefaultValue());
				}else{
					nameValue.setValue(port.getValue().toString());
				}
				inputValues.add(nameValue);
			}
			workflow=workflowString;
			String topic=workflowObj.getName()+"_"+UUID.randomUUID();
			getRegistry().setWorkflowInstanceTemplateName(topic, workflowObj.getName());
			return runWorkflow(topic, inputValues.toArray(new NameValue[]{}), user, metadata, workflowInstanceName,builder);
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<WorkflowInput> getWorkflowInputs(String workflowTemplateId) throws Exception{
		try {
			Workflow workflowTemplate = getWorkflow(workflowTemplateId);
            List<WSComponentPort> inputs = getWSComponentPortInputs(workflowTemplate);
	        List<InputNode> inputNodes = getInputNodes(workflowTemplate);
			List<WorkflowInput> results=new ArrayList<WorkflowInput>();
			for (InputNode port : inputNodes) {
				Object value=null;
				WSComponentPort wsComponentPort = getWSComponentPort(port.getName(), inputs);
				if (wsComponentPort!=null){
					value=wsComponentPort.getValue();
				}
				results.add(new WorkflowInput(port.getName(), port.getParameterType().getLocalPart(), port.getDefaultValue(), value, !port.isVisibility()));
			}
			return results;
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getWorkflowAsString(String workflowTemplateId)
			throws RegistryException, PathNotFoundException,
			RepositoryException {
		Map<String, String> workflows = getRegistry().getWorkflows();
		for (String name : workflows.keySet()) {
			if (name.equals(workflowTemplateId)){
				return workflows.get(name);
			}
		}
		return null;
	}
	

	private List<WSComponentPort> getWSComponentPortInputs(
			String workflowTemplateId) throws RegistryException,
			PathNotFoundException, RepositoryException, GraphException,
			ComponentException, ValueFormatException {
		Workflow workflow = getWorkflow(workflowTemplateId);
		return getWSComponentPortInputs(workflow);
	}

	private Workflow getWorkflow(String workflowTemplateId)
			throws RegistryException, PathNotFoundException,
			RepositoryException, GraphException, ComponentException,
			ValueFormatException {
		Workflow workflow = new Workflow(getWorkflowAsString(workflowTemplateId));
		return workflow;
	}

	private List<WSComponentPort> getWSComponentPortInputs(Workflow workflow)
			throws GraphException, ComponentException {
		workflow.createScript();
		List<WSComponentPort> inputs = workflow.getInputs();
		return inputs;
	}
	
	private List<InputNode> getInputNodes(String workflowTemplateId) throws PathNotFoundException, GraphException, ComponentException, ValueFormatException, RegistryException, RepositoryException{
		Workflow workflow = getWorkflow(workflowTemplateId);
		return getInputNodes(workflow);
	}
	
	private List<InputNode> getInputNodes(Workflow workflow) {
		List<InputNode> inputNodes = GraphUtil.getInputNodes(workflow.getGraph());
		return inputNodes;
	}
	
	private WSComponentPort getWSComponentPort(String name,List<WSComponentPort> ports){
		for (WSComponentPort port : ports) {
			if (port.getName().equals(name)){
				return port;
			}
		}
		return null;
	}
	
	public static WorkflowContextHeaderBuilder getBuilder() {
	       return builder;
	}

 	public static void main(String[] args) throws Exception {
		AiravataAPI api = AiravataClientUtils.getAPI(new URI("http://localhost:8080"), "admin", "admin");
		Workflow w = api.getWorkflowManager().getWorkflow("Workflow1");
		List<WorkflowInput> workflowInputs = w.getWorkflowInputs();
		for (WorkflowInput input : workflowInputs) {
			input.setValue("0");
		}
		System.out.println(api.getExecutionManager().runExperiment(w,workflowInputs));
// 		ProvenanceManager pm = api.getProvenanceManager();
// 		ExperimentData workflowExperimentData = pm.getWorkflowExperimentData("Workflow1_9341caee-b3fc-4474-9b15-b943756a5839");
 		
// 		pm.getExperimentIdList(owner)
// 		workflowInstanceData = d.getWorkflowInstanceData().get(0).getNodeDataList();
// 		d.get
// 		pm.setWorkflowInstanceNodeOutput(new WorkflowInstanceNode(new WorkflowInstance("test", "test"), "test_node"), "some_data");
// 		pm.setWorkflowInstanceStatus(new WorkflowInstanceStatus(new WorkflowInstance("test", "test"), ExecutionStatus.RUNNING));
// 		List<String> experiments = pm.getExperiments();
// 		for (String id : experiments) {
//			System.out.println(id);
//		}
// 		api.getAiravataManager().getEventingServiceURL();
// 		System.out.println(api.getAiravataManager().getEventingServiceURL());
 		
//// 		AiravataAPI api = AiravataClientUtils.getAPI(new URI("http://gf7.ucs.indiana.edu:8030/jackrabbit/rmi"), "admin", "admin");
//// 		System.out.println(api.getAiravataManager().getWorkflowInterpreterServiceURL());
//// 		System.exit(0);
//// 		WorkflowSchedulingContext workflowSchedulingContext= WorkflowSchedulingContext.Factory.newInstance();
////		WorkflowContextHeaderBuilder workflowContextHeader = api.getExecutionManager().createWorkflowContextHeader();
////		workflowContextHeader.setWorkflowSchedulingContext(workflowSchedulingContext);
////		workflowSchedulingContext.addNewApplicationSchedulingContext();
//// 		System.out.println(workflowContextHeader.getWorkflowSchedulingContext());
//// 		System.exit(0);
// 		
//		HashMap<String, String> map;
//		URI uri;
//		
//		uri=new URI("http://gw56.quarry.iu.teragrid.org:8090/jackrabbit-webapp-2.4.0/rmi");
////		uri=new URI("http://localhost:8081/rmi");
//		map = new HashMap<String, String>();
//		map.put("org.apache.jackrabbit.repository.uri", uri.toString());
//		AiravataJCRRegistry reg1 = new AiravataJCRRegistry(uri, "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory", "admin","admin", map);
//		
//		uri=new URI("http://gw26.quarry.iu.teragrid.org:8090/jackrabbit-webapp-2.4.0/rmi");
////		uri=new URI("http://localhost:8082/rmi");
//		map = new HashMap<String, String>();
//		map.put("org.apache.jackrabbit.repository.uri", uri.toString());
//		AiravataJCRRegistry reg2 = new AiravataJCRRegistry(uri, "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory", "admin","admin", map);
////		Session login = reg2.getRepository().login(new SimpleCredentials("admin","admin".toCharArray()));
////		login.getRootNode().getNode("experiments").remove();
////		login.getRootNode().getNode("AIRAVATA_CONFIGURATION_DATA").remove();
////		login.save();
////		login.logout();
////		migrateRespositoryData(reg1, reg2);
//		System.exit(0);
	}
 	
	private static void addNode(Node parentNode, Node childNode) throws RepositoryException{
		Node node;
		String childNodeName = childNode.getName();
		if (!parentNode.hasNode(childNodeName)){
			node=parentNode.addNode(childNodeName);
		}else{
			node=parentNode.getNode(childNodeName);
		}
		System.out.println(node.getPath());
		PropertyIterator childProperties = childNode.getProperties();
		while(childProperties.hasNext()){
			Property childProperty = childProperties.nextProperty();
			if (!(childProperty.getName().startsWith("jcr:") || childProperty.getName().startsWith("rep:"))) {
				if (childProperty.isMultiple()){
					node.setProperty(childProperty.getName(),
							childProperty.getValues());
				}else{
					node.setProperty(childProperty.getName(),
							childProperty.getValue());
				}
				
			}
		}
		NodeIterator children = childNode.getNodes();
		while(children.hasNext()){
			Node c=children.nextNode();
			addNode(node,c);
		}
	}
	
	private static void migrateRespositoryData(
			AiravataJCRRegistry sourceRegistry,
			AiravataJCRRegistry targetRegistry) throws Exception {
		Session session1 = null;
		Session session2 = null;
		try {
			session1 = sourceRegistry.getRepository().login(new SimpleCredentials(sourceRegistry.getUsername(), new String(sourceRegistry.getPassword()).toCharArray()));
			session2 = targetRegistry.getRepository().login(new SimpleCredentials(targetRegistry.getUsername(), new String(targetRegistry.getPassword()).toCharArray()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		
		NodeIterator nodes = session1.getRootNode().getNodes();
		Node rootNode = session2.getRootNode();
		List<String> ignoreRoots=Arrays.asList(new String[]{"/AIRAVATA_CONFIGURATION_DATA"});
		while(nodes.hasNext()){
			Node nextNode = nodes.nextNode();
			String path = nextNode.getPath();
			if (!(path.equals("/jcr:system")||path.equals("/rep:policy") || ignoreRoots.contains(path))) {
				addNode(rootNode,nextNode);
				System.out.println();
			}
		}
		System.out.print("Saving session.");
		session1.logout();
		System.out.print(".");
		session2.save();
		System.out.print(".");
		session2.logout();
		System.out.println(".done");
	}
 		 	
	public AiravataManager getAiravataManager() {
		if (airavataManagerImpl==null) {
			airavataManagerImpl = new AiravataManagerImpl(this);
		}
		return airavataManagerImpl;
	}

	public ApplicationManager getApplicationManager() {
		if (applicationManagerImpl==null) {
			applicationManagerImpl = new ApplicationManagerImpl(this);
		}
		return applicationManagerImpl;
	}

	public WorkflowManager getWorkflowManager() {
		if (workflowManagerImpl==null) {
			workflowManagerImpl = new WorkflowManagerImpl(this);
		}
		return workflowManagerImpl;
	}

	public ProvenanceManager getProvenanceManager() {
		if (provenanceManagerImpl==null) {
			provenanceManagerImpl = new ProvenanceManagerImpl(this);
		}
		return provenanceManagerImpl;
	}

	public UserManager getUserManager() {
		if (userManagerImpl==null) {
			userManagerImpl = new UserManagerImpl(this);
		}
		return userManagerImpl;
	}

	public ExecutionManager getExecutionManager() {
		if (executionManagerImpl==null) {
			executionManagerImpl = new ExecutionManagerImpl(this);
		}
		return executionManagerImpl;
	}

	public String getCurrentUser() {
		return currentUser;
	}


    public List<String> getWorkflowServiceNodeIDs(String templateID) {
        try {
            Workflow workflow = new Workflow(getWorkflowAsString(templateID));
            return workflow.getWorkflowServiceNodeIDs();
        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (GraphException e) {
            e.printStackTrace();
        } catch (ComponentException e) {
            e.printStackTrace();
        }
        return null;
    }
    
	public Version getVersion() {
		return API_VERSION;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	private void launchWorkflow(final String topic, final NameValue[] inputs,
			final String user, final String metadata,
			final String experimentName,
			final WorkflowContextHeaderBuilder builder) {
		try {
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(
					getClientConfiguration().getXbayaServiceURL()
							.toString());
			stub._getServiceClient().addHeader(
					AXIOMUtil.stringToOM(XMLUtil
							.xmlElementToString(builder.getXml())));
			runPreWorkflowExecutionTasks(topic, user, metadata,experimentName);
			stub.launchWorkflow(workflow, topic, inputs);
			//			log.info("Workflow output : " + worflowoutput);
		} catch (RegistryException e) {
			//			log.fine(e.getMessage(), e);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
