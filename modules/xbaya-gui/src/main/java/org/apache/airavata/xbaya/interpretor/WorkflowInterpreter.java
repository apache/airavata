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
//package org.apache.airavata.xbaya.interpretor;
//
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import javax.xml.namespace.QName;
//import javax.xml.xpath.XPath;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;
//
//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.common.utils.Pair;
//import org.apache.airavata.common.utils.StringUtil;
//import org.apache.airavata.common.utils.WSDLUtil;
//import org.apache.airavata.common.utils.XMLUtil;
//import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
//import org.apache.airavata.gfac.ec2.AmazonSecurityContext;
//import org.apache.airavata.registry.api.workflow.WorkflowExecution;
//import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;
//import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
//import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
//import org.apache.airavata.workflow.model.component.Component;
//import org.apache.airavata.workflow.model.component.amazon.InstanceComponent;
//import org.apache.airavata.workflow.model.component.amazon.TerminateInstanceComponent;
//import org.apache.airavata.workflow.model.component.dynamic.DynamicComponent;
//import org.apache.airavata.workflow.model.component.system.ConstantComponent;
//import org.apache.airavata.workflow.model.component.system.DifferedInputComponent;
//import org.apache.airavata.workflow.model.component.system.DoWhileComponent;
//import org.apache.airavata.workflow.model.component.system.EndDoWhileComponent;
//import org.apache.airavata.workflow.model.component.system.EndForEachComponent;
//import org.apache.airavata.workflow.model.component.system.EndifComponent;
//import org.apache.airavata.workflow.model.component.system.ForEachComponent;
//import org.apache.airavata.workflow.model.component.system.IfComponent;
//import org.apache.airavata.workflow.model.component.system.InputComponent;
//import org.apache.airavata.workflow.model.component.system.MemoComponent;
//import org.apache.airavata.workflow.model.component.system.OutputComponent;
//import org.apache.airavata.workflow.model.component.system.S3InputComponent;
//import org.apache.airavata.workflow.model.component.system.SubWorkflowComponent;
//import org.apache.airavata.workflow.model.component.ws.WSComponent;
//import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
//import org.apache.airavata.workflow.model.exceptions.WorkflowException;
//import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
//import org.apache.airavata.workflow.model.graph.ControlPort;
//import org.apache.airavata.workflow.model.graph.DataPort;
//import org.apache.airavata.workflow.model.graph.Node;
//import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
//import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;
//import org.apache.airavata.workflow.model.graph.dynamic.BasicTypeMapping;
//import org.apache.airavata.workflow.model.graph.dynamic.DynamicNode;
//import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
//import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
//import org.apache.airavata.workflow.model.graph.subworkflow.SubWorkflowNode;
//import org.apache.airavata.workflow.model.graph.system.ConstantNode;
//import org.apache.airavata.workflow.model.graph.system.DoWhileNode;
//import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
//import org.apache.airavata.workflow.model.graph.system.EndifNode;
//import org.apache.airavata.workflow.model.graph.system.ForEachNode;
//import org.apache.airavata.workflow.model.graph.system.IfNode;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.graph.system.OutputNode;
//import org.apache.airavata.workflow.model.graph.ws.WSGraph;
//import org.apache.airavata.workflow.model.graph.ws.WSNode;
//import org.apache.airavata.workflow.model.graph.ws.WSPort;
//import org.apache.airavata.workflow.model.ode.ODEClient;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.workflow.model.wf.WorkflowExecutionState;
//import org.apache.airavata.ws.monitor.MonitorException;
//import org.apache.airavata.xbaya.concurrent.PredicatedTaskRunner;
//import org.apache.airavata.xbaya.invoker.DynamicInvoker;
//import org.apache.airavata.xbaya.invoker.EmbeddedGFacInvoker;
//import org.apache.airavata.xbaya.invoker.GenericInvoker;
//import org.apache.airavata.xbaya.invoker.Invoker;
//import org.apache.airavata.xbaya.invoker.WorkflowInputUtil;
//import org.apache.airavata.xbaya.provenance.ProvenanceReader;
//import org.apache.airavata.xbaya.provenance.ProvenanceWrite;
//import org.apache.airavata.xbaya.util.AmazonUtil;
//import org.apache.airavata.xbaya.util.InterpreterUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xmlpull.infoset.XmlElement;
//
//import xsul.lead.LeadResourceMapping;
//import xsul5.XmlConstants;
//
//public class WorkflowInterpreter {
//    private static final Logger log = LoggerFactory.getLogger(WorkflowInterpreter.class);
//
//	public static final String WORKFLOW_STARTED = "Workflow Running";
//	public static final String WORKFLOW_FINISHED = "Workflow Finished";
//
//	private WorkflowInterpreterConfiguration config;
//
//	private Map<Node, Invoker> invokerMap = new HashMap<Node, Invoker>();
//
//	private LeadResourceMapping resourceMapping;
//
//	private PredicatedTaskRunner provenanceWriter;
//
//	private WorkflowInterpreterInteractor interactor;
//
//
//    public static ThreadLocal<WorkflowInterpreterConfiguration> workflowInterpreterConfigurationThreadLocal =
//            new ThreadLocal<WorkflowInterpreterConfiguration>();
//
//    /**
//     *
//     * @param config
//     * @param interactor
//     */
//	public WorkflowInterpreter(WorkflowInterpreterConfiguration config, WorkflowInterpreterInteractor interactor) {
//		this.setConfig(config);
//		config.validateNotifier();
//		this.interactor = interactor;
//		if (config.isActOnProvenance()==null) {
//			config.setActOnProvenance(config.getConfiguration()
//					.isCollectProvenance());
//		}
//		config.setSubWorkflow(false);
//        setWorkflowInterpreterConfigurationThreadLocal(config);
//	}
//
//	public WorkflowInterpreterInteractor getInteractor(){
//		return this.interactor;
//	}
//	public void setResourceMapping(LeadResourceMapping resourceMapping) {
//		this.resourceMapping = resourceMapping;
//	}
//
//	private void notifyViaInteractor(WorkflowExecutionMessage messageType, Object data) {
//		interactor.notify(messageType, config, data);
//	}
//
//	private Object getInputViaInteractor(WorkflowExecutionMessage messageType, Object data) throws Exception {
//		return interactor.retrieveData(messageType, config, data);
//	}
//
//	/**
//	 * @throws WorkflowException
//	 */
//	public void scheduleDynamically() throws WorkflowException {
//		try {
//			if (!this.config.isSubWorkflow() && this.getWorkflow().getExecutionState() != WorkflowExecutionState.NONE) {
//				throw new WorkFlowInterpreterException("XBaya is already running a workflow");
//			}
//
//			this.getWorkflow().setExecutionState(WorkflowExecutionState.RUNNING);
//			ArrayList<Node> inputNodes = this.getInputNodesDynamically();
//			Object[] values = new Object[inputNodes.size()];
//			String[] keywords = new String[inputNodes.size()];
//			for (int i = 0; i < inputNodes.size(); ++i) {
//				Node node = inputNodes.get(i);
//				node.setState(NodeExecutionState.FINISHED);
//				notifyViaInteractor(WorkflowExecutionMessage.NODE_STATE_CHANGED, null);
//				keywords[i] = ((InputNode) node).getName();
//				values[i] = ((InputNode) node).getDefaultValue();
//                //Saving workflow input Node data before running the workflow
//                WorkflowNodeType workflowNodeType = new WorkflowNodeType();
//                workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.INPUTNODE);
//                WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(new WorkflowExecution(getConfig().getTopic(),
//                        getConfig().getTopic()), node.getID());
//                this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowInstanceNodeInput(workflowInstanceNode, keywords[i] + "=" + (String) values[i]);
//                this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowNodeType(workflowInstanceNode, workflowNodeType);
//			}
//			this.config.getNotifier().workflowStarted(values, keywords);
//			this.config.getConfiguration().setContextHeader(WorkflowContextHeaderBuilder.getCurrentContextHeader());
//
//			while (this.getWorkflow().getExecutionState() != WorkflowExecutionState.STOPPED) {
//                ArrayList<Node> readyNodes = this.getReadyNodesDynamically();
//                ArrayList<Thread> threadList = new ArrayList<Thread>();
//                if (getRemainNodesDynamically() == 0) {
//                    notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED, WorkflowExecutionState.STOPPED);
//                }
//                // ok we have paused sleep
//                if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.PAUSED) {
//                	log.info("Workflow execution "+config.getTopic()+" is paused.");
//	                while (this.getWorkflow().getExecutionState() == WorkflowExecutionState.PAUSED) {
//	                    try {
//	                        Thread.sleep(400);
//	                    } catch (InterruptedException e) {
//	                        e.printStackTrace();
//	                    }
//	                }
//	                if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.STOPPED) {
//	                	continue;
//	                }
//	                log.info("Workflow execution "+config.getTopic()+" is resumed.");
//                }
//                // get task list and execute them
//				for (final Node node : readyNodes) {
//					if (node.isBreak()) {
//						this.notifyPause();
//						break;
//					}
//					if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.PAUSED
//							|| this.getWorkflow().getExecutionState() == WorkflowExecutionState.STOPPED) {
//						break;
//					}
//
//                    // Since this is an independent node execution we can run these nodes in separate threads.
//                    Thread th = new Thread() {
//                        public synchronized void run() {
//                            try {
//                                executeDynamically(node);
//                            } catch (WorkflowException e) {
//                                log.error("Error execution workflow Node : " + node.getID());
//                                return;
//                            }
//                        }
//                    };
//                    threadList.add(th);
//                    th.start();
//					if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.STEP) {
//						this.getWorkflow().setExecutionState(WorkflowExecutionState.PAUSED);
//						break;
//					}
//				}
//                //This thread waits until parallel nodes get finished to send the outputs dynamically.
//               for(Thread th:threadList){
//                   try {
//                       th.join();
//                   } catch (InterruptedException e) {
//                       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                   }
//               }
//                // Above statement set the nodeCount back to 0.
//
//				// TODO commented this for foreach, fix this.
//				sendOutputsDynamically();
//				// Dry run sleep a lil bit to release load
//				if (readyNodes.size() == 0) {
//					// when there are no ready nodes and no running nodes
//					// and there are failed nodes then workflow is stuck because
//					// of failure
//					// so we should pause the execution
//					if (InterpreterUtil.getRunningNodeCountDynamically(this.getGraph()) == 0
//							/**&& InterpreterUtil.getFailedNodeCountDynamically(this.getGraph()) != 0**/) {
//                        //Since airavata only support workflow interpreter server mode we do not want to keep thread in sleep mode
//                        // continuously, so we make the workflow stop when there's nothing to do.
//						this.getWorkflow().setExecutionState(WorkflowExecutionState.STOPPED);
//					}
//
//					try {
//						Thread.sleep(400);
//					} catch (InterruptedException e) {
//						log.error("Workflow Excecution is interrupted !");
//                        return;
//					}
//				}
//			}
//
//			if (InterpreterUtil.getFailedNodeCountDynamically(this.getGraph()) == 0) {
//				if (this.config.isActOnProvenance()) {
//
//                    try {
//                        this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowInstanceStatus(
//                                this.config.getTopic(), this.config.getTopic(), State.FINISHED);
//                    } catch (Exception e) {
//                        throw new WorkflowException(e);
//                    }
//
//					// System.out.println(this.config.getConfiguration().getJcrComponentRegistry().getExperimentCatalog().getWorkflowStatus(this.topic));
//				}
//			} else {
//				if (this.config.isActOnProvenance()) {
//					try {
//						this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().
//                                setWorkflowInstanceStatus(this.config.getTopic(),this.config.getTopic(), State.FAILED);
//					} catch (AiravataAPIInvocationException e) {
//						throw new WorkflowException(e);
//					}
//				}
//			}
//
//            this.config.getNotifier().workflowTerminated();
//
//            UUID uuid = UUID.randomUUID();
//			notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_TASK_START, new WorkflowInterpreterInteractor.TaskNotification("Stop Workflow",
//					"Cleaning up resources for Workflow", uuid.toString()));
//			// Send Notification for output values
//			finish();
//			// Sleep to provide for notification delay
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_TASK_END, new WorkflowInterpreterInteractor.TaskNotification("Stop Workflow",
//					"Cleaning up resources for Workflow", uuid.toString()));
//
//		} catch (RuntimeException e) {
//			// we reset all the state
//			cleanup();
//            this.config.getNotifier().workflowFailed(e.getMessage());
//        	raiseException(e);
//		} catch (AiravataAPIInvocationException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }finally{
//        	cleanup();
//			if (config.getNotifier() != null) {
//				this.config.getNotifier().cleanup();
//			}
//			this.getWorkflow().setExecutionState(WorkflowExecutionState.NONE);
//	    }
//    }
//
//	/**
//	 * @param node
//	 * @return
//	 * @throws WorkflowException
//	 * @throws java.io.IOException
//	 */
//	private boolean readProvenance(Node node) {
//
//		try {
//			List<DataPort> inputPorts = node.getInputPorts();
//			Pair<String, String>[] inputs = new Pair[inputPorts.size()];
//			for (int i = 0; i < inputPorts.size(); ++i) {
//				String inputTagname = inputPorts.get(i).getName();
//				// cordinate return
//				if(node instanceof DoWhileNode){
//					inputs[i] = new Pair<String, String>(inputTagname, "<" + inputTagname + ">"
//							+ InterpreterUtil.findInputFromPort(inputPorts.get(i), this.invokerMap).toString() + "</" + inputTagname + ">");
//				break;
//				}else{
//				inputs[i] = new Pair<String, String>(inputTagname, "<" + inputTagname + ">"
//						+ InterpreterUtil.findInputFromPort(inputPorts.get(i), this.invokerMap).toString() + "</" + inputTagname + ">");
//				}
//			}
//
//			String output = ((String) new ProvenanceReader(node, this.config.getTopic(), this.config.getAiravataAPI()).read());
//			if (output != null) {
//				XmlElement result = XMLUtil.stringToXmlElement(output);
//				SystemComponentInvoker invoker = new SystemComponentInvoker();
//				List<DataPort> outPorts = node.getOutputPorts();
//				for (DataPort dataPort : outPorts) {
//
//					Iterable itr = result.children();
//					for (Object outputElem : itr) {
//						if (outputElem instanceof XmlElement) {
//							if (((XmlElement) outputElem).getName().equals(dataPort.getName())) {
//								invoker.addOutput(dataPort.getName(), ((XmlElement) outputElem).children().iterator().next());
//							}
//						}
//					}
//				}
//
//				this.invokerMap.put(node, invoker);
//				node.setState(NodeExecutionState.FINISHED);
//				return true;
//			} else {
////				writeProvenanceLater(node);
//			}
//		} catch (Exception e) {
//			throw new WorkflowRuntimeException(e);
//		}
//		return false;
//
//	}
//
//	/**
//	 * @param node
//	 * @throws WorkflowException
//	 */
//	private void writeProvenanceLater(Node node) throws WorkflowException {
//
//		if (node instanceof ForEachNode) {
//			node = InterpreterUtil.findEndForEachFor((ForEachNode) node);
//		}
//		if (this.provenanceWriter == null) {
//			this.provenanceWriter = new PredicatedTaskRunner(1);
//		}
//		this.provenanceWriter.scedule(new ProvenanceWrite(node, this.getWorkflow().getName(), invokerMap, this.config.getTopic(), this.getConfig()
//				.getConfiguration().getAiravataAPI()));
//	}
//
//	/**
//	 * @param e
//	 */
//	public void raiseException(Throwable e) {
//		notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_ERROR, e);
//	}
//
//	/**
//     *
//     */
//	private void notifyPause() {
//		if (this.getWorkflow().getExecutionState() != WorkflowExecutionState.RUNNING && this.getWorkflow().getExecutionState() != WorkflowExecutionState.STEP) {
//			throw new WorkflowRuntimeException("Cannot pause when not running");
//		}
//		notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED, WorkflowExecutionState.PAUSED);
//	}
//
//	/**
//	 * @throws MonitorException
//	 */
//	public void cleanup() throws MonitorException {
//		this.getWorkflow().setExecutionState(WorkflowExecutionState.STOPPED);
//		notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_CLEANUP, null);
//	}
//
//	private void sendOutputsDynamically() throws WorkflowException, AiravataAPIInvocationException {
//		ArrayList<Node> outputNodes = getReadyOutputNodesDynamically();
//		if (outputNodes.size() != 0) {
//            LinkedList<Object> outputValues = new LinkedList<Object>();
//			LinkedList<String> outputKeywords = new LinkedList<String>();
//			for (Node node : outputNodes) {
//				// Change it to processing state so we will not pic it up in the
//				// next run
//				// even if the next run runs before the notification arrives
//
//				node.setState(NodeExecutionState.EXECUTING);
//				// OutputNode node = (OutputNode) outputNode;
//				List<DataPort> inputPorts = node.getInputPorts();
//
//				for (DataPort dataPort : inputPorts) {
//					Object val = InterpreterUtil.findInputFromPort(dataPort, this.invokerMap);
//					if (null == val) {
//						throw new WorkFlowInterpreterException("Unable to find output for the node:" + node.getID());
//					}
//					// This is ok because the outputnodes always got only one
//					// input
//					if (val instanceof org.xmlpull.v1.builder.XmlElement) {
//						((OutputNode) node).setDescription(XMLUtil.xmlElementToString((org.xmlpull.v1.builder.XmlElement) val));
//					} else {
//						((OutputNode) node).setDescription(val.toString());
//					}
//                    // Saving output Node data in to database
//                    WorkflowNodeType workflowNodeType = new WorkflowNodeType();
//                    workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.OUTPUTNODE);
//                    WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(new WorkflowExecution(config.getTopic(), config.getTopic()), node.getID());
//                    String portname = node.getName();
//                    String portValue = ((OutputNode) node).getDescription();
//                    this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowInstanceNodeOutput(workflowInstanceNode, portname + "=" + portValue);
//                    this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowNodeType(workflowInstanceNode, workflowNodeType);
//
//					if (this.config.isActOnProvenance()) {
//						try {
//							if (val instanceof String) {
//								this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager()
//										.saveWorkflowExecutionOutput(this.config.getTopic(), node.getName(), val.toString());
//							} else if (val instanceof org.xmlpull.v1.builder.XmlElement) {
//								this.getConfig()
//										.getConfiguration()
//										.getAiravataAPI().getProvenanceManager()
//										.saveWorkflowExecutionOutput(this.config.getTopic(), node.getName(),
//												XMLUtil.xmlElementToString((org.xmlpull.v1.builder.XmlElement) val));
//							}
//                            outputValues.add(val);
//                            outputKeywords.add(dataPort.getID());
//						} catch (AiravataAPIInvocationException e) {
//							e.printStackTrace(); // To change body of catch
//													// statement use File |
//													// Settings | File
//													// Templates.
//						}
//					}
//					node.setState(NodeExecutionState.FINISHED);
//				}
//			}
//
//		}
//	}
//
//	private void finish() throws WorkflowException {
//		ArrayList<Node> outoutNodes = new ArrayList<Node>();
//		List<NodeImpl> nodes = this.getGraph().getNodes();
//		for (Node node : nodes) {
//			if (node instanceof OutputNode) {
//				if (node.getInputPort(0).getFromNode().getState()== NodeExecutionState.FINISHED) {
//					outoutNodes.add(node);
//				} else {
//					// The workflow is incomplete so return without sending
//					// workflowFinished
//					return;
//				}
//			}
//		}
//		LinkedList<Object> outputValues = new LinkedList<Object>();
//		LinkedList<String> outputKeywords = new LinkedList<String>();
//		for (Node outputNode : outoutNodes) {
//			OutputNode node = (OutputNode) outputNode;
//			List<DataPort> inputPorts = node.getInputPorts();
//			for (DataPort dataPort : inputPorts) {
//				Object val = InterpreterUtil.findInputFromPort(dataPort, this.invokerMap);
//				;
//
//				if (null == val) {
//					throw new WorkFlowInterpreterException("Unable to find output for the node:" + node.getID());
//				}
//				// Some node not yet updated
//				if (node.getState().equals(NodeExecutionState.FINISHED)) {
//					if (this.config.isActOnProvenance()) {
//						try {
//							if (val instanceof String) {
//                                /**
//                                 TODO :  saveWorkflowExecutionOutput() is not implemented in Registry
//                                  API or Airavata API at the moment
//                                  **/
//								this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager()
//										.saveWorkflowExecutionOutput(this.config.getTopic(), node.getName(), val.toString());
//							} else if (val instanceof org.xmlpull.v1.builder.XmlElement) {
//								this.getConfig()
//										.getConfiguration()
//										.getAiravataAPI().getProvenanceManager()
//										.saveWorkflowExecutionOutput(this.config.getTopic(), node.getName(),
//												XMLUtil.xmlElementToString((org.xmlpull.v1.builder.XmlElement) val));
//							}
//
//						} catch (AiravataAPIInvocationException e) {
//							e.printStackTrace(); // To change body of catch
//													// statement use File |
//													// Settings | File
//													// Templates.
//						}
//					}
//					if (val instanceof XmlElement) {
//						((OutputNode) node).setDescription(XMLUtil.xmlElementToString((XmlElement) val));
//					} else {
//						((OutputNode) node).setDescription(val.toString());
//					}
//                    outputValues.add(val);
//                    outputKeywords.add(dataPort.getID());
//					node.setState(NodeExecutionState.FINISHED);
//				}
//			}
//
//		}
//		this.config.getNotifier().sendingPartialResults(outputValues.toArray(), outputKeywords.toArray(new String[outputKeywords.size()]));
//	}
//
//	private void executeDynamically(final Node node) throws WorkflowException {
//		node.setState(NodeExecutionState.EXECUTING);
//		Component component = node.getComponent();
//		if (component instanceof SubWorkflowComponent) {
//			handleSubWorkComponent(node);
//		} else if (component instanceof WSComponent) {
//			handleWSComponent(node);
//		} else if (component instanceof DynamicComponent) {
//			handleDynamicComponent(node);
//		} else if (component instanceof ForEachComponent) {
//			handleForEach(node);
//		} else if (component instanceof IfComponent) {
//			handleIf(node);
//		} else if (component instanceof EndifComponent) {
//			handleEndIf(node);
//		} else if (component instanceof DoWhileComponent) {
//			handleDoWhile(node);
//		}else if (component instanceof EndDoWhileComponent) {
//		// Component is handled in DoWhileHandler after eval condition
//		}
//		else if (component instanceof InstanceComponent) {
//			handleAmazonInstance(node);
//		} else if (component instanceof TerminateInstanceComponent) {
//			handleAmazonTerminateInstance(node);
//		} else {
//            throw new WorkFlowInterpreterException("Encountered Node that cannot be executed:" + node);
//		}
//	}
//
//	private void handleAmazonTerminateInstance(final Node node)
//			throws WorkflowException {
//		Object inputVal = InterpreterUtil.findInputFromPort(node.getInputPort(0), this.invokerMap);
//		String instanceId = inputVal.toString();
//		/*
//		 * Terminate instance
//		 */
//		AmazonUtil.terminateInstances(instanceId);
//
//		// set color to done
//		node.setState(NodeExecutionState.FINISHED);
//	}
//
//	private void handleAmazonInstance(final Node node) {
//		if (config.getAwsAccessKey().isEmpty() || config.getAwsSecretKey().isEmpty()) {
//			throw new WorkFlowInterpreterException("Please set Amazon Credential before using EC2 Instance Component");
//		}
//		for (ControlPort ports : node.getControlOutPorts()) {
//			ports.setConditionMet(true);
//		}
//	}
//
//	private void handleDoWhile(final Node node) {
//		// Executor thread is shutdown inside as thread gets killed when you
//		// shutdown
//		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
//		DoWhileHandler doWhileHandler = new DoWhileHandler((DoWhileNode) node, this.invokerMap, getWaitingNodesDynamically(),
//				getFinishedNodesDynamically(), this, threadExecutor);
//		threadExecutor.submit(doWhileHandler);
//	}
//
//	private void handleSubWorkComponent(Node node) throws WorkflowException {
//		notifyViaInteractor(WorkflowExecutionMessage.OPEN_SUBWORKFLOW, node);
//		// setting the inputs
//		Workflow subWorkflow = ((SubWorkflowNode) node).getWorkflow();
//		ArrayList<Node> subWorkflowInputNodes = getInputNodes(subWorkflow);
//
//		List<DataPort> inputPorts = node.getInputPorts();
//		for (DataPort port : inputPorts) {
//			Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
//			if (null == inputVal) {
//				throw new WorkFlowInterpreterException("Unable to find inputs for the subworkflow node node:" + node.getID());
//			}
//
//			for (Iterator<Node> iterator = subWorkflowInputNodes.iterator(); iterator.hasNext();) {
//				InputNode inputNode = (InputNode) iterator.next();
//				if (inputNode.getName().equals(port.getName())) {
//					inputNode.setDefaultValue(inputVal);
//				}
//			}
//		}
//		for (Iterator<Node> iterator = subWorkflowInputNodes.iterator(); iterator.hasNext();) {
//			InputNode inputNode = (InputNode) iterator.next();
//			if (inputNode.getDefaultValue() == null) {
//				throw new WorkFlowInterpreterException("Input not set for  :" + inputNode.getID());
//			}
//
//		}
//
//		try {
//			WorkflowInterpreter subworkflowInterpreter = (WorkflowInterpreter) getInputViaInteractor(
//					WorkflowExecutionMessage.INPUT_WORKFLOWINTERPRETER_FOR_WORKFLOW, subWorkflow);
//			subworkflowInterpreter.scheduleDynamically();
//		} catch (Exception e) {
//			throw new WorkflowException(e);
//		}
//	}
//
//	protected void handleWSComponent(Node node) throws WorkflowException {
//		WSComponent wsComponent = ((WSComponent) node.getComponent());
//		QName portTypeQName = wsComponent.getPortTypeQName();
//        Invoker invoker = this.invokerMap.get(node);
//
//        // We do this because invokers cannot be cached because the puretls expires
//        if (invoker != null) {
//            this.invokerMap.remove(invoker);
//        }
//
//        final WSNode wsNode = (WSNode) node;
//        String wsdlLocation = InterpreterUtil.getEPR(wsNode);
//        final String gfacURLString = this.getConfig().getConfiguration().getGFacURL().toString();
//        if (null == wsdlLocation) {
//
//            /* If there is a instance control component connects to this
//             * component send information in soap header */
//            for (Node n : wsNode.getControlInPort().getFromNodes()) {
//                if (n instanceof InstanceNode) {
//                    AmazonSecurityContext amazonSecurityContext;
//                    final String awsAccessKeyId = config.getAwsAccessKey();
//                    final String awsSecretKey = config.getAwsSecretKey();
//                    final String username = ((InstanceNode) n).getUsername();
//
//                    if (((InstanceNode) n).isStartNewInstance()) {
//                        final String amiId = ((InstanceNode) n).getIdAsValue();
//                        final String instanceType = ((InstanceNode) n).getInstanceType();
//
//                        amazonSecurityContext =
//                                new AmazonSecurityContext(username, awsAccessKeyId, awsSecretKey, amiId, instanceType);
//                    } else {
//                        final String instanceId = ((InstanceNode) n).getIdAsValue();
//                        amazonSecurityContext =
//                                new AmazonSecurityContext(username, awsAccessKeyId, awsSecretKey, instanceId);
//                    }
//
//                    this.config.getConfiguration().setAmazonSecurityContext(amazonSecurityContext);
//                }
//            }
//
//            if ((this.config.isGfacEmbeddedMode()) || (config.getAwsAccessKey() != null)) {
//                invoker = new EmbeddedGFacInvoker(portTypeQName, WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(wsNode.getComponent().getWSDL()), node.getID(),
//                        this.config.getMessageBoxURL().toASCIIString(), this.config.getMessageBrokerURL().toASCIIString(), this.config.getNotifier(),
//                        this.config.getTopic(), this.config.getAiravataAPI(), portTypeQName.getLocalPart(), this.config.getConfiguration());
//            } else {
//                invoker = new GenericInvoker(portTypeQName, WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(wsNode.getComponent().getWSDL()), node.getID(),
//                        this.config.getMessageBoxURL().toASCIIString(), gfacURLString, this.config.getNotifier());
//            }
//
//        } else {
//			if (wsdlLocation.endsWith("/")) {
//				wsdlLocation = wsdlLocation.substring(0, wsdlLocation.length() - 1);
//			}
//			if (!wsdlLocation.endsWith("?wsdl")) {
//				wsdlLocation += "?wsdl";
//			}
//			invoker = new GenericInvoker(portTypeQName, wsdlLocation, node.getID(), this.getConfig().getConfiguration().getMessageBoxURL().toString(),
//					gfacURLString, this.config.getNotifier());
//		}
//		invoker.setup();
//		this.invokerMap.put(node, invoker);
//		invoker.setOperation(wsComponent.getOperationName());
//
//		// find inputs
//		List<DataPort> inputPorts = node.getInputPorts();
//		ODEClient odeClient = new ODEClient();
//		for (DataPort port : inputPorts) {
//			Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
//
//			/*
//			 * Need to override inputValue if it is odeClient
//			 */
//			if (port.getFromNode() instanceof InputNode) {
//				inputVal = WorkflowInputUtil.parseValue((WSComponentPort) port.getComponentPort(), (String) inputVal);
//			}
//
//			if (null == inputVal) {
//				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
//			}
//			if (port.getFromNode() instanceof EndForEachNode) {
//				inputVal = WorkflowInputUtil.parseValue((WSComponentPort) port.getComponentPort(), (String) inputVal);
//				// org.xmlpull.v1.builder.XmlElement inputElem = XMLUtil
//				// .stringToXmlElement3("<" + port.getName() + ">"
//				// + inputVal.toString() + "</" + port.getName()
//				// + ">");
//				// inputVal = inputElem;
//			}
//			invoker.setInput(port.getName(), inputVal);
//		}
//		invoker.invoke();
//	}
//
//	private void handleDynamicComponent(Node node) throws WorkflowException {
//		DynamicComponent dynamicComponent = (DynamicComponent) node.getComponent();
//		String className = dynamicComponent.getClassName();
//		String operationName = dynamicComponent.getOperationName();
//		URL implJarLocation = dynamicComponent.getImplJarLocation();
//		DynamicNode dynamicNode = (DynamicNode) node;
//		LinkedList<Object> inputs = new LinkedList<Object>();
//		List<DataPort> inputPorts = dynamicNode.getInputPorts();
//		for (DataPort dataPort : inputPorts) {
//			Object inputVal = InterpreterUtil.findInputFromPort(dataPort, this.invokerMap);
//
//			/*
//			 * Set type after get input value, and override inputValue if output
//			 * type is array
//			 */
//			Node fromNode = dataPort.getFromNode();
//			QName type = null;
//			if (fromNode instanceof InputNode) {
//				type = BasicTypeMapping.STRING_QNAME;
//			} else if (fromNode instanceof ConstantNode) {
//				type = ((ConstantNode) fromNode).getType();
//			} else if ((dataPort.getFromPort() instanceof WSPort)
//					&& BasicTypeMapping.isArrayType(((WSPort) dataPort.getFromPort()).getComponentPort().getElement())) {
//				Invoker fromInvoker = this.invokerMap.get(fromNode);
//				inputVal = BasicTypeMapping.getOutputArray(XmlConstants.BUILDER.parseFragmentFromString(fromInvoker.getOutputs().toString()), dataPort
//						.getFromPort().getName(), BasicTypeMapping.getSimpleTypeIndex(((DataPort) dataPort.getFromPort()).getType()));
//				type = ((DataPort) dataPort.getFromPort()).getType();
//			} else {
//				type = ((DataPort) dataPort.getFromPort()).getType();
//			}
//
//			if (null == inputVal) {
//				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
//			}
//			inputs.add(BasicTypeMapping.getObjectOfType(type, inputVal));
//
//		}
//
//		DynamicInvoker dynamicInvoker = new DynamicInvoker(className, implJarLocation, operationName, inputs.toArray());
//		this.invokerMap.put(node, dynamicInvoker);
//		dynamicInvoker.setup();
//		dynamicInvoker.invoke();
//		node.setState(NodeExecutionState.FINISHED);
//	}
//
//	private void handleForEach(Node node) throws WorkflowException {
//		final ForEachNode forEachNode = (ForEachNode) node;
//		EndForEachNode endForEachNode = null;
//		Collection<Node> repeatNodes = node.getOutputPort(0).getToNodes();
//		// we will support only one for now
//		if (repeatNodes.size() != 1) {
//			throw new WorkFlowInterpreterException("Only one node allowed inside foreach");
//		}
//		Iterator<Node> iterator = repeatNodes.iterator();
//		if (iterator.hasNext()) {
//
//			Node middleNode = iterator.next();
//
//			// forEachNode should point to a WSNode and should have only one
//			// output
//			if ((!(middleNode instanceof WSNode)) && (!(middleNode instanceof SubWorkflowNode))) {
//				throw new WorkFlowInterpreterException("Encountered Node inside foreach that is not a WSNode" + middleNode);
//			} else if (middleNode instanceof SubWorkflowNode) {
//				/* Get the EndforEach Node of the Subworkflow */
//				Iterator<Node> subWorkflowOut = middleNode.getOutputPort(0).getToNodes().iterator();
//				while (subWorkflowOut.hasNext()) {
//					Node node2 = subWorkflowOut.next();
//					if (node2 instanceof EndForEachNode) {
//						endForEachNode = (EndForEachNode) node2;
//					}
//				}
//
//				final LinkedList<String> listOfValues = new LinkedList<String>();
//				InterpreterUtil.getInputsForForEachNode(forEachNode, listOfValues, this.invokerMap);
//				final Integer[] inputNumbers = InterpreterUtil.getNumberOfInputsForForEachNode(forEachNode, this.invokerMap);
//				Workflow workflow1 = ((SubWorkflowNode) middleNode).getWorkflow();
//				List<NodeImpl> nodes = workflow1.getGraph().getNodes();
//				List<Node> wsNodes = new ArrayList<Node>();
//				/* Take the List of WSNodes in the subworkflow */
//				for (NodeImpl subWorkflowNode : nodes) {
//					if (subWorkflowNode instanceof WSNode) {
//						wsNodes.add(subWorkflowNode);
//					}
//				}
//
//				for (int i = 0; i < wsNodes.size(); i++) {
//					final WSNode node1 = (WSNode) wsNodes.get(i);
//					SystemComponentInvoker systemInvoker = null;
//					List<DataPort> outputPorts1 = node1.getOutputPorts();
//					List<Node> endForEachNodes = new ArrayList<Node>();
//					for (DataPort port : outputPorts1) {
//						Iterator<Node> endForEachNodeItr1 = port.getToNodes().iterator();
//						while (endForEachNodeItr1.hasNext()) {
//							Node node2 = endForEachNodeItr1.next();
//							if (node2 instanceof EndForEachNode) {
//								endForEachNodes.add(node2);
//							} else if (node2 instanceof OutputNode) {
//								// intentionally left noop
//							} else {
//								throw new WorkFlowInterpreterException("Found More than one node inside foreach");
//							}
//
//						}
//					}
//					final List<Node> finalEndForEachNodes = endForEachNodes;
//
//					Iterator<Node> endForEachNodeItr1 = node1.getOutputPort(0).getToNodes().iterator();
//					while (endForEachNodeItr1.hasNext()) {
//						Node node2 = endForEachNodeItr1.next();
//						// Start reading input came for foreach node
//						int parallelRuns = listOfValues.size() * node1.getOutputPorts().size();
//						if (listOfValues.size() > 0) {
//							forEachNode.setState(NodeExecutionState.EXECUTING);
//							node1.setState(NodeExecutionState.EXECUTING);
//							List<DataPort> outputPorts = node1.getOutputPorts();
//							final AtomicInteger counter = new AtomicInteger();
//							for (Node endFor : endForEachNodes) {
//								systemInvoker = new SystemComponentInvoker();
//								this.invokerMap.put(endFor, systemInvoker);
//							}
//							final Map<Node, Invoker> finalMap = this.invokerMap;
//							new Thread() {
//								@Override
//								public void run() {
//									try {
//										runInThread(listOfValues, forEachNode, node1, finalEndForEachNodes, finalMap, counter, inputNumbers);
//									} catch (WorkflowException e) {
//
//										WorkflowInterpreter.this.config.getGUI().getErrorWindow().error(e);
//									}
//								}
//
//							}.start();
//
//							while (counter.intValue() < parallelRuns) {
//								try {
//									Thread.sleep(100);
//								} catch (InterruptedException e) {
//									Thread.currentThread().interrupt();
//								}
//
//							}
//							if (!(node2 instanceof OutputNode)) {
//								listOfValues.removeAll(listOfValues);
//								String output = (String) systemInvoker.getOutput(node1.getOutputPort(0).getName());
//								XmlElement xmlElement = XMLUtil.stringToXmlElement("<result>" + output + "</result>");
//								Iterator iterator1 = xmlElement.children().iterator();
//								while (iterator1.hasNext()) {
//									Object next1 = iterator1.next();
//									if (next1 instanceof XmlElement) {
//										listOfValues.add((String) ((XmlElement) next1).children().iterator().next());
//									}
//								}
//							}
//						}
//					}
//				}
//				// we have finished execution so end foreach is finished
//				// todo this has to be done in a separate thread
//				endForEachNode.setState(NodeExecutionState.FINISHED);
//				middleNode.setState(NodeExecutionState.FINISHED);
//				node.setState(NodeExecutionState.FINISHED);
//
//			} else {
//
//				// First node after foreach should end with EndForEachNode
//				List<DataPort> outputPorts1 = middleNode.getOutputPorts();
//				List<Node> endForEachNodes = new ArrayList<Node>();
//				for (DataPort port : outputPorts1) {
//					Iterator<Node> endForEachNodeItr1 = port.getToNodes().iterator();
//					while (endForEachNodeItr1.hasNext()) {
//						Node node2 = endForEachNodeItr1.next();
//						if (node2 instanceof EndForEachNode) {
//							endForEachNodes.add(node2);
//						} else if (node2 instanceof OutputNode) {
//							// intentionally left noop
//						} else {
//							throw new WorkFlowInterpreterException("Found More than one node inside foreach");
//						}
//
//					}
//				}
//				final List<Node> finalEndForEachNodes = endForEachNodes;
//				final Node foreachWSNode = middleNode;
//				final LinkedList<String> listOfValues = new LinkedList<String>();
//
//				// Start reading input came for foreach node
//				InterpreterUtil.getInputsForForEachNode(forEachNode, listOfValues, this.invokerMap);
//				final Integer[] inputNumbers = InterpreterUtil.getNumberOfInputsForForEachNode(forEachNode, this.invokerMap);
//
//				int parallelRuns = createInputValues(listOfValues, inputNumbers).size() * outputPorts1.size();
//				if (listOfValues.size() > 0) {
//
//					forEachNode.setState(NodeExecutionState.EXECUTING);
//					foreachWSNode.setState(NodeExecutionState.EXECUTING);
//					List<DataPort> outputPorts = middleNode.getOutputPorts();
//					final AtomicInteger counter = new AtomicInteger();
//					for (Node endFor : endForEachNodes) {
//						final SystemComponentInvoker systemInvoker = new SystemComponentInvoker();
//						this.invokerMap.put(endFor, systemInvoker);
//					}
//					final Map<Node, Invoker> finalInvokerMap = this.invokerMap;
//
//					new Thread() {
//						@Override
//						public void run() {
//							try {
//								runInThread(listOfValues, forEachNode, foreachWSNode, finalEndForEachNodes, finalInvokerMap, counter, inputNumbers);
//							} catch (WorkflowException e) {
//								WorkflowInterpreter.this.config.getGUI().getErrorWindow().error(e);
//							}
//						}
//
//					}.start();
//					while (counter.intValue() < parallelRuns) {
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							Thread.currentThread().interrupt();
//						}
//
//					}
//					// we have finished execution so end foreach is finished
//					// todo this has to be done in a separate thread
//					middleNode.setState(NodeExecutionState.FINISHED);
//					for (Node endForEach : endForEachNodes) {
//						endForEach.setState(NodeExecutionState.FINISHED);
//					}
//				} else {
//					throw new WorkFlowInterpreterException("No array values found for foreach");
//				}
//
//			}
//		}
//	}
//
//	private void handleIf(Node node) throws WorkflowException {
//		IfNode ifNode = (IfNode) node;
//
//		/*
//		 * Get all input to check condition
//		 */
//		String booleanExpression = ifNode.getXPath();
//		if (booleanExpression == null) {
//			throw new WorkFlowInterpreterException("XPath for if cannot be null");
//		}
//
//		List<DataPort> inputPorts = node.getInputPorts();
//		int i = 0;
//		for (DataPort port : inputPorts) {
//			Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
//
//			if (null == inputVal) {
//				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
//			}
//
//			booleanExpression = booleanExpression.replaceAll("\\$" + i, "'" + inputVal + "'");
//		}
//
//		// Now the XPath expression
//		try {
//			XPathFactory xpathFact = XPathFactory.newInstance();
//			XPath xpath = xpathFact.newXPath();
//			Boolean result = (Boolean) xpath.evaluate(booleanExpression, booleanExpression, XPathConstants.BOOLEAN);
//
//			/*
//			 * Set control port to make execution flow continue according to
//			 * condition
//			 */
//			for (ControlPort controlPort : node.getControlOutPorts()) {
//				if (controlPort.getName().equals(IfComponent.TRUE_PORT_NAME)) {
//					controlPort.setConditionMet(result.booleanValue());
//				} else if (controlPort.getName().equals(IfComponent.FALSE_PORT_NAME)) {
//					controlPort.setConditionMet(!result.booleanValue());
//				}
//			}
//
//			node.setState(NodeExecutionState.FINISHED);
//
//		} catch (XPathExpressionException e) {
//			throw new WorkFlowInterpreterException("Cannot evaluate XPath in If Condition: " + booleanExpression);
//		}
//	}
//
//	private void handleEndIf(Node node) throws WorkflowException {
//		EndifNode endIfNode = (EndifNode) node;
//		SystemComponentInvoker invoker = new SystemComponentInvoker();
//
//		List<DataPort> ports = endIfNode.getOutputPorts();
//		for (int outputPortIndex = 0, inputPortIndex = 0; outputPortIndex < ports.size(); outputPortIndex++, inputPortIndex = inputPortIndex + 2) {
//
//			Object inputVal = InterpreterUtil.findInputFromPort(endIfNode.getInputPort(inputPortIndex), this.invokerMap);
//
//			Object inputVal2 = InterpreterUtil.findInputFromPort(endIfNode.getInputPort(inputPortIndex + 1), this.invokerMap);
//
//			if ((inputVal == null && inputVal2 == null) || (inputVal != null && inputVal2 != null)) {
//				throw new WorkFlowInterpreterException("EndIf gets wrong input number" + "Port:" + inputPortIndex + " and " + (inputPortIndex + 1));
//			}
//
//			Object value = inputVal != null ? inputVal : inputVal2;
//			invoker.addOutput(endIfNode.getOutputPort(outputPortIndex).getID(), value);
//		}
//
//		this.invokerMap.put(node, invoker);
//
//		node.setState(NodeExecutionState.FINISHED);
//	}
//
//	private Invoker createInvokerForEachSingleWSNode(Node foreachWSNode, String gfacURLString, WSComponent wsComponent) throws WorkflowException {
//		Invoker invoker;
//		String wsdlLocation = InterpreterUtil.getEPR((WSNode) foreachWSNode);
//		QName portTypeQName = wsComponent.getPortTypeQName();
//		if (null == wsdlLocation) {
//            // WorkflowInterpreter is no longer using gfac in service mode. we only support embedded mode.
////			if (gfacURLString.startsWith("https")) {
////				LeadContextHeader leadCtxHeader = null;
////				try {
////					leadCtxHeader = XBayaUtil.buildLeadContextHeader(this.getWorkflow(), this.getConfig().getConfiguration(), new MonitorConfiguration(this
////							.getConfig().getConfiguration().getBrokerURL(), this.config.getTopic(), true, this.getConfig().getConfiguration()
////							.getMessageBoxURL()), foreachWSNode.getID(), null);
////				} catch (URISyntaxException e) {
////					throw new WorkflowException(e);
////				}
////				invoker = new WorkflowInvokerWrapperForGFacInvoker(portTypeQName, gfacURLString, this.getConfig().getConfiguration().getMessageBoxURL()
////						.toString(), leadCtxHeader, this.config.getNotifier().createServiceNotificationSender(foreachWSNode.getID()));
////			} else {
//                if (this.config.isGfacEmbeddedMode()) {
//					invoker = new EmbeddedGFacInvoker(portTypeQName, WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(((WSNode)foreachWSNode).getComponent().getWSDL()), foreachWSNode.getID(),
//							this.config.getMessageBoxURL().toASCIIString(), this.config.getMessageBrokerURL().toASCIIString(), this.config.getNotifier(),
//							this.config.getTopic(), this.config.getAiravataAPI(), portTypeQName.getLocalPart(), this.config.getConfiguration());
//				} else {
//					invoker = new GenericInvoker(portTypeQName, WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(((WSNode)foreachWSNode).getComponent().getWSDL()), foreachWSNode.getID(),
//							this.config.getMessageBoxURL().toASCIIString(), gfacURLString, this.config.getNotifier());
//				}
////			}
//
//		} else {
//			if (wsdlLocation.endsWith("/")) {
//				wsdlLocation = wsdlLocation.substring(0, wsdlLocation.length() - 1);
//			}
//			if (!wsdlLocation.endsWith("?wsdl")) {
//				wsdlLocation += "?wsdl";
//			}
//			invoker = new GenericInvoker(portTypeQName, wsdlLocation, foreachWSNode.getID(), this.getConfig().getConfiguration().getMessageBoxURL().toString(),
//					gfacURLString, this.config.getNotifier());
//		}
//		return invoker;
//	}
//
//	private void runInThread(final LinkedList<String> listOfValues, ForEachNode forEachNode, final Node middleNode, List<Node> endForEachNodes,
//			Map<Node, Invoker> tempInvoker, AtomicInteger counter, final Integer[] inputNumber) throws WorkflowException {
//
//		final LinkedList<Invoker> invokerList = new LinkedList<Invoker>();
//
//		if (inputNumber.length > 1) {
//			List<String> inputValues = createInputValues(listOfValues, inputNumber);
//			for (final Iterator<String> iterator = inputValues.iterator(); iterator.hasNext();) {
//				final String gfacURLString = this.getConfig().getConfiguration().getGFacURL().toString();
//				final String input = iterator.next();
//				WSComponent wsComponent = (WSComponent) middleNode.getComponent();
//				final Invoker invoker2 = createInvokerForEachSingleWSNode(middleNode, gfacURLString, wsComponent);
//				invokerList.add(invoker2);
//
//				new Thread() {
//					@Override
//					public void run() {
//						try {
//							getInvoker(middleNode, invoker2);
//							invokeGFacService(listOfValues, middleNode, inputNumber, input, invoker2);
//
//						} catch (WorkflowException e) {
//							WorkflowInterpreter.this.config.getGUI().getErrorWindow().error(e);
//						}
//					}
//
//				}.start();
//
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					WorkflowInterpreter.this.config.getGUI().getErrorWindow().error(e);
//				}
//			}
//		} else {
//			Invoker invoker = null;
//			for (Iterator<String> iterator = listOfValues.iterator(); iterator.hasNext();) {
//				String input = iterator.next();
//				final String gfacURLString = this.getConfig().getConfiguration().getGFacURL().toString();
//				WSComponent wsComponent = (WSComponent) middleNode.getComponent();
//				invoker = createInvokerForEachSingleWSNode(middleNode, gfacURLString, wsComponent);
//				invokerList.add(invoker);
//				getInvoker(middleNode, invoker);
//
//				// find inputs
//				List<DataPort> inputPorts = middleNode.getInputPorts();
//				for (DataPort port : inputPorts) {
//					Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
//
//					/*
//					 * Handle ForEachNode
//					 */
//					Node fromNode = port.getFromNode();
//					// if (fromNode instanceof ForEachNode) {
//					inputVal = WorkflowInputUtil.parseValue((WSComponentPort) port.getComponentPort(), input);
//					// }
//
//					if (null == inputVal) {
//						throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + middleNode.getID());
//					}
//					invoker.setInput(port.getName(), inputVal);
//				}
//				invoker.invoke();
//			}
//		}
//
//		// String arrayElementName = foreachWSNode.getOperationName() +
//		// "ArrayResponse";
//		// String outputStr = "<" + arrayElementName + ">";
//		// invokerMap size and endForEachNodes size can be difference
//		// because we can create endForEachNode with n number of input/output
//		// ports so always have to use
//		// middleNode.getOutputPorts when iterate
//		String[] outputStr = new String[middleNode.getOutputPorts().size()];
//		int i = 0;
//		for (DataPort port : middleNode.getOutputPorts()) {
//			String outputString = "";
//			for (Iterator<Invoker> iterator = invokerList.iterator(); iterator.hasNext();) {
//				Invoker workflowInvoker = iterator.next();
//
//				// /
//				Object output = workflowInvoker.getOutput(port.getName());
//				if (output instanceof org.xmlpull.v1.builder.XmlElement) {
//					org.xmlpull.v1.builder.XmlElement element = (org.xmlpull.v1.builder.XmlElement) ((org.xmlpull.v1.builder.XmlElement) output).children()
//							.next();
//					outputString += "\n" + XMLUtil.xmlElementToString(element);
//				} else {
//					outputString += "\n<value>" + output + "</value>";
//				}
//				counter.incrementAndGet();
//			}
//			outputStr[i] = outputString;
//			System.out.println(outputStr[i]);
//			i++;
//		}
//		i = 0;
//		// outputStr += "\n</" + arrayElementName + ">";
//		int outputPortIndex = 0;
//		for (DataPort port : middleNode.getOutputPorts()) {
//			for (Node endForEachNode : endForEachNodes) {
//				if (tempInvoker.get(endForEachNode) != null) {
//					if (!(endForEachNode instanceof OutputNode)) {
//						((SystemComponentInvoker) tempInvoker.get(endForEachNode)).addOutput(port.getName(), outputStr[i]);
//					}
//				}
//				outputPortIndex++;
//			}
//			i++;
//		}
//		forEachNode.setState(NodeExecutionState.FINISHED);
//	}
//
//	private void invokeGFacService(LinkedList<String> listOfValues, Node middleNode, Integer[] inputNumber, String input, Invoker invoker)
//			throws WorkflowException {
//
//		// find inputs
//		List<DataPort> inputPorts = middleNode.getInputPorts();
//		String[] inputArray = null;
//		if (inputNumber.length == 1) {
//			inputArray = listOfValues.toArray(new String[listOfValues.size()]);
//		} else {
//			inputArray = StringUtil.getElementsFromString(input);
//		}
//		int index = 0;
//		for (DataPort port : inputPorts) {
//			Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
//			/*
//			 * Handle ForEachNode
//			 */
//			Node fromNode = port.getFromNode();
//			if (fromNode instanceof ForEachNode) {
//				inputVal = inputArray[index++];
//			}
//
//			if (null == inputVal) {
//				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + middleNode.getID());
//			}
//			invoker.setInput(port.getName(), inputVal);
//		}
//		invoker.invoke();
//
//	}
//
//	private Invoker getInvoker(Node middleNode, Invoker invoker) throws WorkflowException {
//		if (middleNode instanceof WSNode) {
//			WSComponent wsComponent = (WSComponent) middleNode.getComponent();
//			invoker.setup();
//			invoker.setOperation(wsComponent.getOperationName());
//		} else if (middleNode instanceof SubWorkflowNode) {
//			// ((SubWorkflowNode) middleNode).getWorkflow();
//			// this.config.getConfiguration();
//			// TODO : Need to create a invoker!
//			// new WorkflowInterpreter()
//		} else {
//			throw new WorkflowRuntimeException("Only Web services and subworkflows are supported for For-Each : Found : " + middleNode);
//		}
//		return invoker;
//	}
//
//	private List<String> createInputValues(LinkedList<String> listOfValues, Integer[] inputNumbers) throws WorkflowException {
//		List<String> inputValues = null;
//		try {
//			inputValues = new ArrayList<String>();
//			if (inputNumbers.length == 1) {
//				return listOfValues;
//			}
//			if (this.config.isRunWithCrossProduct()) {
//				for (int i = 0; i < inputNumbers[0]; i++) {
//					for (int j = 0; j < inputNumbers[1]; j++) {
//						inputValues.add(listOfValues.get(i) + StringUtil.DELIMETER + listOfValues.get(inputNumbers[0] + j));
//					}
//				}
//
//			} else {
//				List<String[]> inputList = new ArrayList<String[]>();
//				int startIndex = 0;
//				for (int input = 0; input < inputNumbers.length; input++) {
//					String[] inputArray = new String[inputNumbers[input]];
//					for (int travers = 0; travers < inputNumbers[input]; travers++) {
//						inputArray[travers] = listOfValues.get(startIndex++);
//					}
//					inputList.add(inputArray);
//				}
//				int inputSize = 1;
//				for (String[] inputArrays : inputList) {
//					if (inputArrays.length != 1) {
//						inputSize = inputArrays.length;
//					}
//				}
//				List<String[]> finalInputList = new ArrayList<String[]>();
//				for (String[] inputArrays : inputList) {
//					if (inputArrays.length == 1) {
//						String[] fullArray = new String[inputSize];
//						for (int i = 0; i < fullArray.length; i++) {
//							fullArray[i] = inputArrays[0];
//						}
//						finalInputList.add(fullArray);
//					} else {
//						finalInputList.add(inputArrays);
//					}
//				}
//				for (int i = 0; i < inputSize; i++) {
//					String inputValue = "";
//					for (String[] array : finalInputList) {
//						inputValue = inputValue + StringUtil.DELIMETER + StringUtil.quoteString(array[i]);
//					}
//					inputValue = inputValue.replaceFirst(StringUtil.DELIMETER , "");
//					inputValues.add(inputValue);
//				}
//
//			}
//		} catch (ArrayIndexOutOfBoundsException e) {
//			throw new WorkflowException("Wrong number of Inputs to For EachNode");
//		}
//		return inputValues;
//	}
//
//	private ArrayList<Node> getReadyOutputNodesDynamically() {
//		ArrayList<Node> list = new ArrayList<Node>();
//		List<NodeImpl> nodes = this.getGraph().getNodes();
//		for (Node node : nodes) {
//			if (node instanceof OutputNode && node.getState()==NodeExecutionState.WAITING
//					&& node.getInputPort(0).getFromNode().getState()== NodeExecutionState.FINISHED) {
//
//				list.add(node);
//			}
//		}
//		return list;
//	}
//
//	private int getRemainNodesDynamically() {
//		return InterpreterUtil.getWaitingNodeCountDynamically(this.getGraph()) + InterpreterUtil.getRunningNodeCountDynamically(this.getGraph());
//	}
//
//	private ArrayList<Node> getInputNodesDynamically() {
//		return getInputNodes(this.getWorkflow());
//	}
//
//	private ArrayList<Node> getInputNodes(Workflow wf) {
//		ArrayList<Node> list = new ArrayList<Node>();
//		List<NodeImpl> nodes = wf.getGraph().getNodes();
//		for (Node node : nodes) {
//			String name = node.getComponent().getName();
//			if (InputComponent.NAME.equals(name) || ConstantComponent.NAME.equals(name) || S3InputComponent.NAME.equals(name)) {
//				list.add(node);
//			}
//		}
//		return list;
//	}
//
//	private ArrayList<Node> getReadyNodesDynamically() {
//		ArrayList<Node> list = new ArrayList<Node>();
//		ArrayList<Node> waiting = InterpreterUtil.getWaitingNodesDynamically(this.getGraph());
//		ArrayList<Node> finishedNodes = InterpreterUtil.getFinishedNodesDynamically(this.getGraph());
//		for (Node node : waiting) {
//			Component component = node.getComponent();
//			if (component instanceof WSComponent
//					|| component instanceof DynamicComponent
//					|| component instanceof SubWorkflowComponent
//					|| component instanceof ForEachComponent
//					|| component instanceof EndForEachComponent
//					|| component instanceof IfComponent
//					|| component instanceof InstanceComponent) {
//
//				/*
//				 * Check for control ports from other node
//				 */
//				ControlPort control = node.getControlInPort();
//				boolean controlDone = true;
//				if (control != null) {
//					for (EdgeImpl edge : control.getEdges()) {
//						controlDone = controlDone && (finishedNodes.contains(edge.getFromPort().getNode())
//						// amazon component use condition met to check
//						// whether the control port is done
//						// FIXME I changed the "||" to a "&&" in the following since thats the only this
//						// that makes sense and if anyone found a scenario it should be otherwise pls fix
//								|| ((ControlPort) edge.getFromPort()).isConditionMet());
//					}
//				}
//
//				/*
//				 * Check for input ports
//				 */
//				List<DataPort> inputPorts = node.getInputPorts();
//				boolean inputsDone = true;
//				for (DataPort dataPort : inputPorts) {
//					inputsDone = inputsDone && finishedNodes.contains(dataPort.getFromNode());
//				}
//				if (inputsDone && controlDone) {
//					list.add(node);
//				}
//			} else if (component instanceof EndifComponent) {
//				/*
//				 * EndIfComponent can run if number of input equals to number of
//				 * output that it expects
//				 */
//				int expectedOutput = node.getOutputPorts().size();
//				int actualInput = 0;
//				List<DataPort> inputPorts = node.getInputPorts();
//				for (DataPort dataPort : inputPorts) {
//					if (finishedNodes.contains(dataPort.getFromNode()))
//						actualInput++;
//				}
//
//				if (expectedOutput == actualInput) {
//					list.add(node);
//				}
//			} else if (component instanceof TerminateInstanceComponent) {
//				/*
//				 * All node connected to controlIn port must be done
//				 */
//				ControlPort control = node.getControlInPort();
//				boolean controlDone = true;
//				if (control != null) {
//					for (EdgeImpl edge : control.getEdges()) {
//						controlDone = controlDone && finishedNodes.contains(edge.getFromPort().getFromNode());
//					}
//				}
//
//				/*
//				 * Check for input ports
//				 */
//				List<DataPort> inputPorts = node.getInputPorts();
//				boolean inputsDone = true;
//				for (DataPort dataPort : inputPorts) {
//					inputsDone = inputsDone && finishedNodes.contains(dataPort.getFromNode());
//				}
//				if (inputsDone && controlDone) {
//					list.add(node);
//				}
//
//			} else if (InputComponent.NAME.equals(component.getName())
//					|| DifferedInputComponent.NAME.equals(component.getName())
//					|| S3InputComponent.NAME.equals(component.getName())
//					|| OutputComponent.NAME.equals(component.getName())
//					|| MemoComponent.NAME.equals(component.getName())
//					|| component instanceof EndDoWhileComponent) {
//				// no op
//			} else if (component instanceof DoWhileComponent) {
//				ControlPort control = node.getControlInPort();
//				boolean controlDone = true;
//				if (control != null) {
//					for (EdgeImpl edge : control.getEdges()) {
//						controlDone = controlDone && finishedNodes.contains(edge.getFromPort().getFromNode());
//					}
//				}
//
//				if (controlDone) {
//					list.add(node);
//				}
//			} else {
//				throw new WorkFlowInterpreterException("Component Not handled :" + component.getName());
//			}
//		}
//
//		notifyViaInteractor(WorkflowExecutionMessage.HANDLE_DEPENDENT_NODES_DIFFERED_INPUTS, this.getGraph());
//
//		return list;
//
//	}
//
//	public Workflow getWorkflow() {
//		return this.config.getWorkflow();
//	}
//
//	public void setProvenanceWriter(PredicatedTaskRunner provenanceWriter) {
//		this.provenanceWriter = provenanceWriter;
//	}
//
//	public WorkflowInterpreterConfiguration getConfig() {
//		return config;
//	}
//
//	public void setConfig(WorkflowInterpreterConfiguration config) {
//		this.config = config;
//	}
//
//	private WSGraph getGraph() {
//		return this.getWorkflow().getGraph();
//	}
//
//	private ArrayList<Node> getFinishedNodesDynamically() {
//		return this.getNodesWithState(NodeExecutionState.FINISHED);
//	}
//
//	private ArrayList<Node> getWaitingNodesDynamically() {
//		return this.getNodesWithState(NodeExecutionState.WAITING);
//	}
//
//	private ArrayList<Node> getNodesWithState(NodeExecutionState state) {
//		ArrayList<Node> list = new ArrayList<Node>();
//		List<NodeImpl> nodes = getGraph().getNodes();
//		for (Node node : nodes) {
//			if (state==node.getState()) {
//				list.add(node);
//			}
//		}
//		return list;
//	}
//
//    public static void setWorkflowInterpreterConfigurationThreadLocal(WorkflowInterpreterConfiguration workflowInterpreterConfiguration) {
//        WorkflowInterpreter.workflowInterpreterConfigurationThreadLocal.set(workflowInterpreterConfiguration);
//    }
//
//    public static WorkflowInterpreterConfiguration getWorkflowInterpreterConfiguration() {
//        return workflowInterpreterConfigurationThreadLocal.get();
//    }
//}