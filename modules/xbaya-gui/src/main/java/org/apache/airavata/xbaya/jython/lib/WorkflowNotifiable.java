package org.apache.airavata.xbaya.jython.lib;

import org.apache.axis2.addressing.EndpointReference;
import org.python.core.PyObject;

public interface WorkflowNotifiable {

    /**
     * @return The event sink EPR.
     */
    public abstract EndpointReference getEventSink();

    /**
     * @param args
     * @param keywords
     */
    public abstract void workflowStarted(PyObject[] args, String[] keywords);

    public abstract void workflowStarted(Object[] args, String[] keywords);

    /**
     * @param args
     * @param keywords
     */
    public abstract void workflowFinished(Object[] args, String[] keywords);

    public abstract void sendingPartialResults(Object[] args, String[] keywords);

    /**
     * @param args
     * @param keywords
     */
    public abstract void workflowFinished(PyObject[] args, String[] keywords);

    public abstract void workflowTerminated();

    /**
     * Sends a START_INCOMPLETED notification message.
     * 
     * @param message
     *            The message to send
     */
    public abstract void workflowFailed(String message);

    /**
     * Sends a START_INCOMPLETED notification message.
     * 
     * @param e
     */
    public abstract void workflowFailed(Throwable e);

    /**
     * Sends a START_INCOMPLETED notification message.
     * 
     * @param message
     *            The message to send
     * @param e
     */
    public abstract void workflowFailed(String message, Throwable e);

    /**
     * @param nodeID
     * @return The ServiceNoficationSender created.
     */
    public abstract ServiceNotifiable createServiceNotificationSender(String nodeID);

}