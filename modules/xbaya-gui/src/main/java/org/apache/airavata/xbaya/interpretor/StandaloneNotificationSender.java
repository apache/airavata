package org.apache.airavata.xbaya.interpretor;

import java.net.URI;
import java.util.List;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.jython.lib.ServiceNotifiable;
import org.apache.airavata.xbaya.jython.lib.StandaloneServiceNotificationSender;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.addressing.EndpointReference;
import org.python.core.PyObject;

public class StandaloneNotificationSender implements WorkflowNotifiable {
	
	

	private Workflow workflow;
	private URI workflowID;

	public StandaloneNotificationSender(String topic,
			Workflow workflow) {
		this.workflow = workflow;
		this.workflowID = URI.create(StringUtil.convertToJavaIdentifier(topic));
	}

	@Override
	public EndpointReference getEventSink() {
		return new EndpointReference(XBayaConstants.DEFAULT_BROKER_URL.toString());
	}

	@Override
	public void workflowStarted(PyObject[] args, String[] keywords) {
		List<InputNode> inputs = GraphUtil.getInputNodes(this.workflow.getGraph());
		for (InputNode inputNode : inputs) {
			inputNode.getGUI().setBodyColor(NodeState.FINISHED.color);
		}
		
	}

	@Override
	public void workflowStarted(Object[] args, String[] keywords) {
		List<InputNode> inputs = GraphUtil.getInputNodes(this.workflow.getGraph());
		for (InputNode inputNode : inputs) {
			inputNode.getGUI().setBodyColor(NodeState.FINISHED.color);
		}
	}

	@Override
	public void workflowFinished(Object[] args, String[] keywords) {
		List<OutputNode> outputs = GraphUtil.getOutputNodes(this.workflow.getGraph());
		for (OutputNode outputNode : outputs) {
			outputNode.getGUI().setBodyColor(NodeState.EXECUTING.color);
		}

	}

	@Override
	public void sendingPartialResults(Object[] args, String[] keywords) {
		// noop

	}

	@Override
	public void workflowFinished(PyObject[] args, String[] keywords) {
		List<OutputNode> outputs = GraphUtil.getOutputNodes(this.workflow.getGraph());
		for (OutputNode outputNode : outputs) {
			outputNode.getGUI().setBodyColor(NodeState.EXECUTING.color);
		}

	}

	@Override
	public void workflowTerminated() {
		// noop

	}

	@Override
	public void workflowFailed(String message) {
		// noop

	}

	@Override
	public void workflowFailed(Throwable e) {
		//noop

	}

	@Override
	public void workflowFailed(String message, Throwable e) {
		//noop

	}

	@Override
	public ServiceNotifiable createServiceNotificationSender(String nodeID) {
		return new StandaloneServiceNotificationSender(this.workflow, this.workflowID);
	}

}
