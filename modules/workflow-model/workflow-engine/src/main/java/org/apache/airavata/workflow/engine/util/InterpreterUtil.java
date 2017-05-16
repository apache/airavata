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
package org.apache.airavata.workflow.engine.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.workflow.engine.interpretor.WorkFlowInterpreterException;
import org.apache.airavata.workflow.engine.invoker.Invoker;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;

//import xsul5.XmlConstants;
//import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler;
//import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler.NodeState;

public class InterpreterUtil {
    /**
     * This method returns the input values for given foreach node
     *
     * @param forEachNode
     * @param listOfValues
     * @param invokerMap
     * @return
     * @throws WorkflowException
     */
    public static Object getInputsForForEachNode(final ForEachNode forEachNode, final LinkedList<String> listOfValues, Map<Node, Invoker> invokerMap)
            throws WorkflowException {
        List<DataPort> inputPorts = forEachNode.getInputPorts();

        Object returnValForProvenance = null;
        for (DataPort inputPort : inputPorts) {

            Node inputNode = inputPort.getFromNode();
            // if input node for for-each is WSNode
            if (inputNode instanceof InputNode) {
//                for (DataPort dataPort : forEachNode.getInputPorts()) {
                    returnValForProvenance = InterpreterUtil.findInputFromPort(inputPort, invokerMap);
                    if (null == returnValForProvenance) {
                        throw new WorkFlowInterpreterException("Unable to find input for the node:" + forEachNode.getID());
                    }
                    String[] vals = StringUtil.getElementsFromString(returnValForProvenance.toString());
                    listOfValues.addAll(Arrays.asList(vals));
//                }
            }
        }
        return returnValForProvenance;
    }

    /**
     *
     * @param inputPort
     * @param invokerMap
     * @return
     * @throws WorkflowException
     */
    public static Object findInputFromPort(DataPort inputPort, Map<Node, Invoker> invokerMap) throws WorkflowException {
        Object outputVal = null;
//        Node fromNode = inputPort.getFromNode();
//        if (fromNode instanceof InputNode) {
//            outputVal = ((InputNode) fromNode).getDefaultValue();
//        } else if (fromNode instanceof ConstantNode) {
//            outputVal = ((ConstantNode) fromNode).getValue();
//        } else if (fromNode instanceof DifferedInputNode && ((DifferedInputNode) fromNode).isConfigured()) {
//            outputVal = ((DifferedInputNode) fromNode).getDefaultValue();
//        } else if (fromNode instanceof EndifNode || fromNode instanceof DoWhileNode  || fromNode instanceof EndDoWhileNode) {
//            Invoker fromInvoker = invokerMap.get(fromNode);
////            outputVal = fromInvoker.getOutput(inputPort.getFromPort().getID());
//        } else if (fromNode instanceof InstanceNode) {
//            return ((InstanceNode) fromNode).getOutputInstanceId();
//        } else if (fromNode instanceof EndForEachNode) {
//            outputVal = "";
//            Invoker workflowInvoker = invokerMap.get(fromNode);
//            String outputName = "";
//            if (inputPort instanceof SystemDataPort) {
//                outputName = ((SystemDataPort) inputPort).getWSComponentPort().getName();
//
//            } else if (inputPort instanceof WSPort) {
//                outputName = ((SystemDataPort) fromNode.getInputPort(fromNode.getOutputPorts().indexOf(inputPort.getEdge(0).getFromPort())))
//                        .getWSComponentPort().getName();
//            }
//            XmlElement msgElmt = XmlConstants.BUILDER.parseFragmentFromString("<temp>" + workflowInvoker.getOutput(outputName) + "</temp>");
//            Iterator valItr = msgElmt.children().iterator();
//            while (valItr.hasNext()) {
//                Object object2 = valItr.next();
//                if (object2 instanceof XmlElement) {
//
//                    if (((XmlElement) object2).children().iterator().hasNext()) {
//                        outputVal = outputVal + StringUtil.DELIMETER  + StringUtil.quoteString(((XmlElement) object2).children().iterator().next().toString());
//                    }
//                }
//            }
//
//            if (((String) outputVal).length() == 0) {
//                throw new WorkflowException("Empty Output Generated");
//            }
//            outputVal = ((String) outputVal).substring(1, ((String) outputVal).length());
//        } else {
//            Invoker fromInvoker = invokerMap.get(fromNode);
//            try {
//                if (fromInvoker != null)
//                    outputVal = fromInvoker.getOutput(inputPort.getFromPort().getName());
//
//            } catch (Exception e) {
//                // if the value is still null look it up from the inputport name
//                // because the value is set to the input port name at some point
//                // there is no harm in doing this
//                if (null == outputVal) {
//                    outputVal = fromInvoker.getOutput(inputPort.getName());
//                }
//            }
//
//        }
        return outputVal;

    }

    public static Object findInputFromPort(DataPort inputPort) throws WorkflowException {
        Object outputVal = null;
        Node fromNode = inputPort.getFromNode();
        if (fromNode instanceof InputNode) {
            outputVal = ((InputNode) fromNode).getDefaultValue();
//        } else if (fromNode instanceof ConstantNode) {
//            outputVal = ((ConstantNode) fromNode).getValue();
//        } else if (fromNode instanceof DifferedInputNode && ((DifferedInputNode) fromNode).isConfigured()) {
//            outputVal = ((DifferedInputNode) fromNode).getDefaultValue();
//        } else if (fromNode instanceof EndifNode || fromNode instanceof DoWhileNode  || fromNode instanceof EndDoWhileNode) {
//            Invoker fromInvoker = invokerMap.get(fromNode);
//            outputVal = fromInvoker.getOutput(inputPort.getFromPort().getID());
//        } else if (fromNode instanceof InstanceNode) {
//            return ((InstanceNode) fromNode).getOutputInstanceId();
//        } else if (fromNode instanceof EndForEachNode) {
//            outputVal = "";
//            Invoker workflowInvoker = invokerMap.get(fromNode);
//            String outputName = "";
//            if (inputPort instanceof SystemDataPort) {
//                outputName = ((SystemDataPort) inputPort).getWSComponentPort().getName();
//
//            } else if (inputPort instanceof WSPort) {
//                outputName = ((SystemDataPort) fromNode.getInputPort(fromNode.getOutputPorts().indexOf(inputPort.getEdge(0).getFromPort())))
//                        .getWSComponentPort().getName();
//            }
//            XmlElement msgElmt = XmlConstants.BUILDER.parseFragmentFromString("<temp>" + workflowInvoker.getOutput(outputName) + "</temp>");
//            Iterator valItr = msgElmt.children().iterator();
//            while (valItr.hasNext()) {
//                Object object2 = valItr.next();
//                if (object2 instanceof XmlElement) {
//
//                    if (((XmlElement) object2).children().iterator().hasNext()) {
//                        outputVal = outputVal + StringUtil.DELIMETER  + StringUtil.quoteString(((XmlElement) object2).children().iterator().next().toString());
//                    }
//                }
//            }
//
//            if (((String) outputVal).length() == 0) {
//                throw new WorkflowException("Empty Output Generated");
//            }
//            outputVal = ((String) outputVal).substring(1, ((String) outputVal).length());
//        } else {
//            Invoker fromInvoker = invokerMap.get(fromNode);
//            try {
//                if (fromInvoker != null)
//                    outputVal = fromInvoker.getOutput(inputPort.getFromPort().getName());
//
//            } catch (Exception e) {
//                // if the value is still null look it up from the inputport name
//                // because the value is set to the input port name at some point
//                // there is no harm in doing this
//                if (null == outputVal) {
//                    outputVal = fromInvoker.getOutput(inputPort.getName());
//                }
//            }

        }
        return outputVal;

    }
    /**
     * @param node
     * @return
     */
    public static Node findEndForEachFor(ForEachNode node) {

        Collection<Node> toNodes = node.getOutputPort(0).getToNodes();
        if (toNodes.size() != 1) {
            throw new WorkflowRuntimeException("ForEach output does not contain single out-edge");
        }
        Node middleNode = toNodes.iterator().next();
        List<DataPort> outputPorts = middleNode.getOutputPorts();
        for (DataPort dataPort : outputPorts) {
            if (dataPort.getToNodes().size() == 1) {
                Node possibleEndForEachNode = dataPort.getToNodes().get(0);
                if (possibleEndForEachNode instanceof EndForEachNode) {
                    return possibleEndForEachNode;
                }
            }
        }
        throw new WorkflowRuntimeException("EndForEachNode not found");
    }

    public static Integer[] getNumberOfInputsForForEachNode(final ForEachNode forEachNode, Map<Node, Invoker> invokerMap) throws WorkflowException {
        List<DataPort> inputPorts = forEachNode.getInputPorts();
        Integer[] inputNumbers = new Integer[inputPorts.size()];
        for (DataPort forEachInputPort : inputPorts) {
            // if input node for for-each is WSNode
            Node forEachInputNode = forEachInputPort.getFromNode();
            int index = 0;
            Object returnValForProvenance = null;
            if (forEachInputNode instanceof InputNode) {
                returnValForProvenance = InterpreterUtil.findInputFromPort(forEachInputPort, invokerMap);
                if (null == returnValForProvenance) {
                    throw new WorkFlowInterpreterException("Unable to find input for the node:" + forEachNode.getID());
                }
                String[] vals = StringUtil.getElementsFromString(returnValForProvenance.toString());
                inputNumbers[inputPorts.indexOf(forEachInputPort)] = vals.length;
            }
        }
        return inputNumbers;
    }

    public static ArrayList<Node> getFinishedNodesDynamically(WSGraph graph) {
        return getNodesWithBodyColor(NodeExecutionState.FINISHED, graph);
    }

    public static List<String> getFinishedNodesIds(WSGraph graph) {
        List<String> finishedNodeIds = new ArrayList<String>();
        for (Node node : graph.getNodes()) {
            if (node.getState() == NodeExecutionState.FINISHED) {
                finishedNodeIds.add(node.getID());
            }
        }
        return finishedNodeIds;
    }

    public static ArrayList<Node> getFailedNodesDynamically(WSGraph graph) {
        return getNodesWithBodyColor(NodeExecutionState.FAILED, graph);
    }

    public static ArrayList<Node> getWaitingNodesDynamically(WSGraph graph) {
        return getNodesWithBodyColor(NodeExecutionState.WAITING, graph);
    }

    public static ArrayList<Node> getNodesWithBodyColor(NodeExecutionState state, WSGraph graph) {
        ArrayList<Node> list = new ArrayList<Node>();
        List<NodeImpl> nodes = graph.getNodes();
        for (Node node : nodes) {
            if (node.getState()==state) {
                list.add(node);
            }
        }
        return list;
    }

    public static int getRunningNodeCountDynamically(WSGraph graph) {
        return getNodeCountWithBodyColor(NodeExecutionState.EXECUTING, graph);
    }

    public static int getFailedNodeCountDynamically(WSGraph graph) {
        return getFailedNodesDynamically(graph).size();
    }

    public static int getWaitingNodeCountDynamically(WSGraph graph) {
        return getNodeCountWithBodyColor(NodeExecutionState.WAITING, graph);
    }

    public static int getNodeCountWithBodyColor(NodeExecutionState state, WSGraph graph) {
        int sum = 0;
        List<NodeImpl> nodes = graph.getNodes();
        for (Node node : nodes) {
            if (node.getState()==state) {
                ++sum;
            }
        }
        return sum;
    }

}
