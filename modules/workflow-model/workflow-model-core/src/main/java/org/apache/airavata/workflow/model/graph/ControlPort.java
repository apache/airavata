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
package org.apache.airavata.workflow.model.graph;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.xmlpull.infoset.XmlElement;

public class ControlPort extends PortImpl {

    private boolean allow;

    /**
     * Constructs a ControlPort.
     * 
     */
    public ControlPort() {
        super();
    }

    /**
     * Constructs a ControlPort.
     * 
     * @param portElement
     */
    public ControlPort(XmlElement portElement) {
        super(portElement);
    }

    public ControlPort(JsonObject portObject) {
        super(portObject);
    }
    /**
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement portElement = super.toXML();
        portElement.setAttributeValue(GraphSchema.NS, GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_CONTROL);
        return portElement;
    }

    protected JsonObject toJSON() {
        JsonObject portObject = super.toJSON();
        portObject.addProperty(GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_CONTROL);
        return portObject;
    }    /**
     * Set if this port condition is met, flow will execute throw this port
     * 
     * @param condition
     */
    public void setConditionMet(boolean condition) {
        this.allow = condition;
    }

    /**
     * 
     * @return true if execution flow can be run on this port
     */
    public boolean isConditionMet() {
        return this.allow;
    }
}