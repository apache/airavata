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


import junit.framework.TestCase;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.apache.airavata.persistance.registry.jpa.util.Initialize;

import java.sql.Date;
import java.util.Calendar;

public class ExperimentMetadataResourceTest extends TestCase {
    private GatewayResource gatewayResource;
    private ExperimentResource experimentResource;
    private WorkerResource workerResource;
    private ExperimentDataResource experimentDataResource;

    private Initialize initialize ;
    @Override
    public void setUp() throws Exception {
        initialize = new Initialize();
        initialize.initializeDB();
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
        workerResource = (WorkerResource)ResourceUtils.getWorker(gatewayResource.getGatewayName(), "testUser");

        experimentResource = (ExperimentResource)gatewayResource.create(ResourceType.EXPERIMENT);
        experimentResource.setExpID("testExpID");
        experimentResource.setWorker(workerResource);
        experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));

        Calendar calender = Calendar.getInstance();
        java.util.Date d =  calender.getTime();
        Date currentDate = new Date(d.getTime());
        experimentResource.setSubmittedDate(currentDate);
        experimentResource.save();

        experimentDataResource = (ExperimentDataResource)experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("testExp");
        experimentDataResource.setUserName(workerResource.getUser());
        experimentDataResource.save();

//        super.setUp();
    }

    public void testSave() throws Exception {
        ExperimentMetadataResource experimentMetadataResource = new ExperimentMetadataResource();
        experimentMetadataResource.setExpID("testExpID");
        experimentMetadataResource.setMetadata("testMetadata");
        experimentMetadataResource.save();

        if(experimentDataResource.isExists(ResourceType.EXPERIMENT_METADATA, "testExpID")){
            assertTrue("experiment meta data saved successfully" , true);
        }

        //remove the metadata
        experimentDataResource.remove(ResourceType.EXPERIMENT_METADATA, "testExpID");
    }

    @Override
    protected void tearDown() throws Exception {
        initialize.stopDerbyServer();
//        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
