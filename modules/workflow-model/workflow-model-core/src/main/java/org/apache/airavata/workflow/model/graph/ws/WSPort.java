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

import com.google.gson.JsonObject;
import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.ComponentPort;
import org.apache.airavata.workflow.model.component.system.SystemComponentDataPort;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.xmlpull.infoset.XmlElement;

public class WSPort extends DataPort {

    private WSComponentPort componentPort;

    /**
     * Constructs a WSPort.
     */
    public WSPort() {
        super();
    }

    /**
     * Constructs a WsPort.
     * 
     * @param portElement
     */
    public WSPort(XmlElement portElement) {
        super(portElement);
    }

    public WSPort(JsonObject portObject) {
        super(portObject);
    }
    /**
     * Returns the typeQName.
     * 
     * @return The typeQName
     */
    @Override
    public DataType getType() {
        return getComponentPort().getType();
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.DataPort#copyType(org.apache.airavata.workflow.model.graph.DataPort)
     */
    @Override
    public void copyType(DataPort port) throws GraphException {
        DataType newType = port.getType();
        DataType type = getType();

        NodeImpl node = port.getNode();
        if (node instanceof ForEachNode || node instanceof EndForEachNode) {
            // XXX ignore the check for foreach because we cannot parse arrays
            // from WSDL.
            return;
        }

        if (!(newType == null || newType.equals(WSConstants.XSD_ANY_TYPE) || type == null
                || type.equals(WSConstants.XSD_ANY_TYPE) || newType.equals(type))) {
            String message = "The type (" + newType + ")  must be same as the type  " + " (" + type + ") of " + getID()
                    + ".";
            throw new GraphException(message);
        }
    }

    /**
     * @param componentPort
     */
    @Override
    public void setComponentPort(ComponentPort componentPort) {
        super.setComponentPort(componentPort);
        this.componentPort = (WSComponentPort) componentPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#getComponentPort()
     */
    @Override
    public WSComponentPort getComponentPort() {
        if (this.componentPort == null) {
            ComponentPort port = super.getComponentPort();
            if (port instanceof WSComponentPort) {
                this.componentPort = (WSComponentPort) port;
            }
            if (port instanceof SystemComponentDataPort) {
                // XXX to handle the xwf created by version 2.6.2_XX or earlier.
                SystemComponentDataPort systemPort = (SystemComponentDataPort) port;
                this.componentPort = new WSComponentPort(systemPort.getName(), systemPort.getType(), null);
            }
        }
        return this.componentPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement portElement = super.toXML();
        portElement.setAttributeValue(GraphSchema.NS, GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_WS_DATA);
        return portElement;
    }

    protected JsonObject toJSON() {
        JsonObject portObject = super.toJSON();
        portObject.addProperty(GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_WS_DATA);
        portObject.addProperty(GraphSchema.PORT_DATA_TYPE_TAG, this.getType().toString());
        return portObject;
    }
}