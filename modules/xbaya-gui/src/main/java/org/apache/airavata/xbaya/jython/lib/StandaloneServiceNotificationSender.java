package org.apache.airavata.xbaya.jython.lib;

import java.awt.Color;
import java.net.URI;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.addressing.EndpointReference;

import xsul.wsif.WSIFMessage;

public class StandaloneServiceNotificationSender implements ServiceNotifiable {

    private Workflow workflow;
    private String serviceID;
    private URI workflowID;

    public StandaloneServiceNotificationSender(Workflow workflow, URI workflowID) {
        this.workflow = workflow;
        this.workflowID = workflowID;
    }

    @Override
    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
        System.out.println(serviceID);

    }

    @Override
    public EndpointReference getEventSink() {
        return new EndpointReference(XBayaConstants.DEFAULT_BROKER_URL.toString());
    }

    @Override
    public URI getWorkflowID() {
        return this.workflowID;
    }

    @Override
    public void invokingService(WSIFMessage inputs) {
        this.workflow.getGraph().getNode(this.serviceID).getGUI().setBodyColor(NodeState.EXECUTING.color);
    }

    @Override
    public void serviceFinished(WSIFMessage outputs) {
        this.workflow.getGraph().getNode(this.serviceID).getGUI().setBodyColor(NodeState.FINISHED.color);

    }

    @Override
    public void invocationFailed(String message, Throwable e) {
        this.workflow.getGraph().getNode(this.serviceID).getGUI().setBodyColor(NodeState.FAILED.color);

    }

    @Override
    public void receivedFault(String message) {
        this.workflow.getGraph().getNode(this.serviceID).getGUI().setBodyColor(NodeState.FAILED.color);

    }

    @Override
    public void receivedFault(WSIFMessage fault) {
        this.workflow.getGraph().getNode(this.serviceID).getGUI().setBodyColor(NodeState.FAILED.color);

    }

}
