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

import com.google.gson.JsonObject;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.xmlpull.infoset.XmlElement;

public class SystemDataPort extends DataPort {

    private int arrayDimension;

    private DataType type;

    private WSComponentPort wsComponentPort;

    /**
     * Constructs a DynamicWSPort.
     */
    public SystemDataPort() {
        super();
        resetType();
    }

    /**
     * Constructs a DynamicWSPort.
     * 
     * @param portElement
     */
    public SystemDataPort(XmlElement portElement) {
        super(portElement);
        resetType();
    }

    public SystemDataPort(JsonObject portObject) {
        super(portObject);
        resetType();
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#getNode()
     */
    @Override
    public SystemNode getNode() {
        return (SystemNode) super.getNode();
    }

    /**
     * @return The type QName.
     */
    @Override
    public DataType getType() {
        return this.type;
    }

    /**
     * Returns the arrayDimension.
     * 
     * @return The arrayDimension
     */
    public int getArrayDimension() {
        return this.arrayDimension;
    }

    /**
     * Sets arrayDimension.
     * 
     * @param arrayDimension
     *            The arrayDimension to set.
     */
    public void setArrayDimension(int arrayDimension) {
        this.arrayDimension = arrayDimension;
    }

    /**
     * Returns the wsComponentPort.
     * 
     * @return The wsComponentPort
     */
    public WSComponentPort getWSComponentPort() {
        return this.wsComponentPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.DataPort#copyType(org.apache.airavata.workflow.model.graph.DataPort)
     */
    @Override
    public void copyType(DataPort port) throws GraphException {
        copyType(port, 0);
    }

    /**
     * @param port
     * @param arrayIncrement
     * @throws GraphException
     */
    public void copyType(DataPort port, int arrayIncrement) throws GraphException {
        DataType newType = port.getType();
        if (this.type != newType) {
            this.type = newType;
            if (port instanceof WSPort) {
                WSPort wsPort = (WSPort) port;
                this.wsComponentPort = wsPort.getComponentPort();
                this.arrayDimension = 0;
            } else if (port instanceof SystemDataPort) {
                SystemDataPort systemPort = (SystemDataPort) port;
                this.wsComponentPort = systemPort.getWSComponentPort();
                this.arrayDimension = systemPort.getArrayDimension() + arrayIncrement;
            }

            // propagate to other ports of this node.
            getNode().portTypeChanged(this);

            // propagate to the connected ports.
            Kind kind = getKind();
            for (DataEdge edge : getEdges()) {
                if (kind == Kind.DATA_IN) {
                    DataPort fromPort = edge.getFromPort();
                    fromPort.copyType(this);
                } else if (kind == Kind.DATA_OUT) {
                    DataPort toPort = edge.getToPort();
                    toPort.copyType(this);
                } else {
                    throw new WorkflowRuntimeException();
                }
            }
        }
    }

    /**
     * 
     */
    public void resetType() {
        this.arrayDimension = 0;
        this.type = DataType.STRING;
        this.wsComponentPort = null;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement portElement = super.toXML();

        portElement.setAttributeValue(GraphSchema.NS, GraphSchema.PORT_TYPE_ATTRIBUTE,
                GraphSchema.PORT_TYPE_SYSTEM_DATA);

        return portElement;
    }

    protected JsonObject toJSON() {
        JsonObject portObject = super.toJSON();
        portObject.addProperty(GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_SYSTEM_DATA);
        return portObject;
    }

    public int getIndex(){
            if(this.getNode() instanceof InputNode){
                return this.getGraph().getCurrentInputNodeCount() + 1;
            }else if (this.getNode() instanceof OutputNode){
                return this.getGraph().getCurrentOutputNodeCount() + 1;
            }
        return super.getIndex();
    }

}