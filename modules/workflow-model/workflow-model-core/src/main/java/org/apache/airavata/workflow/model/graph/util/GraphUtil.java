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
package org.apache.airavata.workflow.model.graph.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.ControlEdge;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.system.StreamSourceNode;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.workflow.model.utils.MessageConstants;

public class GraphUtil {
	// private static final MLogger logger = MLoggerFactory.getLogger();

	/**
	 * Returns the WSNodes included in a specified graph.
	 *
	 * @param graph
	 *            The specified graph.
	 * @return The WSNodes.
	 */
	public static Collection<WSNode> getWSNodes(Graph graph) {
		return getNodes(graph, WSNode.class);
	}

	/**
	 * Returns a List of InputNodes from a specified graph.
	 *
	 * @param graph
	 *            the specified graph
	 * @return The List of InputNodes.
	 */
	public static List<InputNode> getInputNodes(Graph graph) {
		return getNodes(graph, InputNode.class);
	}

	/**
	 * Returns a List of OutputNodes from a specified graph.
	 *
	 * @param graph
	 *            the specified graph
	 * @return The List of OutputNodes.
	 */
	public static List<OutputNode> getOutputNodes(Graph graph) {
		return getNodes(graph, OutputNode.class);
	}

	/**
	 * Returns a List of nodes of specific subclass of Node from a specified
	 * graph.
	 *
	 * @param <N>
	 *            One of the subclass of the Node.
	 * @param graph
	 *            The specified graph.
	 * @param klass
	 *            The specified subclass of Node.
	 * @return The list of T
	 */
	@SuppressWarnings("unchecked")
	public static <N extends Node> List<N> getNodes(Graph graph, Class<N> klass) {
		List<N> nodes = new LinkedList<N>();
		for (Node node : graph.getNodes()) {
			if (klass.isInstance(node)) {
				if (!nodes.contains(node)) {
					nodes.add((N) node);
				}
			}
		}
		return nodes;
	}

	/**
	 * @param node
	 * @return The output nodes.
	 */
	public static List<Node> getOutputNodes(Node node) {
		List<Node> outputNodes = new ArrayList<Node>();
		for (Port port : node.getOutputPorts()) {
			Collection<Node> toNodes = port.getToNodes();
			outputNodes.addAll(toNodes);
		}
		return outputNodes;
	}

	/**
	 * Returns next nodes connected to a specified node.
	 *
	 * @param node
	 *            The specified node.
	 * @return The next nodes.
	 */
	public static List<Node> getNextNodes(Node node) {
		List<Node> nextNodes = getOutputNodes(node);
		for (Port port : node.getControlOutPorts()) {
			Collection<Node> toNodes = port.getToNodes();
			nextNodes.addAll(toNodes);
		}
		return nextNodes;
	}

	/**
	 * Sorts the nodes alphabetically by their names.
	 *
	 * @param <T>
	 * @param nodes
	 * @return The list of nodes sorted.
	 */
	public static <T extends Node> List<T> sortByName(Collection<T> nodes) {
		List<T> nodeList = new LinkedList<T>(nodes);
		Comparator<Node> nameComparator = new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				String name1 = node1.getName();
				String name2 = node2.getName();
				return name1.compareToIgnoreCase(name2);
			}
		};
		Collections.sort(nodeList, nameComparator);
		return nodeList;
	}

	/**
	 * @param graph
	 * @param kind
	 * @return The ports of specified kind.
	 */
	public static Collection<Port> getPorts(Graph graph, Port.Kind kind) {
		Collection<Port> ports = new ArrayList<Port>();
		for (Port port : graph.getPorts()) {
			if (port.getKind() == kind) {
				ports.add(port);
			}
		}
		return ports;
	}

	/**
	 * @param <P>
	 * @param graph
	 * @param klass
	 * @return The ports
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Port> List<P> getPorts(Graph graph, Class<P> klass) {
		List<P> ports = new LinkedList<P>();
		for (Port port : graph.getPorts()) {
			if (klass.isInstance(port)) {
				ports.add((P) port);
			}
		}
		return ports;
	}

	private enum Color {
		/**
		 * This node hasn't been visited.
		 */
		WHITE,
		/**
		 * This node has been visited.
		 */
		GRAY,
		/**
		 * This not is not in cycle.
		 */
		BLACK;
	}

	/**
	 * @param graph
	 * @return true if there is a cycle in the graph; false otherwise.
	 */
	public static boolean containsCycle(Graph graph) {
		Map<Node, Color> coloredNodes = new HashMap<Node, Color>();
		for (Node node : graph.getNodes()) {
			coloredNodes.put(node, Color.WHITE);
		}

		for (Node node : graph.getNodes()) {
			if (coloredNodes.get(node) == Color.WHITE) {
				if (visit(node, coloredNodes)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean visit(Node node, Map<Node, Color> coloredNodes) {
		coloredNodes.put(node, Color.GRAY);
		for (Node nextNode : getNextNodes(node)) {
			Color nextNodeColor = coloredNodes.get(nextNode);
			if (nextNodeColor == Color.GRAY) {
				return true;
			} else if (nextNodeColor == Color.WHITE) {
				if (visit(nextNode, coloredNodes)) {
					return true;
				}
			}
		}
		coloredNodes.put(node, Color.BLACK);
		return false;
	}

	/**
	 * @param edge
	 * @throws GraphException
	 */
	public static void validateConnection(Edge edge) throws GraphException {
		Port fromPort = edge.getFromPort();
		Port toPort = edge.getToPort();
		if (edge instanceof ControlEdge) {
			if (!(fromPort instanceof ControlPort && toPort instanceof ControlPort)) {
				throw new GraphException(MessageConstants.UNEXPECTED_ERROR);
			}
		} else if (edge instanceof DataEdge) {
			if (fromPort instanceof EPRPort) {
				// TODO
				return;
			}
			if (!(fromPort instanceof DataPort || fromPort instanceof EPRPort)
					|| !(toPort instanceof DataPort)) {
				throw new GraphException(MessageConstants.UNEXPECTED_ERROR);
			}

			DataPort fromDataPort = (DataPort) fromPort;
			DataPort toDataPort = (DataPort) toPort;

			DataType fromType = fromDataPort.getType();
			DataType toType = toDataPort.getType();

			if (toDataPort.getEdges().size() > 1) {
				throw new GraphException(
						MessageConstants.MORE_THAN_ONE_CONNECTIONS);
			}

			// if connection came from the CEP register component it should be
			// ok
			if (fromPort.getNode() instanceof WSNode) {
				if ("registerStream".equals(((WSNode) fromPort.getNode())
						.getOperationName())) {
					return;
				}
			}

			if (!(fromType == null
					|| fromType.equals(WSConstants.XSD_ANY_TYPE)
					|| fromType.equals(new QName(WSConstants.XSD_NS_URI,
							"anyType"))
					|| toType == null
					|| toType.equals(WSConstants.XSD_ANY_TYPE)
					|| toType.equals(new QName(WSConstants.XSD_NS_URI,
							"anyType")) || fromType.equals(toType)) && (fromType == null
					|| fromType.equals(WSConstants.LEAD_ANY_TYPE)
					|| fromType.equals(new QName(WSConstants.LEAD_NS_URI,
							"anyType"))
					|| toType == null
					|| toType.equals(WSConstants.LEAD_ANY_TYPE)
					|| toType.equals(new QName(WSConstants.LEAD_NS_URI,
							"anyType")) || fromType.equals(toType))) {
				throw new GraphException(
						"Cannot connect ports with different types:"
								+ " \nfrom=\t" + fromType + " \nto=\t" + toType
								+ "");
			}
		}
	}

	/**
	 * @param graph
	 * @throws GraphException
	 */
	public static void propagateTypes(Graph graph) throws GraphException {
		List<WSPort> wsPorts = getPorts(graph, WSPort.class);
		for (WSPort wsPort : wsPorts) {
			List<DataEdge> edges = wsPort.getEdges();
			for (DataEdge edge : edges) {
				DataPort fromPort = edge.getFromPort();

				DataPort toPort = edge.getToPort();
				if (fromPort == wsPort) {
					toPort.copyType(wsPort);
				} else if (toPort == wsPort) {
					fromPort.copyType(wsPort);
				} else {
					throw new WorkflowRuntimeException();
				}
			}
		}

	}


    /**
     *
     * @param graph
     * @return
     */
	public static LinkedList<StreamSourceNode> getStreamSourceNodes(
			WSGraph graph) {
		List<NodeImpl> nodes = graph.getNodes();
		LinkedList<StreamSourceNode> ret = new LinkedList<StreamSourceNode>();
		for (NodeImpl nodeImpl : nodes) {
			if (nodeImpl instanceof StreamSourceNode) {
				ret.add((StreamSourceNode) nodeImpl);
			}
		}
		return ret;
	}

	/**
	 * @param node
	 * @return null if not the same
	 */
	public static String isSameLabeledInput(Node node) {
		if (!isAllInputsConnected(node)) {
			throw new WorkflowRuntimeException("Node inputs not connected" + node);
		}
		if (!isAllInputsLabeled(node)) {
			throw new WorkflowRuntimeException(
					"Some or all of the node inputs not labeled" + node);
		}
		List<DataPort> inputPorts = node.getInputPorts();
		String label = inputPorts.get(0).getEdge(0).getLabel();
		for (DataPort dataPort : inputPorts) {
			// 0 because its got only one
			if (!label.equals(dataPort.getEdge(0).getLabel())) {
				return null;
			}
		}
		return label;
	}

	/**
	 * @param node
	 * @return
	 */
	public static boolean isAllInputsLabeled(Node node) {
		List<DataPort> inputPorts = node.getInputPorts();
		for (DataPort dataPort : inputPorts) {
			// 0 because its got only one
			Edge edge = dataPort.getEdge(0);
			if (edge == null || edge.getLabel() == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param node
	 * @return
	 */
	public static boolean isAllInputsConnected(Node node) {
		List<DataPort> inputPorts = node.getInputPorts();
		for (DataPort dataPort : inputPorts) {
			// 0 because its got only one
			Edge edge = dataPort.getEdge(0);
			if (edge == null) {
				return false;
			}
		}
		return true;
	}

    /**
     *
     * @param node
     * @return
     */
	public static boolean isRegulerNode(Node node) {
		if (node instanceof WSNode) {
			return true;
		}
		return false;
	}

    /**
     *
     * @param node
     * @return
     */
	public static String getEncodedInputLabels(Node node) {
		if (!isAllInputsConnected(node)) {
			throw new WorkflowRuntimeException("Node inputs not connected" + node);
		}
		if (!isAllInputsLabeled(node)) {
			throw new WorkflowRuntimeException(
					"Some or all of the node inputs not labeled" + node);
		}
		List<DataPort> inputPorts = node.getInputPorts();
		String label = "";
		for (DataPort dataPort : inputPorts) {
			label += "#" + dataPort.getEdge(0).getLabel();
		}
		return label;

	}

	/**
	 * @param wsGraph
	 * @return
	 */
	public static List<Node> getJoinRequiredNodes(WSGraph wsGraph) {
		List<NodeImpl> nodes = wsGraph.getNodes();
		List<Node> ret = new LinkedList<Node>();
		for (NodeImpl node : nodes) {
			if (node.getRequireJoin()) {
				ret.add(node);
			}
		}
		return ret;
	}



	/**
	 * @param wsGraph
	 * @return
	 */
	public static HashMap<String, LinkedList<Node>> partitionGraphOnLabel(
			WSGraph wsGraph) {
		HashMap<String, LinkedList<Node>> returnMap = new HashMap<String, LinkedList<Node>>();
		List<NodeImpl> nodes = wsGraph.getNodes();
		for (NodeImpl node : nodes) {
			if (!isInputOutputNode(node)) {
				LinkedList<Node> list = returnMap.get(node.getLabel());
				if (null == list) {
					list = new LinkedList<Node>();
					returnMap.put(node.getLabel(), list);
				}
				list.add(node);
			}
		}
		return returnMap;
	}

	/**
	 * @param node
	 * @return
	 */
	private static boolean isInputOutputNode(NodeImpl node) {
		return node instanceof InputNode || node instanceof StreamSourceNode
				|| node instanceof OutputNode;
	}

	

	/**
	 * @param name
	 * @param nodeList
	 * @param key
	 * @return
	 */
	public static String getSubWorkflowName(String name,
			LinkedList<Node> nodeList, String key) {
		String ret = name + "_subworkflow";
		for (Node node : nodeList) {
			ret += node.getID();

		}

		if(ret.length()>40){
			ret = ret.substring(0, 40);
		}
		// TODO Auto-generated method stub
		return ret;
	}

}
