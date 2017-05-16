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
package org.apache.airavata.workflow.model.graph.ws;

import javax.xml.namespace.QName;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.component.ws.WSComponentApplication;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.ForEachExecutableNode;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class WSNode extends NodeImpl implements ForEachExecutableNode{

    protected String wsdlID;

    protected QName portTypeQName;

    protected String operationName;

    private static final Logger log = LoggerFactory.getLogger(WSNode.class);
    /**
     * Constructs a WsdlNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public WSNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    public WSNode(JsonObject nodeObject) throws GraphException{
        super(nodeObject);
    }

    /**
     * Constructs a WSNode.
     * 
     * @param graph
     */
    public WSNode(Graph graph) {
        super(graph);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Node#getComponent()
     */
    @Override
    public WSComponent getComponent() {
        return (WSComponent) super.getComponent();
    }

    /**
     * Returns the WSDL ID.
     * 
     * @return the WSDL ID
     */
    public String getWSDLID() {
        return this.wsdlID;
    }

    /**
     * @param wsdlID
     */
    public void setWSDLID(String wsdlID) {
        this.wsdlID = wsdlID;
    }

    /**
     * @return The name of the operation.
     */
    public String getOperationName() {
        if (this.operationName == null) {
            if (getComponent() == null) {
                // XXX This happens while parsing xwf created by the version
                // 2.2.6_1 or below.
                return null;
            }
            this.operationName = getComponent().getOperationName();
        }
        return this.operationName;
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        GraphUtil.validateConnection(edge);
    }

    /**
     * @return the node xml
     */
    @Override
    public XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_WS);

//        XmlElement wsdlElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_WSDL_QNAME_TAG);
//        // wsdlElement.setText(getWSDLQName().toString());
//        wsdlElement.setText(getWSDLID());
//
//        XmlElement portTypeElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_WSDL_PORT_TYPE_TAG);
//        portTypeElement.setText(getPortTypeQName().toString());
//
//        XmlElement operationElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_WSDL_OPERATION_TAG);
//        operationElement.setText(getOperationName());

        XmlElement xml = getComponent().toXML();
        xml.setParent(null);
		nodeElement.addElement(xml);
        
        return nodeElement;
    }

    @Override
    protected JsonObject toJSON() {
        JsonObject nodeObject = (JsonObject) super.toJSON();
        nodeObject.addProperty(GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_WS);
        nodeObject.add("Application", getComponent().toJSON());
        return nodeObject;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#parse(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void parse(XmlElement nodeElement) throws GraphException {
        super.parse(nodeElement);
        XmlElement element = nodeElement.element(null, "Application");
        WSComponentApplication application = WSComponentApplication.parse(element);
        try {
			setComponent(new WSComponent(application));
		} catch (ComponentException e) {
            log.error(e.getMessage(), e);
		}
//        XmlElement wsdlElement = nodeElement.element(null, GraphSchema.NODE_WSDL_QNAME_TAG);
//        if (wsdlElement != null) {
//            this.wsdlID = wsdlElement.requiredText();
//            // String wsdlQNameString = wsdlElement.requiredText();
//            // this.wsdlQName = QName.valueOf(wsdlQNameString);
//        }
//
//        XmlElement portTypeElement = nodeElement.element(null, GraphSchema.NODE_WSDL_PORT_TYPE_TAG);
//        if (portTypeElement != null) {
//            String portTypeString = portTypeElement.requiredText();
//            this.portTypeQName = QName.valueOf(portTypeString);
//        }
//
//        XmlElement operationElement = nodeElement.element(null, GraphSchema.NODE_WSDL_OPERATION_TAG);
//        if (operationElement != null) {
//            this.operationName = operationElement.requiredText();
//        }
    }

    protected void parse(JsonObject nodeObject) {
        super.parse(nodeObject);
        JsonObject applicationObject = nodeObject.getAsJsonObject("Application");
        WSComponentApplication application = WSComponentApplication.parse(applicationObject);
        try {
            setComponent(new WSComponent(application));
        } catch (ComponentException e) {
            log.error(e.getMessage(), e);
        }
    }
   
}