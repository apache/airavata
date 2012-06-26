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

package org.apache.airavata.workflow.model.graph.system;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.component.system.DoWhileComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.apache.airavata.workflow.model.graph.dynamic.PortAddable;
import org.xmlpull.infoset.XmlElement;

public class DoWhileNode extends SystemNode implements PortAddable{

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


    /**
     *
     * @param edge
     * @throws GraphException
     */

    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        // XXX cannot detect if the type is array or not from WSDL at this
        // point. so no check here.
        // super.edgeWasAdded(edge);

        Port fromPort = edge.getFromPort();
        Port toPort = edge.getToPort();
        if (edge instanceof DataEdge) {
            if (fromPort instanceof EPRPort) {
                // TODO
                return;
            }

            DataPort fromDataPort = (DataPort) fromPort;
            DataPort toDataPort = (DataPort) toPort;

            QName fromType = fromDataPort.getType();
            QName toType = toDataPort.getType();

            if (fromDataPort.getNode() == this) {
                if (!(toType == null || toType.equals(WSConstants.XSD_ANY_TYPE))) {
                    fromDataPort.copyType(toDataPort);
                }
            } else if (toDataPort.getNode() == this) {
                if (!(fromType == null || fromType
                        .equals(WSConstants.XSD_ANY_TYPE))) {
                    toDataPort.copyType(fromDataPort);
                }
            } else {
                throw new WorkflowRuntimeException();
            }
        }
    }

    /**
     *
     * @param port
     * @throws GraphException
     */

    @Override
    protected void portTypeChanged(SystemDataPort port) throws GraphException {
        super.portTypeChanged(port);

        List<DataPort> inputPorts = getInputPorts();
        List<DataPort> outputPorts = getOutputPorts();

        Kind kind = port.getKind();
        int index;
        if (kind == Kind.DATA_IN) {
            index = inputPorts.indexOf(port);
        } else if (kind == Kind.DATA_OUT) {
            index = outputPorts.indexOf(port);
        } else {
            throw new WorkflowRuntimeException();
        }

        SystemDataPort inputPort = (SystemDataPort) inputPorts.get(index);
        SystemDataPort outputPort = (SystemDataPort) outputPorts.get(index);

        QName inputType = inputPort.getType();
        QName outputType = outputPort.getType();

        QName portType = port.getType();
        if (portType == null || portType.equals(WSConstants.XSD_ANY_TYPE)) {
            // Do nothing
            return;
        }

        if (port == inputPort) {
            // input -> output
            if (outputType.equals(WSConstants.XSD_ANY_TYPE)) {
              //  outputPort.copyType(port, -1);
            } else if (outputType.equals(portType)) {
                // Do nothing.
            } else {
                // XXX cannot parse array from WSDL.
                // String message = "The type of input " + index + " ("
                // + inputType + ") of " + getID()
                // + " must be same as the type of output " + index + " ("
                // + outputType + ").";
                // throw new GraphException(message);
            }

        } else if (port == outputPort) {
            // output -> input1
            if (inputType.equals(WSConstants.XSD_ANY_TYPE)) {
               // inputPort.copyType(port, 1);
            } else if (inputType.equals(portType)) {
                // Do nothing.
            } else {
                // XXX cannot parse array from WSDL.
                // String message = "The type of input " + index + " ("
                // + inputType + ") of " + getID()
                // + " must be same as the type of output " + index + " ("
                // + outputType + ").";
                // throw new GraphException(message);
            }
        } else {
            throw new WorkflowRuntimeException();
        }
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


    public void removeLastDynamicallyAddedInPort() throws GraphException {

        List<DataPort> inputPorts = this.getInputPorts();
        List<DataPort> outputPorts = this.getOutputPorts();
        if (inputPorts.size() == 1) {
            //This is the initial port, so leave it alone
            return;
        }
        if (outputPorts.size() == 1) {
            return;
        }
        DataPort portToBeRemoved = null;
        for (DataPort dataPort : inputPorts) {
            if (null == dataPort.getFromNode()) {
                removeInputPort(dataPort);
                portToBeRemoved = dataPort;
                break;
            }
        }

        if(outputPorts.size() == this.getInputPorts().size()){
            return;
        }
        for (DataPort dataPort : outputPorts) {
            if (0 == dataPort.getToNodes().size()) {
                removeOutputPort(dataPort);
                portToBeRemoved = dataPort;
                break;
            }
        }
    }

    @Override
	protected void parseConfiguration(XmlElement configElement) {
		super.parseConfiguration(configElement);
		XmlElement element = configElement.element(null, XPATH_TAG_NAME);
		if (element != null) {
			// TODO
			this.xpath = element.requiredText();
		}
	}
	@Override
	protected XmlElement toXML() {
		XmlElement nodeElement = super.toXML();
		nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_DOWHILE);
		return nodeElement;
	}

	@Override
	protected XmlElement addConfigurationElement(XmlElement nodeElement) {
		XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);
		if (this.xpath != null) {
			XmlElement element = configElement.addElement(GraphSchema.NS, XPATH_TAG_NAME);
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
				if(node instanceof EndDoWhileNode){
					return (EndDoWhileNode) node;
				}
			}

		}
		return null;
//		throw new XBayaRuntimeException("EndDoWhile node was not found for the DoWhileNode:"+this.id);
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
}