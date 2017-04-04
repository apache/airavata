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
//import org.apache.airavata.client.api.AiravataAPI;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.ws.monitor.Monitor;
//import org.apache.airavata.xbaya.XBayaConfiguration;
//import org.apache.airavata.xbaya.jython.lib.NotificationSender;
//import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
//import org.apache.airavata.xbaya.ui.XBayaGUI;
//
//import java.net.URI;
//
////import org.apache.airavata.registry.api.AiravataRegistry2;
//
//public class WorkflowInterpreterConfiguration {
////	public static final int GUI_MODE = 1;
////	public static final int SERVER_MODE = 2;
////	
//	private URI messageBoxURL;
//	private URI messageBrokerURL;
//	private AiravataAPI registry;
//	private XBayaConfiguration configuration;
//	private XBayaGUI gui;
//	private Monitor monitor;
//	private boolean offline=false;
//	private boolean runWithCrossProduct=false;
//	private Workflow workflow;
//	private WorkflowNotifiable notifier;
//	private String topic;
//    private boolean gfacEmbeddedMode = false;
//	private Boolean actOnProvenance = null;
//	private boolean subWorkflow;
//	private boolean testMode=false;
//    private AiravataAPI airavataAPI;
//    private String awsAccessKey;
//    private String awsSecretKey;
//
//	public WorkflowInterpreterConfiguration(Workflow workflow,
//                                            String topic,
//                                            URI messageBoxURL,
//                                            URI messageBrokerURL,
//                                            AiravataAPI registry,
//                                            XBayaConfiguration configuration,
//                                            XBayaGUI gui,Monitor monitor) {
//		this(workflow, topic, messageBoxURL,messageBrokerURL,registry,configuration,gui,monitor, true);
//	}
//	
//    public WorkflowInterpreterConfiguration(Workflow workflow,
//                                            String topic,
//                                            URI messageBoxURL,
//                                            URI messageBrokerURL,
//                                            AiravataAPI airavataAPI,
//                                            XBayaConfiguration configuration,
//                                            XBayaGUI gui,
//                                            Monitor monitor,
//                                            boolean offline) {
//        this.messageBoxURL = messageBoxURL;
//        this.messageBrokerURL = messageBrokerURL;
//        this.airavataAPI = airavataAPI;
//        this.configuration = configuration;
//        this.gui = gui;
//        this.monitor = monitor;
//        this.offline = offline;
//        this.workflow = workflow;
//        this.topic = topic;
//    }
//	
//	public URI getMessageBoxURL() {
//		return messageBoxURL;
//	}
//	public void setMessageBoxURL(URI messageBoxURL) {
//		this.messageBoxURL = messageBoxURL;
//	}
//	public URI getMessageBrokerURL() {
//		return messageBrokerURL;
//	}
//	public void setMessageBrokerURL(URI messageBrokerURL) {
//		this.messageBrokerURL = messageBrokerURL;
//	}
//	public AiravataAPI getExperimentCatalog() {
//		return registry;
//	}
//	public void setRegistry(AiravataAPI registry) {
//		this.registry = registry;
//	}
//	public XBayaConfiguration getConfiguration() {
//		return configuration;
//	}
//	public void setConfiguration(XBayaConfiguration configuration) {
//		this.configuration = configuration;
//	}
//	public XBayaGUI getGUI() {
//		return gui;
//	}
//	public void setGUI(XBayaGUI gui) {
//		this.gui = gui;
//	}
//	public Monitor getMonitor() {
//		return monitor;
//	}
//	public void setMonitor(Monitor monitor) {
//		this.monitor = monitor;
//	}
//
//	public boolean isOffline() {
//		return offline;
//	}
//
//	public void setOffline(boolean offline) {
//		this.offline = offline;
//	}
//
//	public boolean isRunWithCrossProduct() {
//		return runWithCrossProduct;
//	}
//
//	public void setRunWithCrossProduct(boolean runWithCrossProduct) {
//		this.runWithCrossProduct = runWithCrossProduct;
//	}
//
//	public Workflow getWorkflow() {
//		return workflow;
//	}
//
//	public void setWorkflow(Workflow workflow) {
//		this.workflow = workflow;
//	}
//
//	public WorkflowNotifiable getNotifier() {
//		if (notifier==null){
//			notifier=new NotificationSender(getMessageBrokerURL(), getTopic());
//		}
//		return notifier;
//	}
//
//	public void validateNotifier(){
//		getNotifier();
//	}
//	
//	public String getTopic() {
//		return topic;
//	}
//
//	public void setTopic(String topic) {
//		this.topic = topic;
//	}
//
////	public int getMode() {
////		return getMonitor()==null? SERVER_MODE:GUI_MODE;
////	}
//
//	public boolean isGfacEmbeddedMode() {
//		return gfacEmbeddedMode;
//	}
//
//	public void setGfacEmbeddedMode(boolean gfacEmbeddedMode) {
//		this.gfacEmbeddedMode = gfacEmbeddedMode;
//	}
//
//	public Boolean isActOnProvenance() {
//		return actOnProvenance;
//	}
//
//	public void setActOnProvenance(Boolean actOnProvenance) {
//		this.actOnProvenance = actOnProvenance;
//	}
//
//	public boolean isSubWorkflow() {
//		return subWorkflow;
//	}
//
//	public void setSubWorkflow(boolean subWorkflow) {
//		this.subWorkflow = subWorkflow;
//	}
//	
//	public void setNotifier(WorkflowNotifiable notifier) {
//		this.notifier = notifier;
//		setTestMode(notifier instanceof StandaloneNotificationSender);
//	}
//
//	public boolean isTestMode() {
//		return testMode;
//	}
//
//	private void setTestMode(boolean testMode) {
//		this.testMode = testMode;
//	}
//
//    public AiravataAPI getAiravataAPI() {
//        return airavataAPI;
//    }
//
//    public void setAiravataAPI(AiravataAPI airavataAPI) {
//        this.airavataAPI = airavataAPI;
//    }
//
//    public String getAwsAccessKey() {
//        return awsAccessKey;
//    }
//
//    public void setAwsAccessKey(String awsAccessKey) {
//        this.awsAccessKey = awsAccessKey;
//    }
//
//    public String getAwsSecretKey() {
//        return awsSecretKey;
//    }
//
//    public void setAwsSecretKey(String awsSecretKey) {
//        this.awsSecretKey = awsSecretKey;
//    }
//}
