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
package org.apache.airavata.workflow.model.component.system;

import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.OutputNode;

public class OutputComponent extends SystemComponent {

    /**
     * The name of the parameter component
     */
    public static final String NAME = "Output";

    private static final String DESCRIPTION = "A system component that represents an output parameter of a workflow.";

    private static final String PORT_NAME = "Parameter";

    private static final String PORT_DESCRIPTION = "This port can be connected to any type.";

    /**
     * Creates an OutputComponent.
     */
    public OutputComponent() {
        setName(NAME);
        setDescription(DESCRIPTION);

        SystemComponentDataPort port = new SystemComponentDataPort(PORT_NAME);
        port.setDescription(PORT_DESCRIPTION);
        this.inputs.add(port);
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#createNode(org.apache.airavata.workflow.model.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        OutputNode node = new OutputNode(graph);

        node.setName(NAME);
        node.setComponent(this);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        // Creates an input port
        createPorts(node);

        return node;
    }
}