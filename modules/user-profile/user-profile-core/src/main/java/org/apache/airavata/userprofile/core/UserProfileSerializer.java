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

import org.apache.airavata.model.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileSerializer extends AbstractThriftSerializer<UserProfile._Fields, UserProfile>  {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileSerializer.class);

    /**
     * Returns an array of {@code <E>} enumerated values that represent the fields present in the
     * Thrift class associated with this serializer.
     *
     * @return The array of {@code <E>} enumerated values that represent the fields present in the
     * Thrift class.
     */
    @Override
    protected UserProfile._Fields[] getFieldValues() {
        return UserProfile._Fields.values();
    }

    /**
     * Returns the {@code <T>} implementation class associated with this serializer.
     *
     * @return The {@code <T>} implementation class
     */
    @Override
    protected Class<UserProfile> getThriftClass() {
        return null;
    }
}