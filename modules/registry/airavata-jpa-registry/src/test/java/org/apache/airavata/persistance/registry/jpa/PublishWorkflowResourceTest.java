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

package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.PublishWorkflowResource;

import java.sql.Timestamp;
import java.util.Calendar;

public class PublishWorkflowResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private PublishWorkflowResource publishWorkflowResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gatewayResource = super.getGatewayResource();
        publishWorkflowResource = gatewayResource.createPublishedWorkflow("workflow1");
        publishWorkflowResource.setCreatedUser("admin");
        publishWorkflowResource.setContent("testContent");
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp currentTime = new Timestamp(d.getTime());
        publishWorkflowResource.setPublishedDate(currentTime);
    }

    public void testSave() throws Exception {
        publishWorkflowResource.save();
        assertTrue("published workflow saved successfully", gatewayResource.isPublishedWorkflowExists("workflow1"));
        //remove workflow
        gatewayResource.removePublishedWorkflow("workflow1");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
