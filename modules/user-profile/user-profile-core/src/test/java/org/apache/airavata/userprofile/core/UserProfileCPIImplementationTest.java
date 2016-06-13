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
package org.apache.airavata.userprofile.core;

import junit.framework.Assert;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.userprofile.cpi.UserProfileException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserProfileCPIImplementationTest {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileCPIImplementationTest.class);

    @Test
    public void testUserProfileCPIImplementation(){
        UserProfile userProfile  = new UserProfile();
        userProfile.setUserName("jsfsjdfsdg" + System.currentTimeMillis());
        userProfile.setGatewayId("seagrid");
        userProfile.setComments("vidfkj dfgndfkg dfkgndkjfng");
        userProfile.setCountry("USA");
        userProfile.setState(Status.ACTIVE);

        try {
            UserProfileCPIImplementation userProfileCPIImplementation = new UserProfileCPIImplementation();
            String userId = userProfileCPIImplementation.createUserProfile(userProfile);
            Assert.assertNotNull(userId);
            userProfile = userProfileCPIImplementation.getUserProfileFromUserId(userId);
            Assert.assertNotNull(userProfile);
            Assert.assertTrue(userProfile.getCountry().equals("USA"));
            userProfile.setCountry("Sri Lanka");
            userProfileCPIImplementation.updateUserProfile(userProfile);
            userProfile = userProfileCPIImplementation.getUserProfileFromUserId(userId);
            Assert.assertTrue(userProfile.getCountry().equals("Sri Lanka"));
            userProfile = userProfileCPIImplementation.getUserProfileFromUserName(userProfile.getUserName(),
                    userProfile.getGatewayId());
            Assert.assertNotNull(userProfile);
            List<UserProfile> userProfileList = userProfileCPIImplementation.getAllUserProfilesInGateway("seagrid");
            Assert.assertNotNull(userProfileList);
            userProfileCPIImplementation.deleteUserProfile(userProfile.getUserId());
            Assert.assertNull(userProfileCPIImplementation.getUserProfileFromUserId(userProfile.getUserId()));
        } catch (UserProfileException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}