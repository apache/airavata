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
package org.apache.airavata.service.profile.user.core.utils;

import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.service.profile.commons.utils.Utils;

public class UserProfileCatalogJDBCConfig implements JDBCConfig {

    @Override
    public String getURL() {
        return Utils.getJDBCURL();
    }

    @Override
    public String getDriver() {
        return Utils.getJDBCDriver();
    }

    @Override
    public String getUser() {
        return Utils.getJDBCUser();
    }

    @Override
    public String getPassword() {
        return Utils.getJDBCPassword();
    }

    @Override
    public String getValidationQuery() {
        return Utils.getValidationQuery();
    }
}
