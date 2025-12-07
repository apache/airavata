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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Utils {
    private static Utils instance;

    @Autowired
    private AiravataServerProperties properties;

    @jakarta.annotation.PostConstruct
    public void init() {
        instance = this;
    }

    private String getJDBCURLImpl() {
        return properties.getDatabase().getProfileService().getJdbcUrl();
    }

    private String getHostImpl() {
        try {
            String jdbcURL = getJDBCURLImpl();
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

    private int getPortImpl() {
        try {
            String jdbcURL = getJDBCURLImpl();
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

    private String getJDBCUserImpl() {
        return properties.getDatabase().getProfileService().getJdbcUser();
    }

    private String getValidationQueryImpl() {
        return properties.getDatabase().getProfileService().getValidationQuery();
    }

    private String getJDBCPasswordImpl() {
        return properties.getDatabase().getProfileService().getJdbcPassword();
    }

    private String getJDBCDriverImpl() {
        return properties.getDatabase().getProfileService().getJdbcDriver();
    }

    // Static methods for backward compatibility
    public static String getJDBCURL() {
        return instance != null ? instance.getJDBCURLImpl() : null;
    }

    public static String getHost() {
        return instance != null ? instance.getHostImpl() : null;
    }

    public static int getPort() {
        return instance != null ? instance.getPortImpl() : -1;
    }

    public static String getJDBCUser() {
        return instance != null ? instance.getJDBCUserImpl() : null;
    }

    public static String getValidationQuery() {
        return instance != null ? instance.getValidationQueryImpl() : null;
    }

    public static String getJDBCPassword() {
        return instance != null ? instance.getJDBCPasswordImpl() : null;
    }

    public static String getJDBCDriver() {
        return instance != null ? instance.getJDBCDriverImpl() : null;
    }
}
