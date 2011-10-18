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

package org.apache.airavata.xbaya.ode.gui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.gui.StreamSourceNode;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;
import org.ietf.jgss.GSSCredential;

public class ODEDeploymentClient {

	private XBayaEngine engine;

	private WaitDialog invokingDialog;

	public ODEDeploymentClient(XBayaEngine engine) {
		this.engine = engine;
	}

	public ODEDeploymentClient(XBayaEngine engine, WaitDialog invokingDialog) {
		this(engine);
		this.invokingDialog = invokingDialog;
	}

	/**
	 * Deploy to ODE and XRegistry
	 * 
	 * @param wfClient
	 * @param workflow
	 * @param gssCredential
	 * @param makePublic
	 */
	public void deploy(WorkflowProxyClient wfClient, Workflow workflow,
			GSSCredential gssCredential, boolean makePublic) {
		try {

			org.xmlpull.infoset.XmlElement workflowXml = workflow.toXML();
			XMLUtil.xmlElementToString(workflowXml);
			wfClient.deploy(workflow, false);
			hideUI();
			String oldWorkflowName = workflow.getName();

		} catch (Throwable e) {
			hideUI();

			// The swing components get confused when there is html in the error
			// message
			if (e.getMessage() != null
					&& e.getMessage().indexOf("<html>") != -1) {
				this.engine.getErrorWindow().error(
						e.getMessage().substring(0,
								e.getMessage().indexOf("<html>")));
			} else {
				this.engine.getErrorWindow().error(e);
			}
		}
	}

	/**
	 * @param oldWorkflow
	 * @return static inputs
	 */
	private LinkedList<InputNode> getStaticInputNodes(Workflow workflow) {

		List<NodeImpl> nodes = workflow.getGraph().getNodes();
		LinkedList<InputNode> streamNodes = new LinkedList<InputNode>();
		LinkedList<InputNode> ret = new LinkedList<InputNode>();
		for (NodeImpl nodeImpl : nodes) {
			if (nodeImpl instanceof StreamSourceNode) {
				streamNodes.addAll(((StreamSourceNode) nodeImpl)
						.getInputNodes());
			}
		}

		for (NodeImpl nodeImpl : nodes) {
			if (nodeImpl instanceof InputNode
					&& !streamNodes.contains(nodeImpl)) {
				ret.add((InputNode) nodeImpl);
			}
		}
		return ret;
	}

	private void hideUI() {
		if (this.invokingDialog != null) {
			this.invokingDialog.hide();
		}
	}

}