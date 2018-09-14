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

import org.apache.airavata.helix.impl.task.parsing.ParserRequest;

import java.util.*;

/**
 * Dijkstra algorithm implementation considering shortest path,
 * {@link ParserRequest#application}, and  {@link ParserRequest#operationList}
 *
 * @since 1.0.0-SNAPSHOT
 */
public class DijkstraShortestPath {
    private final DirectedGraph graph;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, LinkedList<Edge>> predecessors;
    private Map<Vertex, Integer> distance;
    private ParserRequest request;
    private String source;

    private Boolean settleApplication = null;
    private Map<String, Boolean> settleOperations = new HashMap<>();

    public DijkstraShortestPath(DirectedGraph graph, ParserRequest request) throws Exception {
        this.graph = graph;
        this.request = request;
        this.source = request.inputFileType();

        // If an application is found in the Parser request
        if (request.getApplication() != null && !request.getApplication().isEmpty()) {
            settleApplication = false;
        }

        // If there are operations in the parser request
        if (request.getOperationList() != null && request.getOperationList().size() > 0) {
            for (String op : request.getOperationList()) {
                settleOperations.put(op, null);
            }
        }
        execute(source);
    }

    /**
     * Compute the graph making source vertex id as <code>id</code>
     *
     * @param id source vertex id
     */
    private void execute(String id) throws Exception {
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
    public List<Edge> getEdgeList(String target) throws Exception {
        Vertex step = new Vertex(target);
        LinkedList<Vertex> path = new LinkedList<>();
        List<Edge> edges = new ArrayList<>();
        boolean sourceVertexSelfLoop = false;

        // Check whether a path exists or not
        if (predecessors.get(step) != null) {
            path.add(step);
            while (predecessors.get(step) != null) {
                for (Edge e : predecessors.get(step)) {
                    if (e.isSelfEdge()) {
                        if (source.equals(e.getCatalogEntry().getInputFileExtension())) {
                            step = e.getSource();
                            path.add(step);
                            sourceVertexSelfLoop = true;
                        }
                        continue;
                    }
                    step = e.getSource();
                    path.add(step);
                }

                if (sourceVertexSelfLoop) {
                    break;
                }
            }
            // Arrange in the correct order
            Collections.reverse(path);

            for (int i = 1; i < path.size(); i++) {
                edges.addAll(predecessors.get(path.get(i)));
            }

            if (settleOperations.size() > 0) {
                for (String op : settleOperations.keySet()) {

                    Optional<Edge> edge = edges.stream().filter(e -> {
                        String operation = e.getCatalogEntry().getOperation();
                        return !operation.isEmpty() && operation.equals(op);
                    }).findFirst();

                    if (!edge.isPresent()) {
                        throw new Exception("Path could not be found due to incompleteness of operations. " +
                                "Missing operation: " + op);
                    }
                }
            }
            return edges;
        }
        return null;
    }

    private void findMinimalDistances(Vertex vertex) throws Exception {
        for (Edge edge : getRelatedOutgoingEdges(vertex)) {
            Vertex target = edge.getTarget();
            String operation = edge.getCatalogEntry().getOperation();

            if (getShortestDistance(target) > getShortestDistance(vertex) + getDistance(vertex, target)) {
                distance.put(target, getShortestDistance(vertex) + getDistance(vertex, target));
                updatePredecessors(edge, target);
                unSettledNodes.add(target);
            }
            if (edge.isSelfEdge() && !operation.isEmpty() && settleOperations.get(operation) != null) {
                updatePredecessors(edge, target);
            }
        }
    }

    private int getDistance(Vertex source, Vertex target) {
        for (Edge edge : graph.getOutgoingEdges(source)) {
            if (edge.getTarget().equals(target)) {
                return edge.getWeight();
            }
        }
        return Integer.MAX_VALUE;
    }

    private List<Edge> getRelatedOutgoingEdges(Vertex source) throws Exception {
        Set<Edge> neighbors = new HashSet<>();
        for (Edge edge : graph.getOutgoingEdges(source)) {

            String application = edge.getCatalogEntry().getApplicationType();
            // If there is an application specific content to be parsed when visiting the first set of neighbours
            if (settleApplication != null && !settleApplication) {
                if (!application.equals(request.getApplication())) {
                    continue;
                }

            } else if (settleApplication == null && !application.isEmpty()) {
                //When parser request does not expect application, avoid the edge
                continue;
            }

            String operation = edge.getCatalogEntry().getOperation();
            if (!operation.isEmpty()) {
                if (settleOperations.size() == 0) {
                    // If the edge has an operation but not in the parser request
                    continue;

                } else {
                    if (settleOperations.containsKey(operation)) {
                        settleOperations.put(edge.getCatalogEntry().getOperation(), false);

                    } else {
                        // If the operation is not requested by the parser request
                        continue;
                    }
                }
            }
            if (edge.getSource().equals(source) && (!isSettled(edge.getTarget()) || edge.isSelfEdge())) {
                neighbors.add(edge);
            }
        }

        if (settleApplication != null && !settleApplication) {
            if (!neighbors.isEmpty()) {
                settleApplication = true;
            } else {
                throw new Exception("No path is found for the application specific content to be parsed!");
            }
        }
        return new ArrayList<>(neighbors);
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

    public String getSource() {
        return source;
    }

    private void updatePredecessors(Edge edge, Vertex target) {
        if (predecessors.get(target) != null) {
            predecessors.get(target).add(edge);

        } else {
            LinkedList<Edge> list = new LinkedList<>();
            list.add(edge);
            predecessors.put(target, list);
        }
    }
}
