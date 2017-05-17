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
package org.apache.airavata.workflow.model.component.dynamic;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.dynamic.DynamicNode;

public class DynamicComponent extends Component {

    public static final String NAME = "Dynamic Node";

    /**
     * The list of output component ports.
     */
    protected List<DynamicComponentPort> inputs;

    /**
     * The list of input component ports.
     */
    protected List<DynamicComponentPort> outputs;

    protected URL implJarLocation;

    protected String operationName;

    protected String className;

    public DynamicComponent() {
        super();
        setName(NAME);
        this.inputs = new ArrayList<DynamicComponentPort>();
        this.inputs.add(new DynamicComponentPort(this));
        this.outputs = new ArrayList<DynamicComponentPort>();
        this.outputs.add(new DynamicComponentPort(this));
    }

    public Node createNode(Graph graph) {
        DynamicNode node = new DynamicNode(graph);

        // Copy some infomation from the component

        node.setName(getName());
        node.setComponent(new DynamicComponent());

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        // Creat ports
        createPorts(node);

        return node;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#getInputPorts()
     */
    @Override
    public List<? extends ComponentDataPort> getInputPorts() {
        return inputs;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#getOutputPorts()
     */
    @Override
    public List<? extends ComponentDataPort> getOutputPorts() {
        return outputs;
    }

    public void addInputPort(DynamicComponentPort port) {
        this.inputs.add(port);
    }

    public void removeInputPort(DynamicComponentPort port) {
        this.inputs.remove(port);
    }

    public void addOutputPort(DynamicComponentPort port) {
        this.outputs.add(port);
    }

    public void removeOutputPort(DynamicComponentPort port) {
        this.outputs.remove(port);
    }

    /**
     * @see org.apache.airavata.workflow.model.component.Component#toHTML()
     */
    @Override
    public String toHTML() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the implJarLocation.
     * 
     * @return The implJarLocation
     */
    public URL getImplJarLocation() {
        return this.implJarLocation;
    }

    /**
     * Sets implJarLocation.
     * 
     * @param implJarLocation
     *            The implJarLocation to set.
     */
    public void setImplJarLocation(URL implJarLocation) {
        this.implJarLocation = implJarLocation;
    }

    /**
     * Returns the operationName.
     * 
     * @return The operationName
     */
    public String getOperationName() {
        return this.operationName;
    }

    /**
     * Sets operationName.
     * 
     * @param operationName
     *            The operationName to set.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Returns the className.
     * 
     * @return The className
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Sets className.
     * 
     * @param className
     *            The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

}