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
package org.apache.airavata.registry.services;

import org.apache.airavata.registry.services.utils.DatabaseCreator;
import org.apache.airavata.registry.services.utils.JdbcStorage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;

public class RegistryService implements ServiceLifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    public static final String PERSISTANT_DATA = "Configuration";
    private JdbcStorage db;

    @Override
    public void startUp(ConfigurationContext configurationContext, AxisService axisService) {
        //todo have to read these properties from some configuration
        String jdbcUrl = null;
        String jdbcDriver = null;
        URL resource = this.getClass().getClassLoader().getResource("repository.properties");
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        jdbcDriver = properties.getProperty("registry.jdbc.driver");
        jdbcUrl = properties.getProperty("registry.jdbc.url");
        String jdbcUser = properties.getProperty("registry.jdbc.user");
        String jdbcPassword = properties.getProperty("registry.jdbc.password");
        jdbcUrl = jdbcUrl + "?"  + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;
        db = new JdbcStorage(10, 50, jdbcUrl, jdbcDriver, true);

        Connection conn = null;
        try {
            conn = db.connect();
            if (!DatabaseCreator.isDatabaseStructureCreated(PERSISTANT_DATA, conn)) {
                DatabaseCreator.createRegistryDatabase(conn);
                logger.info("New Database created for Registry");
            } else {
                logger.info("Database already created for Registry!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure");
        } finally {
            db.closeConnection(conn);
        }
    }

    @Override
    public void shutDown(ConfigurationContext configurationContext, AxisService axisService) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
