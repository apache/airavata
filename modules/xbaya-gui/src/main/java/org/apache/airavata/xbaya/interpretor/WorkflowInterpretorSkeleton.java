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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.registry.api.impl.JCRRegistry;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.concurrent.PredicatedTaskRunner;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xsul5.MLogger;

import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

    public void startUp(final ConfigurationContext configctx, AxisService service) {
    	new Thread(){
    		@Override
    		public void run() {
    			try {
					Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		        URL url = this.getClass().getClassLoader().getResource("xbaya.properties");
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
                                if(jcrComponentRegistry.getRegistry().getHostDescription(host.getType().getHostName()) == null){
                                    log.info("Saving the predefined Host: " + host.getType().getHostName());
                                    jcrComponentRegistry.getRegistry().saveHostDescription(host);
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
	 * Auto generated method signature
	 *
	 * @param workflowAsString
	 * @param topic
	 * @param password
	 * @param username
	 * @param inputs
	 * @param configurations
	 */

	public java.lang.String launchWorkflow(java.lang.String workflowAsString, java.lang.String topic, java.lang.String password, java.lang.String username, NameValue[] inputs, NameValue[] configurations) {
        return setupAndLaunch(workflowAsString, topic, password, username, inputs, configurations,runInThread);
	}

    private String setupAndLaunch(String workflowAsString, String topic, String password, String username, NameValue[] inputs, NameValue[] configurations,boolean inNewThread) {
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
                throw new XBayaRuntimeException("Could not find a input value for component with name :" + inputNode.getName());
            }

        }
        System.err.println("Input all set");

        XBayaConfiguration conf = null;
        try {
            conf = getConfiguration(configurations);
            conf.setTopic(topic);
            conf.setRunWithCrossProduct(true);
        } catch (URISyntaxException e1) {
            throw new XBayaRuntimeException(e1);
        }
        WorkflowInterpretorEventListener listener = null;
        WorkflowInterpreter interpreter = null;
        if("true".equals(configurations[10].getValue())){
            listener = new WorkflowInterpretorEventListener(workflow, conf);
            interpreter = new WorkflowInterpreter(conf, topic, workflow, username, password);
            try {
                System.err.println("start listener set");
                listener.start();
            } catch (MonitorException e1) {
                e1.printStackTrace();
            }
        }else{
            interpreter = new WorkflowInterpreter(conf, topic, workflow, username, password,true);
        }
        final WorkflowInterpretorEventListener finalListener = listener;
        conf.setJcrComponentRegistry(jcrComponentRegistry);

       
        final WorkflowInterpreter finalInterpreter = interpreter;
        interpreter.setActOnProvenance(provenance);
        interpreter.setProvenanceWriter(runner);
        interpreter.setGfacEmbeddedMode(gfacEmbeddedMode);
        final String experimentId = topic;
        System.err.println("Created the interpreter");
        if(inNewThread){
            runInThread(finalInterpreter,finalListener,experimentId);
        }else{
            executeWorkflow(finalInterpreter,finalListener,experimentId);
        }
        System.err.println("topic return:" + topic);
        return topic;
    }

    private void runInThread(final WorkflowInterpreter interpreter,final WorkflowInterpretorEventListener listener,final String experimentId) {
        new Thread(new Runnable() {

            public void run() {
                executeWorkflow(interpreter, listener,experimentId);
            }
        }).start();
    }

    private void executeWorkflow(WorkflowInterpreter interpreter, WorkflowInterpretorEventListener listener,String experimentId) {
        try {
            interpreter.scheduleDynamically();
            System.err.println("Called the interpreter");
        } catch (XBayaException e) {
            throw new XBayaRuntimeException(e);
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

    public  XBayaConfiguration getConfiguration(NameValue[] vals) throws URISyntaxException {
		XBayaConfiguration configuration = new XBayaConfiguration();
		configuration.setBrokerURL(new URI(findValue(vals, BROKER, XBayaConstants.DEFAULT_BROKER_URL.toString())));
		configuration.setDSCURL(new URI(findValue(vals, DSC, XBayaConstants.DEFAULT_DSC_URL.toString())));
		configuration.setGFacURL(new URI(findValue(vals, GFAC, XBayaConstants.DEFAULT_GFAC_URL.toString())));
		configuration.setMessageBoxURL(new URI(findValue(vals, MSGBOX, XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString())));
		configuration.setMyProxyLifetime(XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
		configuration.setMyProxyPort(XBayaConstants.DEFAULT_MYPROXY_PORT);
		configuration.setMyProxyServer(findValue(vals, PROXYSERVER, (String)WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_SERVER)));
        configuration.setMyProxyPassphrase(findValue(vals, MYPROXY_PASS, (String)WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_PASS)));
        configuration.setMyProxyUsername(findValue(vals,MYPROXY_USER,(String)WorkflowInterpretorSkeleton.configurationContext.getProperty(MYPROXY_USER)));
        configuration.setTrustedCertLocation(findValue(vals, TRUSTED_CERT_LOCATION, (String)WorkflowInterpretorSkeleton.configurationContext.getProperty(TRUSTED_CERT_LOCATION)));
		return configuration;
	}

	public String findValue(NameValue[] vals, String key, String defaultVal) {
		for (int i = 0; i < vals.length; i++) {
			if (key.equals(vals[i].getName()) && !"".equals(vals[i].getValue()) && (vals[i].getValue() != null)) {
				return vals[i].getValue();
			}
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
