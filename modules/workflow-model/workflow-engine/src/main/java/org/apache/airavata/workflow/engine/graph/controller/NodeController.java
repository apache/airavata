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
package org.apache.airavata.workflow.engine.graph.controller;
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.graph.controller;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.airavata.workflow.model.graph.Edge;
//import org.apache.airavata.workflow.model.graph.Graph;
//import org.apache.airavata.workflow.model.graph.GraphPiece;
//import org.apache.airavata.workflow.model.graph.Node;
//import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
//import org.apache.airavata.workflow.model.graph.Port;
//import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;
//import org.apache.airavata.workflow.model.graph.amazon.TerminateInstanceNode;
//import org.apache.airavata.workflow.model.graph.dynamic.DynamicNode;
//import org.apache.airavata.workflow.model.graph.subworkflow.SubWorkflowNode;
//import org.apache.airavata.workflow.model.graph.system.BlockNode;
//import org.apache.airavata.workflow.model.graph.system.ConstantNode;
//import org.apache.airavata.workflow.model.graph.system.DifferedInputNode;
//import org.apache.airavata.workflow.model.graph.system.DoWhileNode;
//import org.apache.airavata.workflow.model.graph.system.EndBlockNode;
//import org.apache.airavata.workflow.model.graph.system.EndDoWhileNode;
//import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
//import org.apache.airavata.workflow.model.graph.system.EndifNode;
//import org.apache.airavata.workflow.model.graph.system.ExitNode;
//import org.apache.airavata.workflow.model.graph.system.ForEachNode;
//import org.apache.airavata.workflow.model.graph.system.IfNode;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.graph.system.MemoNode;
//import org.apache.airavata.workflow.model.graph.system.OutputNode;
//import org.apache.airavata.workflow.model.graph.system.ReceiveNode;
//import org.apache.airavata.workflow.model.graph.system.S3InputNode;
//import org.apache.airavata.workflow.model.graph.system.StreamSourceNode;
//import org.apache.airavata.workflow.model.graph.ws.WSNode;
//import org.apache.airavata.workflow.model.graph.ws.WorkflowNode;
//import org.apache.airavata.xbaya.ui.graph.EdgeGUI;
//import org.apache.airavata.xbaya.ui.graph.GraphGUI;
//import org.apache.airavata.xbaya.ui.graph.GraphPieceGUI;
//import org.apache.airavata.xbaya.ui.graph.NodeGUI;
//import org.apache.airavata.xbaya.ui.graph.PortGUI;
//import org.apache.airavata.xbaya.ui.graph.amazon.InstanceNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.amazon.TerminateInstanceNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.dynamic.DynamicNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.subworkflow.SubWorkflowNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.BlockNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.ConstantNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.DifferedInputNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.DoWhileNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.EndBlockNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.EndDoWhileNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.EndForEachNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.EndifNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.ExitNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.ForEachNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.IfNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.InputNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.MemoNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.OutputNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.ReceiveNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.S3InputNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.system.StreamSourceNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.ws.WSNodeGUI;
//import org.apache.airavata.xbaya.ui.graph.ws.WorkflowNodeGUI;
//
//public class NodeController {
//	private static Map<GraphPiece,GraphPieceGUI> nodeMap=new HashMap<GraphPiece, GraphPieceGUI>();
////	private static Map<Port,PortGUI> portMap=new HashMap<Port, PortGUI>();
//
//	public static GraphPieceGUI getGUI(GraphPiece node){
//		if (!nodeMap.containsKey(node)){
//			nodeMap.put(node,createNodeGUI(node));
//		}
//		return nodeMap.get(node);
//	}
//
//	public static GraphGUI getGUI(Graph node){
//		return (GraphGUI)getGUI((GraphPiece)node);
//	}
//
//	public static NodeGUI getGUI(Node node){
//		return (NodeGUI)getGUI((GraphPiece)node);
//	}
//
//	public static EdgeGUI getGUI(Edge port){
//		return (EdgeGUI)getGUI((GraphPiece)port);
//	}
//
//	public static PortGUI getGUI(Port port){
//		return (PortGUI)getGUI((GraphPiece)port);
//	}
//
////	public static PortGUI getGUI(Port node){
////		if (!portMap.containsKey(node)){
////			portMap.put(node,createPortGUI(node));
////		}
////		return portMap.get(node);
////	}
////
////	private static PortGUI createPortGUI(Port port){
////		PortGUI portGUI=new PortGUI(port);
////		return portGUI;
////	}
//
//	private static GraphPieceGUI createNodeGUI(GraphPiece node){
//		GraphPieceGUI nodeGUI=null;
//		if (node instanceof SubWorkflowNode){
//		    nodeGUI=new SubWorkflowNodeGUI((SubWorkflowNode)node);
//		} else if (node instanceof DynamicNode){
//		    nodeGUI=new DynamicNodeGUI((DynamicNode)node);
//		} else if (node instanceof ConstantNode){
//		    nodeGUI=new ConstantNodeGUI((ConstantNode)node);
//		} else if (node instanceof IfNode){
//		    nodeGUI=new IfNodeGUI((IfNode)node);
//		} else if (node instanceof ExitNode){
//		    nodeGUI=new ExitNodeGUI((ExitNode)node);
//		} else if (node instanceof OutputNode){
//		    nodeGUI=new OutputNodeGUI((OutputNode)node);
//		} else if (node instanceof DifferedInputNode){
//		    nodeGUI=new DifferedInputNodeGUI((DifferedInputNode)node);
//		} else if (node instanceof BlockNode){
//		    nodeGUI=new BlockNodeGUI((BlockNode)node);
//		} else if (node instanceof EndForEachNode){
//		    nodeGUI=new EndForEachNodeGUI((EndForEachNode)node);
//		} else if (node instanceof S3InputNode){
//		    nodeGUI=new S3InputNodeGUI((S3InputNode)node);
//		} else if (node instanceof ForEachNode){
//		    nodeGUI=new ForEachNodeGUI((ForEachNode)node);
//		}else if (node instanceof DoWhileNode){
//		    nodeGUI=new DoWhileNodeGUI((DoWhileNode)node);
//		} else if (node instanceof EndDoWhileNode){
//		    nodeGUI=new EndDoWhileNodeGUI((EndDoWhileNode)node);
//		}  else if (node instanceof MemoNode){
//		    nodeGUI=new MemoNodeGUI((MemoNode)node);
//		} else if (node instanceof ReceiveNode){
//		    nodeGUI=new ReceiveNodeGUI((ReceiveNode)node);
//		} else if (node instanceof InputNode){
//		    nodeGUI=new InputNodeGUI((InputNode)node);
//		} else if (node instanceof EndifNode){
//		    nodeGUI=new EndifNodeGUI((EndifNode)node);
//		} else if (node instanceof EndBlockNode){
//		    nodeGUI=new EndBlockNodeGUI((EndBlockNode)node);
//		} else if (node instanceof WorkflowNode){
//		    nodeGUI=new WorkflowNodeGUI((WorkflowNode)node);
//		} else if (node instanceof WSNode){
//		    nodeGUI=new WSNodeGUI((WSNode)node);
////		} else if (node instanceof Graph){
////		    nodeGUI=new GraphGUI((Graph)node);
////		} else if (node instanceof GraphPiece){
////		    nodeGUI=new GraphPieceGUI((GraphPiece)node);
//		} else if (node instanceof Port){
//		    nodeGUI=new PortGUI((Port)node);
//		} else if (node instanceof Edge){
//		    nodeGUI=new EdgeGUI((Edge)node);
//		} else if (node instanceof TerminateInstanceNode){
//		    nodeGUI=new TerminateInstanceNodeGUI((TerminateInstanceNode)node);
//		} else if (node instanceof InstanceNode){
//		    nodeGUI=new InstanceNodeGUI((InstanceNode)node);
//		} else if (node instanceof StreamSourceNode){
//		    nodeGUI=new StreamSourceNodeGUI((StreamSourceNode)node);
//		} else if (node instanceof Graph){
//		    nodeGUI=new GraphGUI((Graph)node);
//		}
//
//		return nodeGUI;
//	}
//
//	public static boolean isFinished(Node node){
//		return node.getState() == NodeExecutionState.FINISHED;
//	}
//	public static boolean isWaiting(Node node){
//		return node.getState() == NodeExecutionState.WAITING;
//	}
//	public static boolean isRunning(Node node){
//		return node.getState() == NodeExecutionState.EXECUTING;
//	}
//	public static boolean isFailed(Node node){
//		return node.getState() == NodeExecutionState.FAILED;
//	}
//}
