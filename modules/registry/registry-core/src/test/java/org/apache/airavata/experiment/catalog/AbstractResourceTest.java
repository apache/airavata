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

package org.apache.airavata.registry.core.experiment.catalog;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.experiment.catalog.util.Initialize;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.resources.*;
import org.apache.airavata.registry.core.experiment.catalog.resources.GatewayExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.WorkerExperimentCatResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.sql.Timestamp;
import java.util.Calendar;

public abstract class AbstractResourceTest {

    private GatewayExperimentCatResource gatewayResource;
    private WorkerExperimentCatResource workerResource;
    private UserExperimentCatResource userResource;
    private ProjectExperimentCatResource projectResource;

    private static Initialize initialize;
   
    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
    	  initialize = new Initialize("registry-derby.sql");
          initialize.initializeDB();
    }
    @Before
    public void setUp() throws Exception {
        gatewayResource = (GatewayExperimentCatResource) ExpCatResourceUtils.getGateway(ServerSettings.getDefaultUserGateway());
        workerResource = (WorkerExperimentCatResource) ExpCatResourceUtils.getWorker(gatewayResource.getGatewayName(), ServerSettings.getDefaultUser());
        userResource = (UserExperimentCatResource) ExpCatResourceUtils.getUser(ServerSettings.getDefaultUser());
        projectResource = workerResource.getProject("default");
    }

    public Timestamp getCurrentTimestamp() {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        return new Timestamp(d.getTime());
    }
    @AfterClass
	public static void tearDownAfterClass() throws Exception {
        initialize.stopDerbyServer();
	}
   

    public GatewayExperimentCatResource getGatewayResource() {
        return gatewayResource;
    }

    public WorkerExperimentCatResource getWorkerResource() {
        return workerResource;
    }

    public UserExperimentCatResource getUserResource() {
        return userResource;
    }

	public ProjectExperimentCatResource getProjectResource() {
		return projectResource;
	}

	public void setProjectResource(ProjectExperimentCatResource projectResource) {
		this.projectResource = projectResource;
	}


}
