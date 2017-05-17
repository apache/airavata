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

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.model.application.io.DataType;
import org.xmlpull.infoset.XmlElement;

public class EPRPort extends DataPort {

    /**
     * Constructs a ControlPort.
     * 
     */
    public EPRPort() {
        super();
    }

    /**
     * Constructs a ControlPort.
     * 
     * @param portElement
     */
    public EPRPort(XmlElement portElement) {
        super(portElement);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.PortImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement portElement = super.toXML();
        portElement.setAttributeValue(GraphSchema.NS, GraphSchema.PORT_TYPE_ATTRIBUTE, GraphSchema.PORT_TYPE_EPR);
        return portElement;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.DataPort#getType()
     */
    @Override
    public DataType getType() {
        return DataType.STRING;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.DataPort#copyType(org.apache.airavata.workflow.model.graph.DataPort)
     */
    @Override
    public void copyType(DataPort port) {
        // TODO Auto-generated method stub
    }
}