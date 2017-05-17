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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentDataPort;

public abstract class SystemComponent extends Component {

    /**
     * The list of output component ports.
     */
    protected List<ComponentDataPort> inputs;

    /**
     * The list of input component ports.
     */
    protected List<ComponentDataPort> outputs;

    /**
     * Constructs a SystemComponent.
     */
    public SystemComponent() {
        this.inputs = new ArrayList<ComponentDataPort>();
        this.outputs = new ArrayList<ComponentDataPort>();
    }

    /**
     * @return The list of input WSComponentPorts
     */
    @Override
    public List<ComponentDataPort> getInputPorts() {
        return this.inputs;
    }

    /**
     * @return The list of output WSComponentPorts
     */
    @Override
    public List<ComponentDataPort> getOutputPorts() {
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