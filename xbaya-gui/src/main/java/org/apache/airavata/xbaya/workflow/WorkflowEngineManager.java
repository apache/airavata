/**
 * @author: Eran Chinthaka (eran.chinthaka@gmail.com)
 */
package org.apache.airavata.xbaya.workflow;

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

import java.net.URI;

import org.apache.airavata.xbaya.gpel.GPELClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;

public class WorkflowEngineManager {

    public static final String WORKFLOW_CLIENT_PROXY = "WorkflowProxy";
    public static final String WORKFLOW_CLIENT_GPEL = "GPELClient";

    // private static WorkflowEngineManager ourInstance = new WorkflowEngineManager();
    //
    // public static WorkflowEngineManager getInstance() {
    // return ourInstance;
    // }

    private WorkflowEngineManager() {
    }

    public static WorkflowClient getWorkflowClient() {
        String workflowClientName = System.getProperty("org.apache.airavata.xbaya.workflow.ClientType");
        if (WORKFLOW_CLIENT_PROXY.equals(workflowClientName)) {
            return new WorkflowProxyClient();
        } else {
            return new GPELClient();
        }
    }

    public static WorkflowClient getWorkflowClient(URI engineURL, UserX509Credential gpelUserX509Credential)
            throws WorkflowEngineException {
        String workflowClientName = System.getProperty("org.apache.airavata.xbaya.workflow.ClientType");
        if (WORKFLOW_CLIENT_PROXY.equals(workflowClientName)) {
            return new WorkflowProxyClient(engineURL, "", gpelUserX509Credential);
        } else {
            return new GPELClient(engineURL, gpelUserX509Credential);
        }
    }
}
