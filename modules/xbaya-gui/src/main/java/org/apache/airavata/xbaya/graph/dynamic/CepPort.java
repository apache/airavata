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

import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentPort;
import org.apache.airavata.xbaya.component.dynamic.CepComponentPort;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.xmlpull.infoset.XmlElement;

public class CepPort extends DataPort {

    private CepComponentPort componentPort;

    public CepPort(XmlElement portElement) {
        super(portElement);
    }

    /**
     * Constructs a CepPort.
     * 
     */
    public CepPort() {
        super();
        this.id = UUID.randomUUID().toString();
    }

    /**
     * @see org.apache.airavata.xbaya.graph.DataPort#copyType(org.apache.airavata.xbaya.graph.DataPort)
     */
    @Override
    public void copyType(DataPort port) throws GraphException {
        // TODO Auto-generated method stub

    }

    /**
     * Returns the typeQName.
     * 
     * @return The typeQName
     */
    @Override
    public QName getType() {
        return WSConstants.XSD_ANY_TYPE;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setComponentPort(ComponentPort componentPort) {
        super.setComponentPort(componentPort);
        this.componentPort = (CepComponentPort) componentPort;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.PortImpl#getComponentPort()
     */
    @Override
    public CepComponentPort getComponentPort() {
        if (this.componentPort == null) {
            ComponentPort port = super.getComponentPort();
            if (port instanceof CepComponentPort) {
                this.componentPort = (CepComponentPort) port;
            } else {
                throw new XBayaRuntimeException("UNEXPECTED ERROR: Unable to resolve Port");
            }

        }
        return this.componentPort;
    }


    protected XmlElement toXML() {
        XmlElement portElement = super.toXML();
        portElement.setAttributeValue(GraphSchema.NS, GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_CEP);
        return portElement;
    }

    public void setNode(NodeImpl node) {
        super.setNode(node);
    }
}