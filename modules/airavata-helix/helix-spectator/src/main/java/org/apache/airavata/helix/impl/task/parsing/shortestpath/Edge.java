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

/**
 * Implementation of an Edge which holds two
 * vertices in the {@link DirectedGraph}
 *
 * @since 1.0.0-SNAPSHOT
 */
public class Edge {
    private final Vertex sourceVertex;
    private final Vertex targetVertex;
    private final CatalogEntry entry;
    private final boolean selfEdge;
    private final int weight;

    public Edge(Vertex sourceVertex, Vertex targetVertex, CatalogEntry entry) {
        this.sourceVertex = sourceVertex;
        this.targetVertex = targetVertex;
        this.entry = entry;
        this.selfEdge = sourceVertex.equals(targetVertex);
        this.weight = 1;
    }

    public Vertex getSource() {
        return sourceVertex;
    }

    public Vertex getTarget() {
        return targetVertex;
    }

    public CatalogEntry getCatalogEntry() {
        return entry;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isSelfEdge() {
        return selfEdge;
    }
}
