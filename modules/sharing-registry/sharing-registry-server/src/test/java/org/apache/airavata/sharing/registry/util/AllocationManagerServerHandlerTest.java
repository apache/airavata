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
package org.apache.airavata.sharing.registry.util;

import junit.framework.Assert;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.server.AllocationManagerServerHandler;
import org.apache.thrift.TException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AllocationManagerServerHandlerTest {
    private final static Logger logger = LoggerFactory.getLogger(AllocationManagerServerHandlerTest.class);

    @BeforeClass
    public static void setup() throws SharingRegistryException, SQLException {
        Initialize initialize = new Initialize("sharing-registry-derby.sql");
        initialize.initializeDB();
    }

    @Test
    public void test() throws TException, ApplicationSettingsException {
        AllocationManagerServerHandler allocationManagerServerHandler = new AllocationManagerServerHandler();

        //Creating requests
        UserAllocationDetails userAllocationDetails = new UserAllocationDetails();
        userAllocationDetails.setProjectId("123");
        userAllocationDetails.setApplicationsToBeUsed("AB, BC");
        userAllocationDetails.setDiskUsageRangePerJob(2L);
        userAllocationDetails.setExternalAllocationAccessMechanisms("Get");
        userAllocationDetails.setExternalAllocationAccountPassword("abc");
        userAllocationDetails.setTypicalSuPerJob(12l);
        userAllocationDetails.setTypeOfAllocation("temp");
        userAllocationDetails.setTitle("Project");
        userAllocationDetails.setSpecificResourceSelection("resource");
        userAllocationDetails.setServiceUnits(2L);
        userAllocationDetails.setProjectDescription("This project does the calculation ...");
        userAllocationDetails.setProjectReviewedAndFundedBy("def");
        userAllocationDetails.setPrincipalInvistigatorName("Harsha Phulwani");
        userAllocationDetails.setPrincipalInvistigatorEmail("harshaphulwani16@gmail.com");
        userAllocationDetails.setMaxMemoryPerCpu(100L);
        userAllocationDetails.setDiskUsageRangePerJob(20L);
        userAllocationDetails.setNumberOfCpuPerJob(5L);
        userAllocationDetails.setKeywords("chemistry,biology");
        userAllocationDetails.setExternalAllocationResourceName("efg");
        userAllocationDetails.setExternalAllocationProjectId("567");
        userAllocationDetails.setFieldOfScience("chemistry");


        Assert.assertNotNull(allocationManagerServerHandler.createAllocationRequest(userAllocationDetails));
        Assert.assertEquals(allocationManagerServerHandler.getAllocationRequest("123"),userAllocationDetails);

        UserAllocationDetails userAllocationDetails1 = new UserAllocationDetails();
        userAllocationDetails1.setProjectId("123");


        Assert.assertTrue(allocationManagerServerHandler.isAllocationRequestExists(userAllocationDetails1.getProjectId()));
        Assert.assertEquals(allocationManagerServerHandler.createAllocationRequest(userAllocationDetails1),"There exist project with the id");

        Assert.assertTrue(allocationManagerServerHandler.deleteAllocationRequest("123"));
    }
}