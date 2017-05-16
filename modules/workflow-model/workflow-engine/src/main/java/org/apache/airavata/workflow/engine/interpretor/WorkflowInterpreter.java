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

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.workflow.engine.invoker.DynamicInvoker;
import org.apache.airavata.workflow.engine.invoker.Invoker;
import org.apache.airavata.workflow.engine.util.AmazonUtil;
import org.apache.airavata.workflow.engine.util.InterpreterUtil;
import org.apache.airavata.workflow.engine.util.ProxyMonitorPublisher;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.amazon.InstanceComponent;
import org.apache.airavata.workflow.model.component.amazon.TerminateInstanceComponent;
import org.apache.airavata.workflow.model.component.dynamic.DynamicComponent;
import org.apache.airavata.workflow.model.component.system.*;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
import org.apache.airavata.workflow.model.graph.dynamic.BasicTypeMapping;
import org.apache.airavata.workflow.model.graph.dynamic.DynamicNode;
import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.subworkflow.SubWorkflowNode;
import org.apache.airavata.workflow.model.graph.system.*;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowExecutionState;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkflowInterpreter implements AbstractActivityListener{
    private static final Logger log = LoggerFactory.getLogger(WorkflowInterpreter.class);

	public static final String WORKFLOW_STARTED = "Workflow Running";
	public static final String WORKFLOW_FINISHED = "Workflow Finished";
    private final Publisher publisher;

    private WorkflowInterpreterConfiguration config;

	private Map<Node, Invoker> invokerMap = new HashMap<Node, Invoker>();

	private List<Node> invokedNode = new ArrayList<Node>();
	
	private WorkflowInterpreterInteractor interactor;

	private Map<Node,ProcessModel> nodeInstanceList;

	private ExperimentModel experiment;
	private ExperimentCatalog experimentCatalog;

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    private String gatewayId;

	private OrchestratorService.Client orchestratorClient;
	
	private Map<String, Node> awaitingTasks;
	private Map<Node, Map<String,String>> nodeOutputData;
	
    public static ThreadLocal<WorkflowInterpreterConfiguration> workflowInterpreterConfigurationThreadLocal =
            new ThreadLocal<WorkflowInterpreterConfiguration>();

    private String credentialStoreToken;
    /**
     * @param experiment
     * @param credentialStoreToken
     * @param config
     * @param orchestratorClient
     */
	public WorkflowInterpreter(ExperimentModel experiment, String credentialStoreToken,
                               WorkflowInterpreterConfiguration config, OrchestratorService.Client orchestratorClient, Publisher publisher) {
		this.setConfig(config);
		this.setExperiment(experiment);
		this.setCredentialStoreToken(credentialStoreToken);
		this.interactor = new SSWorkflowInterpreterInteractorImpl();
		this.orchestratorClient = orchestratorClient;
        this.publisher = publisher;
        // if gateway id is not set, we will get it from airavata server properties
        if (gatewayId == null){
            try {
                gatewayId = ServerSettings.getDefaultUserGateway();
            } catch (ApplicationSettingsException e) {
                log.error("error while reading airavata-server properties..", e);
            }
        }
		//TODO set act of provenance
		nodeInstanceList=new HashMap<Node, WorkflowNodeDetails>();
        setWorkflowInterpreterConfigurationThreadLocal(config);
        awaitingTasks = new HashMap<String, Node>();
        nodeOutputData = new HashMap<Node, Map<String,String>>();
        ProxyMonitorPublisher.registerListener(this);
	}

	public WorkflowInterpreterInteractor getInteractor(){
		return this.interactor;
	}

	private void notifyViaInteractor(WorkflowExecutionMessage messageType, Object data) {
		interactor.notify(messageType, config, data);
	}

	private Object getInputViaInteractor(WorkflowExecutionMessage messageType, Object data) throws Exception {
		return interactor.retrieveData(messageType, config, data);
	}
	
	private ExperimentCatalog getExperimentCatalog() throws RegistryException{
		if (experimentCatalog ==null){
			experimentCatalog = RegistryFactory.getDefaultExpCatalog();
		}
		return experimentCatalog;
//		return new TmpRegistry();
	}
	
	private void updateWorkflowNodeStatus(WorkflowNodeDetails node, WorkflowNodeState state) throws RegistryException{
		WorkflowNodeStatus status = ExperimentModelUtil.createWorkflowNodeStatus(state);
		node.setWorkflowNodeStatus(status);
		getExperimentCatalog().update(ExperimentCatalogModelType.WORKFLOW_NODE_STATUS, status, node.getNodeInstanceId());
	}
	
	private void updateExperimentStatus(ExperimentState state){
//		node.setWorkflowNodeStatus(ExperimentModelUtil.createWorkflowNodeStatus(state));
		//TODO trigger node status update
	}
	
	/**
	 * @throws WorkflowException
	 * @throws RegistryException 
	 */
	public void scheduleDynamically() throws WorkflowException, RegistryException, AiravataException {
		try {
			this.getWorkflow().setExecutionState(WorkflowExecutionState.RUNNING);
			ArrayList<Node> inputNodes = this.getInputNodesDynamically();
			List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
			Map<String,String> inputDataStrings=new HashMap<String, String>();
			for (InputDataObjectType dataObjectType : experimentInputs) {
				inputDataStrings.put(dataObjectType.getName(), dataObjectType.getValue());
			}
			for (Node node : inputNodes) {
                publishNodeStatusChange(WorkflowNodeState.EXECUTING,node.getID(),experiment.getExperimentID());
				if (inputDataStrings.containsKey(node.getID())){
					((InputNode)node).setDefaultValue(inputDataStrings.get(node.getID()));
				} else {
					log.warn("value for node not found "+node.getName());
				}
			}
			for (int i = 0; i < inputNodes.size(); ++i) {
				Node node = inputNodes.get(i);
				invokedNode.add(node);
				node.setState(NodeExecutionState.FINISHED);
                publishNodeStatusChange(WorkflowNodeState.INVOKED, node.getID(), experiment.getExperimentID());
				notifyViaInteractor(WorkflowExecutionMessage.NODE_STATE_CHANGED, null);
				String portId= ((InputNode) node).getID();
				Object portValue = ((InputNode) node).getDefaultValue();
				DataType dataType = ((InputNode) node).getDataType();
				//Saving workflow input Node data before running the workflow
				WorkflowNodeDetails workflowNode = createWorkflowNodeDetails(node);
                InputDataObjectType elem = new InputDataObjectType();
				elem.setName(portId);
				elem.setValue(portValue == null ? null : portValue.toString());
				elem.setType(dataType);
				workflowNode.addToNodeInputs(elem);
				getExperimentCatalog().update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, workflowNode, workflowNode.getNodeInstanceId());
				updateWorkflowNodeStatus(workflowNode, WorkflowNodeState.COMPLETED);
                publishNodeStatusChange(WorkflowNodeState.COMPLETED, node.getID(), experiment.getExperimentID());
			}

			while (this.getWorkflow().getExecutionState() != WorkflowExecutionState.STOPPED) {
                ArrayList<Thread> threadList = new ArrayList<Thread>();
                if (getRemainNodesDynamically() == 0) {
                    notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED, WorkflowExecutionState.STOPPED);
                }
                // ok we have paused sleep
                if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.PAUSED) {
                	log.info("Workflow execution "+experiment.getExperimentID()+" is paused.");
	                while (this.getWorkflow().getExecutionState() == WorkflowExecutionState.PAUSED) {
	                    try {
	                        Thread.sleep(400);
	                    } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
	                    }
	                }
	                if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.STOPPED) {
	                	continue;
	                }
	                log.info("Workflow execution "+experiment.getExperimentID()+" is resumed.");
                }
                // get task list and execute them
                ArrayList<Node> readyNodes = this.getReadyNodesDynamically();
				for (final Node node : readyNodes) {
					if (node.isBreak()) {
						this.notifyPause();
						break;
					}
					if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.PAUSED
							|| this.getWorkflow().getExecutionState() == WorkflowExecutionState.STOPPED) {
						break;
					}
                	WorkflowNodeDetails workflowNodeDetails = createWorkflowNodeDetails(node);
                    // Since this is an independent node execution we can run these nodes in separate threads.
                    Thread th = new Thread() {
                        public synchronized void run() {
                            try {
                                executeDynamically(node);
                            } catch (WorkflowException e) {
                                log.error("Error execution workflow Node : " + node.getID());
                                return;
                            } catch (TException e) {
                                log.error(e.getMessage(), e);
							} catch (RegistryException e) {
                                log.error(e.getMessage(), e);
							} catch (AiravataException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    };
                	updateWorkflowNodeStatus(workflowNodeDetails, WorkflowNodeState.INVOKED);
                    publishNodeStatusChange(WorkflowNodeState.INVOKED, node.getID(), experiment.getExperimentID());
                    threadList.add(th);
                    th.start();
					if (this.getWorkflow().getExecutionState() == WorkflowExecutionState.STEP) {
						this.getWorkflow().setExecutionState(WorkflowExecutionState.PAUSED);
						//TODO update experiment state to suspend
						break;
					}
				}
                //This thread waits until parallel nodes get finished to send the outputs dynamically.
               for(Thread th:threadList){
                   try {
                       th.join();
                   } catch (InterruptedException e) {
                       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                   }
               }
                // Above statement set the nodeCount back to 0.

				// TODO commented this for foreach, fix this.
				sendOutputsDynamically();
				// Dry run sleep a lil bit to release load
				if (readyNodes.size() == 0) {
					// when there are no ready nodes and no running nodes
					// and there are failed nodes then workflow is stuck because
					// of failure
					// so we should pause the execution
					if (InterpreterUtil.getRunningNodeCountDynamically(this.getGraph()) == 0
							/**&& InterpreterUtil.getFailedNodeCountDynamically(this.getGraph()) != 0**/) {
                        //Since airavata only support workflow interpreter server mode we do not want to keep thread in sleep mode
                        // continuously, so we make the workflow stop when there's nothing to do.
						this.getWorkflow().setExecutionState(WorkflowExecutionState.STOPPED);
					}

					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						log.error("Workflow Excecution is interrupted !");
                        return;
					}
				}
			}

			if (InterpreterUtil.getFailedNodeCountDynamically(this.getGraph()) == 0) {
				if (this.config.isActOnProvenance()) {

                    try {
//                        this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowInstanceStatus(
//                                this.config.getTopic(), this.config.getTopic(), State.FINISHED);
                        updateExperimentStatus(ExperimentState.COMPLETED);
                    } catch (Exception e) {
                        throw new WorkflowException(e);
                    }

					// System.out.println(this.config.getConfiguration().getJcrComponentRegistry().getExperimentCatalog().getWorkflowStatus(this.topic));
				}
			} else {
				if (this.config.isActOnProvenance()) {
					//						this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().
//                                setWorkflowInstanceStatus(this.config.getTopic(),this.config.getTopic(), State.FAILED);
					updateExperimentStatus(ExperimentState.FAILED);
				}
			}

            UUID uuid = UUID.randomUUID();
			notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_TASK_START, new WorkflowInterpreterInteractor.TaskNotification("Stop Workflow",
					"Cleaning up resources for Workflow", uuid.toString()));
			// Send Notification for output values
			finish();
			// Sleep to provide for notification delay
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
                log.error(e.getMessage(), e);
			}
			notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_TASK_END, new WorkflowInterpreterInteractor.TaskNotification("Stop Workflow",
					"Cleaning up resources for Workflow", uuid.toString()));

		} catch (RuntimeException e) {
			// we reset all the state
			cleanup();
        	raiseException(e);
		} finally{
        	cleanup();
			this.getWorkflow().setExecutionState(WorkflowExecutionState.NONE);
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.COMPLETED, experiment.getExperimentId(), gatewayId);
            MessageContext msgCtx = new MessageContext(event, MessageType.EXPERIMENT, AiravataUtils.getId("EXPERIMENT"), gatewayId);
            msgCtx.setUpdatedTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
            publisher.publish(msgCtx);
        }
    }

    private void publishNodeStatusChange(ProcessState state, String nodeId , String expId)
            throws AiravataException {
        if (publisher != null) {
            MessageContext msgCtx = new MessageContext(new ProcessStatusChangeEvent(state, new ProcessIdentifier(nodeId,
                    expId, gatewayId)), MessageType.PROCESS, AiravataUtils.getId("NODE"), gatewayId);
            msgCtx.setUpdatedTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
            publisher.publish(msgCtx);
        } else {
            log.warn("Failed to publish workflow status change, publisher is null");
        }
    }

	private WorkflowNodeDetails createWorkflowNodeDetails(Node node) throws RegistryException {
		WorkflowNodeDetails workflowNode = ExperimentModelUtil.createWorkflowNode(node.getName(), null);
		ExecutionUnit executionUnit = ExecutionUnit.APPLICATION;
		String executionData = null;
		if (node instanceof InputNode){
			executionUnit = ExecutionUnit.INPUT;
		} else if (node instanceof OutputNode){
			executionUnit = ExecutionUnit.OUTPUT;
		} if (node instanceof WSNode){
			executionUnit = ExecutionUnit.APPLICATION;
			executionData = ((WSNode)node).getComponent().getApplication().getApplicationId();
		}  
		workflowNode.setExecutionUnit(executionUnit);
		workflowNode.setExecutionUnitData(executionData);
		workflowNode.setNodeInstanceId((String) getExperimentCatalog().add(ExpCatChildDataType.WORKFLOW_NODE_DETAIL, workflowNode, getExperiment().getExperimentID()));
		nodeInstanceList.put(node, workflowNode);
		setupNodeDetailsInput(node, workflowNode);
		return workflowNode;
	}

	/**
	 * @param e
	 */
	public void raiseException(Throwable e) {
		notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_ERROR, e);
	}

	/**
     *
     */
	private void notifyPause() {
		if (this.getWorkflow().getExecutionState() != WorkflowExecutionState.RUNNING && this.getWorkflow().getExecutionState() != WorkflowExecutionState.STEP) {
			throw new WorkflowRuntimeException("Cannot pause when not running");
		}
		notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_STATE_CHANGED, WorkflowExecutionState.PAUSED);
	}

	public void cleanup() {
		this.getWorkflow().setExecutionState(WorkflowExecutionState.STOPPED);
		notifyViaInteractor(WorkflowExecutionMessage.EXECUTION_CLEANUP, null);
	}

	private void sendOutputsDynamically() throws WorkflowException, RegistryException, AiravataException {
		ArrayList<Node> outputNodes = getReadyOutputNodesDynamically();
		if (outputNodes.size() != 0) {
            LinkedList<Object> outputValues = new LinkedList<Object>();
			LinkedList<String> outputKeywords = new LinkedList<String>();
			for (Node node : outputNodes) {
				// Change it to processing state so we will not pic it up in the
				// next run
				// even if the next run runs before the notification arrives
				WorkflowNodeDetails workflowNodeDetails = createWorkflowNodeDetails(node);
//				workflowNodeDetails.setNodeInstanceId((String)getExperimentCatalog().add(ChildDataType.WORKFLOW_NODE_DETAIL, workflowNodeDetails, getExperiment().getExperimentID()));
				node.setState(NodeExecutionState.EXECUTING);
				updateWorkflowNodeStatus(workflowNodeDetails, WorkflowNodeState.EXECUTING);
                publishNodeStatusChange(WorkflowNodeState.EXECUTING, node.getID(), experiment.getExperimentID());
				// OutputNode node = (OutputNode) outputNode;
				List<DataPort> inputPorts = node.getInputPorts();

				for (DataPort dataPort : inputPorts) {
					Object val = InterpreterUtil.findInputFromPort(dataPort);
					if (null == val) {
						throw new WorkFlowInterpreterException("Unable to find output for the node:" + node.getID());
					}
					// This is ok because the outputnodes always got only one
					// input
					if (val instanceof org.xmlpull.v1.builder.XmlElement) {
						((OutputNode) node).setDescription(XMLUtil.xmlElementToString((org.xmlpull.v1.builder.XmlElement) val));
					} else {
						((OutputNode) node).setDescription(val.toString());
					}
                    // Saving output Node data in to database
//                    WorkflowNodeType workflowNodeType = new WorkflowNodeType();
//                    workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.OUTPUTNODE);
//                    WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(new WorkflowExecution(config.getTopic(), config.getTopic()), node.getID());
                    String portname = node.getName();
                    String portValue = ((OutputNode) node).getDescription();
//                    this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowInstanceNodeOutput(workflowInstanceNode, portname + "=" + portValue);
//                    this.getConfig().getConfiguration().getAiravataAPI().getProvenanceManager().setWorkflowNodeType(workflowInstanceNode, workflowNodeType);
                    OutputDataObjectType elem = new OutputDataObjectType();
                    elem.setName(portname);
                    elem.setValue(portValue);
					workflowNodeDetails.addToNodeOutputs(elem);
					getExperimentCatalog().update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetails, workflowNodeDetails.getNodeInstanceId());
					if (this.config.isActOnProvenance()) {
						//TODO do provanence thing
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
					}
				}
				node.setState(NodeExecutionState.FINISHED);
                publishNodeStatusChange(WorkflowNodeState.COMPLETED, node.getID(), experiment.getExperimentID());
                updateWorkflowNodeStatus(workflowNodeDetails, WorkflowNodeState.COMPLETED);
				notifyViaInteractor(WorkflowExecutionMessage.NODE_STATE_CHANGED, null);
			}

		}
	}

	private void finish() throws WorkflowException, RegistryException {
		ArrayList<Node> outoutNodes = new ArrayList<Node>();
		List<NodeImpl> nodes = this.getGraph().getNodes();
		for (Node node : nodes) {
			if (node instanceof OutputNode) {
				if (node.getInputPort(0).getFromNode().getState()== NodeExecutionState.FINISHED) {
					outoutNodes.add(node);
				} else {
					// The workflow is incomplete so return without sending
					// workflowFinished
					return;
				}
			}
		}
		LinkedList<Object> outputValues = new LinkedList<Object>();
		LinkedList<String> outputKeywords = new LinkedList<String>();
		for (Node outputNode : outoutNodes) {
			OutputNode node = (OutputNode) outputNode;
			List<DataPort> inputPorts = node.getInputPorts();
			for (DataPort dataPort : inputPorts) {
				Object val = InterpreterUtil.findInputFromPort(dataPort, this.invokerMap);
				;

				if (null == val) {
					throw new WorkFlowInterpreterException("Unable to find output for the node:" + node.getID());
				}
				WorkflowNodeDetails workflowNodeDetails = nodeInstanceList.get(node);
                OutputDataObjectType elem = new OutputDataObjectType();
				elem.setName(node.getName());
				elem.setValue(val.toString());
				workflowNodeDetails.addToNodeOutputs(elem);
				try {
					getExperimentCatalog().update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetails, workflowNodeDetails.getNodeInstanceId());
				} catch (RegistryException e) {
					log.error(e.getMessage(), e);
				}
				updateWorkflowNodeStatus(workflowNodeDetails, WorkflowNodeState.COMPLETED);
			}

		}
	}

	private void executeDynamically(final Node node) throws WorkflowException, TException, RegistryException, AiravataException {
		node.setState(NodeExecutionState.EXECUTING);
        invokedNode.add(node);
        updateWorkflowNodeStatus(nodeInstanceList.get(node), WorkflowNodeState.EXECUTING);
        publishNodeStatusChange(WorkflowNodeState.EXECUTING, node.getID(), experiment.getExperimentID());
        Component component = node.getComponent();
		if (component instanceof SubWorkflowComponent) {
			handleSubWorkComponent(node);
		} else if (component instanceof WSComponent) {
			handleWSComponent(node);
		} else if (component instanceof DynamicComponent) {
			handleDynamicComponent(node);
		} else if (component instanceof ForEachComponent) {
			handleForEach(node);
		} else if (component instanceof IfComponent) {
			handleIf(node);
		} else if (component instanceof EndifComponent) {
			handleEndIf(node);
		} else if (component instanceof DoWhileComponent) {
			handleDoWhile(node);
		}else if (component instanceof EndDoWhileComponent) {
		// Component is handled in DoWhileHandler after eval condition
		}
		else if (component instanceof InstanceComponent) {
			handleAmazonInstance(node);
		} else if (component instanceof TerminateInstanceComponent) {
			handleAmazonTerminateInstance(node);
		} else {
            throw new WorkFlowInterpreterException("Encountered Node that cannot be executed:" + node);
		}
	}

	private void handleAmazonTerminateInstance(final Node node)
			throws WorkflowException {
		Object inputVal = InterpreterUtil.findInputFromPort(node.getInputPort(0), this.invokerMap);
		String instanceId = inputVal.toString();
		/*
		 * Terminate instance
		 */
		AmazonUtil.terminateInstances(instanceId);

		// set color to done
		node.setState(NodeExecutionState.FINISHED);
	}

	private void handleAmazonInstance(final Node node) {
		//TODO
		for (ControlPort ports : node.getControlOutPorts()) {
			ports.setConditionMet(true);
		}
	}

	private void handleDoWhile(final Node node) {
		// Executor thread is shutdown inside as thread gets killed when you
		// shutdown
		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		DoWhileHandler doWhileHandler = new DoWhileHandler((DoWhileNode) node, this.invokerMap, getWaitingNodesDynamically(),
				getFinishedNodesDynamically(), this, threadExecutor);
		threadExecutor.submit(doWhileHandler);
	}

	private void handleSubWorkComponent(Node node) throws WorkflowException {
		//TODO we will not support this in 0.13
		notifyViaInteractor(WorkflowExecutionMessage.OPEN_SUBWORKFLOW, node);
		// setting the inputs
		Workflow subWorkflow = ((SubWorkflowNode) node).getWorkflow();
		ArrayList<Node> subWorkflowInputNodes = getInputNodes(subWorkflow);

		List<DataPort> inputPorts = node.getInputPorts();
		for (DataPort port : inputPorts) {
			Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
			if (null == inputVal) {
				throw new WorkFlowInterpreterException("Unable to find inputs for the subworkflow node node:" + node.getID());
			}

			for (Iterator<Node> iterator = subWorkflowInputNodes.iterator(); iterator.hasNext();) {
				InputNode inputNode = (InputNode) iterator.next();
				if (inputNode.getName().equals(port.getName())) {
					inputNode.setDefaultValue(inputVal);
				}
			}
		}
		for (Iterator<Node> iterator = subWorkflowInputNodes.iterator(); iterator.hasNext();) {
			InputNode inputNode = (InputNode) iterator.next();
			if (inputNode.getDefaultValue() == null) {
				throw new WorkFlowInterpreterException("Input not set for  :" + inputNode.getID());
			}
		}
		try {
			WorkflowInterpreter subworkflowInterpreter = (WorkflowInterpreter) getInputViaInteractor(
					WorkflowExecutionMessage.INPUT_WORKFLOWINTERPRETER_FOR_WORKFLOW, subWorkflow);
			subworkflowInterpreter.scheduleDynamically();
		} catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	private OrchestratorService.Client getOrchestratorClient(){
		return orchestratorClient;
	}
	
	protected void handleWSComponent(Node node) throws WorkflowException, TException, RegistryException {
        TaskDetails taskDetails = createTaskDetails(node);
        log.debug("Launching task , node = " + node.getName() + " node id = " + node.getID());
        getOrchestratorClient().launchTask(taskDetails.getTaskID(), getCredentialStoreToken());
	}
	
	private void addToAwaitingTaskList(String taskId, Node node){
		synchronized (awaitingTasks) {
			awaitingTasks.put(taskId, node);
		}
	}
	
	private boolean isTaskAwaiting(String taskId){
		boolean result;
		synchronized (awaitingTasks) {
			result = awaitingTasks.containsKey(taskId);
		}
		return result;
	}

	private Node getAwaitingNodeForTask(String taskId){
		Node node;
		synchronized (awaitingTasks) {
			node = awaitingTasks.get(taskId);
		}
		return node;
	}
	
	private void removeAwaitingTask(String taskId){
		synchronized (awaitingTasks) {
			awaitingTasks.remove(taskId);
		}
	}
	private void handleDynamicComponent(Node node) throws WorkflowException {
		DynamicComponent dynamicComponent = (DynamicComponent) node.getComponent();
		String className = dynamicComponent.getClassName();
		String operationName = dynamicComponent.getOperationName();
		URL implJarLocation = dynamicComponent.getImplJarLocation();
		DynamicNode dynamicNode = (DynamicNode) node;
		LinkedList<Object> inputs = new LinkedList<Object>();
		List<DataPort> inputPorts = dynamicNode.getInputPorts();
		for (DataPort dataPort : inputPorts) {
			Object inputVal = InterpreterUtil.findInputFromPort(dataPort, this.invokerMap);

			/*
			 * Set type after get input value, and override inputValue if output
			 * type is array
			 */
			Node fromNode = dataPort.getFromNode();
			DataType type = null;
			if (fromNode instanceof InputNode) {
				type = DataType.STRING;
			} else if (fromNode instanceof ConstantNode) {
				type = ((ConstantNode) fromNode).getType();
			} else if ((dataPort.getFromPort() instanceof WSPort)
					&& BasicTypeMapping.isArrayType(((WSPort) dataPort.getFromPort()).getComponentPort().getElement())) {
				Invoker fromInvoker = this.invokerMap.get(fromNode);
//				inputVal = BasicTypeMapping.getOutputArray(XmlConstants.BUILDER.parseFragmentFromString(fromInvoker.getOutputs().toString()), dataPort
//						.getFromPort().getName(), BasicTypeMapping.getSimpleTypeIndex(((DataPort) dataPort.getFromPort()).getType()));
				type = ((DataPort) dataPort.getFromPort()).getType();
			} else {
				type = ((DataPort) dataPort.getFromPort()).getType();
			}

			if (null == inputVal) {
				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
			}
//			inputs.add(BasicTypeMapping.getObjectOfType(type, inputVal));

		}

		DynamicInvoker dynamicInvoker = new DynamicInvoker(className, implJarLocation, operationName, inputs.toArray());
		this.invokerMap.put(node, dynamicInvoker);
		dynamicInvoker.setup();
		dynamicInvoker.invoke();
		node.setState(NodeExecutionState.FINISHED);
	}

	private void handleForEach(Node node) throws WorkflowException {
		final ForEachNode forEachNode = (ForEachNode) node;
		EndForEachNode endForEachNode = null;
		Collection<Node> repeatNodes = node.getOutputPort(0).getToNodes();
		// we will support only one for now
		if (repeatNodes.size() != 1) {
			throw new WorkFlowInterpreterException("Only one node allowed inside foreach");
		}
		Iterator<Node> iterator = repeatNodes.iterator();
		if (iterator.hasNext()) {

			Node middleNode = iterator.next();

			// forEachNode should point to a WSNode and should have only one
			// output
			if ((!(middleNode instanceof WSNode)) && (!(middleNode instanceof SubWorkflowNode))) {
				throw new WorkFlowInterpreterException("Encountered Node inside foreach that is not a WSNode" + middleNode);
			} else if (middleNode instanceof SubWorkflowNode) {
				/* Get the EndforEach Node of the Subworkflow */
				Iterator<Node> subWorkflowOut = middleNode.getOutputPort(0).getToNodes().iterator();
				while (subWorkflowOut.hasNext()) {
					Node node2 = subWorkflowOut.next();
					if (node2 instanceof EndForEachNode) {
						endForEachNode = (EndForEachNode) node2;
					}
				}

				final LinkedList<String> listOfValues = new LinkedList<String>();
				InterpreterUtil.getInputsForForEachNode(forEachNode, listOfValues, this.invokerMap);
				final Integer[] inputNumbers = InterpreterUtil.getNumberOfInputsForForEachNode(forEachNode, this.invokerMap);
				Workflow workflow1 = ((SubWorkflowNode) middleNode).getWorkflow();
				List<NodeImpl> nodes = workflow1.getGraph().getNodes();
				List<Node> wsNodes = new ArrayList<Node>();
				/* Take the List of WSNodes in the subworkflow */
				for (NodeImpl subWorkflowNode : nodes) {
					if (subWorkflowNode instanceof WSNode) {
						wsNodes.add(subWorkflowNode);
					}
				}

				for (int i = 0; i < wsNodes.size(); i++) {
					final WSNode node1 = (WSNode) wsNodes.get(i);
					SystemComponentInvoker systemInvoker = null;
					List<DataPort> outputPorts1 = node1.getOutputPorts();
					List<Node> endForEachNodes = new ArrayList<Node>();
					for (DataPort port : outputPorts1) {
						Iterator<Node> endForEachNodeItr1 = port.getToNodes().iterator();
						while (endForEachNodeItr1.hasNext()) {
							Node node2 = endForEachNodeItr1.next();
							if (node2 instanceof EndForEachNode) {
								endForEachNodes.add(node2);
							} else if (node2 instanceof OutputNode) {
								// intentionally left noop
							} else {
								throw new WorkFlowInterpreterException("Found More than one node inside foreach");
							}

						}
					}
					final List<Node> finalEndForEachNodes = endForEachNodes;

					Iterator<Node> endForEachNodeItr1 = node1.getOutputPort(0).getToNodes().iterator();
					while (endForEachNodeItr1.hasNext()) {
						Node node2 = endForEachNodeItr1.next();
						// Start reading input came for foreach node
						int parallelRuns = listOfValues.size() * node1.getOutputPorts().size();
						if (listOfValues.size() > 0) {
							forEachNode.setState(NodeExecutionState.EXECUTING);
							node1.setState(NodeExecutionState.EXECUTING);
							List<DataPort> outputPorts = node1.getOutputPorts();
							final AtomicInteger counter = new AtomicInteger();
							for (Node endFor : endForEachNodes) {
								systemInvoker = new SystemComponentInvoker();
								this.invokerMap.put(endFor, systemInvoker);
							}
							final Map<Node, Invoker> finalMap = this.invokerMap;
							new Thread() {
								@Override
								public void run() {
									try {
										runInThread(listOfValues, forEachNode, node1, finalEndForEachNodes, finalMap, counter, inputNumbers);
									} catch (WorkflowException e) {
										log.error(e.getLocalizedMessage(), e);
									} catch (RegistryException e) {
                                        log.error(e.getMessage(), e);
									} catch (TException e) {
                                        log.error(e.getMessage(), e);
									}
								}

							}.start();

							while (counter.intValue() < parallelRuns) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
								}

							}
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
						}
					}
				}
				// we have finished execution so end foreach is finished
				// todo this has to be done in a separate thread
				endForEachNode.setState(NodeExecutionState.FINISHED);
				middleNode.setState(NodeExecutionState.FINISHED);
				node.setState(NodeExecutionState.FINISHED);

			} else {

				// First node after foreach should end with EndForEachNode
				List<DataPort> outputPorts1 = middleNode.getOutputPorts();
				List<Node> endForEachNodes = new ArrayList<Node>();
				for (DataPort port : outputPorts1) {
					Iterator<Node> endForEachNodeItr1 = port.getToNodes().iterator();
					while (endForEachNodeItr1.hasNext()) {
						Node node2 = endForEachNodeItr1.next();
						if (node2 instanceof EndForEachNode) {
							endForEachNodes.add(node2);
						} else if (node2 instanceof OutputNode) {
							// intentionally left noop
						} else {
							throw new WorkFlowInterpreterException("Found More than one node inside foreach");
						}

					}
				}
				final List<Node> finalEndForEachNodes = endForEachNodes;
				final Node foreachWSNode = middleNode;
				final LinkedList<String> listOfValues = new LinkedList<String>();

				// Start reading input came for foreach node
				InterpreterUtil.getInputsForForEachNode(forEachNode, listOfValues, this.invokerMap);
				final Integer[] inputNumbers = InterpreterUtil.getNumberOfInputsForForEachNode(forEachNode, this.invokerMap);

				int parallelRuns = createInputValues(listOfValues, inputNumbers).size() * outputPorts1.size();
				if (listOfValues.size() > 0) {

					forEachNode.setState(NodeExecutionState.EXECUTING);
					foreachWSNode.setState(NodeExecutionState.EXECUTING);
					List<DataPort> outputPorts = middleNode.getOutputPorts();
					final AtomicInteger counter = new AtomicInteger();
					for (Node endFor : endForEachNodes) {
						final SystemComponentInvoker systemInvoker = new SystemComponentInvoker();
						this.invokerMap.put(endFor, systemInvoker);
					}
					final Map<Node, Invoker> finalInvokerMap = this.invokerMap;

					new Thread() {
						@Override
						public void run() {
							try {
								runInThread(listOfValues, forEachNode, foreachWSNode, finalEndForEachNodes, finalInvokerMap, counter, inputNumbers);
							} catch (WorkflowException e) {
								log.error(e.getLocalizedMessage(), e);
							} catch (RegistryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (TException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}.start();
					while (counter.intValue() < parallelRuns) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}

					}
					// we have finished execution so end foreach is finished
					// todo this has to be done in a separate thread
					middleNode.setState(NodeExecutionState.FINISHED);
					for (Node endForEach : endForEachNodes) {
						endForEach.setState(NodeExecutionState.FINISHED);
					}
				} else {
					throw new WorkFlowInterpreterException("No array values found for foreach");
				}

			}
		}
	}

	private void handleIf(Node node) throws WorkflowException {
		IfNode ifNode = (IfNode) node;

		/*
		 * Get all input to check condition
		 */
		String booleanExpression = ifNode.getXPath();
		if (booleanExpression == null) {
			throw new WorkFlowInterpreterException("XPath for if cannot be null");
		}

		List<DataPort> inputPorts = node.getInputPorts();
		int i = 0;
		for (DataPort port : inputPorts) {
			Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);

			if (null == inputVal) {
				throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
			}

			booleanExpression = booleanExpression.replaceAll("\\$" + i, "'" + inputVal + "'");
		}

		// Now the XPath expression
		try {
			XPathFactory xpathFact = XPathFactory.newInstance();
			XPath xpath = xpathFact.newXPath();
			Boolean result = (Boolean) xpath.evaluate(booleanExpression, booleanExpression, XPathConstants.BOOLEAN);

			/*
			 * Set control port to make execution flow continue according to
			 * condition
			 */
			for (ControlPort controlPort : node.getControlOutPorts()) {
				if (controlPort.getName().equals(IfComponent.TRUE_PORT_NAME)) {
					controlPort.setConditionMet(result.booleanValue());
				} else if (controlPort.getName().equals(IfComponent.FALSE_PORT_NAME)) {
					controlPort.setConditionMet(!result.booleanValue());
				}
			}

			node.setState(NodeExecutionState.FINISHED);

		} catch (XPathExpressionException e) {
			throw new WorkFlowInterpreterException("Cannot evaluate XPath in If Condition: " + booleanExpression);
		}
	}

	private void handleEndIf(Node node) throws WorkflowException {
		EndifNode endIfNode = (EndifNode) node;
		SystemComponentInvoker invoker = new SystemComponentInvoker();

		List<DataPort> ports = endIfNode.getOutputPorts();
		for (int outputPortIndex = 0, inputPortIndex = 0; outputPortIndex < ports.size(); outputPortIndex++, inputPortIndex = inputPortIndex + 2) {

			Object inputVal = InterpreterUtil.findInputFromPort(endIfNode.getInputPort(inputPortIndex), this.invokerMap);

			Object inputVal2 = InterpreterUtil.findInputFromPort(endIfNode.getInputPort(inputPortIndex + 1), this.invokerMap);

			if ((inputVal == null && inputVal2 == null) || (inputVal != null && inputVal2 != null)) {
				throw new WorkFlowInterpreterException("EndIf gets wrong input number" + "Port:" + inputPortIndex + " and " + (inputPortIndex + 1));
			}

			Object value = inputVal != null ? inputVal : inputVal2;
			invoker.addOutput(endIfNode.getOutputPort(outputPortIndex).getID(), value);
		}

		this.invokerMap.put(node, invoker);

		node.setState(NodeExecutionState.FINISHED);
	}

	
	private String createInvokerForEachSingleWSNode(Node foreachWSNode, WSComponent wsComponent) throws WorkflowException, RegistryException, TException {
        TaskDetails taskDetails = createTaskDetails(foreachWSNode);
        getOrchestratorClient().launchTask(taskDetails.getTaskID(), getCredentialStoreToken());
		return taskDetails.getTaskID();
	}

	private void setupNodeDetailsInput(Node node, WorkflowNodeDetails nodeDetails){
		List<DataPort> inputPorts = node.getInputPorts();
		for (DataPort dataPort : inputPorts) {
			Node fromNode = dataPort.getFromNode();
			String portInputValue = null;
			if (fromNode instanceof InputNode){
				portInputValue = (String) ((InputNode) fromNode).getDefaultValue();
			} else if (fromNode instanceof WSNode){
				Map<String, String> outputData = nodeOutputData.get(fromNode);
                portInputValue = outputData.get(dataPort.getName());
                if (portInputValue == null) {
                    portInputValue = outputData.get(dataPort.getEdge(0).getFromPort().getName());
                }
			}
			// 123456789
			InputDataObjectType elem = new InputDataObjectType();
			elem.setName(dataPort.getName());
			elem.setValue(portInputValue);
			if (dataPort instanceof WSPort) {
				WSPort port = (WSPort) dataPort;
				elem.setInputOrder(port.getComponentPort().getInputOrder());
				elem.setApplicationArgument(
						(port.getComponentPort().getApplicationArgument() != null ? port.getComponentPort().getApplicationArgument() : ""));
				elem.setType(port.getType());
			}

			nodeDetails.addToNodeInputs(elem);
		}
		try {
			getExperimentCatalog().update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, nodeDetails, nodeDetails.getNodeInstanceId());
		} catch (RegistryException e) {
            log.error(e.getMessage(), e);
		}
	}
	
	private void setupNodeDetailsOutput(Node node){
		WorkflowNodeDetails nodeDetails = nodeInstanceList.get(node);
		List<DataPort> outputPorts = node.getOutputPorts();
		Map<String, String> outputData = nodeOutputData.get(node);
		for (DataPort dataPort : outputPorts) {
			String portInputValue = outputData.get(dataPort.getName());
            OutputDataObjectType elem = new OutputDataObjectType();
			elem.setName(dataPort.getName());
			elem.setValue(portInputValue);
			nodeDetails.addToNodeOutputs(elem);
		}
		try {
			getExperimentCatalog().update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, nodeDetails, nodeDetails.getNodeInstanceId());
		} catch (RegistryException e) {
            log.error(e.getMessage(), e);
		}
	}
	
	private TaskDetails createTaskDetails(Node node)
			throws RegistryException {
		setupNodeDetailsInput(node, nodeInstanceList.get(node));
		TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromWorkflowNodeDetails(experiment, nodeInstanceList.get(node));
        taskDetails.setTaskID(getExperimentCatalog().add(ExpCatChildDataType.TASK_DETAIL, taskDetails,nodeInstanceList.get(node).getNodeInstanceId()).toString());
        addToAwaitingTaskList(taskDetails.getTaskID(), node);
		return taskDetails;
	}

	private void runInThread(final LinkedList<String> listOfValues, ForEachNode forEachNode, final Node middleNode, List<Node> endForEachNodes,
			Map<Node, Invoker> tempInvoker, AtomicInteger counter, final Integer[] inputNumber) throws WorkflowException, RegistryException, TException {

		final LinkedList<String> taskIdList = new LinkedList<String>();

		if (inputNumber.length > 1) {
			List<String> inputValues = createInputValues(listOfValues, inputNumber);
			for (final Iterator<String> iterator = inputValues.iterator(); iterator.hasNext();) {
				final String input = iterator.next();
				WSComponent wsComponent = (WSComponent) middleNode.getComponent();
				final String invoker2 = createInvokerForEachSingleWSNode(middleNode, wsComponent);
				taskIdList.add(invoker2);

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getLocalizedMessage(), e);
				}
			}
		} else {
			String invoker = null;
			for (Iterator<String> iterator = listOfValues.iterator(); iterator.hasNext();) {
				String input = iterator.next();
				WSComponent wsComponent = (WSComponent) middleNode.getComponent();
				taskIdList.add(invoker);

				// find inputs
				List<DataPort> inputPorts = middleNode.getInputPorts();
//				for (DataPort port : inputPorts) {
//					Object inputVal = InterpreterUtil.findInputFromPort(port, this.invokerMap);
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
//				}
				invoker = createInvokerForEachSingleWSNode(middleNode, wsComponent);
			}
		}

		// String arrayElementName = foreachWSNode.getOperationName() +
		// "ArrayResponse";
		// String outputStr = "<" + arrayElementName + ">";
		// invokerMap size and endForEachNodes size can be difference
		// because we can create endForEachNode with n number of input/output
		// ports so always have to use
		// middleNode.getOutputPorts when iterate
		String[] outputStr = new String[middleNode.getOutputPorts().size()];
		int i = 0;
		for (DataPort port : middleNode.getOutputPorts()) {
			String outputString = "";
//			for (Iterator<Invoker> iterator = taskIdList.iterator(); iterator.hasNext();) {
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
			outputStr[i] = outputString;
			System.out.println(outputStr[i]);
			i++;
		}
		i = 0;
		// outputStr += "\n</" + arrayElementName + ">";
		int outputPortIndex = 0;
		for (DataPort port : middleNode.getOutputPorts()) {
			for (Node endForEachNode : endForEachNodes) {
				if (tempInvoker.get(endForEachNode) != null) {
					if (!(endForEachNode instanceof OutputNode)) {
						((SystemComponentInvoker) tempInvoker.get(endForEachNode)).addOutput(port.getName(), outputStr[i]);
					}
				}
				outputPortIndex++;
			}
			i++;
		}
		forEachNode.setState(NodeExecutionState.FINISHED);
	}

	private List<String> createInputValues(LinkedList<String> listOfValues, Integer[] inputNumbers) throws WorkflowException {
		List<String> inputValues = null;
		try {
			inputValues = new ArrayList<String>();
			if (inputNumbers.length == 1) {
				return listOfValues;
			}
			if (this.config.isRunWithCrossProduct()) {
				for (int i = 0; i < inputNumbers[0]; i++) {
					for (int j = 0; j < inputNumbers[1]; j++) {
						inputValues.add(listOfValues.get(i) + StringUtil.DELIMETER + listOfValues.get(inputNumbers[0] + j));
					}
				}

			} else {
				List<String[]> inputList = new ArrayList<String[]>();
				int startIndex = 0;
				for (int input = 0; input < inputNumbers.length; input++) {
					String[] inputArray = new String[inputNumbers[input]];
					for (int travers = 0; travers < inputNumbers[input]; travers++) {
						inputArray[travers] = listOfValues.get(startIndex++);
					}
					inputList.add(inputArray);
				}
				int inputSize = 1;
				for (String[] inputArrays : inputList) {
					if (inputArrays.length != 1) {
						inputSize = inputArrays.length;
					}
				}
				List<String[]> finalInputList = new ArrayList<String[]>();
				for (String[] inputArrays : inputList) {
					if (inputArrays.length == 1) {
						String[] fullArray = new String[inputSize];
						for (int i = 0; i < fullArray.length; i++) {
							fullArray[i] = inputArrays[0];
						}
						finalInputList.add(fullArray);
					} else {
						finalInputList.add(inputArrays);
					}
				}
				for (int i = 0; i < inputSize; i++) {
					String inputValue = "";
					for (String[] array : finalInputList) {
						inputValue = inputValue + StringUtil.DELIMETER + StringUtil.quoteString(array[i]);
					}
					inputValue = inputValue.replaceFirst(StringUtil.DELIMETER , "");
					inputValues.add(inputValue);
				}

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new WorkflowException("Wrong number of Inputs to For EachNode");
		}
		return inputValues;
	}

	private ArrayList<Node> getReadyOutputNodesDynamically() {
		ArrayList<Node> list = new ArrayList<Node>();
		List<NodeImpl> nodes = this.getGraph().getNodes();
		for (Node node : nodes) {
			if (node instanceof OutputNode && node.getState()==NodeExecutionState.WAITING
					&& node.getInputPort(0).getFromNode().getState()== NodeExecutionState.FINISHED) {

				list.add(node);
			}
		}
		return list;
	}

	private int getRemainNodesDynamically() {
		return InterpreterUtil.getWaitingNodeCountDynamically(this.getGraph()) + InterpreterUtil.getRunningNodeCountDynamically(this.getGraph());
	}

	private ArrayList<Node> getInputNodesDynamically() {
		return getInputNodes(this.getWorkflow());
	}

	private ArrayList<Node> getInputNodes(Workflow wf) {
		ArrayList<Node> list = new ArrayList<Node>();
		List<NodeImpl> nodes = wf.getGraph().getNodes();
		for (Node node : nodes) {
			String name = node.getComponent().getName();
			if (InputComponent.NAME.equals(name) || ConstantComponent.NAME.equals(name) || S3InputComponent.NAME.equals(name)) {
				list.add(node);
			}
		}
		return list;
	}

	private ArrayList<Node> getReadyNodesDynamically() {
		ArrayList<Node> list = new ArrayList<Node>();
		ArrayList<Node> waiting = InterpreterUtil.getWaitingNodesDynamically(this.getGraph());
//		ArrayList<Node> finishedNodes = InterpreterUtil.getFinishedNodesDynamically(this.getGraph());
        // This is to support repeat the same application in the workflow.
        List<String> finishedNodeIds = InterpreterUtil.getFinishedNodesIds(this.getGraph());
        for (Node node : waiting) {
			Component component = node.getComponent();
			if (component instanceof WSComponent
					|| component instanceof DynamicComponent
					|| component instanceof SubWorkflowComponent
					|| component instanceof ForEachComponent
					|| component instanceof EndForEachComponent
					|| component instanceof IfComponent
					|| component instanceof InstanceComponent) {

				/*
				 * Check for control ports from other node
				 */
				ControlPort control = node.getControlInPort();
				boolean controlDone = true;
				if (control != null) {
					for (EdgeImpl edge : control.getEdges()) {
                        controlDone = controlDone && (finishedNodeIds.contains(edge.getFromPort().getNode().getID())
                                // amazon component use condition met to check
                                // whether the control port is done
                                // FIXME I changed the "||" to a "&&" in the following since thats the only this
                                // that makes sense and if anyone found a scenario it should be otherwise pls fix
                                || ((ControlPort) edge.getFromPort()).isConditionMet());
                    }
                }

				/*
				 * Check for input ports
				 */
				List<DataPort> inputPorts = node.getInputPorts();
				boolean inputsDone = true;
				for (DataPort dataPort : inputPorts) {
					inputsDone = inputsDone && finishedNodeIds.contains(dataPort.getFromNode().getID());
				}
				if (inputsDone && controlDone) {
					list.add(node);
				}
			} else if (component instanceof EndifComponent) {
				/*
				 * EndIfComponent can run if number of input equals to number of
				 * output that it expects
				 */
				int expectedOutput = node.getOutputPorts().size();
				int actualInput = 0;
				List<DataPort> inputPorts = node.getInputPorts();
				for (DataPort dataPort : inputPorts) {
					if (finishedNodeIds.contains(dataPort.getFromNode().getID()))
						actualInput++;
				}

				if (expectedOutput == actualInput) {
					list.add(node);
				}
			} else if (component instanceof TerminateInstanceComponent) {
				/*
				 * All node connected to controlIn port must be done
				 */
				ControlPort control = node.getControlInPort();
				boolean controlDone = true;
				if (control != null) {
					for (EdgeImpl edge : control.getEdges()) {
						controlDone = controlDone && finishedNodeIds.contains(edge.getFromPort().getFromNode().getID());
					}
				}

				/*
				 * Check for input ports
				 */
				List<DataPort> inputPorts = node.getInputPorts();
				boolean inputsDone = true;
				for (DataPort dataPort : inputPorts) {
					inputsDone = inputsDone && finishedNodeIds.contains(dataPort.getFromNode().getID());
				}
				if (inputsDone && controlDone) {
					list.add(node);
				}

			} else if (InputComponent.NAME.equals(component.getName())
					|| DifferedInputComponent.NAME.equals(component.getName())
					|| S3InputComponent.NAME.equals(component.getName())
					|| OutputComponent.NAME.equals(component.getName())
					|| MemoComponent.NAME.equals(component.getName())
					|| component instanceof EndDoWhileComponent) {
				// no op
			} else if (component instanceof DoWhileComponent) {
				ControlPort control = node.getControlInPort();
				boolean controlDone = true;
				if (control != null) {
					for (EdgeImpl edge : control.getEdges()) {
						controlDone = controlDone && finishedNodeIds.contains(edge.getFromPort().getFromNode().getID());
					}
				}

				if (controlDone) {
					list.add(node);
				}
			} else {
				throw new WorkFlowInterpreterException("Component Not handled :" + component.getName());
			}
		}

		notifyViaInteractor(WorkflowExecutionMessage.HANDLE_DEPENDENT_NODES_DIFFERED_INPUTS, this.getGraph());

		return list;

	}

	public Workflow getWorkflow() {
		return this.config.getWorkflow();
	}

	public WorkflowInterpreterConfiguration getConfig() {
		return config;
	}

	public void setConfig(WorkflowInterpreterConfiguration config) {
		this.config = config;
	}

	private WSGraph getGraph() {
		return this.getWorkflow().getGraph();
	}

	private ArrayList<Node> getFinishedNodesDynamically() {
		return this.getNodesWithState(NodeExecutionState.FINISHED);
	}

	private ArrayList<Node> getWaitingNodesDynamically() {
		return this.getNodesWithState(NodeExecutionState.WAITING);
	}

	private ArrayList<Node> getNodesWithState(NodeExecutionState state) {
		ArrayList<Node> list = new ArrayList<Node>();
		List<NodeImpl> nodes = getGraph().getNodes();
		for (Node node : nodes) {
			if (state==node.getState()) {
				list.add(node);
			}
		}
		return list;
	}

    public static void setWorkflowInterpreterConfigurationThreadLocal(WorkflowInterpreterConfiguration workflowInterpreterConfiguration) {
        WorkflowInterpreter.workflowInterpreterConfigurationThreadLocal.set(workflowInterpreterConfiguration);
    }

    public static WorkflowInterpreterConfiguration getWorkflowInterpreterConfiguration() {
        return workflowInterpreterConfigurationThreadLocal.get();
    }

	public ExperimentModel getExperiment() {
		return experiment;
	}

	public void setExperiment(ExperimentModel experiment) {
		this.experiment = experiment;
	}

	public String getCredentialStoreToken() {
		return credentialStoreToken;
	}

	public void setCredentialStoreToken(String credentialStoreToken) {
		this.credentialStoreToken = credentialStoreToken;
	}
	
	@Override
	public void setup(Object... configurations) {
		
	}
	
	@Subscribe
    public void taskOutputChanged(TaskOutputChangeEvent taskOutputEvent){
		String taskId = taskOutputEvent.getTaskIdentity().getTaskId();
		if (isTaskAwaiting(taskId)){
        	ProcessState state=ProcessState.COMPLETED;
			Node node = getAwaitingNodeForTask(taskId);
    		List<OutputDataObjectType> applicationOutputs = taskOutputEvent.getOutput();
			Map<String, String> outputData = new HashMap<String, String>();
			for (OutputDataObjectType outputObj : applicationOutputs) {
				List<DataPort> outputPorts = node.getOutputPorts();
				for (DataPort dataPort : outputPorts) {
					if (dataPort.getName().equals(outputObj.getName())){
						outputData.put(outputObj.getName(), outputObj.getValue());
					}
				}
			}
			nodeOutputData.put(node, outputData);
			setupNodeDetailsOutput(node);
			node.setState(NodeExecutionState.FINISHED);
        	try {
                publishNodeStatusChange(WorkflowNodeState.COMPLETED, node.getID(), experiment.getExperimentID());
                updateWorkflowNodeStatus(nodeInstanceList.get(node), state);
			} catch (RegistryException e) {
                log.error(e.getMessage(), e);
			} catch (AiravataException e) {
                log.error(e.getMessage(), e);
            }
        }
	}
	
    @Subscribe
    public void taskStatusChanged(TaskStatusChangeEvent taskStatus){
    	String taskId = taskStatus.getTaskIdentity().getTaskId();
		if (isTaskAwaiting(taskId)){
        	WorkflowNodeState state=WorkflowNodeState.UNKNOWN;
			Node node = getAwaitingNodeForTask(taskId);
        	switch(taskStatus.getState()){
        	case CANCELED:
        		; break;
        	case COMPLETED:
        		//task has completed
        		//but we'll wait for outputdata
        		break;
        	case CONFIGURING_WORKSPACE:
        		break;
        	case FAILED:
        		state = WorkflowNodeState.FAILED;
        		node.setState(NodeExecutionState.FAILED);
        		break;
        	case EXECUTING: case WAITING: case PRE_PROCESSING: case POST_PROCESSING: case OUTPUT_DATA_STAGING: case INPUT_DATA_STAGING:
        		state = WorkflowNodeState.EXECUTING;
        		node.setState(NodeExecutionState.EXECUTING);
        		break;
        	case STARTED:
        		break;
        	case CANCELING:
        		state = WorkflowNodeState.CANCELING;
        		break;
    		default:
    			break;
        	}
        	try {
				if (state != WorkflowNodeState.UNKNOWN) {
					updateWorkflowNodeStatus(nodeInstanceList.get(node), state);
				}
			} catch (RegistryException e) {
                log.error(e.getMessage(), e);
			}
    	}

    }
}