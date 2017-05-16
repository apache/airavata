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
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.apache.airavata.registry.cpi.UsrResourceProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserResourceProfileTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;
    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileTest.class);

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
    public void userProfileTest() throws Exception {
        UsrResourceProfile userProfile = appcatalog.getUserResourceProfile();
        UserResourceProfile uf = new UserResourceProfile();
        ComputeResource computeRs = appcatalog.getComputeResource();
        ComputeResourceDescription cm1 = new ComputeResourceDescription();
        cm1.setHostName("localhost");
        cm1.setResourceDescription("test compute host");
        String hostId1 = computeRs.addComputeResource(cm1);

        ComputeResourceDescription cm2 = new ComputeResourceDescription();
        cm2.setHostName("localhost");
        cm2.setResourceDescription("test compute host");
        String hostId2 = computeRs.addComputeResource(cm2);

        UserComputeResourcePreference preference1 = new UserComputeResourcePreference();
        preference1.setComputeResourceId(hostId1);
        preference1.setPreferredBatchQueue("queue1");
        preference1.setScratchLocation("/tmp");
        preference1.setAllocationProjectNumber("project1");

        UserComputeResourcePreference preference2 = new UserComputeResourcePreference();
        preference2.setComputeResourceId(hostId2);
        preference2.setPreferredBatchQueue("queue2");
        preference2.setScratchLocation("/tmp");
        preference2.setAllocationProjectNumber("project2");

        UserStoragePreference storagePreference = new UserStoragePreference();
        storagePreference.setStorageResourceId("st3");
        storagePreference.setLoginUserName("Anuj");
        storagePreference.setFileSystemRootLocation("/home/Anuj/scratch/");

        List<UserComputeResourcePreference> list = new ArrayList<UserComputeResourcePreference>();
        list.add(preference1);
        list.add(preference2);
        List<UserStoragePreference> stList = new ArrayList<>();
        stList.add(storagePreference);

        uf.setUserComputeResourcePreferences(list);
        uf.setGatewayID("airavataPGA");
        uf.setUserId("Anuj");
        uf.setUserStoragePreferences(stList);

        // Check if UserResourceProfile exists (should not exist)
        // This tests the mechanism that PGA will use to figure out if a user doesn't already have a UserResourceProfile
        UserResourceProfile checkUserResourceProfile = userProfile.getUserResourceProfile(uf.getUserId(), uf.getGatewayID());
        assertNotNull(checkUserResourceProfile.getUserId());
        assertNotNull(checkUserResourceProfile.getGatewayID());
        assertTrue(checkUserResourceProfile.isIsNull());

        String gwId = userProfile.addUserResourceProfile(uf);
        UserResourceProfile retrievedProfile = null;


        // This test is to check whether an existing user can add more compute preferences - AIRAVATA-2245

        System.out.println("*********Start Airavata-2245************");

        ComputeResource computeRs1 = appcatalog.getComputeResource();
        ComputeResourceDescription cm12 = new ComputeResourceDescription();
        cm12.setHostName("localhost123");
        cm12.setResourceDescription("test compute host");
        String hostId12 = computeRs1.addComputeResource(cm12);

        UserComputeResourcePreference preference12 = new UserComputeResourcePreference();
        preference12.setComputeResourceId(hostId12);
        preference12.setPreferredBatchQueue("queue112");
        preference12.setScratchLocation("/tmp21");
        preference12.setAllocationProjectNumber("project12");

        List<UserComputeResourcePreference> list12 = new ArrayList<UserComputeResourcePreference>();
        list12.add(preference12);

        UserResourceProfile uf12 = new UserResourceProfile();
        uf12.setUserComputeResourcePreferences(list12);
        uf12.setGatewayID("airavataPGA");
        uf12.setUserId("Anuj");

        String gwId12 = userProfile.addUserResourceProfile(uf12);

        System.out.println("*******End Airavata-2245******* : success");

        // End of test for AIRAVATA-2245


        //retrievedProfile = userProfile.getUserResourceProfile("hello",uf.getGatewayID());
        if (userProfile.isUserResourceProfileExists(uf.getUserId(),uf.getGatewayID())){
            retrievedProfile = userProfile.getUserResourceProfile(uf.getUserId(),uf.getGatewayID());
            assertFalse(retrievedProfile.isIsNull());
            System.out.println("gateway ID :" + retrievedProfile.getGatewayID());
            System.out.println("user ID : " + retrievedProfile.getUserId());
            System.out.println("compute resource size : " + retrievedProfile.getUserComputeResourcePreferencesSize());
        }
        if(retrievedProfile != null){
            List<UserComputeResourcePreference> preferences = userProfile.getAllUserComputeResourcePreferences(retrievedProfile.getUserId(),retrievedProfile.getGatewayID());
            System.out.println("compute preferences size : " + preferences.size());
            if (preferences != null && !preferences.isEmpty()){
                for (UserComputeResourcePreference cm : preferences){
                    System.out.println("******** host id ********* : " + cm.getComputeResourceId());
                    System.out.println(cm.getPreferredBatchQueue());
                    // this statement will remove all the compute resources created
                    System.out.println("Compute Preference removed : " + userProfile.removeUserComputeResourcePreferenceFromGateway(retrievedProfile.getUserId(),retrievedProfile.getGatewayID(),cm.getComputeResourceId()));
                }
            }
            List<UserStoragePreference> storagePreferences = userProfile.getAllUserStoragePreferences(retrievedProfile.getUserId(),retrievedProfile.getGatewayID());
            System.out.println("storage preferences size : " + storagePreferences.size());
            if (storagePreferences != null && !storagePreferences.isEmpty()){
                for (UserStoragePreference cm : storagePreferences){
                    System.out.println("******** storage id ********* : " + cm.getStorageResourceId());
                    System.out.println(cm.getFileSystemRootLocation());
                    // this statement will remove all the compute resources created
                    System.out.println("Storage Preference removed : " + userProfile.removeUserDataStoragePreferenceFromGateway(retrievedProfile.getUserId(),retrievedProfile.getGatewayID(),cm.getStorageResourceId()));
                }
            }
            //remove the user resource profile created.
            System.out.println("User Resource profile removed : " + userProfile.removeUserResourceProfile(retrievedProfile.getUserId(),retrievedProfile.getGatewayID()));
        }else{
            System.out.println("User resource profile is null");
        }
        assertTrue("App interface saved successfully", retrievedProfile != null);
    }

}
