/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.config;

import org.apache.airavata.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provider to make RegistryService available to components that can't use Spring injection
 * (e.g., workflow managers started via main methods).
 */
@Component
public class RegistryServiceProvider {

    private static RegistryService instance;

    @Autowired
    public void setRegistryService(RegistryService registryService) {
        RegistryServiceProvider.instance = registryService;
    }

    public static RegistryService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RegistryService not initialized. Spring context may not be ready.");
        }
        return instance;
    }
}
