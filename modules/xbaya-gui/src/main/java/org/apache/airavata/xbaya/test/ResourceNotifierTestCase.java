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

package org.apache.airavata.xbaya.test;

import java.net.URI;
import java.util.Properties;


import org.apache.airavata.workflow.tracking.Notifier;
import org.apache.airavata.workflow.tracking.NotifierFactory;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;

import org.apache.axis2.addressing.EndpointReference;

public class ResourceNotifierTestCase extends XBayaTestCase {

    /**
     * 
     */
    public void test() {
        EndpointReference brokerEPR = WseMsgBrokerClient.createEndpointReference(this.configuration.getBrokerURL()
                .toString(), this.configuration.getTopic());
        Notifier notifier = NotifierFactory.createNotifier();

        URI initiatorWorkflowID = URI.create("Workflow");
        URI initiatorServiceID = URI.create("Adder_add");
        String initiatorWorkflowNodeID1 = "Adder_add";
        Integer workflowTimeStep = new Integer(0);
        WorkflowTrackingContext context = notifier.createTrackingContext(new Properties(),brokerEPR,
                initiatorWorkflowID,initiatorServiceID,initiatorWorkflowNodeID1,workflowTimeStep);
        notifier.resourceMapping(context, "resource1.example.com", 1,null);
        notifier.resourceMapping(context, "resource2.example.com", 2);
        notifier.resourceMapping(context, "resource3.example.com", 3);

        String initiatorWorkflowNodeID2 = "Adder_add_2";
        context = notifier.createTrackingContext(new Properties(),brokerEPR,
                initiatorWorkflowID,initiatorServiceID,initiatorWorkflowNodeID2,workflowTimeStep);
        notifier.resourceMapping(context, "resource.example.com", 0);
    }
}