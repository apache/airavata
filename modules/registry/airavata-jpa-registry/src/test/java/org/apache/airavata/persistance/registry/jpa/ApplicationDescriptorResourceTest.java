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
import junit.framework.TestSuite;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.util.Initialize;
import org.junit.*;

public class ApplicationDescriptorResourceTest extends TestCase {
    private GatewayResource gatewayResource;
    private Initialize initialize;

    @BeforeClass
    public void setUp() throws Exception {
        initialize = new Initialize();
        initialize.initializeDB();
        Class[] testClasses = { ConfigurationResourceTest.class,
                GatewayResourceTest.class, UserResourceTest.class, WorkerResourceTest.class, ProjectResourceTest.class,
                HostDescriptorResourceTest.class, ServiceDescriptorResourceTest.class, ApplicationDescriptorResourceTest.class,
                UserWorkflowResourceTest.class, PublishWorkflowResourceTest.class,
                ExperimentResourceTest.class, ExperimentMetadataResourceTest.class, ExperimentDataResourceTest.class,
                WorkflowDataResourceTest.class, NodeDataResourceTest.class, GramDataResourceTest.class};
        TestSuite testSuite = new TestSuite(testClasses);
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
    }

    @org.junit.Test
    public void testSave() throws Exception{
        ApplicationDescriptorResource applicationDescriptorResouce = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        applicationDescriptorResouce.setHostDescName("testHostDesc");
        applicationDescriptorResouce.setServiceDescName("testServiceDesc");
        applicationDescriptorResouce.setName("testAppDesc");
        applicationDescriptorResouce.setContent("testContent");
        applicationDescriptorResouce.setUpdatedUser("testUser");
        applicationDescriptorResouce.save();

        if(gatewayResource.isExists(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc") ){
            assertTrue("application descriptor saved successfully", true);
        }

        gatewayResource.remove(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc");
    }

    @Override
    protected void tearDown() throws Exception {
        initialize.stopDerbyServer();
//        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
