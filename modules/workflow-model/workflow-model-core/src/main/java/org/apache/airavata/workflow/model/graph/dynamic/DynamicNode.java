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
package org.apache.airavata.workflow.model.graph.dynamic;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.airavata.workflow.model.component.dynamic.DynamicComponent;
import org.apache.airavata.workflow.model.component.dynamic.DynamicComponentPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;

public class DynamicNode extends NodeImpl implements PortAddable {

    /**
     * Constructs a WSNode.
     * 
     * @param graph
     */
    public DynamicNode(Graph graph) {
        super(graph);
        Collection<PortImpl> allPorts = this.getAllPorts();
        for (Port port : allPorts) {
            ((DynamicPort) port).setNode(this);
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Node#getComponent()
     */
    @Override
    public DynamicComponent getComponent() {
        return (DynamicComponent) super.getComponent();
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        GraphUtil.validateConnection(edge);
    }

    public DataPort getFreeInPort() {
        List<DataPort> inputPorts = this.getInputPorts();
        for (DataPort dataPort : inputPorts) {
            if (null == dataPort.getFromNode()) {
                return dataPort;
            }
        }
        // none found, so make a new one.
        DynamicComponentPort comPort = new DynamicComponentPort(getComponent());
        getComponent().addInputPort(comPort);
        DataPort port = comPort.createPort();
        ((DynamicPort) port).setNode(this);
        this.addInputPort(port);

        return port;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.dynamic.PortAddable#removeLastDynamicallyAddedInPort()
     */
    @Override
    public void removeLastDynamicallyAddedInPort() throws GraphException {

        List<DataPort> inputPorts = this.getInputPorts();
        if (inputPorts.size() == 1) {
            // This is the initial port, so leave it alone
            return;
        }
        DataPort portToBeRemoved = null;
        for (DataPort dataPort : inputPorts) {
            if (null == dataPort.getFromNode()) {
                getComponent().removeInputPort((DynamicComponentPort) dataPort.getComponentPort());
                portToBeRemoved = dataPort;
                break;
            }
        }
        if (null != portToBeRemoved) {
            this.removeInputPort(portToBeRemoved);
        }
    }

    /**
     * @param url
     */
    public void setImplURL(URL url) {
        this.getComponent().setImplJarLocation(url);
    }

    /**
     * @param operationName
     */
    public void setOperationName(String operationName) {
        this.getComponent().setOperationName(operationName);
    }

    /**
     * @param className
     */
    public void setClassName(String className) {
        this.getComponent().setClassName(className);
    }

}