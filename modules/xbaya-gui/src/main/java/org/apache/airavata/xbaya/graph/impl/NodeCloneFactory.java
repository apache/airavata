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
 *
 */

package org.apache.airavata.xbaya.graph.impl;

import java.util.List;

import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.dynamic.CepNode;
import org.apache.airavata.xbaya.graph.dynamic.CepPort;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.graph.ws.WSGraph;
import org.apache.airavata.xbaya.graph.ws.WSNode;

public class NodeCloneFactory {

    /**
     * @param node
     * @param wsGraph
     * @return
     * @throws GraphException
     */
    public static Node clone(Node node, WSGraph wsGraph) throws GraphException {

        if (node instanceof WSNode) {
            WSNode wsNode = new WSNode(((WSNode) node).toXML());
            ((NodeImpl) wsNode).setGraph(wsGraph);
            List<DataPort> inputPorts = node.getInputPorts();
            for (DataPort dataPort : inputPorts) {
                wsNode.addInputPort(new CepPort(dataPort.toXML()));
            }

            List<DataPort> outputPorts = node.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                wsNode.addOutputPort(new CepPort(dataPort.toXML()));
            }
            return wsNode;
        } else if (node instanceof CepNode) {
            CepNode cepNode = new CepNode(((CepNode) node).toXML());

            List<DataPort> inputPorts = node.getInputPorts();
            for (DataPort dataPort : inputPorts) {
                CepPort cepPort = new CepPort(dataPort.toXML());
                cepNode.addInputPort(cepPort);
            }

            List<DataPort> outputPorts = node.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                cepNode.addOutputPort(new CepPort(dataPort.toXML()));
            }
            ((NodeImpl) cepNode).setGraph(wsGraph);
            return cepNode;
        } else if (node instanceof InputNode) {
            InputNode inputNode = new InputNode(((InputNode) node).toXML());

            List<DataPort> outputPorts = node.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                inputNode.addOutputPort(new CepPort(dataPort.toXML()));
            }
            ((NodeImpl) inputNode).setGraph(wsGraph);
            return inputNode;
        } else if (node instanceof OutputNode) {
            OutputNode outputNode = new OutputNode(((OutputNode) node).toXML());

            List<DataPort> inputPorts = node.getInputPorts();
            for (DataPort dataPort : inputPorts) {
                outputNode.addInputPort(new CepPort(dataPort.toXML()));
            }

            ((NodeImpl) outputNode).setGraph(wsGraph);
            return outputNode;
        }

        throw new XBayaRuntimeException("Unhandled node type for clonning:" + node);
    }

}