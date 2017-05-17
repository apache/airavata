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
package org.apache.airavata.workflow.model.component.amazon;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentControlPort;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;

public class InstanceComponent extends Component {

    private List<InstanceComponentDataPort> inputs;

    private List<InstanceComponentDataPort> outputs;

    public static final String NAME = "Instance";

    public static final String DESCRIPTION = "TODO";

    private static final String OUTPUT_PORT_NAME = "Output";

    private static final String OUTPUT_PORT_DESCRIPTION = "";

    private static final String CONTROL_OUT_NAME = "";

    private static final String CONTROL_OUT_DESCRIPTION = "";

    /**
     * 
     * Constructs a InstanceComponent.
     * 
     */
    public InstanceComponent() {
        setName(NAME);
        setDescription(DESCRIPTION);

        this.inputs = new ArrayList<InstanceComponentDataPort>(0);

        this.outputs = new ArrayList<InstanceComponentDataPort>(1);
        this.outputs.add(new InstanceComponentDataPort(OUTPUT_PORT_NAME));

        ComponentControlPort outputPort = new ComponentControlPort(CONTROL_OUT_NAME);
        outputPort.setDescription(CONTROL_OUT_DESCRIPTION);
        this.controlOutPorts.add(outputPort);
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#createNode(org.apache.airavata.workflow.model.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        InstanceNode node = new InstanceNode(graph);

        node.setName(NAME);
        node.setComponent(this);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        createPorts(node);

        return node;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#getInputPorts()
     */
    @Override
    public List<? extends ComponentDataPort> getInputPorts() {
        return this.inputs;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#getOutputPorts()
     */
    @Override
    public List<? extends ComponentDataPort> getOutputPorts() {
        return this.outputs;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#toHTML()
     */
    @Override
    public String toHTML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html> <h1>" + this.name + " Component</h1>");
        buf.append("<h2>Description:</h2> " + this.description);

        for (int i = 0; i < this.inputs.size(); i++) {
            ComponentDataPort port = this.inputs.get(i);
            buf.append("<h3>Input" + (i + 1) + "</h3>");
            buf.append("<strong>Name: </strong>");
            buf.append("" + port.getName() + "<br>");
            buf.append("<strong>Description: </strong>");
            buf.append("" + port.getDescription());
        }

        for (int i = 0; i < this.outputs.size(); i++) {
            ComponentDataPort port = this.outputs.get(i);
            buf.append("<h3>Output" + (i + 1) + "</h3>");
            buf.append("<strong>Name: </strong>");
            buf.append("" + port.getName() + "<br>");
            buf.append("<strong>Description: </strong>");
            buf.append("" + port.getDescription());
        }

        buf.append("</html>");
        return buf.toString();
    }
}