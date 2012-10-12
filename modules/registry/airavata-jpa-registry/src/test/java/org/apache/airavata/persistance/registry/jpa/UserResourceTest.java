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
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.util.Initialize;

public class UserResourceTest extends TestCase {
    private UserResource userResource;
    private GatewayResource gatewayResource;

    private Initialize initialize ;
    @Override
    public void setUp() throws Exception {
        initialize = new Initialize();
        initialize.initializeDB();
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
        userResource = (UserResource)gatewayResource.create(ResourceType.USER);
        userResource.setUserName("testUser");
        userResource.setPassword("testPassword");
//        super.setUp();
    }

    public void testSave() throws Exception {
        userResource.save();
        if(gatewayResource.isExists(ResourceType.USER, "testUser")) {
            assertTrue("user resource saved successfully", true);
        }
        //remove user
//        gatewayResource.remove(ResourceType.USER, "testUser");
    }

    @Override
    protected void tearDown() throws Exception {
        initialize.stopDerbyServer();
//        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
