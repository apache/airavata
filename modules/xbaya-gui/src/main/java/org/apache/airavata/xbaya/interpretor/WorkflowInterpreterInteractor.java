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

import javax.xml.namespace.QName;

import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;

import xsul.wsdl.WsdlDefinitions;

public interface WorkflowInterpreterInteractor {
	public boolean notify(WorkflowExecutionMessage messageType, WorkflowInterpreterConfiguration config, Object data);
	public Object retrieveData(WorkflowExecutionMessage messageType, WorkflowInterpreterConfiguration config, Object data) throws Exception;
	public static class TaskNotification{
		String messageTitle;
		String message;
		String messageId;
		public TaskNotification(String messageTitle, String message, String messageId) {
			this.messageTitle=messageTitle;
			this.message=message;
			this.messageId=messageId;
		}
	}
	
	public static class WorkflowExecutionData{
		Workflow workflow;
		String topic;
		WorkflowInterpreter currentInterpreter;
		public WorkflowExecutionData(Workflow workflow,String topic, WorkflowInterpreter currentInterpreter) {
			this.workflow=workflow;
			this.topic=topic;
			this.currentInterpreter=currentInterpreter;
		}
	}
	
	public static class WSNodeData{
		WSNode wsNode;
		WorkflowInterpreter currentInterpreter;
		public WSNodeData(WSNode wsNode, WorkflowInterpreter currentInterpreter) {
			this.wsNode=wsNode;
			this.currentInterpreter=currentInterpreter;
		}
	}
	
	public static class GFacInvokerData{
		QName portTypeQName;
		WsdlDefinitions wsdl;
		String nodeID;
		String messageBoxURL;
        String gfacURL;
        WorkflowNotifiable notifier;
        String topic;
        AiravataRegistry registry;
        String serviceName;
        XBayaConfiguration config;
        boolean embeddedMode;
        
        public GFacInvokerData(boolean embeddedMode, QName portTypeQName, WsdlDefinitions wsdl, String nodeID, String messageBoxURL,
                String gfacURL, WorkflowNotifiable notifier,String topic,AiravataRegistry registry,String serviceName,XBayaConfiguration config) {
        	this.embeddedMode=embeddedMode;
        	this.portTypeQName = portTypeQName;
        	this.wsdl = wsdl;
        	this.nodeID = nodeID;
        	this.messageBoxURL = messageBoxURL;
        	this.gfacURL = gfacURL;
        	this.notifier = notifier;
        	this.topic = topic;
        	this.registry = registry;
        	this.serviceName = serviceName;
        	this.config = config;
		}
	}
}
