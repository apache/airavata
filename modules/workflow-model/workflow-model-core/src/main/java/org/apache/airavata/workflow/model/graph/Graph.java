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
import java.util.List;

import com.google.gson.JsonElement;
import org.xmlpull.infoset.XmlElement;

public interface Graph extends GraphPiece {
    
    /**
     * Returns if the graph should be editable
     * @return
     */
    public boolean isEditable();

    /**
     * set if graph can be edited
     * @param editable
     */
    public void setEditable(boolean editable);
    
    /**
     * Returns the ID of this graph.
     * 
     * @return The ID of this graph
     */
    public String getID();

    /**
     * Sets the name of this graph.
     * 
     * @param name
     *            The name to set
     */
    public void setName(String name);

    /**
     * Returns the name of this graph.
     * 
     * @return The name
     */
    public String getName();

    /**
     * Returns the description of this graph.
     * 
     * @return The description.
     */
    public String getDescription();

    /**
     * Sets the description of this graph.
     * 
     * @param description
     *            The description to set.
     */
    public void setDescription(String description);

    /**
     * Returns the list of the nodes of this graph.
     * 
     * It's a list because the order of input nodes matters.
     * 
     * @return the list of the nodes
     */
    public List<? extends Node> getNodes();

    /**
     * Returns the list of the ports of this graph.
     * 
     * @return the list of the ports
     */
    public Collection<? extends Port> getPorts();

    /**
     * Returns the list of edges of this graph.
     * 
     * @return the list of edges
     */
    public Collection<? extends Edge> getEdges();

    /**
     * Removes a specified node from this graph.
     * 
     * @param node
     *            the node to delete
     * @throws GraphException
     *             If the specified node does not exist in the graph
     */
    public void removeNode(Node node) throws GraphException;

    /**
     * Returns a node with a specified ID.
     * 
     * @param nodeID
     *            The specified ID.
     * @return The node with the specified ID if exists; null otherwise
     */
    public Node getNode(String nodeID);

    /**
     * Returns a port with a specified ID.
     * 
     * @param portID
     *            The specified ID.
     * @return The port with the specified ID if exists; null otherwise
     */
    public Port getPort(String portID);

    /**
     * Adds an edge between two ports specified. If the edge already exists between two ports, it returns null without
     * creating a new one.
     * 
     * @param fromPort
     *            The port the edge is connected from
     * @param toPort
     *            The port the edge is connected to
     * @return The edge added
     * @throws GraphException
     */
    public Edge addEdge(Port fromPort, Port toPort) throws GraphException;

    /**
     * Removes a specified edge.
     * 
     * @param edge
     *            The edge to remove.
     * @throws GraphException
     */
    public void removeEdge(Edge edge) throws GraphException;

    /**
     * Removes an Edge between two specified ports.
     * 
     * @param fromPort
     * @param toPort
     * @throws GraphException
     */
    public void removeEdge(Port fromPort, Port toPort) throws GraphException;

    /**
     * Checks if an Edge exist between two specified ports.
     * 
     * @param fromPort
     * @param toPort
     * @return true if an Edge exists; false otherwise;
     */
    public boolean containsEdge(Port fromPort, Port toPort);

    /**
     * Imports a Graph to this Graph.
     * 
     * @param graph
     *            the Graph to import
     * @throws GraphException
     */
    public void importGraph(Graph graph) throws GraphException;

    /**
     * @return The graph XML
     */
    public XmlElement toXML();


    public com.google.gson.JsonObject toJSON();

    /**
     * @param multipleSelectedNodes
     * @throws GraphException
     */
}