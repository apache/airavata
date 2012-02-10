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

package org.apache.airavata.xbaya.graph.system;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.graph.DataEdge;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.EPRPort;
import org.apache.airavata.xbaya.graph.Edge;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.common.utils.WSConstants;
import org.xmlpull.infoset.XmlElement;

public abstract class SystemNode extends NodeImpl {

    /**
     * Constructs a SystemNode.
     * 
     * @param graph
     */
    protected SystemNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs a NodeImpl.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public SystemNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.xbaya.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        super.edgeWasAdded(edge);
        GraphUtil.validateConnection(edge);

        Port fromPort = edge.getFromPort();
        Port toPort = edge.getToPort();
        if (edge instanceof DataEdge) {
            if (fromPort instanceof EPRPort) {
                // TODO
                return;
            }

            DataPort fromDataPort = (DataPort) fromPort;
            DataPort toDataPort = (DataPort) toPort;

            QName fromType = fromDataPort.getType();
            QName toType = toDataPort.getType();

            if (fromDataPort.getNode() == this) {
                // setType() propagates the change to the whole workflow.
                if (!(toType == null || toType.equals(WSConstants.XSD_ANY_TYPE))) {
                    fromDataPort.copyType(toDataPort);
                }
            } else if (toDataPort.getNode() == this) {
                if (!(fromType == null || fromType.equals(WSConstants.XSD_ANY_TYPE))) {
                    toDataPort.copyType(fromDataPort);
                }
            } else {
                throw new XBayaRuntimeException();
            }
        }
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#edgeWasRemoved(org.apache.airavata.xbaya.graph.Edge)
     */
    @Override
    protected void edgeWasRemoved(Edge removedEdge) {
        super.edgeWasRemoved(removedEdge);

        if (removedEdge instanceof DataEdge) {
            // maybe only the way to propagate the type change is to reset
            // everything and repropagate port types from WSPort.

            List<SystemDataPort> systemDataPorts = GraphUtil.getPorts(getGraph(), SystemDataPort.class);
            for (SystemDataPort port : systemDataPorts) {
                port.resetType();
            }

            try {
                GraphUtil.propagateTypes(getGraph());
            } catch (GraphException e) {
                // this should not happen.
                throw new XBayaRuntimeException(e);
            }
        }
    }

    /**
     * @param port
     * @throws GraphException
     */
    @SuppressWarnings("unused")
    protected void portTypeChanged(SystemDataPort port) throws GraphException {
        // Do nothing by default.
    }
}