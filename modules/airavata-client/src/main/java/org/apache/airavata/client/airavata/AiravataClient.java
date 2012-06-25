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
package org.apache.airavata.client.airavata;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.WorkflowExecution;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorStub;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorEventListener;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;

public class AiravataClient {
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

	private AiravataRegistry registry;

    private Map<String, String> configuration = new HashMap<String, String>();

	// private NameValue[] configurations = new NameValue[7];

	public AiravataClient(Map<String,String> configuration)
			throws MalformedURLException {
		this.configuration = configuration;
		initialize();
	}

	public AiravataClient(String fileName) throws RegistryException,
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
		configuration.put(JCR,validateURL(properties
				.getProperty(DEFAULT_JCR_URL)));
		configuration.put(JCR_USERNAME,properties.getProperty(JCR_USERNAME));

		configuration.put(JCR_PASSWORD,properties.getProperty(JCR_PASSWORD));

		configuration.put(WITHLISTENER,properties.getProperty(WITHLISTENER));

		initialize();        
	}

	private void initialize() throws MalformedURLException {
		updateClientConfiguration(configuration);
		
        // At this point we do not know the workflowExperimentId
		builder = new WorkflowContextHeaderBuilder(configuration.get(BROKER),
        		configuration.get(GFAC),configuration.get(JCR),null,null,
        		configuration.get(MSGBOX));
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
				clientConfiguration.setGfacURL(new URL(getRegistry().getGFacDescriptorList().get(0)));
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
        new Thread() {
            @Override
            public void run() {
                try {
                    monitor.start();
                } catch (MonitorException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return monitor;
    }
    
	private void runPreWorkflowExecutionTasks(String topic, String user,
			String metadata,String workflowInstanceName) throws RegistryException {
		if (user != null) {
			getRegistry().saveWorkflowExecutionUser(topic, user);
		}
		if (metadata != null) {
			getRegistry().saveWorkflowExecutionMetadata(topic, metadata);
		}
		
		if (workflowInstanceName!=null) {
			getRegistry().saveWorkflowExecutionName(topic, workflowInstanceName);
		}
	
	}

	public String runWorkflow(String topic, NameValue[] inputs) throws Exception {
		return runWorkflow(topic, inputs, null);
	}

	public String runWorkflow(String topic, NameValue[] inputs, String user) throws Exception {
		return runWorkflow(topic, inputs, user, null,topic);
	}

	public String runWorkflow(String topic, NameValue[] inputs, String user,
			String metadata,String workflowInstanceName) throws Exception{
		String worflowoutput = null;
		try {
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(
					getClientConfiguration().getXbayaServiceURL().toString());
            stub._getServiceClient().addHeader(AXIOMUtil.stringToOM(XMLUtil.xmlElementToString(builder.getXml())));
			runPreWorkflowExecutionTasks(topic, user, metadata,workflowInstanceName);
			worflowoutput = stub.launchWorkflow(workflow, topic, inputs);
//			log.info("Workflow output : " + worflowoutput);
		} catch (RegistryException e) {
//			log.fine(e.getMessage(), e);
		}
		return worflowoutput;
	}

	public List<WorkflowExecution> getWorkflowExecutionDataByUser(String user)
			throws RegistryException {
		return getRegistry().getWorkflowExecutionByUser(user);
	}

	public WorkflowExecution getWorkflowExecutionData(String topic)
			throws RegistryException {
		return getRegistry().getWorkflowExecution(topic);
	}

	/**
	 * 
	 * @param user
	 * @param pageSize
	 *            - number of executions to return (page size)
	 * @param PageNo
	 *            - which page to return to (>=0)
	 * @return
	 * @throws RegistryException
	 */
	public List<WorkflowExecution> getWorkflowExecutionData(String user,
			int pageSize, int PageNo) throws RegistryException {
		return getRegistry().getWorkflowExecutionByUser(user, pageSize, PageNo);
	}

	public static String getWorkflow() {
		return workflow;
	}

	public static void setWorkflow(String workflow) {
		AiravataClient.workflow = workflow;
	}

	public AiravataRegistry getRegistry() {
		if (registry == null) {
			try {
				HashMap<String, String> map = new HashMap<String, String>();
				URI uri = getClientConfiguration().getJcrURL().toURI();
				map.put("org.apache.jackrabbit.repository.uri", uri.toString());
				registry = new AiravataJCRRegistry(
						uri,
						"org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory",
						getClientConfiguration().getJcrUsername(),
						getClientConfiguration().getJcrPassword(), map);
			} catch (RepositoryException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
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

	/**
	 * Retrieve the names of the workflow templates saved in the registry
	 * @return
	 */
	public List<String> getWorkflowTemplateIds() {
		List<String> workflowList = new ArrayList<String>();
		Map<QName, Node> workflows;
		try {
			workflows = getRegistry().getWorkflows(
					getClientConfiguration().getJcrUsername());
			for (QName qname : workflows.keySet()) {
				workflowList.add(qname.getLocalPart());
			}
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workflowList;
	}

	/**
	 * Execute the given workflow template with the given inputs and return the topic id 
	 * @param workflowTemplateId
	 * @param inputs
	 * @return
	 */
	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs,String workflowInstanceName) throws Exception{
		return runWorkflow(workflowTemplateId,inputs,getRegistry().getUsername(),null,workflowInstanceName);
	}
	
	/**
	 * Execute the given workflow template with the given inputs, user, metadata and return the topic id
	 * @param workflowTemplateId
	 * @param inputs
	 * @param user
	 * @param metadata
	 * @return
	 * @throws Exception
	 */
	public String runWorkflow(String workflowTemplateId,List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName) throws Exception{
		try {
			List<WSComponentPort> ports = getWSComponentPortInputs(workflowTemplateId);
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
			workflow=getWorkflowAsString(workflowTemplateId).getString();
			String topic=workflowTemplateId+"_"+UUID.randomUUID();
			return runWorkflow(topic, inputValues.toArray(new NameValue[]{}), user, metadata, workflowInstanceName);
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
	
	/**
	 * Retrieve the inputs for the given workflow template
	 * @param workflowTemplateId
	 * @return
	 */
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

	public Property getWorkflowAsString(String workflowTemplateId)
			throws RegistryException, PathNotFoundException,
			RepositoryException {
		Map<QName, Node> workflows = getRegistry().getWorkflows(getClientConfiguration().getJcrUsername());
		for (QName qname : workflows.keySet()) {
			if (qname.getLocalPart().equals(workflowTemplateId)){
				return workflows.get(qname).getProperty("workflow");
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
		Property workflowAsString = getWorkflowAsString(workflowTemplateId);
		Workflow workflow = new Workflow(workflowAsString.getString());
		return workflow;
	}

	private List<WSComponentPort> getWSComponentPortInputs(Workflow workflow)
			throws GraphException, ComponentException {
		WorkflowClient.createScript(workflow);
		List<WSComponentPort> inputs = workflow.getInputs();
		return inputs;
	}
	
	private List<InputNode> getInputNodes(
			String workflowTemplateId) throws PathNotFoundException, GraphException, ComponentException, ValueFormatException, RegistryException, RepositoryException{
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
}
