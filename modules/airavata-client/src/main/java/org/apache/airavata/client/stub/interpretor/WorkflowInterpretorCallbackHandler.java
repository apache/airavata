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

package org.apache.airavata.client.stub.interpretor;

/**
 * WorkflowInterpretorCallbackHandler Callback class, Users can extend this class and implement their own receiveResult
 * and receiveError methods.
 */
public abstract class WorkflowInterpretorCallbackHandler {

    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking Web service call is finished and
     * appropriate method of this CallBack is called.
     * 
     * @param clientData
     *            Object mechanism by which the user can pass in user data that will be avilable at the time this
     *            callback is called.
     */
    public WorkflowInterpretorCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public WorkflowInterpretorCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */

    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for launchWorkflow method override this method for handling normal response
     * from launchWorkflow operation
     */
    public void receiveResultlaunchWorkflow(java.lang.String result) {
    }

    /**
     * auto generated Axis2 Error handler override this method for handling error response from launchWorkflow operation
     */
    public void receiveErrorlaunchWorkflow(java.lang.Exception e) {
    }

}
