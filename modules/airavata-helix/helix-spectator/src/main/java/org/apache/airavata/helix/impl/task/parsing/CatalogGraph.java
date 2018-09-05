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
package org.apache.airavata.helix.impl.task.parsing;

import org.apache.airavata.helix.impl.task.parsing.shortestpath.DijkstraShortestPath;
import org.apache.airavata.helix.impl.task.parsing.shortestpath.DirectedGraph;
import org.apache.airavata.helix.impl.task.parsing.shortestpath.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a directed graph which has {@link CatalogEntry#getInputFileExtension()}s and
 * {@link CatalogEntry#getOutputFileExtension()}s as vertices and {@link CatalogEntry}s as edges.
 * Then the directed graph will be used to generate the shortest path from
 * input file type to a given output file type
 *
 * @since 1.0.0-SNAPSHOT
 */
public class CatalogGraph {

    private final static Logger logger = LoggerFactory.getLogger(CatalogGraph.class);

    private DijkstraShortestPath dsp;
    private String inputFileType;

    public CatalogGraph(String inputFileType, String catalogPath) throws FileNotFoundException {
        this.inputFileType = inputFileType;
        DirectedGraph directedGraph = new DirectedGraph();

        for (CatalogEntry e : CatalogUtil.catalogLookup(catalogPath)) {
            // If an application is found "Source Vertex" will be
            // inserted as [fileType]/[application] (eg. ".out/gaussian") else [fileType].
            String sourceVertex = e.getApplicationType().isEmpty()
                    ? e.getInputFileExtension()
                    : e.getInputFileExtension() + '/' + e.getApplicationType();

            // Insert the edge and the two vertices
            boolean success = directedGraph.addEdge(sourceVertex, e.getOutputFileExtension(), e);
            logger.info(String.format((success ? "Successfully inserted " : "Couldn't insert ")
                            + "the source vertex: %s, target vertex: %s, and the edge: %s",
                    sourceVertex, e.getOutputFileExtension(), e.getDockerImageName()));
        }

        dsp = new DijkstraShortestPath(directedGraph);
        dsp.execute(inputFileType);
    }

    /**
     * Returns the {@link CatalogEntry}s corresponding to the shortest path edges.
     * Unless an exception is thrown, the returned list always contains at least an element
     *
     * @param outputFileType ultimate file type which is required to be converted
     *                       from {@link CatalogEntry#inputFileName}
     * @return list of {@link CatalogEntry}s
     */
    public List<CatalogEntry> getSPCatalogEntries(String outputFileType) throws Exception {
        List<CatalogEntry> entryList = new ArrayList<>();
        List<Edge> edges = dsp.getEdgeList(outputFileType);
        if (edges != null) {
            edges.forEach(x -> entryList.add(x.getCatalogEntry()));
            return entryList;

        } else {
            throw new Exception(String.format("A path could not be found in between " +
                    "input file type: %s and output file type: %s", inputFileType, outputFileType));
        }
    }
}
