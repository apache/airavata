package org.apache.airavata.xbaya.jython.lib;

import java.net.URI;

import org.apache.axis2.addressing.EndpointReference;

import xsul.wsif.WSIFMessage;

public interface ServiceNotifiable {

    /**
     * @param serviceID
     */
    public abstract void setServiceID(String serviceID);

    /**
     * @return The event sink.
     */
    public abstract EndpointReference getEventSink();

    /**
     * @return The workflow ID.
     */
    public abstract URI getWorkflowID();

    /**
     * @param inputs
     */
    public abstract void invokingService(WSIFMessage inputs);

    /**
     * @param outputs
     */
    public abstract void serviceFinished(WSIFMessage outputs);

    /**
     * Sends an InvokeServiceFinishedFailed notification message.
     * 
     * @param message
     *            The message to send
     * @param e
     */
    public abstract void invocationFailed(String message, Throwable e);

    /**
     * Sends a receivedFault notification message.
     * 
     * @param message
     *            The message to send
     */
    @Deprecated
    public abstract void receivedFault(String message);

    /**
     * Sends a receivedFault notification message.
     * 
     * @param fault
     */
    public abstract void receivedFault(WSIFMessage fault);

}