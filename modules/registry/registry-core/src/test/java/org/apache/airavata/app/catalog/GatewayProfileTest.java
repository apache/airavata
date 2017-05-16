/**
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
 */
package org.apache.airavata.app.catalog;

import org.apache.airavata.app.catalog.util.Initialize;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.apache.airavata.registry.cpi.GwyResourceProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GatewayProfileTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;
    private static final Logger logger = LoggerFactory.getLogger(GatewayProfileTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            appcatalog = RegistryFactory.getAppCatalog();
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void gatewayProfileTest() throws Exception {
        GwyResourceProfile gatewayProfile = appcatalog.getGatewayProfile();
        GatewayResourceProfile gf = new GatewayResourceProfile();
        ComputeResource computeRs = appcatalog.getComputeResource();
        ComputeResourceDescription cm1 = new ComputeResourceDescription();
        cm1.setHostName("localhost");
        cm1.setResourceDescription("test compute host");
        String hostId1 = computeRs.addComputeResource(cm1);

        ComputeResourceDescription cm2 = new ComputeResourceDescription();
        cm2.setHostName("localhost");
        cm2.setResourceDescription("test compute host");
        String hostId2 = computeRs.addComputeResource(cm2);

        ComputeResourcePreference preference1 = new ComputeResourcePreference();
        preference1.setComputeResourceId(hostId1);
        preference1.setOverridebyAiravata(true);
        preference1.setPreferredJobSubmissionProtocol(JobSubmissionProtocol.SSH);
                preference1.setPreferredDataMovementProtocol(DataMovementProtocol.SCP);
        preference1.setPreferredBatchQueue("queue1");
        preference1.setScratchLocation("/tmp");
        preference1.setAllocationProjectNumber("project1");

        ComputeResourcePreference preference2 = new ComputeResourcePreference();
        preference2.setComputeResourceId(hostId2);
        preference2.setOverridebyAiravata(true);
        preference2.setPreferredJobSubmissionProtocol(JobSubmissionProtocol.LOCAL);
        preference2.setPreferredDataMovementProtocol(DataMovementProtocol.GridFTP);
        preference2.setPreferredBatchQueue("queue2");
        preference2.setScratchLocation("/tmp");
        preference2.setAllocationProjectNumber("project2");

        List<ComputeResourcePreference> list = new ArrayList<ComputeResourcePreference>();
        list.add(preference1);
        list.add(preference2);
        gf.setComputeResourcePreferences(list);
        gf.setGatewayID("testGateway");

        String gwId = gatewayProfile.addGatewayResourceProfile(gf);
        GatewayResourceProfile retrievedProfile = null;
        if (gatewayProfile.isGatewayResourceProfileExists(gwId)){
            retrievedProfile = gatewayProfile.getGatewayProfile(gwId);
            System.out.println("************ gateway id ************** :" + retrievedProfile.getGatewayID());
        }
        List<ComputeResourcePreference> preferences = gatewayProfile.getAllComputeResourcePreferences(gwId);
        System.out.println("compute preferences size : " + preferences.size());
        if (preferences != null && !preferences.isEmpty()){
            for (ComputeResourcePreference cm : preferences){
                System.out.println("******** host id ********* : " + cm.getComputeResourceId());
                System.out.println(cm.getPreferredBatchQueue());
                System.out.println(cm.getPreferredDataMovementProtocol());
                System.out.println(cm.getPreferredJobSubmissionProtocol());
            }
        }

        assertTrue("App interface saved successfully", retrievedProfile != null);
    }

}
