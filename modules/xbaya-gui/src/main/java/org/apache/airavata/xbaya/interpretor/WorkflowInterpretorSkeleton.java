/**
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
 */
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.interpretor;
//
//import org.apache.airavata.client.AiravataAPIFactory;
//import org.apache.airavata.client.api.AiravataAPI;
//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.client.api.exception.DescriptorAlreadyExistsException;
//import org.apache.airavata.client.stub.interpretor.NameValue;
//import org.apache.airavata.client.tools.PeriodicExecutorThread;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.common.utils.AiravataUtils;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.apache.airavata.common.utils.ServiceUtils;
//import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
//import org.apache.airavata.commons.gfac.type.HostDescription;
//import org.apache.airavata.schemas.gfac.GlobusHostType;
//import org.apache.airavata.schemas.gfac.GsisshHostType;
//import org.apache.airavata.schemas.gfac.SSHHostType;
//import org.apache.airavata.schemas.wec.ContextHeaderDocument;
//import org.apache.airavata.workflow.model.component.ComponentException;
//import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
//import org.apache.airavata.workflow.model.graph.GraphException;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.ode.ODEClient;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.workflow.model.wf.WorkflowExecutionState;
//import org.apache.airavata.ws.monitor.MonitorException;
//import org.apache.airavata.xbaya.XBayaConfiguration;
//import org.apache.airavata.xbaya.XBayaConstants;
//import org.apache.airavata.xbaya.concurrent.PredicatedTaskRunner;
//import org.apache.axiom.om.OMElement;
//import org.apache.axiom.om.impl.builder.StAXOMBuilder;
//import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
//import org.apache.axiom.soap.SOAPHeader;
//import org.apache.axis2.context.ConfigurationContext;
//import org.apache.axis2.context.MessageContext;
//import org.apache.axis2.description.AxisService;
//import org.apache.axis2.engine.ServiceLifeCycle;
//import org.apache.xmlbeans.XmlException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.xml.namespace.QName;
//import javax.xml.stream.XMLInputFactory;
//import javax.xml.stream.XMLStreamException;
//import javax.xml.stream.XMLStreamReader;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.*;
////import org.apache.airavata.registry.api.AiravataRegistry2;
//
///**
// * WorkflowInterpretorSkeleton java skeleton for the axisService
// */
//public class WorkflowInterpretorSkeleton implements ServiceLifeCycle {
//    private static final Logger log = LoggerFactory.getLogger(WorkflowInterpretorSkeleton.class);
//
////	public static final String PROXYSERVER = "myproxy.url";
//	public static final String MSGBOX = "msgbox";
//	public static final String GFAC = "gfac";
//	public static final String BROKER = "broker";
//    public static final String MYPROXY_USER = "myproxy.user";
//    public static final String MYPROXY_PASS = "myproxy.pass";
//    public static final String MYPROXY_SERVER = "myproxy.server";
//    public static final String MYPROXY_LIFETIME = "myproxy.life";
//    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
//
//    public static boolean provenance = false;
//    public static final String PROVENANCE = "provenance";
//    public static  String systemUserName = "";
//    public static  String systemUserPW = "";
//    public static boolean runInThread = false;
//    public static final String RUN_IN_THREAD = "runInThread";
//    public static  Boolean gfacEmbeddedMode = true;
//    private static PredicatedTaskRunner runner = null;
////    public static  JCRComponentRegistry jcrComponentRegistry = null;
//    private static AiravataAPI airavataAPI=null;
//    public static int provenanceWriterThreadPoolSize = 1;
//    public static final String PROVENANCE_WRITER_THREAD_POOL_SIZE = "provenanceWriterThreadPoolSize";
//    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;
//    public static final String GFAC_EMBEDDED = "gfac.embedded";
//    public static  ConfigurationContext configurationContext;
//    public static final String SERVICE_NAME="WorkflowInterpretor";
//    public static boolean notInterrupted = true;
//    public Map<String, WorkflowInterpreterConfiguration> workflowConfigurations=new HashMap<String, WorkflowInterpreterConfiguration>();
//    private WorkflowInterpreterInteractor interactor;
//    private String gateway;
//
//	protected static final String SERVICE_URL = "interpreter_service_url";
//
//	protected static final String JCR_REG = "jcr_registry";
//
//	protected WIServiceThread thread;
//
//    private AiravataAPI getAiravataAPI(){
//        if (airavataAPI==null) {
//			try {
//				systemUserName = ServerSettings.getSystemUser();
//				systemUserPW = ServerSettings.getSystemUserPassword();
//				gateway = ServerSettings.getSystemUserGateway();
//				airavataAPI = AiravataAPIFactory.getAPI(gateway, systemUserName);
//			} catch (ApplicationSettingsException e) {
//				log.error("Unable to read the properties file", e);
//			} catch (AiravataAPIInvocationException e) {
//				log.error("Unable to create Airavata API", e);
//			}
//		}
//		return airavataAPI;
//    }
//
//    private WorkflowInterpreterInteractor getInteractor(){
//    	if (interactor==null){
//        	interactor=new SSWorkflowInterpreterInteractorImpl();
//    	}
//    	return interactor;
//    }
//
//    public void startUp(final ConfigurationContext configctx, AxisService service) {
//    	AiravataUtils.setExecutionAsServer();
//    	new Thread(){
//			@Override
//    		public void run() {
//    			try {
//					Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
//		        try {
//                    // Airavata deployer have to configure these properties,but if user send them alone the incoming message
//                    // We are overwriting those values only for that particular request
//		            configctx.setProperty(MYPROXY_PASS, ServerSettings.getSetting(MYPROXY_PASS));
//		            configctx.setProperty(MYPROXY_USER, ServerSettings.getSetting(MYPROXY_USER));
//		            configctx.setProperty(MYPROXY_LIFETIME,ServerSettings.getSetting(MYPROXY_LIFETIME));
//                    configctx.setProperty(TRUSTED_CERT_LOCATION,ServerSettings.getSetting(TRUSTED_CERT_LOCATION));
//                    configctx.setProperty(MYPROXY_SERVER,ServerSettings.getSetting(MYPROXY_SERVER));
//		            provenanceWriterThreadPoolSize = Integer.parseInt((String) ServerSettings.getSetting(PROVENANCE_WRITER_THREAD_POOL_SIZE));
//		            if("true".equals(ServerSettings.getSetting(PROVENANCE))){
//		                provenance = true;
//		                runner = new PredicatedTaskRunner(provenanceWriterThreadPoolSize);
//		                try {
//                            List<HostDescription> hostList = getDefinedHostDescriptions();
//                            for(HostDescription host:hostList){
//                                // This will avoid the changes user is doing to one of the predefined Hosts during a restart of the system
//                                AiravataAPI registry = getAiravataAPI();
//								if(!registry.getApplicationManager().isHostDescriptorExists(host.getType().getHostName())){
//                                    log.debug("Saving the predefined Host: " + host.getType().getHostName());
//                                    registry.getApplicationManager().addHostDescription(host);
//                                }
//                            }
//		                } catch (DescriptorAlreadyExistsException e) {
//                            e.printStackTrace();
//                        } catch (AiravataAPIInvocationException e) {
//		                    e.printStackTrace();
//
//                        }
//                    }else{
//		                provenance = false;
//		            }
//		            if("true".equals(ServerSettings.getSetting(RUN_IN_THREAD))){
//		                runInThread = true;
//		            }else{
//		                runInThread = false;
//		            }
//
//                     if("true".equals(ServerSettings.getSetting(GFAC_EMBEDDED))){
//		                gfacEmbeddedMode = true;
//		            }else{
//		                gfacEmbeddedMode = false;
//		            }
//
//                     //save the interpreter service url in context
//                    String localAddress = ServiceUtils.generateServiceURLFromConfigurationContext(configctx,SERVICE_NAME);
// 					configctx.setProperty(SERVICE_URL,new URI(localAddress));
// 					configctx.setProperty(JCR_REG,getAiravataAPI());
// 					/*
//					 * Heart beat message to registry
//					 */
//					thread = new WIServiceThread(getAiravataAPI(), configctx);
//					thread.start();
//                } catch (IOException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                } catch (ApplicationSettingsException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//                WorkflowInterpretorSkeleton.configurationContext = configctx;
//    		}
//    	}.start();
//
//    }
//
//    public void suspendWorkflow(String experimentId)throws Exception{
//    	if (workflowConfigurations.containsKey(experimentId)){
//    		if (getInteractor().isExecutionPaused(workflowConfigurations.get(experimentId))){
//    			throw new Exception("Experiment '"+experimentId+"' is already paused!!!");
//    		}else{
//    			log.info("Suspending workflow execution "+experimentId+"...");
//    			getInteractor().pauseExecution(workflowConfigurations.get(experimentId));
//    		}
//    	}else{
//    		throw new Exception("Invalid Experiment id: Experiment "+experimentId+" not running");
//    	}
//    }
//
//    public void resumeWorkflow(String experimentId)throws Exception{
//    	if (workflowConfigurations.containsKey(experimentId)){
//    		if (getInteractor().isExecutionPaused(workflowConfigurations.get(experimentId)) || workflowConfigurations.get(experimentId).getWorkflow().getExecutionState()==WorkflowExecutionState.STOPPED){
//    			log.info("Resuming workflow execution "+experimentId+"...");
//    			getInteractor().resumeExecution(workflowConfigurations.get(experimentId));
//
//    		}else{
//    			throw new Exception("Experiment '"+experimentId+"' is not suspended!!!");
//    		}
//    	}else{
//    		//TODO chk to see if the experiment is present in registry if so reload it and resume execution else error
//    		throw new Exception("Invalid Experiment id: Experiment "+experimentId+" not running");
//    	}
//    }
//
//    public void haltWorkflow(String experimentId)throws Exception{
//    	if (workflowConfigurations.containsKey(experimentId)){
//			log.info("Terminating workflow execution "+experimentId+"...");
//			getInteractor().terminateExecution(workflowConfigurations.get(experimentId));
//    	}else{
//    		throw new Exception("Invalid Experiment id: Experiment "+experimentId+" not running");
//    	}
//    }
//
//    /**
//     * @param workflowAsString
//     * @param topic
//     * @param inputs
//     * @return
//     * @throws XMLStreamException
//     */
//	public java.lang.String launchWorkflow(java.lang.String workflowAsString, java.lang.String topic, NameValue[] inputs) throws XMLStreamException {
//        OMElement workflowContext = getWorkflowContextHeader();
//        if(workflowContext == null){
//            workflowContext = AXIOMUtil.stringToOM("<wor:context-header xmlns:wor=\"http://airavata.apache.org/schemas/wec/2012/05\">\n" +
//                "    <wor:soa-service-eprs>\n" +
//                "        <wor:gfac-url></wor:gfac-url>\n" +
//                "        <wor:registry-url></wor:registry-url>\n" +
//                "    </wor:soa-service-eprs>\n" +
//                "    <wor:workflow-monitoring-context>\n" +
//                "        <wor:experiment-id></wor:experiment-id>\n" +
//                "        <wor:workflow-instance-id xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\" />\n" +
//                "        <wor:event-publish-epr></wor:event-publish-epr>\n" +
//                "        <wor:msg-box-epr></wor:msg-box-epr>\n" +
//                "    </wor:workflow-monitoring-context>\n" +
//                "    <wor:workflow-scheduling-context />\n" +
//                "    <wor:security-context />\n" +
//                "</wor:context-header>");
//        }
//        Map<String, String> configuration = new HashMap<String, String>();
//        WorkflowContextHeaderBuilder workflowContextHeaderBuilder = parseContextHeader(workflowContext, configuration);
//        String user = workflowContextHeaderBuilder.getSubmissionUser();
//
//        String s = null;
//        try {
//             s = setupAndLaunch(workflowAsString, topic, ServerSettings.getSystemUserGateway(),
//                    user,inputs, configuration, runInThread, workflowContextHeaderBuilder);
//        } catch (AiravataAPIInvocationException e) {
//            log.error(e.getMessage());
//        } catch (ApplicationSettingsException e) {
//            log.error(e.getMessage());
//        }
//        return s;
//    }
//
//    private OMElement getWorkflowContextHeader() {
//        MessageContext currentMessageContext = MessageContext.getCurrentMessageContext();
//        SOAPHeader header = currentMessageContext.getEnvelope().getHeader();
//        Iterator<?> childrenWithName = header.getChildrenWithName(new QName("http://airavata.apache.org/schemas/wec/2012/05", "context-header"));
//        if (childrenWithName.hasNext()) {
//            return (OMElement) childrenWithName.next();
//        } else {
//            return null;
//        }
//    }
//
//    private WorkflowContextHeaderBuilder parseContextHeader(OMElement workflowContext, Map<String, String> configuration) throws XMLStreamException {
//        ContextHeaderDocument parse = null;
//        try {
//            parse = ContextHeaderDocument.Factory.parse(workflowContext.toStringWithConsume());
//            String msgBox = parse.getContextHeader().getWorkflowMonitoringContext().getMsgBoxEpr();
//            if(msgBox == null || "".equals(msgBox)){
//                msgBox = getAiravataAPI().getAiravataManager().getMessageBoxServiceURL().toASCIIString();
//            }
//            String msgBroker = parse.getContextHeader().getWorkflowMonitoringContext().getEventPublishEpr();
//            if(msgBroker == null || "".equals(msgBroker)){
//                msgBroker = getAiravataAPI().getAiravataManager().getEventingServiceURL().toASCIIString();
//            }
//            String gfac =  parse.getContextHeader().getSoaServiceEprs().getGfacUrl();
////            if(gfac == null || "".equals(gfac)){
////                gfac = getAiravataAPI().getAiravataManager().getGFaCURLs().get(0).toString();
////            }
//            configuration.put(BROKER, msgBroker);
//            configuration.put(GFAC, gfac);
//            configuration.put(MSGBOX, msgBox);
//        } catch (XmlException e) {
//            log.error(e.getMessage());
//        } catch (AiravataAPIInvocationException e) {
//            log.error(e.getMessage());
//        }
//    	String submissionUser = workflowContext.getAttributeValue(new QName(workflowContext.getNamespace().getNamespaceURI(), "submissionUser"));
//        WorkflowContextHeaderBuilder workflowContextHeaderBuilder = new WorkflowContextHeaderBuilder(parse.getContextHeader());
//        workflowContextHeaderBuilder.setSubmissionUser(submissionUser);
//		return workflowContextHeaderBuilder;
//    }
//
//    public String setupAndLaunch(String workflowAsString, String experimentId, String gatewayId, String username,
//            Map<String,String> inputs,boolean inNewThread,WorkflowContextHeaderBuilder builder) throws AiravataAPIInvocationException{
//    	List<NameValue> inputData=new ArrayList<NameValue>();
//    	for (String inputName : inputs.keySet()) {
//			NameValue input = new NameValue();
//			input.setName(inputName);
//			input.setValue(inputs.get(inputName));
//			inputData.add(input);
//		}
//    	Map<String, String> configuration = new HashMap<String, String>();
//    	configuration.put(BROKER, getAiravataAPI().getAiravataManager().getEventingServiceURL().toASCIIString());
//        configuration.put(MSGBOX, getAiravataAPI().getAiravataManager().getMessageBoxServiceURL().toASCIIString());
//
//    	return setupAndLaunch(workflowAsString, experimentId, gatewayId, username, inputData.toArray(new NameValue[]{}), configuration, inNewThread, builder);
//    }
//
//    private String setupAndLaunch(String workflowAsString, String topic, String gatewayId, String username,
//                                  NameValue[] inputs,Map<String,String>configurations,boolean inNewThread,
//                                  WorkflowContextHeaderBuilder builder) throws AiravataAPIInvocationException{
//        log.debug("Launch is called for topic:"+topic);
//
//        Workflow workflow = null;
//        try {
//            workflow = new Workflow(workflowAsString);
//            log.debug("Workflow Object created");
//        } catch (GraphException e1) {
//            e1.printStackTrace();
//        } catch (ComponentException e1) {
//            e1.printStackTrace();
//        }
//        log.debug("Setting Input values");
//        List<InputNode> inputNodes = new ODEClient().getInputNodes(workflow);
//        for (InputNode inputNode : inputNodes) {
//            for (NameValue input : inputs) {
//                if (inputNode.getID().equals(input.getName())) {
//                    inputNode.setDefaultValue(input.getValue());
//                    break;
//                }
//            }
//            if (inputNode.getDefaultValue() == null) {
//                throw new WorkflowRuntimeException("Could not find a input value for component with name :" + inputNode.getName());
//            }
//
//        }
//        log.debug("Input all set");
//
//        XBayaConfiguration conf = null;
//        try {
//            conf = getConfiguration(configurations);
//            conf.setTopic(topic);
//            conf.setRunWithCrossProduct(true);
//        } catch (URISyntaxException e1) {
//            throw new WorkflowRuntimeException(e1);
//        }
//        WorkflowInterpretorEventListener listener = null;
//        WorkflowInterpreter interpreter = null;
//        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(gatewayId, username);
//        WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(workflow, topic, conf.getMessageBoxURL(), conf.getBrokerURL(), airavataAPI, conf, null, null);
//        workflowInterpreterConfiguration.setGfacEmbeddedMode(gfacEmbeddedMode);
//        workflowInterpreterConfiguration.setActOnProvenance(provenance);
//
//        if (builder.getSecurityContext().getAmazonWebservices() != null) {
//            workflowInterpreterConfiguration.setAwsSecretKey(builder.getSecurityContext().getAmazonWebservices().getSecretAccessKey());
//            workflowInterpreterConfiguration.setAwsAccessKey(builder.getSecurityContext().getAmazonWebservices().getAccessKeyId());
//        }
//        // WorkflowInterpreter object should create prior creation of Listener, because listener needs the threadlocal variable
//        interpreter = new WorkflowInterpreter(workflowInterpreterConfiguration, getInteractor());
//        listener = new WorkflowInterpretorEventListener(workflow, conf);
//        try {
//            log.debug("start listener set");
//            listener.start();
//        } catch (MonitorException e1) {
//            e1.printStackTrace();
//        }
//
//        WorkflowContextHeaderBuilder.setCurrentContextHeader(builder.getContextHeader());
//
//        final WorkflowInterpretorEventListener finalListener = listener;
//
//        final WorkflowInterpreter finalInterpreter = interpreter;
////        interpreter.setActOnProvenance(provenance);
//        interpreter.setProvenanceWriter(runner);
//        final String experimentId = topic;
//        log.debug("Created the interpreter");
//        if(inNewThread){
//            runInThread(finalInterpreter,finalListener,experimentId,builder);
//        }else{
//            executeWorkflow(finalInterpreter, finalListener, experimentId);
//        }
//        log.info("Experiment launched :" + topic);
//        return topic;
//    }
//
//    private void runInThread(final WorkflowInterpreter interpreter,final WorkflowInterpretorEventListener listener,final String experimentId,final WorkflowContextHeaderBuilder builder) {
//        new Thread(new Runnable() {
//
//            public void run() {
//                WorkflowContextHeaderBuilder.setCurrentContextHeader(builder.getContextHeader());
//                executeWorkflow(interpreter, listener, experimentId);
//            }
//        }).start();
//    }
//
//    private void executeWorkflow(WorkflowInterpreter interpreter, WorkflowInterpretorEventListener listener,String experimentId) {
//        try {
//        	workflowConfigurations.put(experimentId,interpreter.getConfig());
//            interpreter.scheduleDynamically();
//            log.debug("Interpreter invoked...");
//        } catch (Exception e) {
//            throw new WorkflowRuntimeException(e);
//        } finally {
//        	if (workflowConfigurations.containsKey(experimentId)){
//        		workflowConfigurations.remove(experimentId);
//        	}
//            /*
//             * stop listener no matter what happens
//             */
////            try {
////                if(listener != null)
////                listener.stop();
////            } catch (MonitorException e) {
////                e.printStackTrace();
////            }
//        }
//    }
//
//    public  XBayaConfiguration getConfiguration(Map<String,String> vals) throws URISyntaxException {
//		XBayaConfiguration configuration = new XBayaConfiguration();
//		configuration.setBrokerURL(new URI(findValue(vals, BROKER, XBayaConstants.DEFAULT_BROKER_URL.toString())));
////		configuration.setGFacURL(new URI(findValue(vals, GFAC, XBayaConstants.DEFAULT_GFAC_URL.toString())));
//		configuration.setMessageBoxURL(new URI(findValue(vals, MSGBOX, XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString())));
//		configuration.setMyProxyLifetime(XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
//		configuration.setMyProxyPort(XBayaConstants.DEFAULT_MYPROXY_PORT);
//        //This null check will fix some test failures
//        if (WorkflowInterpretorSkeleton.configurationContext != null) {
//            configuration.setMyProxyServer(findValue(vals, MYPROXY_SERVER, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_SERVER)));
//            configuration.setMyProxyPassphrase(findValue(vals, MYPROXY_PASS, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_PASS)));
//            configuration.setMyProxyUsername(findValue(vals, MYPROXY_USER, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_USER)));
//            configuration.setTrustedCertLocation(findValue(vals, TRUSTED_CERT_LOCATION, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(TRUSTED_CERT_LOCATION)));
//            configuration.setTrustedCertLocation(findValue(vals, MYPROXY_LIFETIME, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_LIFETIME)));
//        }
//		return configuration;
//	}
//
//	private String findValue(Map<String,String> vals, String key, String defaultVal) {
//		if(vals.get(key) != null) {
//            return vals.get(key);
//        }
//		return defaultVal;
//	}
//
//    public void shutDown(ConfigurationContext configctx, AxisService service) {
//        URI gfacURL = (URI) configctx.getProperty(SERVICE_URL);
//        if (getAiravataAPI() != null && thread != null) {
//            AiravataAPI registry = getAiravataAPI();
//            try {
//                registry.getAiravataManager().removeWorkflowInterpreterURI(gfacURL);
//            } catch (AiravataAPIInvocationException e) {
//                e.printStackTrace();
//            }
//            thread.interrupt();
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                log.warn("GFacURL update thread is interrupted");
//            }
//        }
//        if (runner != null) {
//            runner.shutDown();
//        }
//
//        notInterrupted = false;
//    }
//
//    private List<HostDescription> getDefinedHostDescriptions() {
//        URL url = this.getClass().getClassLoader().getResource("host.xml");
//        ArrayList<HostDescription> hostDescriptions = new ArrayList<HostDescription>();
//        XMLStreamReader reader = null;
//        try {
//            if (url != null) {
//                reader = XMLInputFactory.newInstance().createXMLStreamReader(url.openStream());
//            } else {
//                throw new RuntimeException("Error retrieving host.xml file. Should reside in " +
//                        "$SERVER_HOME/webapps/axis2/WEB-INF/classes/host.xml");
//            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        StAXOMBuilder builder = new StAXOMBuilder(reader);
//        OMElement documentElement = builder.getDocumentElement();
//        Iterator<?> server = documentElement.getChildrenWithName(new QName("server"));
//        while (server.hasNext()) {
//            HostDescription hostDescription = new HostDescription();
//            OMElement next = (OMElement) server.next();
//            if (next.getFirstChildWithName(new QName("gram.endpoint")) != null &&
//                    "globus".equals(next.getFirstChildWithName(new QName("type")).getText())) {
//                hostDescription.getType().changeType(GlobusHostType.type);
//                ((GlobusHostType) hostDescription.getType()).addGlobusGateKeeperEndPoint(next.getFirstChildWithName(new QName("gram.endpoint")).getText());
//                ((GlobusHostType) hostDescription.getType()).addGridFTPEndPoint(next.getFirstChildWithName(new QName("gridftp.endpoint")).getText());
//            } else if("ssh".equals(next.getFirstChildWithName(new QName("type")).getText())) {
//                hostDescription.getType().changeType(SSHHostType.type);
//                if(next.getFirstChildWithName(new QName("hpc.resource")) != null){
//                    if("true".equals(next.getFirstChildWithName(new QName("gram.endpoint")))){
//                        ((SSHHostType) hostDescription.getType()).setHpcResource(true);
//                    }
//                }
//                ((SSHHostType) hostDescription.getType()).setHpcResource(false);
//            } else if("gsissh".equals(next.getFirstChildWithName(new QName("type")).getText())) {
//                hostDescription.getType().changeType(GsisshHostType.type);
//            }
//            (hostDescription.getType()).setHostName(next.getFirstChildWithName(new QName("name")).getText());
//            (hostDescription.getType()).setHostAddress(next.getFirstChildWithName(new QName("host")).getText());
//            hostDescriptions.add(hostDescription);
//        }
//        return hostDescriptions;
//    }
//
//    public static final int URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;
//
//    class WIServiceThread extends PeriodicExecutorThread {
//        private ConfigurationContext context = null;
//
//        WIServiceThread(AiravataAPI registry, ConfigurationContext context) {
//            super(registry);
//            this.context = context;
//        }
//
//        @Override
//        protected void updateRegistry(AiravataAPI registry) throws Exception {
//            URI localAddress = (URI) this.context.getProperty(SERVICE_URL);
//            registry.getAiravataManager().addWorkflowInterpreterURI(localAddress);
//            log.debug("Updated Workflow Interpreter service URL in to Repository");
//
//        }
//    }
//}
