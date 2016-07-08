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

import org.apache.airavata.userprofile.cpi.UserProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileCPIImplFactory {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileCPIImplFactory.class);

    private static UserProfileCPIImpl userProfileCPIImpl;

    public static UserProfileCPIImpl getRegistry() throws UserProfileException {
        try {
            if (userProfileCPIImpl == null) {
                userProfileCPIImpl = new UserProfileCPIImpl();
            }
        } catch (Exception e) {
            logger.error("Unable to create UserProfileCPIImpl instance", e);
            throw new UserProfileException(e);
        }
        return userProfileCPIImpl;
    }
}