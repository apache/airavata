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
package org.apache.airavata.xbaya;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.workflow.model.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.core.ide.XBayaExecutionModeListener;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul.lead.LeadDeploymentConfig;

public class XBayaConfiguration extends Observable implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(XBayaConfiguration.class);

    private String title = XBayaConstants.APPLICATION_NAME;

    private String workflow = null;

    private String ogceWorkflow = null;

    private List<String> localRegistris = new ArrayList<String>();

    // GPEL Engine related

    private URI gpelEngineURL = XBayaConstants.DEFAULT_GPEL_ENGINE_URL;

    private URI gpelTemplateID = null;

    private URI gpelInstanceID = null;

    // ODE
    private String odeURL = XBayaConstants.DEFAULT_ODE_URL;

    // WorkflowInterpreter
    private URI workflowInterpreterURL = XBayaConstants.DEFAULT_WORKFLOW_INTERPRETER_URL;

    // Proxy Service

    private URI proxyURI = XBayaConstants.DEFAULT_PROXY_URI;

    private URI dscURL = XBayaConstants.DEFAULT_DSC_URL;

    // Monitor related

    private boolean startMonitor = false;

    private URI brokerURL = XBayaConstants.DEFAULT_BROKER_URL;

    private String topic = null;

    private boolean pullMode = true;

    private URI messageBoxURL = XBayaConstants.DEFAULT_MESSAGE_BOX_URL;

    // Kerma

    private URI karmaURL = null;

    private URI karmaWorkflowInstanceID = null;

    // MyProxy

    private String myProxyServer = XBayaConstants.DEFAULT_MYPROXY_SERVER;

    private int myProxyPort = XBayaConstants.DEFAULT_MYPROXY_PORT;

    private int myProxyLifetime = XBayaConstants.DEFAULT_MYPROXY_LIFTTIME;

    private String myProxyUsername = null;

    private String myProxyPassphrase = null;

    private boolean loadMyProxy = false;

    private boolean loadRunJythonWorkflow = false;

    // Size

    private int width;

    private int height;

    private int x = 50;

    private int y= 50;

    // Errors

    private List<Throwable> errors;

    private boolean closeOnExit = true;

    private boolean collectProvenance = false;

    private boolean provenanceSmartRun = false;

    private boolean runWithCrossProduct = true;

    private String trustedCertLocation = "";

    private JCRComponentRegistry jcrComponentRegistry=null;

    private XBayaExecutionMode xbayaExecutionMode=XBayaExecutionMode.IDE;
    
    private List<XBayaExecutionModeListener> xbayaExecutionModeChangeListners=new ArrayList<XBayaExecutionModeListener>();

    private boolean regURLSetByCMD = false;

    private Map<ThriftServiceType, ThriftClientData> thriftClientDataList = new HashMap<ThriftServiceType, ThriftClientData>();
    
    public enum XBayaExecutionMode{
    	IDE,
    	MONITOR
    }
    /**
     * Constructs an XwfConfiguration.
     */
    public XBayaConfiguration() {
        this.errors = new ArrayList<Throwable>();

        // Read from system properties first.
        String systemConfig = System.getProperty("xbaya.config");
        try {
            if (systemConfig != null) {
                loadConfiguration(systemConfig);
            }
        } catch (RuntimeException e) {
            String message = "Error while reading a configuration file, " + systemConfig;
            logger.warn(message, e);
        }
    }

    /**
     * @param configFilePath
     */
    public void loadConfiguration(String configFilePath) {
        File configFile = new File(configFilePath);
        URI uri = configFile.toURI();
        loadConfiguration(uri);
    }

    private void loadConfiguration(URI uri) {
        LeadDeploymentConfig config = LeadDeploymentConfig.loadConfig(null, uri);
        URI gpel = config.getGpelUrl();
        if (gpel != null) {
            this.gpelEngineURL = config.getGpelUrl();
        }
        URI dsc = config.getDscUrl();
        if (dsc != null) {
            this.dscURL = dsc;
        }
        URI broker = config.getBrokerUrl();
        if (broker != null) {
            this.brokerURL = broker;
        }
        URI msgBox = config.getMsgBoxUrl();
        if (msgBox != null) {
            this.messageBoxURL = msgBox;
        }
    }

    /**
     * Returns the title.
     * 
     * @return The title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets title.
     * 
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the defaultWorkflow.
     * 
     * @return The defaultWorkflow
     */
    public String getWorkflow() {
        return this.workflow;
    }

    /**
     * Sets defaultWorkflow.
     * 
     * @param defaultWorkflow
     *            The defaultWorkflow to set.
     */
    public void setWorkflow(String defaultWorkflow) {
        this.workflow = defaultWorkflow;
    }

    /**
     * Sets ogceWorkflow.
     * 
     * @param ogceWorkflow
     *            The ogceWorkflow to set.
     */
    public void setOGCEWorkflow(String ogceWorkflow) {
        this.ogceWorkflow = ogceWorkflow;
    }

    /**
     * Returns the ogceWorkflow.
     * 
     * @return The ogceWorkflow
     */
    public String getOGCEWorkflow() {
        return this.ogceWorkflow;
    }

    /**
     * Enable the system default local registry.
     */
    public void enableLocalRegistry() {
        addLocalRegistry(XBayaPathConstants.WSDL_DIRECTORY);
    }

    /**
     * @param path
     *            The path of local registry.
     */
    public void addLocalRegistry(String path) {
        this.localRegistris.add(path);
    }

    /**
     * @return The list of pathes of local registries.
     */
    public List<String> getLocalRegistry() {
        return this.localRegistris;
    }

    /**
     * Returns the gpelEngineUrl.
     * 
     * @return The gpelEngineUrl
     */
    public URI getGPELEngineURL() {
        return this.gpelEngineURL;
    }

    /**
     * Sets gpelEngineUrl.
     * 
     * @param gpelEngineURL
     *            The gpelEngineUrl to set.
     */
    public void setGPELEngineURL(URI gpelEngineURL) {
        this.gpelEngineURL = gpelEngineURL;
    }

    /**
     * @param templateID
     */
    public void setGPELTemplateID(URI templateID) {
        this.gpelTemplateID = templateID;
    }

    /**
     * @return The GPEL template ID.
     */
    public URI getGPELTemplateID() {
        return this.gpelTemplateID;
    }

    /**
     * Returns the gpelInstanceID.
     * 
     * @return The gpelInstanceID
     */
    public URI getGPELInstanceID() {
        return this.gpelInstanceID;
    }

    /**
     * Sets gpelInstanceID.
     * 
     * @param gpelInstanceID
     *            The gpelInstanceID to set.
     */
    public void setGPELInstanceID(URI gpelInstanceID) {
        this.gpelInstanceID = gpelInstanceID;
    }

    /**
     * @return The DSC URL
     */
    public URI getDSCURL() {
        return this.dscURL;
    }

    /**
     * @param dscURL
     */
    public void setDSCURL(URI dscURL) {
        this.dscURL = dscURL;
    }

    /**
     * Returns the topic.
     * 
     * @return The topic
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Sets topic.
     * 
     * @param topic
     *            The topic to set.
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Returns the messageBoxUrl.
     * 
     * @return The messageBoxUrl
     */
    public URI getMessageBoxURL() {
        return this.messageBoxURL;
    }

    /**
     * Sets messageBoxUrl.
     * 
     * @param messageBoxURL
     *            The messageBoxUrl to set.
     */
    public void setMessageBoxURL(URI messageBoxURL) {
        this.messageBoxURL = messageBoxURL;
    }

    /**
     * Returns the pullMode.
     * 
     * @return The pullMode
     */
    public boolean isPullMode() {
        return this.pullMode;
    }

    /**
     * Sets pullMode.
     * 
     * @param pullMode
     *            The pullMode to set.
     */
    public void setPullMode(boolean pullMode) {
        this.pullMode = pullMode;
    }

    /**
     * Returns the brokerLocation.
     * 
     * @return The brokerLocation
     */
    public URI getBrokerURL() {
        return this.brokerURL;
    }

    /**
     * Sets brokerLocation.
     * 
     * @param brokerURL
     *            The brokerLocation to set.
     */
    public void setBrokerURL(URI brokerURL) {
        this.brokerURL = brokerURL;
    }

    /**
     * Returns the startMonitor.
     * 
     * @return The startMonitor
     */
    public boolean isStartMonitor() {
        return this.startMonitor;
    }

    /**
     * Sets startMonitor.
     * 
     * @param startMonitor
     *            The startMonitor to set.
     */
    public void setStartMonitor(boolean startMonitor) {
        this.startMonitor = startMonitor;
    }

    /**
     * Returns the kermaURI.
     * 
     * @return The kermaURI
     */
    public URI getKarmaURL() {
        return this.karmaURL;
    }

    /**
     * Sets kermaURI.
     * 
     * @param kermaURI
     *            The kermaURI to set.
     */
    public void setKarmaURL(URI kermaURI) {
        this.karmaURL = kermaURI;
    }

    /**
     * Returns the kermaWorkflowInstanceID.
     * 
     * @return The kermaWorkflowInstanceID
     */
    public URI getKarmaWorkflowInstanceID() {
        return this.karmaWorkflowInstanceID;
    }

    /**
     * Sets kermaWorkflowInstanceID.
     * 
     * @param karmaWorkflowInstanceID
     *            The kermaWorkflowInstanceID to set.
     */
    public void setKarmaWorkflowInstanceID(URI karmaWorkflowInstanceID) {
        this.karmaWorkflowInstanceID = karmaWorkflowInstanceID;
    }

    /**
     * Returns the myProxyServer.
     * 
     * @return The myProxyServer
     */
    public String getMyProxyServer() {
        return this.myProxyServer;
    }

    /**
     * Sets myProxyServer.
     * 
     * @param myProxyServer
     *            The myProxyServer to set.
     */
    public void setMyProxyServer(String myProxyServer) {
        this.myProxyServer = myProxyServer;
    }

    /**
     * Returns the myProxyPort.
     * 
     * @return The myProxyPort
     */
    public int getMyProxyPort() {
        return this.myProxyPort;
    }

    /**
     * Sets myProxyPort.
     * 
     * @param myProxyPort
     *            The myProxyPort to set.
     */
    public void setMyProxyPort(int myProxyPort) {
        this.myProxyPort = myProxyPort;
    }

    /**
     * Returns the myProxyLifetime.
     * 
     * @return The myProxyLifetime
     */
    public int getMyProxyLifetime() {
        return this.myProxyLifetime;
    }

    /**
     * Sets myProxyLifetime.
     * 
     * @param myProxyLifetime
     *            The myProxyLifetime to set.
     */
    public void setMyProxyLifetime(int myProxyLifetime) {
        this.myProxyLifetime = myProxyLifetime;
    }

    /**
     * Returns the myProxyUsername.
     * 
     * @return The myProxyUsername
     */
    public String getMyProxyUsername() {
        return this.myProxyUsername;
    }

    /**
     * Sets myProxyUsername.
     * 
     * @param myProxyUsername
     *            The myProxyUsername to set.
     */
    public void setMyProxyUsername(String myProxyUsername) {
        this.myProxyUsername = myProxyUsername;
    }

    /**
     * Returns the odeURL.
     * 
     * @return The odeURL
     */
    public String getOdeURL() {
        return this.odeURL;
    }

    /**
     * Sets odeURL.
     * 
     * @param odeURL
     *            The odeURL to set.
     */
    public void setOdeURL(String odeURL) {
        this.odeURL = odeURL;
    }

    /**
     * Returns the workflowInterpreterURL.
     * 
     * @return The workflowInterpreterURL
     */
    public URI getWorkflowInterpreterURL() {
        return this.workflowInterpreterURL;
    }

    /**
     * Sets workflowInterpreterURL.
     * 
     * @param workflowInterpreterURL
     *            The workflowInterpreterURL to set.
     */
    public void setWorkflowInterpreterURL(URI workflowInterpreterURL) {
        this.workflowInterpreterURL = workflowInterpreterURL;
    }

    /**
     * Sets proxyURI.
     * 
     * @param proxyURI
     *            The proxyURI to set.
     */
    public void setProxyURI(URI proxyURI) {
        this.proxyURI = proxyURI;
    }

    /**
     * Returns the myProxyPassphrase.
     * 
     * @return The myProxyPassphrase
     */
    public String getMyProxyPassphrase() {
        return this.myProxyPassphrase;
    }

    /**
     * Sets myProxyPassphrase.
     * 
     * @param myProxyPassphrase
     *            The myProxyPassphrase to set.
     */
    public void setMyProxyPassphrase(String myProxyPassphrase) {
        this.myProxyPassphrase = myProxyPassphrase;
    }

    /**
     * Returns the loadMyProxy.
     * 
     * @return The loadMyProxy
     */
    public boolean isLoadMyProxy() {
        return this.loadMyProxy;
    }

    /**
     * Sets loadMyProxy.
     * 
     * @param loadMyProxy
     *            The loadMyProxy to set.
     */
    public void setLoadMyProxy(boolean loadMyProxy) {
        this.loadMyProxy = loadMyProxy;
    }

    /**
     * Returns the loadRunJythonWorkflow.
     * 
     * @return The loadRunJythonWorkflow
     */
    public boolean isLoadRunJythonWorkflow() {
        return this.loadRunJythonWorkflow;
    }

    /**
     * Sets loadRunJythonWorkflow.
     * 
     * @param loadRunJythonWorkflow
     *            The loadRunJythonWorkflow to set.
     */
    public void setLoadRunJythonWorkflow(boolean loadRunJythonWorkflow) {
        this.loadRunJythonWorkflow = loadRunJythonWorkflow;
    }

    /**
     * Returns the height.
     * 
     * @return The height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Sets height.
     * 
     * @param height
     *            The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the width.
     * 
     * @return The width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Sets width.
     * 
     * @param width
     *            The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Add an error.
     * 
     * @param e
     */
    public void addError(Throwable e) {
        this.errors.add(e);
    }

    /**
     * Returns errors.
     * 
     * @return Errors.
     */
    public Iterable<Throwable> getErrors() {
        return this.errors;
    }

    /**
     * @return
     */
    public String getODEURL() {
        // TODO Auto-generated method stub
        return this.odeURL;
    }

    //

    /**
     * Returns current proxy URI
     * 
     * @return
     */
    public URI getProxyURI() {
        return this.proxyURI;
    }

    /**
     * @param b
     */
    public void setCloseOnExit(boolean b) {
        this.closeOnExit = b;
    }

    /**
     * @return
     */
    public boolean isCloseOnExit() {
        return this.closeOnExit;
    }

    public void servicesChanged(ThriftServiceType type) {
        if (type==ThriftServiceType.API_SERVICE) {
        	try {
				Client airavataClient = XBayaUtil.getAiravataClient(getThriftClientData(ThriftServiceType.API_SERVICE));
				if (getJcrComponentRegistry() == null) {
					setJcrComponentRegistry(new JCRComponentRegistry(getThriftClientData(ThriftServiceType.API_SERVICE).getGatewayId(),airavataClient));
				} else {
					getJcrComponentRegistry().setClient(airavataClient);
				}
				triggerObservers(getJcrComponentRegistry());
			} catch (AiravataClientConnectException e) {
                logger.error(e.getMessage(), e);
			}
		}
    }

    protected void triggerObservers(Object o) {
        setChanged();
        notifyObservers(o);
    }

    public void update(Observable observable, Object o) {
        triggerObservers(observable);
    }

    public boolean isCollectProvenance() {
        return collectProvenance;
    }

    public boolean isProvenanceSmartRun() {
        return provenanceSmartRun;
    }

    public void setCollectProvenance(boolean collectProvenance) {
        this.collectProvenance = collectProvenance;
    }

    public void setProvenanceSmartRun(boolean provenanceSmartRun) {
        this.provenanceSmartRun = provenanceSmartRun;
    }

    public void setRunWithCrossProduct(boolean runWithCrossProduct) {
        this.runWithCrossProduct = runWithCrossProduct;
    }

    public boolean isRunWithCrossProduct() {
        return runWithCrossProduct;
    }

    public String getTrustedCertLocation() {
        return trustedCertLocation;
    }

    public void setTrustedCertLocation(String trustedCertLocation) {
        this.trustedCertLocation = trustedCertLocation;
    }

	public XBayaExecutionMode getXbayaExecutionMode() {
		return xbayaExecutionMode;
	}

	public void setXbayaExecutionMode(XBayaExecutionMode xbayaExecutionMode) {
		boolean modeChanged=(this.xbayaExecutionMode != xbayaExecutionMode);
		this.xbayaExecutionMode = xbayaExecutionMode;
		if (modeChanged) {
			for (XBayaExecutionModeListener listner : xbayaExecutionModeChangeListners) {
				try {
					listner.executionModeChanged(this);
				} catch (Exception e) {
                    logger.error(e.getMessage(), e);
				}
			}
		}
	}
	
	public void registerExecutionModeChangeListener(XBayaExecutionModeListener listner){
		xbayaExecutionModeChangeListners.add(listner);
	}
	
	public void unregisterExecutionModeChangeListener(XBayaExecutionModeListener listner){
		if (xbayaExecutionModeChangeListners.contains(listner)) {
			xbayaExecutionModeChangeListners.remove(listner);
		}
	}

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

	public JCRComponentRegistry getJcrComponentRegistry() {
		return jcrComponentRegistry;
	}

	public void setJcrComponentRegistry(JCRComponentRegistry jcrComponentRegistry) {
		this.jcrComponentRegistry = jcrComponentRegistry;
	}

    public boolean isRegURLSetByCMD() {
        return regURLSetByCMD;
    }

    public void setRegURLSetByCMD(boolean regURLSetByCMD) {
        this.regURLSetByCMD = regURLSetByCMD;
    }

	public Map<ThriftServiceType, ThriftClientData> getThriftClientDataList() {
		return thriftClientDataList;
	}

	public void addThriftClientData(ThriftClientData data){
		getThriftClientDataList().put(data.getServiceType(), data);
		servicesChanged(data.getServiceType());
	}
	
	public ThriftClientData getThriftClientData(ThriftServiceType serviceType){
		return (isThriftServiceDataExist(serviceType)?getThriftClientDataList().get(serviceType):null);
	}

	public boolean isThriftServiceDataExist(ThriftServiceType serviceType) {
		return getThriftClientDataList().containsKey(serviceType);
	}
	
	
}