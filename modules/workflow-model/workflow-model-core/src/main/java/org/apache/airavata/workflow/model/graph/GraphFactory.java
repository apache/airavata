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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.xmlpull.infoset.XmlElement;

public interface GraphFactory {

    /**
     * Creates a Node.
     * 
     * @param nodeElement
     * @return The node created
     * @throws GraphException
     */
    public NodeImpl createNode(XmlElement nodeElement) throws GraphException;

    public NodeImpl createNode(JsonObject nodeObject) throws GraphException;

    /**
     * Creates a Port.
     * 
     * @param portElement
     * @return the port created
     */
    public PortImpl createPort(XmlElement portElement);

    public PortImpl createPort(JsonObject portObject);
    /**
     * Creates a Edge.
     * 
     * @param fromPort
     * @param toPort
     * @return The edge created
     */
    public EdgeImpl createEdge(Port fromPort, Port toPort);

    /**
     * Creates an Edge.
     * 
     * @param edgeXml
     * @return the edge created
     */
    public EdgeImpl createEdge(XmlElement edgeXml);

    public EdgeImpl createEdge(JsonObject edgeObject);

}