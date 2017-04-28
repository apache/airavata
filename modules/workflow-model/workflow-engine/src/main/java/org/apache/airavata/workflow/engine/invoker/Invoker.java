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
package org.apache.airavata.workflow.engine.invoker;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;

//import xsul.wsif.WSIFMessage;
//import xsul.xwsif_runtime.WSIFClient;

public interface Invoker {

    /**
     * Sets up the service.
     * 
     * @throws WorkflowException
     */
    public void setup() throws WorkflowException;

    /**
     * @return The WSIFClient.
     */
//    public WSIFClient getClient();

    /**
     * Sets the operation name to invoke.
     * 
     * @param operationName
     *            The name of the operation
     * @throws WorkflowException
     */
//    public void setOperation(String operationName) throws WorkflowException;
//
//    /**
//     * Sets an input parameter
//     * 
//     * @param name
//     *            The name of the input parameter
//     * @param value
//     *            The value of the input parameter
//     * @throws WorkflowException
//     */
//    public void setInput(String name, Object value) throws WorkflowException;
//
//    /**
//     * Returns the all input parameters
//     * 
//     * @return The input parameters
//     * @throws WorkflowException
//     */
////    public WSIFMessage getInputs() throws WorkflowException;
//
    /**
     * Invokes the service.
     * 
     * @return true if the invocation succeeds; fase otherwise
     * @throws WorkflowException
     */
    public boolean invoke() throws WorkflowException;
//
//    /**
//     * Returns the all output parameters
//     * 
//     * @return The output parameters
//     * @throws WorkflowException
//     */
////    public WSIFMessage getOutputs() throws WorkflowException;
//
//    /**
//     * Returns the output of a specified name.
//     * 
//     * @param name
//     *            The name of the output parameter
//     * @return The value of the output
//     * @throws WorkflowException
//     */
    public Object getOutput(String name) throws WorkflowException;

    /**
     * Returns the fault message.
     * 
     * @return The fault message
     * @throws WorkflowException
     */
//    public WSIFMessage getFault() throws WorkflowException;

}