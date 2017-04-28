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

import javax.xml.namespace.QName;

import com.google.gson.JsonObject;
import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.xmlpull.infoset.XmlElement;

public class InstanceDataPort extends DataPort {

    /**
     * String
     */
    public static final QName STRING_TYPE = new QName(WSConstants.XSD_NS_URI, "string", WSConstants.XSD_NS_PREFIX);

    /**
     * 
     * Constructs a InstanceDataPort.
     * 
     */
    public InstanceDataPort() {
        super();
    }

    /**
     * 
     * Constructs a InstanceDataPort.
     * 
     * @param portElement
     */
    public InstanceDataPort(XmlElement portElement) {
        super(portElement);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.DataPort#getType()
     */
    @Override
    public DataType getType() {
        return DataType.STRING;
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement portElement = super.toXML();
        portElement.setAttributeValue(GraphSchema.NS, GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_INSTANCE);
        return portElement;
    }

    protected JsonObject toJSON() {
        JsonObject portObject = super.toJSON();
        portObject.addProperty(GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_INSTANCE);
        return portObject;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.DataPort#copyType(org.apache.airavata.workflow.model.graph.DataPort)
     */
    @Override
    public void copyType(DataPort port) throws GraphException {
        // left blank

    }
}