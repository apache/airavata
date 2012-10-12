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
import org.apache.airavata.persistance.registry.jpa.resources.UserWorkflowResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;

import java.sql.Date;
import java.util.Calendar;

public class UserWorkflowResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private UserWorkflowResource userWorkflowResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gatewayResource = super.getGatewayResource();
        workerResource = super.getWorkerResource();

        userWorkflowResource = workerResource.createWorkflowTemplate("workflow1");
        userWorkflowResource.setGateway(gatewayResource);
        userWorkflowResource.setContent("testContent");
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        userWorkflowResource.setLastUpdateDate(currentTime);
    }

    public void testSave() throws Exception {
        userWorkflowResource.save();
        if (workerResource.isWorkflowTemplateExists("workflow1")) {
            assertTrue("user workflow saved successfully", true);
        }
        //remove user workflow
        workerResource.removeWorkflowTemplate("workflow1");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
