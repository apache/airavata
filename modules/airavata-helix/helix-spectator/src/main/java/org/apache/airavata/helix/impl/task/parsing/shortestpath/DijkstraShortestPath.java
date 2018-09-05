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

import java.util.*;

/**
 * Dijkstra algorithm implementation
 *
 * @since 1.0.0-SNAPSHOT
 */
public class DijkstraShortestPath {
    private final DirectedGraph graph;
    private final List<Edge> edges;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;

    public DijkstraShortestPath(DirectedGraph graph) {
        this.graph = graph;
        this.edges = new ArrayList<>(graph.getEdgeSet());
    }

    /**
     * Compute the graph making source vertex id as <code>id</code>
     *
     * @param id source vertex id
     */
    public void execute(String id) {
        Vertex source = new Vertex(id);
        settledNodes = new HashSet<>();
        unSettledNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();
        distance.put(source, 0);
        unSettledNodes.add(source);

        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    /**
     * Computes the shortest path from source vertex
     * to <code>target</code> vertex and generate the list
     * of {@link Edge}s in that path
     *
     * @param target target vertex {@link Vertex#id}
     * @return the list of {@link Edge}s in the shortest
     * path from source vertex to target vertex
     */
    public List<Edge> getEdgeList(String target) {
        Vertex step = new Vertex(target);
        LinkedList<Vertex> path = new LinkedList<>();
        List<Edge> edges = new ArrayList<>();

        // Check whether a path exists or not
        if (predecessors.get(step) != null) {
            path.add(step);
            while (predecessors.get(step) != null) {
                step = predecessors.get(step);
                path.add(step);
            }
            // Arrange in the correct order
            Collections.reverse(path);

            for (int i = 0; i < path.size() - 1; i++) {
                edges.add(graph.getEdge(path.get(i), path.get(i + 1)));
            }
            return edges;
        }
        return null;
    }

    private void findMinimalDistances(Vertex vertex) {
        List<Vertex> adjacentVertices = getNeighbors(vertex);
        for (Vertex target : adjacentVertices) {
            if (getShortestDistance(target) > getShortestDistance(vertex) + getDistance(vertex, target)) {
                distance.put(target, getShortestDistance(vertex) + getDistance(vertex, target));
                predecessors.put(target, vertex);
                unSettledNodes.add(target);
            }
        }
    }

    private int getDistance(Vertex source, Vertex target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(source) && edge.getTarget().equals(target)) {
                return edge.getWeight();
            }
        }
        return Integer.MAX_VALUE;
    }

    private List<Vertex> getNeighbors(Vertex source) {
        List<Vertex> neighbors = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(source) && !isSettled(edge.getTarget())) {
                neighbors.add(edge.getTarget());
            }
        }
        return neighbors;
    }

    private Vertex getMinimum(Set<Vertex> vertices) {
        Vertex minimum = null;
        for (Vertex vertex : vertices) {
            if (minimum == null) {
                minimum = vertex;
            } else if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                minimum = vertex;
            }
        }
        return minimum;
    }

    private boolean isSettled(Vertex vertex) {
        return settledNodes.contains(vertex);
    }

    private int getShortestDistance(Vertex destination) {
        Integer destDistance = distance.get(destination);
        if (destDistance == null) {
            return Integer.MAX_VALUE;
        } else {
            return destDistance;
        }
    }
}
