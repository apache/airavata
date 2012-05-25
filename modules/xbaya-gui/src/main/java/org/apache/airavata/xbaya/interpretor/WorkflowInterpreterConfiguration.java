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

import java.net.URI;

import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.monitor.Monitor;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.utils.MyProxyChecker;

public class WorkflowInterpreterConfiguration {
	private URI messageBoxURL;
	private URI messageBrokerURL;
	private AiravataRegistry registry;
	private XBayaConfiguration configuration;
	private XBayaGUI gui;
	private MyProxyChecker myProxyChecker;
	private Monitor monitor;
	
	public WorkflowInterpreterConfiguration(URI messageBoxURL,URI messageBrokerURL,AiravataRegistry registry,XBayaConfiguration configuration,XBayaGUI gui,MyProxyChecker myProxyChecker,Monitor monitor) {
		this.messageBoxURL = messageBoxURL;
		this.messageBrokerURL = messageBrokerURL;
		this.registry = registry;
		this.configuration = configuration;
		this.gui = gui;
		this.myProxyChecker = myProxyChecker;
		this.monitor = monitor;
	}
	
	public URI getMessageBoxURL() {
		return messageBoxURL;
	}
	public void setMessageBoxURL(URI messageBoxURL) {
		this.messageBoxURL = messageBoxURL;
	}
	public URI getMessageBrokerURL() {
		return messageBrokerURL;
	}
	public void setMessageBrokerURL(URI messageBrokerURL) {
		this.messageBrokerURL = messageBrokerURL;
	}
	public AiravataRegistry getRegistry() {
		return registry;
	}
	public void setRegistry(AiravataRegistry registry) {
		this.registry = registry;
	}
	public XBayaConfiguration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(XBayaConfiguration configuration) {
		this.configuration = configuration;
	}
	public XBayaGUI getGUI() {
		return gui;
	}
	public void setGUI(XBayaGUI gui) {
		this.gui = gui;
	}
	public MyProxyChecker getMyProxyChecker() {
		return myProxyChecker;
	}
	public void setMyProxyChecker(MyProxyChecker myProxyChecker) {
		this.myProxyChecker = myProxyChecker;
	}
	public Monitor getMonitor() {
		return monitor;
	}
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}
	
}
