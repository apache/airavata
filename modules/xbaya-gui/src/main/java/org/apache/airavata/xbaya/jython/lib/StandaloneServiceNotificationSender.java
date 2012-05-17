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

package org.apache.airavata.xbaya.jython.lib;

import java.net.URI;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.addressing.EndpointReference;

import xsul.wsif.WSIFMessage;

public class StandaloneServiceNotificationSender implements ServiceNotifiable {

    private Workflow workflow;
    private String serviceID;
    private URI workflowID;

    public StandaloneServiceNotificationSender(Workflow workflow, URI workflowID) {
        this.workflow = workflow;
        this.workflowID = workflowID;
    }

    @Override
    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
        System.out.println(serviceID);

    }

    @Override
    public EndpointReference getEventSink() {
        return new EndpointReference(XBayaConstants.DEFAULT_BROKER_URL.toString());
    }

    @Override
    public URI getWorkflowID() {
        return this.workflowID;
    }

    @Override
    public void invokingService(WSIFMessage inputs) {
        getNodeGUI().setBodyColor(NodeState.EXECUTING.color);
    }

    @Override
    public void serviceFinished(WSIFMessage outputs) {
    	getNodeGUI().setBodyColor(NodeState.FINISHED.color);

    }

    @Override
    public void invocationFailed(String message, Throwable e) {
    	getNodeGUI().setBodyColor(NodeState.FAILED.color);

    }

    @Override
    public void receivedFault(String message) {
    	getNodeGUI().setBodyColor(NodeState.FAILED.color);

    }

    @Override
    public void receivedFault(WSIFMessage fault) {
    	getNodeGUI().setBodyColor(NodeState.FAILED.color);

    }

	private NodeGUI getNodeGUI() {
		return NodeController.getGUI(this.workflow.getGraph().getNode(this.serviceID));
	}

}
