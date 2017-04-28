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
package org.apache.airavata.xbaya.ui.graph;

import org.apache.airavata.workflow.model.wf.Workflow;

public class GraphCanvasEvent {

    /**
     * The type of an event.
     */
    public enum GraphCanvasEventType {
        /**
         * A graph was loaded.
         */
        GRAPH_LOADED,
        /**
         * The name or the description of the workflow has changed.
         */
        NAME_CHANGED,
        /**
         * A node is selected.
         */
        NODE_SELECTED,
        /**
         * An input port is selected.
         */
        INPUT_PORT_SELECTED,
        /**
         * An output port is selected.
         */
        OUTPUT_PORT_SELECTED,
        
        /**
         * Event when the workflow was changed
         */
        WORKFLOW_CHANGED
    }

    private GraphCanvasEventType type;

    private GraphCanvas graphCanvas;

    private Workflow workflow;

    /**
     * Constructs a GraphPanelEvent.
     * 
     * @param type
     * @param canvas
     * @param workflow
     */
    public GraphCanvasEvent(GraphCanvasEventType type, GraphCanvas canvas, Workflow workflow) {
        this.type = type;
        this.graphCanvas = canvas;
        this.workflow = workflow;
    }

    /**
     * @return The type of the event
     */
    public GraphCanvasEventType getType() {
        return this.type;
    }

    /**
     * @return The graph panel
     */
    public GraphCanvas getGraphCanvas() {
        return this.graphCanvas;
    }

    /**
     * @return The graph
     */
    public Workflow getWorkflow() {
        return this.workflow;
    }

}