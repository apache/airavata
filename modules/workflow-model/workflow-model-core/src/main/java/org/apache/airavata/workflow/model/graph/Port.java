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

import java.util.Collection;

import org.apache.airavata.workflow.model.component.ComponentPort;

public interface Port extends GraphPiece {

    /**
     * Kinds of ports
     */
    public enum Kind {
        /**
         * Output port
         */
        DATA_OUT,

        /**
         * Input port
         */
        DATA_IN,

        /**
         * CONTROL_OUT
         */
        CONTROL_OUT,

        /**
         * CONTROL_IN
         */
        CONTROL_IN,

        /**
         * EPR
         */
        EPR,
    }

    /**
     * Returns an ID that can be used to distinguish a port, and also can be used as a variable name in scripts
     * 
     * @return the ID
     */
    public String getID();

    /**
     * @return the name
     */
    public String getName();

    /**
     * @param name
     */
    public void setName(String name);

    /**
     * @return the node that this port belongs to
     */
    public Node getNode();

    /**
     * Returns the Collection of Edges that this Port is connected.
     * 
     * @return The Collection of Edges; an empty Collection if there is no edges.
     */
    public Collection<? extends Edge> getEdges();

    /**
     * Returns the Edge with a specified index.
     * 
     * @param index
     *            The specified index
     * @return The Edge with the specified index.
     */
    public Edge getEdge(int index);

    /**
     * Returns the kind of the port.
     * 
     * @return The kind of the port.
     */
    public Kind getKind();

    /**
     * Returns the Collection of Ports that this Port is connected from.
     * 
     * @return The Collection of Ports; an empty Collection if there are no Ports.
     */
    public Collection<Port> getFromPorts();

    /**
     * Returns the Port that this port is connected from.
     * 
     * @return The Port; null if there is no Port.
     */
    public Port getFromPort();

    /**
     * Returns the Collection of Nodes that this Port is connected from.
     * 
     * @return The Collection of Nodes if any; an empty Collection if there are no Nodes.
     */
    public Collection<Node> getFromNodes();

    /**
     * Returns the Nord that this port is connected from.
     * 
     * @return The Nort; null if there is no Nort.
     */
    public Node getFromNode();

    /**
     * Returns the Collection of Ports this Port is connected to.
     * 
     * @return The Collection of Ports if any; an epty Collection if there are no Ports.
     */
    public Collection<Port> getToPorts();

    /**
     * Returns the Collection of Nodes that this Port is connected to.
     * 
     * @return The Collection of Nodes if any; an empty Collection if there are no Nodes.
     */
    public Collection<Node> getToNodes();

    /**
     * Sets a ComponentPort.
     * 
     * @param componentPort
     *            The ComponentPort to set
     */
    public void setComponentPort(ComponentPort componentPort);

    /**
     * Returns the ComponentPort.
     * 
     * @return The ComponentPort
     */
    public ComponentPort getComponentPort();

}