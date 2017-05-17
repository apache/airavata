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
package org.apache.airavata.workflow.model.graph.system;

import java.util.List;

import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.component.system.DoWhileComponent;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.xmlpull.infoset.XmlElement;

public class DoWhileNode extends SystemNode {

	private static final String XPATH_TAG_NAME = "xpath";

	private String xpath;

	/**
	 * Creates a InputNode.
	 *
	 * @param graph
	 */
	public DoWhileNode(Graph graph) {
		super(graph);
		this.xpath = "$1";
	}

	/**
	 * Constructs a InputNode.
	 *
	 * @param nodeElement
	 * @throws GraphException
	 */
	public DoWhileNode(XmlElement nodeElement) throws GraphException {
		super(nodeElement);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public DoWhileComponent getComponent() {
		DoWhileComponent component = (DoWhileComponent) super.getComponent();
		if (component == null) {
			// The component is null when read from the graph XML.
			component = new DoWhileComponent();
			setComponent(component);
		}
		return component;
	}

	/**
	 * Adds additional input port.
	 */
	public void addInputPort() {
		DoWhileComponent component = getComponent();
		ComponentDataPort input = component.getInputPort();
		DataPort port = input.createPort();
		addInputPort(port);
	}

	public DataPort addInputPortAndReturn() {
		DoWhileComponent component = getComponent();
		ComponentDataPort input = component.getInputPort();
		DataPort port = input.createPort();
		addInputPort(port);
		return port;
	}

	public void removeInputPort() throws GraphException {
		List<DataPort> inputPorts = getInputPorts();
		// Remove the last one.
		DataPort inputPort = inputPorts.get(inputPorts.size() - 1);
		removeInputPort(inputPort);
	}

	/**
	 * Adds additional output port.
	 */
	public void addOutputPort() {
		DoWhileComponent component = getComponent();
		ComponentDataPort outputPort = component.getOutputPort();
		DataPort port = outputPort.createPort();
		addOutputPort(port);
	}

	/**
	 * Removes the last output port.
	 *
	 * @throws GraphException
	 */
	public void removeOutputPort() throws GraphException {
		List<DataPort> outputPorts = getOutputPorts();
		// Remove the last one.
		DataPort outputPort = outputPorts.get(outputPorts.size() - 1);
		removeOutputPort(outputPort);
	}

	public DataPort getFreeInPort() {
		List<DataPort> inputPorts = this.getInputPorts();
		for (DataPort dataPort : inputPorts) {
			if (null == dataPort.getFromNode()) {
				return dataPort;
			}
		}
		addOutputPort();
		return addInputPortAndReturn();
	}

	@Override
	protected void parseConfiguration(XmlElement configElement) {
		super.parseConfiguration(configElement);
		XmlElement element = configElement.element(null, XPATH_TAG_NAME);
		if (element != null) {
			this.xpath = element.requiredText();
		}
	}

	@Override
	protected XmlElement toXML() {
		XmlElement nodeElement = super.toXML();
		nodeElement.setAttributeValue(GraphSchema.NS,
				GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_DOWHILE);
		return nodeElement;
	}

	@Override
	protected XmlElement addConfigurationElement(XmlElement nodeElement) {
		XmlElement configElement = nodeElement.addElement(GraphSchema.NS,
				GraphSchema.NODE_CONFIG_TAG);
		if (this.xpath != null) {
			XmlElement element = configElement.addElement(GraphSchema.NS,
					XPATH_TAG_NAME);
			element.addChild(this.xpath.toString());
		}
		return configElement;
	}

	/**
	 * @return
	 *
	 */
	public EndDoWhileNode getEndDoWhileNode() {
		List<DataPort> outputPorts = this.getOutputPorts();
		for (DataPort dataPort : outputPorts) {
			List<Node> toNodes = dataPort.getToNodes();
			for (Node node : toNodes) {
				if (node instanceof EndDoWhileNode) {
					return (EndDoWhileNode) node;
				}
			}

		}
		return null;
		// throw new
		// XBayaRuntimeException("EndDoWhile node was not found for the DoWhileNode:"+this.id);
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
}