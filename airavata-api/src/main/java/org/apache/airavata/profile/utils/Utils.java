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
package org.apache.airavata.profile.utils;

import java.net.URI;
import org.apache.airavata.config.AiravataServerProperties;
import org.springframework.stereotype.Component;

@Component("profileUtils")
public class Utils {

    private final AiravataServerProperties properties;

    public Utils(AiravataServerProperties properties) {
        this.properties = properties;
    }

    public String getJDBCURL() {
        return properties.database.profile.url;
    }

    public String getHost() {
        try {
            String jdbcURL = getJDBCURL();
            if (jdbcURL != null && jdbcURL.length() > 5) {
                String cleanURI = jdbcURL.substring(5);
                URI uri = URI.create(cleanURI);
                return uri.getHost();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public int getPort() {
        try {
            String jdbcURL = getJDBCURL();
            if (jdbcURL != null && jdbcURL.length() > 5) {
                String cleanURI = jdbcURL.substring(5);
                URI uri = URI.create(cleanURI);
                return uri.getPort();
            }
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    public String getJDBCUser() {
        return properties.database.profile.user;
    }

    public String getValidationQuery() {
        return properties.database.profile.validationQuery;
    }

    public String getJDBCPassword() {
        return properties.database.profile.password;
    }

    public String getJDBCDriver() {
        return properties.database.profile.driver;
    }
}
