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
package org.apache.airavata.common.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPAUtils
 */
public class JPAUtils {

    private static final Logger logger = LoggerFactory.getLogger(JPAUtils.class);

    /**
     * Create an {@link EntityManagerFactory} with the default settings.
     *
     * @param persistenceUnitName
     * @param jdbcConfig
     * @return {@link EntityManagerFactory}
     */
    public static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName, JDBCConfig jdbcConfig) {
        var properties = createConnectionProperties(jdbcConfig);
        return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
    }

    public static Map<String, String> createConnectionProperties(JDBCConfig jdbcConfig) {
        var properties = new HashMap<String, String>();
        properties.put("hibernate.connection.driver_class", jdbcConfig.getDriver());
        properties.put("hibernate.connection.url", jdbcConfig.getURL());
        properties.put("hibernate.connection.username", jdbcConfig.getUser());
        properties.put("hibernate.connection.password", jdbcConfig.getPassword());
        properties.put("hibernate.connection.validationQuery", jdbcConfig.getValidationQuery());
        logger.debug("Connection properties={}", properties);
        return properties;
    }
}
