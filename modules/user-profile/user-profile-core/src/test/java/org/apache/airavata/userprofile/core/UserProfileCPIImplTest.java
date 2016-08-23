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

public class UserProfileCPIImplTest {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileCPIImplTest.class);

  /*  @Test
    public void testUserProfileCPIImplementation(){
        UserProfile userProfile  = new UserProfile();
        userProfile.setUserName("jsfsjdfsdg" + System.currentTimeMillis());
        userProfile.setGatewayId("seagrid");
        userProfile.setComments("vidfkj dfgndfkg dfkgndkjfng");
        userProfile.setCountry("USA");
        userProfile.setState(Status.ACTIVE);

        try {
            UserProfileCPIImpl userProfileCPIImpl = new UserProfileCPIImpl();
            String userId = userProfileCPIImpl.createUserProfile(userProfile);
            Assert.assertNotNull(userId);
            userProfile = userProfileCPIImpl.getUserProfileFromUserId(userId);
            Assert.assertNotNull(userProfile);
            Assert.assertTrue(userProfile.getCountry().equals("USA"));
            userProfile.setCountry("Sri Lanka");
            userProfileCPIImpl.updateUserProfile(userProfile);
            userProfile = userProfileCPIImpl.getUserProfileFromUserId(userId);
            Assert.assertTrue(userProfile.getCountry().equals("Sri Lanka"));
            userProfile = userProfileCPIImpl.getUserProfileFromUserName(userProfile.getUserName(),
                    userProfile.getGatewayId());
            Assert.assertNotNull(userProfile);
            List<UserProfile> userProfileList = userProfileCPIImpl.getAllUserProfilesInGateway("seagrid");
            Assert.assertNotNull(userProfileList);
            userProfileCPIImpl.deleteUserProfile(userProfile.getUserId());
            Assert.assertNull(userProfileCPIImpl.getUserProfileFromUserId(userProfile.getUserId()));
        } catch (UserProfileException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }*/
}