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
import java.util.ArrayList;

import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axis2.addressing.EndpointReference;
import edu.indiana.extreme.lead.workflow_tracking.Notifier;
import edu.indiana.extreme.lead.workflow_tracking.NotifierFactory;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorConsts;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorProps;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationEntity;
import edu.indiana.extreme.lead.workflow_tracking.impl.state.DataObjImpl;

public class MonitorTestCase extends XBayaTestCase {

    private final static URI WORKFLOW_INSTANCE_ID = URI
            .create("tag:gpel.leadproject.org,2006:6B9/GFacTestWorkflow1/instance1");

    /**
     */
    public void test() {
        EndpointReference brokerEPR = WseMsgBrokerClient.createEndpointReference(this.configuration.getBrokerURL()
                .toString(), this.configuration.getTopic());

        ConstructorProps props = ConstructorProps.newProps();
        props.set(ConstructorConsts.BROKER_EPR, brokerEPR);
        Notifier notifier = NotifierFactory.createNotifier(props);

        InvocationEntity entity = notifier.createEntity(URI.create("workflowID"), URI.create("serviceID"),
                "workflowNodeID", new Integer(1) /* step */);
        notifier.publishURL(entity, "title", "http://www.google.com", "descriptionAndAnnotation");

        notifier.workflowInitialized(WORKFLOW_INSTANCE_ID);

        DataObjImpl dataObj = new DataObjImpl(URI.create("test"), new ArrayList<URI>());
        notifier.dataConsumed(entity, dataObj, "description");
    }

    /**
     * 
     */
    public void testScalability() {
        for (int i = 0; i < 100; i++) {
            test();
        }
    }
}