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
 */
package org.apache.airavata.helix.impl.task.parsing.shortestpath;

import org.apache.airavata.helix.impl.task.parsing.CatalogEntry;

import java.util.*;

/**
 * Implementation of a directed graph
 *
 * @since 1.0.0-SNAPSHOT
 */
public class DirectedGraph {
    private Set<Vertex> vertices;
    // Outgoing edges from vertex
    private Map<Vertex, Set<Edge>> vertexMap;
    private Set<Edge> edges;

    public DirectedGraph() {
        vertices = new HashSet<>();
        vertexMap = new HashMap<>();
        edges = new HashSet<>();
    }

    /**
     * Connects the <code>sourceVertex</code> and <code>targetVertex</code>
     * using an {@link Edge}
     *
     * @param sourceVertex starting vertex id
     * @param targetVertex ending vertex id
     * @param entry        which will be used to create the edge
     * @return true if <code>sourceVertex</code>, <code>targetVertex</code> and edge
     * were successfully inserted or false otherwise
     */
    public void addEdge(String sourceVertex, String targetVertex, CatalogEntry entry) {
        Vertex sv = new Vertex(sourceVertex);
        Vertex tv = new Vertex(targetVertex);
        addVertex(sv);
        addVertex(tv);
        Edge e = new Edge(sv, tv, entry);
        edges.add(e);
        vertexMap.get(sv).add(e);
    }

    private void addVertex(Vertex v) {
        if (vertexMap.get(v) == null) {
            vertices.add(v);
            vertexMap.put(v, new HashSet<>());
        }
    }

    public List<Edge> getOutgoingEdges(Vertex sourceVertex) {
        return new ArrayList<>(vertexMap.get(sourceVertex));
    }
}
