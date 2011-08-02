/*
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
 *
 */

package org.apache.airavata.xbaya.workflow;

import org.apache.airavata.xbaya.XBayaException;

import xsul.wsif.WSIFMessage;

public interface WorkflowInvoker {

    /**
     * Sets up the invoker.
     * 
     * @throws XBayaException
     */
    public abstract void setup() throws XBayaException;

    /**
     * Sets the operation name to invoke.
     * 
     * @param operationName
     *            The name of the operation
     * @throws XBayaException
     */
    public abstract void setOperation(String operationName) throws XBayaException;

    /**
     * Sets an input parameter
     * 
     * @param name
     *            The name of the input parameter
     * @param value
     *            The value of the input parameter
     * @throws XBayaException
     */
    public abstract void setInput(String name, Object value) throws XBayaException;

    /**
     * Invokes the service. This is a non-blocking call.
     * 
     * @throws XBayaException
     */
    public abstract void invoke() throws XBayaException;

    /**
     * Returns the output of a specified name. This method blocks until the execution finishes.
     * 
     * @param name
     *            The name of the output parameter
     * @return The value of the output
     * @throws XBayaException
     */
    public abstract Object getOutput(String name) throws XBayaException;

    public WSIFMessage getOutputs() throws XBayaException;

}