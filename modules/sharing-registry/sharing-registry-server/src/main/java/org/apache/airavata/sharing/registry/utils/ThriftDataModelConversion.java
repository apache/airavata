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
package org.apache.airavata.sharing.registry.utils;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.sharing.registry.models.User;


/**
 * Created by Ajinkya on 3/29/17.
 */
public class ThriftDataModelConversion {

    /**
     * Build user object from UserProfile
     * @param userProfile thrift object
     * @return
     * User corresponding to userProfile thrift
     */
    public static User getUser(UserProfile userProfile){
        User user = new User();
        user.setUserId(userProfile.getAiravataInternalUserId());
        user.setDomainId(userProfile.getGatewayId());
        user.setUserName(userProfile.getUserId());
        user.setFirstName(userProfile.getFirstName());
        user.setLastName(userProfile.getLastName());
        user.setEmail(userProfile.getEmails().get(0));
        return user;
    }
}
