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
package org.apache.airavata.workflow.model.graph.amazon;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.amazon.InstanceComponent;
import org.apache.airavata.workflow.model.graph.ControlEdge;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.xmlpull.infoset.XmlElement;

public class InstanceNode extends ResourceNode {

    /*
     * XML configuration
     */
    private static final String NEW_TAG_NAME = "newInstance";
    private static final String AMI_ID_TAG_NAME = "ami";
    private static final String INSTANCE_ID_TAG_NAME = "instance";
    private static final String INSTANCE_TYPE_TAG_NAME = "type";
    private static final String USERNAME_TAG_NAME = "username";

    private boolean startNewInstance;

    private String instanceId;

    private String amiId;

    private String username;

    private String instanceType;

    private String outputInstanceId;

    /**
     * 
     * Constructs a InstanceNode.
     * 
     * @param graph
     */
    public InstanceNode(Graph graph) {
        super(graph);
        this.startNewInstance = true;
    }

    /**
     * Constructs an InstanceNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public InstanceNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    public boolean isStartNewInstance() {
        return this.startNewInstance;
    }

    public void setStartNewInstance(boolean startNewInstance) {
        this.startNewInstance = startNewInstance;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInstanceType() {
        return this.instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Returns the instanceId.
     * 
     * @return The instanceId
     */
    public String getInstanceId() {
        return this.instanceId;
    }

    /**
     * Sets instanceId.
     * 
     * @param instanceId
     *            The instanceId to set.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Sets amiId.
     * 
     * @param amiId
     *            The amiId to set.
     */
    public void setAmiId(String amiId) {
        this.amiId = amiId;
    }

    /**
     * 
     * @return AMI_ID or Instance ID depend on user's choice
     */
    public String getIdAsValue() {
        if (this.startNewInstance)
            return this.amiId;
        else
            return this.instanceId;
    }

    /**
     * 
     * @return
     */
    public String getOutputInstanceId() {
        return this.outputInstanceId;
    }

    /**
     * 
     * @param outputInstanceId
     */
    public void setOutputInstanceId(String outputInstanceId) {
        this.outputInstanceId = outputInstanceId;
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new InstanceComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#parseConfiguration(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void parseConfiguration(XmlElement configElement) {
        super.parseConfiguration(configElement);

        // new instance
        XmlElement element = configElement.element(null, NEW_TAG_NAME);
        if (element != null) {
            for (Object child : element.children()) {
                this.startNewInstance = Boolean.valueOf((String) child).booleanValue();
            }
        }

        if (this.startNewInstance) {
            // ami id
            XmlElement element2 = configElement.element(null, AMI_ID_TAG_NAME);
            if (element != null) {
                for (Object child : element2.children()) {
                    this.amiId = (String) child;
                }
            }

            // instance type
            XmlElement element3 = configElement.element(null, INSTANCE_TYPE_TAG_NAME);
            if (element != null) {
                for (Object child : element3.children()) {
                    this.instanceType = (String) child;
                }
            }

        } else {
            // instance id
            XmlElement element2 = configElement.element(null, INSTANCE_ID_TAG_NAME);
            if (element != null) {
                for (Object child : element2.children()) {
                    this.instanceId = (String) child;
                }
            }
        }

        // username
        XmlElement element2 = configElement.element(null, USERNAME_TAG_NAME);
        if (element != null) {
            for (Object child : element2.children()) {
                this.username = (String) child;
            }
        }
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_INSTANCE);
        return nodeElement;
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#addConfigurationElement(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = super.addConfigurationElement(nodeElement);

        // save start new instance
        XmlElement element = configElement.addElement(GraphSchema.NS, NEW_TAG_NAME);
        element.addChild(String.valueOf(this.startNewInstance));

        if (this.startNewInstance) {
            // save ami id
            if (this.amiId != null) {
                XmlElement element2 = configElement.addElement(GraphSchema.NS, AMI_ID_TAG_NAME);
                element2.addChild(this.amiId);
            }

            // save instance type
            if (this.instanceType != null) {
                XmlElement element3 = configElement.addElement(GraphSchema.NS, INSTANCE_TYPE_TAG_NAME);
                element3.addChild(this.instanceType);
            }
        } else {
            // save instance id
            if (this.instanceId != null) {
                XmlElement element2 = configElement.addElement(GraphSchema.NS, INSTANCE_ID_TAG_NAME);
                element2.addChild(this.instanceId);
            }
        }

        // save username
        if (this.username != null) {
            XmlElement element2 = configElement.addElement(GraphSchema.NS, USERNAME_TAG_NAME);
            element2.addChild(this.username);
        }

        return configElement;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        super.edgeWasAdded(edge);

        if (edge instanceof ControlEdge) {
            Port toPort = edge.getToPort();
            Node toNode = toPort.getNode();
            /*
             * check if there is already more than instance node connecting to destination node
             */
            if (!(toNode instanceof InstanceNode)) {
                for (Node node : toNode.getControlInPort().getFromNodes()) {
                    if ((node instanceof InstanceNode) && this != node) {
                        throw new GraphException("Cannot connect more than one instance node to another node.");
                    }
                }
            }
        }
    }
}