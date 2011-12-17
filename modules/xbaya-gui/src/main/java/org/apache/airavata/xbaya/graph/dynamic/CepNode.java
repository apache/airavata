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

package org.apache.airavata.xbaya.graph.dynamic;

import java.util.Collection;
import java.util.List;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.component.dynamic.CepComponent;
import org.apache.airavata.xbaya.component.dynamic.CepComponentPort;
import org.apache.airavata.xbaya.graph.ControlPort;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Edge;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.dynamic.gui.CepNodeGUI;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.impl.PortImpl;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.xmlpull.infoset.XmlElement;

public class CepNode extends NodeImpl implements PortAddable {

    protected CepNodeGUI gui;
    private String query;

    /**
     * Constructs a WSNode.
     * 
     * @param graph
     */
    public CepNode(Graph graph) {
        super(graph);
        Collection<PortImpl> allPorts = this.getAllPorts();
        for (Port port : allPorts) {
            ((CepPort) port).setNode(this);
        }
    }

    /**
     * Constructs a CepNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public CepNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
        this.setComponent(new CepComponent());
    }

    /**
     * @see org.apache.airavata.xbaya.graph.Node#getGUI()
     */
    public synchronized NodeGUI getGUI() {
        if (this.gui == null) {
            this.gui = new CepNodeGUI(this);
        }
        return this.gui;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.Node#getComponent()
     */
    @Override
    public CepComponent getComponent() {
        return (CepComponent) super.getComponent();
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.xbaya.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        GraphUtil.validateConnection(edge);
    }

    public void setName(String name) {
        super.setName(name);
        this.createID();
    }

    public DataPort getFreeInPort() {
        List<DataPort> inputPorts = this.getInputPorts();
        for (DataPort dataPort : inputPorts) {
            if (null == dataPort.getFromNode()) {
                return dataPort;
            }
        }
        // none found, so make a new one.
        CepComponentPort comPort = new CepComponentPort(getComponent());
        getComponent().addInputPort(comPort);
        DataPort port = comPort.createPort();
        ((CepPort) port).setNode(this);
        this.addInputPort(port);

        return port;
    }

    public DataPort getFreeOutPort() {
        List<DataPort> outputPorts = this.getOutputPorts();
        for (DataPort dataPort : outputPorts) {
            if (0 == dataPort.getToPorts().size()) {
                return dataPort;
            }
        }
        // none found, so make a new one.
        CepComponentPort comPort = new CepComponentPort(getComponent());
        getComponent().addOutputPort(comPort);
        DataPort port = comPort.createPort();
        ((CepPort) port).setNode(this);
        this.addOutputPort(port);

        return port;
    }

    public void removeLastDynamicallyAddedInPort() throws GraphException {

        List<DataPort> inputPorts = this.getInputPorts();
        if (inputPorts.size() == 1) {
            // This is the initial port, so leave it alone
            return;
        }
        DataPort portToBeRemoved = null;
        for (DataPort dataPort : inputPorts) {
            if (null == dataPort.getFromNode()) {
                getComponent().removeInputPort((CepComponentPort) dataPort.getComponentPort());
                portToBeRemoved = dataPort;
                break;
            }
        }
        if (null != portToBeRemoved) {
            this.removeInputPort(portToBeRemoved);
        }
    }

   
    public XmlElement toXML() {
        XmlElement nodeElement = XMLUtil.BUILDER.newFragment(GraphSchema.NS, GraphSchema.NODE_TAG);

        nodeElement.setAttributeValue(GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_CEP);
        XmlElement idElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_ID_TAG);
        idElement.addChild(getID());

        XmlElement nameElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_NAME_TAG);
        nameElement.addChild(getName());

        List<DataPort> outputPorts = getOutputPorts();
        // Output ports
        for (PortImpl port : outputPorts) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_OUTPUT_PORT_TAG);
            portElement.addChild(port.getID());
        }

        List<DataPort> inputPorts = getInputPorts();
        // Input ports
        for (PortImpl port : inputPorts) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_INPUT_PORT_TAG);
            portElement.addChild(port.getID());
        }

        // Control-in port
        if (getControlInPort() != null) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONTROL_IN_PORT_TAG);
            portElement.addChild(getControlInPort().getID());
        }

        List<ControlPort> controlOutPorts = getControlOutPorts();
        // Control-out ports
        for (PortImpl port : controlOutPorts) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONTROL_OUT_PORT_TAG);
            portElement.addChild(port.getID());
        }

        XmlElement xElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_X_LOCATION_TAG);
        xElement.addChild(Integer.toString(getPosition().x));

        XmlElement yElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_Y_LOCATION_TAG);
        yElement.addChild(Integer.toString(getPosition().y));

        addConfigurationElement(nodeElement);

        return nodeElement;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.Node#inventLabel(java.lang.String)
     */
    @Override
    public void inventLabel(String seed) {
        this.label = this.streamName + "(" + seed + ")";

    }

    /**
     * @param text
     */
    public void setQuery(String query) {
        this.query = query;

    }

    public String getQuery() {
        return this.query;

    }

}