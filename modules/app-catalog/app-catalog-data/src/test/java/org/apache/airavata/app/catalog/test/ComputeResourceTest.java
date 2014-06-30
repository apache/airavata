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

package org.apache.airavata.app.catalog.test;


import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ComputeResource;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogImpl;
import org.apache.airavata.app.catalog.test.util.Initialize;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.computehost.ComputeResourceDescription;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ComputeResourceTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;

    @Before
    public void setUp() {
        AiravataUtils.setExecutionAsServer();
        initialize = new Initialize("appcatalog-derby.sql");
        initialize.initializeDB();
        appcatalog = new AppCatalogImpl();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        initialize.stopDerbyServer();
    }

    @Test
    public void testAddComputeResource (){
        try {
            ComputeResource computeResource = appcatalog.getComputeResource();
            ComputeResourceDescription description = new ComputeResourceDescription();
            description.setHostName("localhost");
            description.setPreferredJobSubmissionProtocol("SSH");
            description.setResourceDescription("test compute resource");
            String resourceId = computeResource.addComputeResource(description);
            System.out.println("**********Resource id ************* : " +  resourceId);
            ComputeResourceDescription host = computeResource.getComputeResource(resourceId);
            System.out.println("**********Resource name ************* : " +  host.getHostName());
            assertTrue("Compute resource save successfully", host!=null);
        } catch (AppCatalogException e) {
            e.printStackTrace();
        }

    }

}

