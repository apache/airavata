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
package org.apache.airavata.workflow.model.graph.ws;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonObject;
import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.common.utils.JSONUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.ControlEdge;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphFactory;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.apache.airavata.workflow.model.graph.amazon.InstanceDataPort;
import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;
import org.apache.airavata.workflow.model.graph.amazon.TerminateInstanceNode;
import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.apache.airavata.workflow.model.graph.system.BlockNode;
import org.apache.airavata.workflow.model.graph.system.ConstantNode;
import org.apache.airavata.workflow.model.graph.system.DoWhileNode;
import org.apache.airavata.workflow.model.graph.system.EndBlockNode;
import org.apache.airavata.workflow.model.graph.system.EndDoWhileNode;
import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
import org.apache.airavata.workflow.model.graph.system.EndifNode;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.apache.airavata.workflow.model.graph.system.IfNode;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.MemoNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.system.ReceiveNode;
import org.apache.airavata.workflow.model.graph.system.StreamSourceNode;
import org.apache.airavata.workflow.model.graph.system.SystemDataPort;
import org.apache.airavata.workflow.model.utils.MessageConstants;
import org.xmlpull.infoset.XmlElement;

/**
 * The GraphFactory class is a factory to create nodes, ports, and edges.
 *
 */
public class WSGraphFactory implements GraphFactory {

	/**
	 * Creates a empty Graph
	 *
	 * @return the empty graph created
	 */
	public static WSGraph createGraph() {
		return createWSGraph();
	}

	/**
	 * Reads a specified file and creates a graph.
	 *
	 * @param file
	 * @return The graph created
	 * @throws GraphException
	 * @throws IOException
	 */
	public static WSGraph createGraph(File file) throws GraphException,
			IOException {
		String graphString = IOUtil.readFileToString(file);
		return createGraph(graphString);
	}

	/**
	 * @param graphString
	 * @return the graph created
	 * @throws GraphException
	 */
	public static WSGraph createGraph(String graphString) throws GraphException {
		XmlElement graphElement;
        JsonObject graphElementJSON;
		try {
//			graphElement = XMLUtil.stringToXmlElement(graphString);
            graphElementJSON = JSONUtil.stringToJSONObject(graphString);
        } catch (RuntimeException e) {
			throw new GraphException(MessageConstants.XML_ERROR, e);
		}
		return createGraph(graphElementJSON);
	}

	/**
	 * @param graphElement
	 * @return The graph created
	 * @throws GraphException
	 */
	public static WSGraph createGraph(XmlElement graphElement)
			throws GraphException {
		try {
			WSGraph graph = createWSGraph();
			graph.parse(graphElement);
			return graph;
		} catch (RuntimeException e) {
			throw new GraphException(MessageConstants.XML_ERROR, e);
		}
	}

    public static WSGraph createGraph(JsonObject graphObject) throws GraphException {
        WSGraph graph = createWSGraph();
        graph.parse(graphObject);
        return graph;
    }

	/**
	 * @see org.apache.airavata.workflow.model.graph.GraphFactory#createNode(org.xmlpull.infoset.XmlElement)
	 */
	public NodeImpl createNode(XmlElement nodeElement) throws GraphException {
		String type = nodeElement.attributeValue(GraphSchema.NS,
				GraphSchema.NODE_TYPE_ATTRIBUTE);
		if (type == null) {
			// Old graphs don't have the namespace for the attribute.
			type = nodeElement.attributeValue(GraphSchema.NODE_TYPE_ATTRIBUTE);
		}

		NodeImpl node;
		if (GraphSchema.NODE_TYPE_WS.equals(type)) {
			node = new WSNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_STREAM_SOURCE.equals(type)) {
			node = new StreamSourceNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_WORKFLOW.equals(type)) {
			node = new WorkflowNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_INPUT.equals(type)) {
			node = new InputNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_OUTPUT.equals(type)) {
			node = new OutputNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_CONSTANT.equals(type)) {
			node = new ConstantNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_SPLIT.equals(type)) {
			node = new ForEachNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_MERGE.equals(type)) {
			node = new EndForEachNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_IF.equals(type)) {
			node = new IfNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_ENDIF.equals(type)) {
			node = new EndifNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_DOWHILE.equals(type)) {
			node = new DoWhileNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_ENDDOWHILE.equals(type)) {
			node = new EndDoWhileNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_MEMO.equals(type)) {
			node = new MemoNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_RECEIVE.equals(type)) {
			node = new ReceiveNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_BLOCK.equals(type)) {
			node = new BlockNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_ENDBLOCK.equals(type)) {
			node = new EndBlockNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_INSTANCE.equals(type)) {
			node = new InstanceNode(nodeElement);
		} else if (GraphSchema.NODE_TYPE_TERMINATE.equals(type)) {
			node = new TerminateInstanceNode(nodeElement);
		} else {
			// Default is WsNode for backward compatibility.
			node = new WSNode(nodeElement);
		}
		return node;
	}

    public NodeImpl createNode(JsonObject nodeObject) throws GraphException {

        String type = nodeObject.getAsJsonPrimitive(GraphSchema.NODE_TYPE_ATTRIBUTE).getAsString();

        NodeImpl node;
        if (GraphSchema.NODE_TYPE_WS.equals(type)) {
            node = new WSNode(nodeObject);
        } else if (GraphSchema.NODE_TYPE_WORKFLOW.equals(type)) {
            node = new WorkflowNode(nodeObject);
        } else if (GraphSchema.NODE_TYPE_INPUT.equals(type)) {
            node = new InputNode(nodeObject);
        } else if (GraphSchema.NODE_TYPE_OUTPUT.equals(type)) {
            node = new OutputNode(nodeObject);
/*        } else if (GraphSchema.NODE_TYPE_STREAM_SOURCE.equals(type)) {
            node = new StreamSourceNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_CONSTANT.equals(type)) {
            node = new ConstantNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_SPLIT.equals(type)) {
            node = new ForEachNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_MERGE.equals(type)) {
            node = new EndForEachNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_IF.equals(type)) {
            node = new IfNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_ENDIF.equals(type)) {
            node = new EndifNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_DOWHILE.equals(type)) {
            node = new DoWhileNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_ENDDOWHILE.equals(type)) {
            node = new EndDoWhileNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_MEMO.equals(type)) {
            node = new MemoNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_RECEIVE.equals(type)) {
            node = new ReceiveNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_BLOCK.equals(type)) {
            node = new BlockNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_ENDBLOCK.equals(type)) {
            node = new EndBlockNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_INSTANCE.equals(type)) {
            node = new InstanceNode(nodeElement);
        } else if (GraphSchema.NODE_TYPE_TERMINATE.equals(type)) {
            node = new TerminateInstanceNode(nodeElement);*/
        } else {
            // Default is WsNode for backward compatibility.
            node = new WSNode(nodeObject);
        }

        return node;
    }
	/**
	 * @see org.apache.airavata.workflow.model.graph.GraphFactory#createPort(org.xmlpull.infoset.XmlElement)
	 */
	public PortImpl createPort(JsonObject portObject) {

        String type = portObject.getAsJsonPrimitive(GraphSchema.PORT_TYPE_ATTRIBUTE).getAsString();
        PortImpl port;
        if (GraphSchema.PORT_TYPE_WS_DATA.equals(type)) {
            port = new WSPort(portObject);
        } else if (GraphSchema.PORT_TYPE_SYSTEM_DATA.equals(type)) {
            port = new SystemDataPort(portObject);
        } else if (GraphSchema.PORT_TYPE_CONTROL.equals(type)) {
            port = new ControlPort(portObject);
/*        } else if (GraphSchema.PORT_TYPE_EPR.equals(type)) {
            port = new EPRPort(portElement);
        } else if (GraphSchema.PORT_TYPE_INSTANCE.equals(type)) {
            port = new InstanceDataPort(portElement);*/
        } else {
            // Default is WsPort because of backword compatibility
            port = new WSPort(portObject);
        }
        return port;
    }

    public PortImpl createPort(XmlElement portElement) {
        String type = portElement.attributeValue(GraphSchema.NS,
                GraphSchema.PORT_TYPE_ATTRIBUTE);
        if (type == null) {
            // Old graphs don't have the namespace for the attribute.
            type = portElement.attributeValue(GraphSchema.PORT_TYPE_ATTRIBUTE);
        }
        PortImpl port;
        if (GraphSchema.PORT_TYPE_WS_DATA.equals(type)) {
            port = new WSPort(portElement);
        } else if (GraphSchema.PORT_TYPE_SYSTEM_DATA.equals(type)) {
            port = new SystemDataPort(portElement);
        } else if (GraphSchema.PORT_TYPE_CONTROL.equals(type)) {
            port = new ControlPort(portElement);
        } else if (GraphSchema.PORT_TYPE_EPR.equals(type)) {
            port = new EPRPort(portElement);
        } else if (GraphSchema.PORT_TYPE_INSTANCE.equals(type)) {
            port = new InstanceDataPort(portElement);
        } else {
            // Default is WsPort because of backword compatibility
            port = new WSPort(portElement);
        }
        return port;
    }
	/**
	 * @see org.apache.airavata.workflow.model.graph.GraphFactory#createEdge(org.apache.airavata.workflow.model.graph.Port,
	 *      org.apache.airavata.workflow.model.graph.Port)
	 */
	public EdgeImpl createEdge(Port fromPort, Port toPort) {
		Kind fromKind = fromPort.getKind();
		Kind toKind = toPort.getKind();
		if (!((fromKind == Kind.DATA_OUT && toKind == Kind.DATA_IN)
				|| (fromKind == Kind.CONTROL_OUT && toKind == Kind.CONTROL_IN) || (fromKind == Kind.EPR && toKind == Kind.DATA_IN))) {
			throw new WorkflowRuntimeException();
		}
		EdgeImpl edge;
		if (toKind == Kind.DATA_IN) {
			edge = new DataEdge();
		} else if (toKind == Kind.CONTROL_IN) {
			edge = new ControlEdge();
		} else {
			// Should not happen.
			throw new WorkflowRuntimeException();
		}
		return edge;
	}

	/**
	 * @see org.apache.airavata.workflow.model.graph.GraphFactory#createEdge(org.xmlpull.infoset.XmlElement)
	 */
	public EdgeImpl createEdge(XmlElement edgeElement) {
		String type = edgeElement.attributeValue(GraphSchema.NS,
				GraphSchema.EDGE_TYPE_ATTRIBUTE);
		EdgeImpl edge;
		if (GraphSchema.EDGE_TYPE_DATA.equals(type)) {
			edge = new DataEdge(edgeElement);
		} else if (GraphSchema.PORT_TYPE_CONTROL.equals(type)) {
			edge = new ControlEdge(edgeElement);
		} else {
			// Default is WsPort because of backword compatibility
			edge = new DataEdge(edgeElement);
		}
		return edge;
	}

    public EdgeImpl createEdge(JsonObject edgeObject) {
        String type = edgeObject.getAsJsonPrimitive(GraphSchema.EDGE_TYPE_ATTRIBUTE).getAsString();
        EdgeImpl edge;
        if (GraphSchema.EDGE_TYPE_DATA.equals(type)) {
            edge = new DataEdge(edgeObject);
        } else if (GraphSchema.PORT_TYPE_CONTROL.equals(type)) {
            edge = new ControlEdge(edgeObject);
        } else {
            // Default is WsPort because of backword compatibility
            edge = new DataEdge(edgeObject);
        }
        return edge;
    }

    /**
	 * @return The graph created.
	 */
	private static WSGraph createWSGraph() {
		return new WSGraph(new WSGraphFactory());
	}
}