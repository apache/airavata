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

package org.apache.airavata.xbaya.graph;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.graph.impl.PortImpl;
import org.xmlpull.infoset.XmlElement;

public abstract class DataPort extends PortImpl {

    /**
     * Constructs a DataPort.
     * 
     */
    public DataPort() {
        super();
    }

    /**
     * Constructs a DataPort.
     * 
     * @param portElement
     */
    public DataPort(XmlElement portElement) {
        super(portElement);
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.PortImpl#getEdges()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<DataEdge> getEdges() {
        // TODO Auto-generated method stub
        return (List<DataEdge>) super.getEdges();
    }

    /**
     * @return The type QName.
     */
    public abstract QName getType();

    /**
     * @param port
     * @throws GraphException
     */
    public abstract void copyType(DataPort port) throws GraphException;
}