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

import edu.indiana.extreme.lead.workflow_tracking.Notifier;
import edu.indiana.extreme.lead.workflow_tracking.NotifierFactory;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorConsts;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorProps;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationEntity;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.xbaya.util.XMLUtil;

import org.apache.axis2.addressing.EndpointReference;
import xsul.ws_addressing.WsaEndpointReference;

public class ResourceNotifierTestCase extends XBayaTestCase {

    /**
     * 
     */
    public void test() {
        EndpointReference brokerEPR = WseMsgBrokerClient.createEndpointReference(this.configuration.getBrokerURL()
                .toString(), this.configuration.getTopic());
        //TODO remove the xsul dependency here to WsaEndpointReference object
        URI temporaryURI = URI.create(brokerEPR.getAddress());
        ConstructorProps props = ConstructorProps.newProps();
        props.set(ConstructorConsts.BROKER_EPR, XMLUtil.xmlElementToString(new WsaEndpointReference(temporaryURI)));
        Notifier notifier = NotifierFactory.createNotifier(props);

        URI initiatorWorkflowID = URI.create("Workflow");
        URI initiatorServiceID = URI.create("Adder_add");
        String initiatorWorkflowNodeID1 = "Adder_add";
        Integer workflowTimeStep = new Integer(0);
        InvocationEntity entity1 = notifier.createEntity(initiatorWorkflowID, initiatorServiceID,
                initiatorWorkflowNodeID1, workflowTimeStep);
        notifier.resourceMapping(entity1, "resource1.example.com", 1);
        notifier.resourceMapping(entity1, "resource2.example.com", 2);
        notifier.resourceMapping(entity1, "resource3.example.com", 3);

        String initiatorWorkflowNodeID2 = "Adder_add_2";
        InvocationEntity entity2 = notifier.createEntity(initiatorWorkflowID, initiatorServiceID,
                initiatorWorkflowNodeID2, workflowTimeStep);
        notifier.resourceMapping(entity2, "resource.example.com", 0);
    }
}