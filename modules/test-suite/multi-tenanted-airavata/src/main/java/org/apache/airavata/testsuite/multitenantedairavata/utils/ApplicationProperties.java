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
package org.apache.airavata.testsuite.multitenantedairavata.utils;

/**
 * Created by Ajinkya on 12/14/16.
 */
public class ApplicationProperties {

    private String applicationModuleId;
    private String applicationInterfaceId;
    private String applicationDeployId;

    public ApplicationProperties(String applicationModuleId, String applicationInterfaceId, String applicationDeployId) {
        this.applicationModuleId = applicationModuleId;
        this.applicationInterfaceId = applicationInterfaceId;
        this.applicationDeployId = applicationDeployId;
    }

    public String getApplicationModuleId() {
        return applicationModuleId;
    }

    public String getApplicationDeployId() {
        return applicationDeployId;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationProperties{");
        sb.append("applicationModuleId='").append(applicationModuleId).append('\'');
        sb.append(", applicationInterfaceId='").append(applicationInterfaceId).append('\'');
        sb.append(", applicationDeployId='").append(applicationDeployId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
