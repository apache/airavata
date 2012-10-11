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
import org.apache.airavata.persistance.registry.jpa.resources.HostDescriptorResource;

public class HostDescriptorResourceTest extends TestCase {
    private GatewayResource gatewayResource;
    private HostDescriptorResource hostDescriptorResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
        hostDescriptorResource = gatewayResource.createHostDescriptorResource("testHostDesc");
        hostDescriptorResource.setUserName("testUser");
        hostDescriptorResource.setContent("testContent");
    }

    public void testGetList() throws Exception{
        assertNotNull("application data being retrieved successfully" ,hostDescriptorResource.get(ResourceType.APPLICATION_DESCRIPTOR));
    }

    public void testSave() throws Exception {
        hostDescriptorResource.save();
        if(gatewayResource.isHostDescriptorExists("testHostDesc")){
            assertTrue("host descriptor saved successfully", true);
        }
        //remove host descriptor
        gatewayResource.removeHostDescriptor("testHostDesc");
    }
}
