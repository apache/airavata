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
package org.apache.airavata.service.profile.client.util;

import java.net.URL;
import java.util.Properties;

/**
 * Created by goshenoy on 3/23/17.
 */
public class ProfileServiceClientUtil {

    private static final String PROFILE_SERVICE_SERVER_HOST = "profile.service.server.host";
    private static final String PROFILE_SERVICE_SERVER_PORT = "profile.service.server.port";
    private static final String PROFILE_CLIENT_SAMPLE_PROPERTIES = "profile-client-sample.properties";

    private static Properties properties;
    private static Exception propertyLoadException;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        URL url = ProfileServiceClientUtil.class.getClassLoader().getResource(PROFILE_CLIENT_SAMPLE_PROPERTIES);
        try {
            properties = new Properties();
            properties.load(url.openStream());
        } catch (Exception ex) {
            propertyLoadException = ex;
        }
    }

    public static String  getProfileServiceServerHost() throws Exception {
        validateSuccessfullPropertyLoad();
        return properties.getProperty(PROFILE_SERVICE_SERVER_HOST);
    }

    public static int getProfileServiceServerPort() throws Exception {
        validateSuccessfullPropertyLoad();
        return Integer.parseInt(properties.getProperty(PROFILE_SERVICE_SERVER_PORT));
    }

    private static void validateSuccessfullPropertyLoad() throws Exception {
        if (propertyLoadException != null) {
            throw new Exception(propertyLoadException.getMessage(), propertyLoadException);
        }
    }
}
