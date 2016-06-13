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
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileDeserializer extends AbstractThriftDeserializer<UserProfile._Fields, UserProfile>{
    private final static Logger logger = LoggerFactory.getLogger(UserProfileDeserializer.class);

    /**
     * Returns the {@code <E>} enumerated value that represents the target
     * field in the Thrift entity referenced in the JSON document.
     *
     * @param fieldName The name of the Thrift entity target field.
     * @return The {@code <E>} enumerated value that represents the target
     * field in the Thrift entity referenced in the JSON document.
     */
    @Override
    protected UserProfile._Fields getField(String fieldName) {
        return UserProfile._Fields.valueOf(fieldName);
    }

    /**
     * Creates a new instance of the Thrift entity class represented by this deserializer.
     *
     * @return A new instance of the Thrift entity class represented by this deserializer.
     */
    @Override
    protected UserProfile newInstance() {
        return new UserProfile();
    }

    /**
     * Validates that the Thrift entity instance contains all required fields after deserialization.
     *
     * @param instance A Thrift entity instance.
     * @throws TException if unable to validate the instance.
     */
    @Override
    protected void validate(UserProfile instance) throws TException {
        instance.validate();
    }
}