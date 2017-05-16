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
package org.apache.airavata.workflow.engine.interpretor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.airavata.workflow.engine.invoker.Invoker;
import org.apache.airavata.workflow.engine.util.InterpreterUtil;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
import org.apache.airavata.workflow.model.graph.system.DoWhileNode;
import org.apache.airavata.workflow.model.graph.system.EndDoWhileNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoWhileHandler implements Callable<Boolean> {
	private static Logger log = LoggerFactory.getLogger(DoWhileHandler.class);
	private DoWhileNode dowhilenode;
	private Map<Node, Invoker> invokerMap;
	private ArrayList<Node> waitingNode;
	private ArrayList<Node> finishedNodes;
	private WorkflowInterpreter interpreter;
	private ExecutorService threadExecutor;

	/**
	 *
	 * Constructs a DoWhileHandler.
	 *
	 * @param node
	 * @param invokerMap
	 * @param waitingNode
	 * @param finishedNodes
	 * @param interpreter
	 */

	public DoWhileHandler(DoWhileNode node, Map<Node, Invoker> invokerMap, ArrayList<Node> waitingNode, ArrayList<Node> finishedNodes,
			WorkflowInterpreter interpreter, ExecutorService threadExecutor) {
		this.dowhilenode = node;
		this.invokerMap = invokerMap;
		this.waitingNode = waitingNode;
		this.finishedNodes = finishedNodes;
		this.interpreter = interpreter;
		this.threadExecutor = threadExecutor;
	}

	/**
	 * To evaluate dowhile condition with the input values
	 *
	 * @param doWhileNode
	 * @param inputPorts
	 * @param invokerMap
	 * @return boolean value
	 * @throws WorkFlowInterpreterException
	 * @throws XBayaException
	 */
	private boolean evaluate(DoWhileNode doWhileNode, List<DataPort> inputPorts, Map<Node, Invoker> invokerMap) throws WorkFlowInterpreterException,
			WorkflowException {
		String booleanExpression = doWhileNode.getXpath();
		if (booleanExpression == null) {
			throw new WorkFlowInterpreterException("XPath for if cannot be null");
		}

		int i = 0;
		for (DataPort port : inputPorts) {
			Object inputVal1 = InterpreterUtil.findInputFromPort(port, invokerMap);
			if (null == inputVal1) {
				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + doWhileNode.getID());
			}
		    booleanExpression = booleanExpression.replaceAll("\\$" + i, "'" + inputVal1 + "'");
			i++;
		}
		Boolean result = new Boolean(false);
		// Now the XPath expression
		try {
			XPathFactory xpathFact = XPathFactory.newInstance();
			XPath xpath = xpathFact.newXPath();
			result = (Boolean) xpath.evaluate(booleanExpression, booleanExpression, XPathConstants.BOOLEAN);
		} catch (XPathExpressionException e) {
			throw new WorkFlowInterpreterException("Cannot evaluate XPath in If Condition: " + booleanExpression);
		}
		return result.booleanValue();
	}

	/**
	 * To get only web service components attached to dowhile
	 *
	 * @param waitingNode
	 * @return list
	 */
	private ArrayList<Node> handleDowhile(ArrayList<Node> waitingNode, ArrayList<Node> finishedNodes) {
		ArrayList<Node> list = new ArrayList<Node>();
		for (Node node : waitingNode) {
			Component component = node.getComponent();
			if (component instanceof WSComponent) {
				ControlPort control = node.getControlInPort();
				boolean controlDone = true;
				if (control != null) {
					for (EdgeImpl edge : control.getEdges()) {
						controlDone = controlDone && (finishedNodes.contains(edge.getFromPort().getNode())
								|| ((ControlPort) edge.getFromPort()).isConditionMet());
					}
				}

				/*
				 * Check for input ports
				 */
				List<DataPort> inputPorts = node.getInputPorts();
				boolean inputsDone = true;
				for (DataPort dataPort : inputPorts) {
					inputsDone = inputsDone && finishedNodes.contains(dataPort.getFromNode());
				}
				if (inputsDone && controlDone) {
					list.add(node);
				}
			}
		}

		return list;
	}

	/**
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() throws Exception {
		log.debug("Invoked Dowhile node");
		SystemComponentInvoker dowhileinvoker = new SystemComponentInvoker();
		// TODO check for multiple input case
		Object inputVal1 = InterpreterUtil.findInputFromPort(this.dowhilenode.getInputPort(0), this.invokerMap);
		dowhileinvoker.addOutput(this.dowhilenode.getOutputPort(0).getID(), inputVal1);
		this.invokerMap.put(this.dowhilenode, dowhileinvoker);
		this.finishedNodes.add(this.dowhilenode);

		ArrayList<Node> readyNodes = this.handleDowhile(this.waitingNode, this.finishedNodes);

		// When you are starting 1st time its getting input from 1st node and
		// invoking all the webservice components
		if (readyNodes.size() != 1) {
			throw new WorkflowRuntimeException("More than one dowhile execution not supported");
		}
		Node donode = readyNodes.get(0);
		this.interpreter.handleWSComponent(donode);
		log.debug("Invoked service " + donode.getName());

		List<DataPort> inputPorts = this.dowhilenode.getInputPorts();
		boolean runflag = true;
		while (runflag) {
//			while (true) {
//				if (NodeController.isRunning(donode) || NodeController.isWaiting(donode)) {
//					Thread.sleep(500);
//					log.debug("Service " + donode.getName() + " waiting");
//				} else if (NodeController.isFinished(donode)) {
//					log.debug("Service " + donode.getName() + " Finished");
//					List<DataPort> ports = this.dowhilenode.getOutputPorts();
//					for (int outputPortIndex = 0, inputPortIndex = 1; outputPortIndex < ports.size(); outputPortIndex++) {
//						Object inputValue = InterpreterUtil.findInputFromPort(this.dowhilenode.getInputPort(inputPortIndex), this.invokerMap);
//						dowhileinvoker.addOutput(this.dowhilenode.getOutputPort(outputPortIndex).getID(), inputValue);
//					}
//					break;
//				} else if (NodeController.isFailed(donode)) {
//					log.debug("Service " + donode.getName() + " Failed");
//					runflag = false;
//					dowhilenode.setState(NodeExecutionState.FAILED);
//					this.threadExecutor.shutdown();
//					return false;
//				} else if (donode.isBreak()) {
//					log.debug("Service " + donode.getName() + " set to break");
//					runflag = false;
//					break;
//				} else {
//					log.error("Service " + donode.getName() + " have unknow status");
//					throw new WorkFlowInterpreterException("Unknow status of the node");
//				}
//			}

//			this.invokerMap.put(this.dowhilenode, dowhileinvoker);
			log.debug("Going to evaluate do while expression for " + donode.getName());
			if (!evaluate(this.dowhilenode, inputPorts, this.invokerMap)) {
				log.debug("Expression evaluation is false so calling EndDoWhile");
				runflag = false;
			} else {
				if (readyNodes.size() != 1) {
					throw new WorkFlowInterpreterException("More than one dowhile execution not supported");
				}

				Node whileNode = readyNodes.get(0);
				log.debug("Expression evaluation is true so invoking service again " + whileNode.getName());

				this.interpreter.handleWSComponent(whileNode);
			}
		}
		// WS node should be done
		dowhilenode.setState(NodeExecutionState.FINISHED);
		EndDoWhileNode endDoWhileNode = this.dowhilenode.getEndDoWhileNode();

		// /////////////////////////////////////////////////////////
		// // Do WHile finished execution thus we can set the //////
		// //inputs to the EndDOWHile and resume the executions/////
		SystemComponentInvoker invoker = new SystemComponentInvoker();

		List<DataPort> inputports = endDoWhileNode.getInputPorts();

		for (int inputPortIndex = 0; inputPortIndex < inputports.size(); inputPortIndex++) {
			Object inputVal = dowhileinvoker.getOutput(inputports.get(inputPortIndex).getFromPort().getID());
			invoker.addOutput(endDoWhileNode.getOutputPort(inputPortIndex).getID(), inputVal);
		}

		this.invokerMap.put(endDoWhileNode, invoker);
		// TODO send mail once the iterations have converged

		endDoWhileNode.setState(NodeExecutionState.FINISHED);
		this.threadExecutor.shutdown();
		return true;
	}
}
