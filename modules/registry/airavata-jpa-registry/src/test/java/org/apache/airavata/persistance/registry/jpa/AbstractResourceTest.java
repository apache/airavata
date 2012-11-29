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
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.persistance.registry.jpa.util.Initialize;

public abstract class AbstractResourceTest extends TestCase {

    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private UserResource userResource;

    private Initialize initialize;
    @Override
    public void setUp() throws Exception {
        initialize = new Initialize();
        initialize.initializeDB();
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("default");
        workerResource = (WorkerResource)ResourceUtils.getWorker(gatewayResource.getGatewayName(), "admin");
        userResource = (UserResource)gatewayResource.create(ResourceType.USER);
        userResource.setUserName("admin");
        userResource.setPassword("admin");
    }

    @Override
    public void tearDown() throws Exception {
        initialize.stopDerbyServer();
    }

    public GatewayResource getGatewayResource() {
        return gatewayResource;
    }

    public WorkerResource getWorkerResource() {
        return workerResource;
    }

    public UserResource getUserResource() {
        return userResource;
    }


}
