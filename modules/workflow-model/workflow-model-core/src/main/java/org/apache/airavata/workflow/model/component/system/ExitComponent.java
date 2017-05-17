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

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentControlPort;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.component.system.SystemComponent;
import org.apache.airavata.workflow.model.component.system.SystemComponentDataPort;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.ExitNode;

public class ExitComponent extends SystemComponent {

    public static final String NAME = "Exit";

    private static final String DESCRIPTION = "Exit workflow";

    private static final String PORT_NAME = "Parameter";

    private static final String PORT_DESCRIPTION = "This port can be connected to any type.";

    /**
     * Constructs a BPELExit.
     * 
     */
    public ExitComponent() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
        SystemComponentDataPort port = new SystemComponentDataPort(PORT_NAME);
        port.setDescription(PORT_DESCRIPTION);
        this.controlInPort = new ComponentControlPort("exit");

    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#createNode(org.apache.airavata.workflow.model.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        ExitNode exitNode = new ExitNode(graph);
        exitNode.createID();
        createPorts(exitNode);
        return exitNode;
    }

    protected void createPorts(NodeImpl node) {
        for (ComponentDataPort input : getInputPorts()) {
            DataPort port = input.createPort();
            node.addInputPort(port);
        }

        for (ComponentDataPort output : getOutputPorts()) {
            DataPort port = output.createPort();
            node.addOutputPort(port);
        }

        if (this.controlInPort != null) {
            ControlPort port = this.controlInPort.createPort();
            node.setControlInPort(port);
        }

        for (ComponentControlPort componentPort : this.controlOutPorts) {
            ControlPort port = componentPort.createPort();
            node.addControlOutPort(port);
        }

        if (this.eprPort != null) {
            EPRPort port = this.eprPort.createPort();
            node.setEPRPort(port);
        }
    }

}