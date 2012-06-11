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

package org.apache.airavata.xbaya.interpretor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.registry.api.impl.JCRRegistry;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.registry.JCRComponentRegistry;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.ode.ODEClient;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.concurrent.PredicatedTaskRunner;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.xmlbeans.XmlException;

import xsul5.MLogger;

/**
 * WorkflowInterpretorSkeleton java skeleton for the axisService
 */
public class WorkflowInterpretorSkeleton implements ServiceLifeCycle {
    private static final MLogger log = MLogger.getLogger();

	public static final String PROXYSERVER = "myproxy.url";
	public static final String MSGBOX = "msgbox";
	public static final String GFAC = "gfac";
	public static final String DSC = "dsc";
	public static final String BROKER = "broker";
    public static final String MYPROXY_USER = "myproxy.username";
    public static final String MYPROXY_PASS = "myproxy.password";
    public static final String MYPROXY_SERVER = "myproxy.url";
    public static final String MYPROXY_LIFETIME = "myproxy.lifetime";
    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String JCR_USER = "jcr.username";
    public static final String JCR_PASS = "jcr.password";
    public static final String JCR_URL = "jcr.url";
    public static boolean provenance = false;
    public static final String PROVENANCE = "provenance";
    public static  String jcrUserName = "";
    public static  String jcrPassword = "";
    public static  String jcrURL = "";
    public static boolean runInThread = false;
    public static final String RUN_IN_THREAD = "runInThread";
    public static  Boolean gfacEmbeddedMode = false;
    private static PredicatedTaskRunner runner = null;
    public static  JCRComponentRegistry jcrComponentRegistry = null;
    public static int provenanceWriterThreadPoolSize = 1;
    public static final String PROVENANCE_WRITER_THREAD_POOL_SIZE = "provenanceWriterThreadPoolSize";
    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;
    public static final String GFAC_EMBEDDED = "gfac.embedded";
    public static  ConfigurationContext configurationContext;
    public static final String OUTPUT_DATA_PATH = "outputDataPath";

    private AiravataRegistry getRegistry(){
        Properties properties = new Properties();
        try {
            URL url = getXBayaPropertiesURL();
            properties.load(url.openStream());
            jcrUserName = (String)properties.get(JCR_USER);
            jcrPassword = (String) properties.get(JCR_PASS);
            jcrURL = (String) properties.get(JCR_URL);
            jcrComponentRegistry = new JCRComponentRegistry(new URI(jcrURL),jcrUserName,jcrPassword);
            return jcrComponentRegistry.getRegistry();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }

	private URL getXBayaPropertiesURL() {
		return this.getClass().getClassLoader().getResource("xbaya.properties");
	}
	
    public void startUp(final ConfigurationContext configctx, AxisService service) {
    	new Thread(){
    		@Override
    		public void run() {
    			try {
					Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		        URL url = getXBayaPropertiesURL();
		        Properties properties = new Properties();
		        try {
		            properties.load(url.openStream());
                    // Airavata deployer have to configure these properties,but if user send them alone the incoming message
                    // We are overwriting those values only for that particular request
		            configctx.setProperty(MYPROXY_PASS, properties.get(MYPROXY_PASS));
		            configctx.setProperty(MYPROXY_USER, properties.get(MYPROXY_USER));
		            configctx.setProperty(MYPROXY_LIFETIME,properties.getProperty(MYPROXY_LIFETIME));
                    configctx.setProperty(TRUSTED_CERT_LOCATION,properties.getProperty(TRUSTED_CERT_LOCATION));
                    configctx.setProperty(MYPROXY_SERVER,properties.getProperty(MYPROXY_SERVER));
		            jcrUserName = (String)properties.get(JCR_USER);
		            jcrPassword = (String) properties.get(JCR_PASS);
		            jcrURL = (String) properties.get(JCR_URL);
		            provenanceWriterThreadPoolSize = Integer.parseInt((String) properties.get(PROVENANCE_WRITER_THREAD_POOL_SIZE));
		            if("true".equals(properties.get(PROVENANCE))){
		                provenance = true;
		                runner = new PredicatedTaskRunner(provenanceWriterThreadPoolSize);
		                try {
		                    jcrComponentRegistry = new JCRComponentRegistry(new URI(jcrURL),jcrUserName,jcrPassword);
                            List<HostDescription> hostList = getDefinedHostDescriptions();
                            for(HostDescription host:hostList){
                                // This will avoid the changes user is doing to one of the predefined Hosts during a restart of the system
                                AiravataRegistry registry = jcrComponentRegistry.getRegistry();
								if(registry.getHostDescription(host.getType().getHostName()) == null){
                                    log.info("Saving the predefined Host: " + host.getType().getHostName());
                                    registry.saveHostDescription(host);
                                }
                            }
		                } catch (RepositoryException e) {
		                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		                } catch (URISyntaxException e) {
		                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		                } catch (RegistryException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }else{
		                provenance = false;
		            }
		            if("true".equals(properties.get(RUN_IN_THREAD))){
		                runInThread = true;
		            }else{
		                runInThread = false;
		            }

                     if("true".equals(properties.get(GFAC_EMBEDDED))){
		                gfacEmbeddedMode = true;
		            }else{
		                gfacEmbeddedMode = false;
		            }
		
		        } catch (IOException e) {
		            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		        }
                WorkflowInterpretorSkeleton.configurationContext = configctx;
    		}
    	}.start();

    }

    /**
     *
     * @param workflowAsString
     * @param topic
     * @param inputs
     * @return
     * @throws XMLStreamException
     */

	public java.lang.String launchWorkflow(java.lang.String workflowAsString, java.lang.String topic, NameValue[] inputs) throws XMLStreamException {
        OMElement workflowContext = getWorkflowContextHeader();
        Map<String, String> configuration = new HashMap<String, String>();
        WorkflowContextHeaderBuilder workflowContextHeaderBuilder = parseContextHeader(workflowContext, configuration);
        return setupAndLaunch(workflowAsString, topic,
                (String)configurationContext.getProperty(MYPROXY_USER),(String)configurationContext.getProperty(MYPROXY_PASS),inputs,configuration,runInThread,workflowContextHeaderBuilder);
	}

    private OMElement getWorkflowContextHeader() {
        MessageContext currentMessageContext = MessageContext.getCurrentMessageContext();
        SOAPHeader header = currentMessageContext.getEnvelope().getHeader();
        Iterator childrenWithName = header.getChildrenWithName(new QName("http://schemas.airavata.apache.org/workflow-execution-context", "context-header"));
        return (OMElement)childrenWithName.next();
    }

    private WorkflowContextHeaderBuilder parseContextHeader(OMElement workflowContext, Map<String, String> configuration) throws XMLStreamException {
        ContextHeaderDocument parse = null;
        try {
            parse = ContextHeaderDocument.Factory.parse(workflowContext.toStringWithConsume());
            configuration.put(BROKER, parse.getContextHeader().getWorkflowMonitoringContext().getEventPublishEpr());
            configuration.put(GFAC, parse.getContextHeader().getSoaServiceEprs().getGfacUrl());
            configuration.put(MSGBOX, parse.getContextHeader().getWorkflowMonitoringContext().getMsgBoxEpr());
        } catch (XmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new WorkflowContextHeaderBuilder(parse.getContextHeader());
    }

    private String setupAndLaunch(String workflowAsString, String topic, String password, String username,
                                  NameValue[] inputs,Map<String,String>configurations,boolean inNewThread,WorkflowContextHeaderBuilder builder) throws XMLStreamException {
        System.err.println("Launch is called for topi:");

        Workflow workflow = null;
        try {
            workflow = new Workflow(workflowAsString);
            System.err.println("Workflow Object created");
        } catch (GraphException e1) {
            e1.printStackTrace();
        } catch (ComponentException e1) {
            e1.printStackTrace();
        }
        System.err.println("Setting Input values");
        List<InputNode> inputNodes = new ODEClient().getInputNodes(workflow);
        for (InputNode inputNode : inputNodes) {
            for (NameValue input : inputs) {
                if (inputNode.getName().equals(input.getName())) {
                    inputNode.setDefaultValue(input.getValue());
                    break;
                }
            }
            if (inputNode.getDefaultValue() == null) {
                throw new WorkflowRuntimeException("Could not find a input value for component with name :" + inputNode.getName());
            }

        }
        System.err.println("Input all set");

        XBayaConfiguration conf = null;
        try {
            conf = getConfiguration(configurations);
            conf.setTopic(topic);
            conf.setRunWithCrossProduct(true);
        } catch (URISyntaxException e1) {
            throw new WorkflowRuntimeException(e1);
        }
        WorkflowInterpretorEventListener listener = null;
        WorkflowInterpreter interpreter = null;
        AiravataRegistry registry = getRegistry();
		WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(workflow,topic,conf.getMessageBoxURL(), conf.getBrokerURL(), registry, conf, null, null, null);
        workflowInterpreterConfiguration.setGfacEmbeddedMode(gfacEmbeddedMode);
        workflowInterpreterConfiguration.setActOnProvenance(provenance);
        listener = new WorkflowInterpretorEventListener(workflow, conf);
        interpreter = new WorkflowInterpreter(workflowInterpreterConfiguration, new SSWorkflowInterpreterInteractorImpl(workflow));
        try {
            System.err.println("start listener set");
            listener.start();
        } catch (MonitorException e1) {
            e1.printStackTrace();
        }

        WorkflowContextHeaderBuilder.setCurrentContextHeader(builder.getContextHeader());

        final WorkflowInterpretorEventListener finalListener = listener;
        conf.setJcrComponentRegistry(jcrComponentRegistry);
       
        final WorkflowInterpreter finalInterpreter = interpreter;
//        interpreter.setActOnProvenance(provenance);
        interpreter.setProvenanceWriter(runner);
        final String experimentId = topic;
        System.err.println("Created the interpreter");
        if(inNewThread){
            runInThread(finalInterpreter,finalListener,experimentId,builder);
        }else{
            executeWorkflow(finalInterpreter, finalListener, experimentId);
        }
        System.err.println("topic return:" + topic);
        return topic;
    }

    private void runInThread(final WorkflowInterpreter interpreter,final WorkflowInterpretorEventListener listener,final String experimentId,final WorkflowContextHeaderBuilder builder) {
        new Thread(new Runnable() {

            public void run() {
                WorkflowContextHeaderBuilder.setCurrentContextHeader(builder.getContextHeader());
                executeWorkflow(interpreter, listener, experimentId);
            }
        }).start();
    }

    private void executeWorkflow(WorkflowInterpreter interpreter, WorkflowInterpretorEventListener listener,String experimentId) {
        try {
            interpreter.scheduleDynamically();
            System.err.println("Called the interpreter");
        } catch (WorkflowException e) {
            throw new WorkflowRuntimeException(e);
        } finally {
            /*
             * stop listener no matter what happens
             */
            try {
                if(listener != null)
                listener.stop();
            } catch (MonitorException e) {
                e.printStackTrace();
            }
        }
    }

    public  XBayaConfiguration getConfiguration(Map<String,String> vals) throws URISyntaxException {
		XBayaConfiguration configuration = new XBayaConfiguration();
		configuration.setBrokerURL(new URI(findValue(vals, BROKER, XBayaConstants.DEFAULT_BROKER_URL.toString())));
		configuration.setGFacURL(new URI(findValue(vals, GFAC, XBayaConstants.DEFAULT_GFAC_URL.toString())));
		configuration.setMessageBoxURL(new URI(findValue(vals, MSGBOX, XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString())));
		configuration.setMyProxyLifetime(XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
		configuration.setMyProxyPort(XBayaConstants.DEFAULT_MYPROXY_PORT);
        //This null check will fix some test failures
        if (WorkflowInterpretorSkeleton.configurationContext != null) {
            configuration.setMyProxyServer(findValue(vals, PROXYSERVER, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_SERVER)));
            configuration.setMyProxyPassphrase(findValue(vals, MYPROXY_PASS, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_PASS)));
            configuration.setMyProxyUsername(findValue(vals, MYPROXY_USER, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_USER)));
            configuration.setTrustedCertLocation(findValue(vals, TRUSTED_CERT_LOCATION, (String) WorkflowInterpretorSkeleton.configurationContext.getProperty(TRUSTED_CERT_LOCATION)));
        }
		return configuration;
	}

	public String findValue(Map<String,String> vals, String key, String defaultVal) {
		if(vals.get(key) != null) {
            return vals.get(key);
        }
		return defaultVal;
	}
     public void shutDown(ConfigurationContext configctx, AxisService service) {
            ((JCRRegistry)jcrComponentRegistry.getRegistry()).closeConnection();
         if(runner != null){
             runner.shutDown();
         }
    }

    private List<HostDescription> getDefinedHostDescriptions() {
        URL url = this.getClass().getClassLoader().getResource("host.xml");
        ArrayList<HostDescription> hostDescriptions = new ArrayList<HostDescription>();
        XMLStreamReader reader = null;
        try {
            File fXmlFile = new File(url.getPath());
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(fXmlFile));
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement documentElement = builder.getDocumentElement();
        Iterator server = documentElement.getChildrenWithName(new QName("server"));
        while (server.hasNext()) {
            OMElement next = (OMElement) server.next();
            HostDescription hostDescription;
            if (next.getFirstChildWithName(new QName("gram.endpoint")) != null) {
                hostDescription = new HostDescription(GlobusHostType.type);
                ((GlobusHostType) hostDescription.getType()).addGlobusGateKeeperEndPoint(next.getFirstChildWithName(new QName("gram.endpoint")).getText());
                ((GlobusHostType) hostDescription.getType()).addGridFTPEndPoint(next.getFirstChildWithName(new QName("gridftp.endpoint")).getText());
            } else {
                hostDescription = new HostDescription(HostDescriptionType.type);
            }
            (hostDescription.getType()).setHostName(next.getFirstChildWithName(new QName("name")).getText());
            (hostDescription.getType()).setHostAddress(next.getFirstChildWithName(new QName("host")).getText());
            hostDescriptions.add(hostDescription);
        }
        return hostDescriptions;
    }
}
