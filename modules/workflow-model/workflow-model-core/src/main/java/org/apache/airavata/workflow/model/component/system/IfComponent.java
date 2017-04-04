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

import org.apache.airavata.workflow.model.component.ComponentControlPort;
import org.apache.airavata.workflow.model.component.ComponentPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.IfNode;

public class IfComponent extends SystemComponent {

    /**
     * The name of the const component
     */
    public static final String NAME = "If";

    /**
     * The name of control output port if condition is true
     */
    public static final String TRUE_PORT_NAME = "If_True";

    /**
     * The name of control output port if condition is false
     */
    public static final String FALSE_PORT_NAME = "If_False";

    private static final String DESCRIPTION = "A system component that represents a condition to branch.";

    private static final String INPUT_PORT_NAME = "Input";

    private static final String INPUT_PORT_DESCRIPTION = "The input value is used to evaluate the if condition.";

    private static final String CONTROL_OUT_TRUE_DESCRIPTION = "If the condition is true, services connected to this port will be executed.";

    private static final String CONTROL_OUT_FALSE_DESCRIPTION = "If the condition is false, services connected to this port will be executed.";

    private SystemComponentDataPort inputPort;

    /**
     * Creates an InputComponent.
     */
    public IfComponent() {
        setName(NAME);
        setDescription(DESCRIPTION);

        this.inputPort = new SystemComponentDataPort(INPUT_PORT_NAME);
        this.inputPort.setDescription(INPUT_PORT_DESCRIPTION);
        this.inputs.add(this.inputPort);

        this.controlInPort = new ComponentControlPort();

        ComponentControlPort outputPortTrue = new ComponentControlPort(TRUE_PORT_NAME);
        outputPortTrue.setDescription(CONTROL_OUT_TRUE_DESCRIPTION);
        this.controlOutPorts.add(outputPortTrue);

        ComponentControlPort outputPortFalse = new ComponentControlPort(FALSE_PORT_NAME);
        outputPortFalse.setDescription(CONTROL_OUT_FALSE_DESCRIPTION);
        this.controlOutPorts.add(outputPortFalse);
    }

    /**
     * Returns the input component port.
     * 
     * This method is used by IfNode to create additional output port.
     * 
     * @return The input component port.
     */
    public SystemComponentDataPort getInputPort() {
        return this.inputPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#getInputPort(int)
     */
    @Override
    public ComponentPort getInputPort(int index) {
        // This method is called during the parsing to bind port to a component
        // port. Since the number of output ports in a if node dynamically
        // changes, we overwrite it.
        return getInputPort();
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#createNode(org.apache.airavata.workflow.model.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        IfNode node = new IfNode(graph);

        node.setName(NAME);
        node.setComponent(this);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        createPorts(node);

        return node;
    }
}