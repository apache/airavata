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

package org.apache.airavata.test.suite.workflowtracking.tests;

import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LeadNotificationManagerTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testGetBrokerPublishEPR() {

        String endpointRefAsStr = LeadNotificationManager.getBrokerPublishEPR("http://localhost:8080/axis2/services/EventingService", "testtopic");

        Assert.assertEquals("<EndpointReference><wsa:Address xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://localhost:8080/axis2/services/EventingService/topic/testtopic</wsa:Address></EndpointReference>",endpointRefAsStr);

    }

}
