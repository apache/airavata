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

    public String getTopic();

    public abstract void cleanup();

}