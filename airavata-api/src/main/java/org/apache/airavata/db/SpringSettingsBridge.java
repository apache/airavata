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
package org.apache.airavata.db;

import org.apache.airavata.config.ApplicationSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Bridges Spring datasource configuration into {@link ApplicationSettings} so that legacy code
 * reading {@code airavata.jdbc.*} transparently receives the same values as
 * {@code spring.datasource.*}. Runs in the constructor (not @PostConstruct) to ensure values
 * are available before any other bean reads them.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringSettingsBridge {

    public SpringSettingsBridge(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        ApplicationSettings.setOverride("airavata.jdbc.url", url);
        ApplicationSettings.setOverride("airavata.jdbc.user", username);
        ApplicationSettings.setOverride("airavata.jdbc.password", password);
        ApplicationSettings.setOverride("airavata.jdbc.driver", driverClassName);
    }
}
