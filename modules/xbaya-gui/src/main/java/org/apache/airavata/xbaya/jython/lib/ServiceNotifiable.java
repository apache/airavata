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