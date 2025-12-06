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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Configuration class that loads airavata-server.properties from the correct location.
 * Respects the airavata.config.dir system property, checking file system first, then classpath.
 */
@Configuration
@PropertySource(
        value = "classpath:airavata-server.properties",
        factory = AiravataPropertiesConfiguration.AiravataPropertySourceFactory.class,
        ignoreResourceNotFound = true)
public class AiravataPropertiesConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AiravataPropertiesConfiguration.class);
    private static final String SERVER_PROPERTIES = "airavata-server.properties";
    private static final String AIRAVATA_CONFIG_DIR = "airavata.config.dir";

    /**
     * Custom PropertySourceFactory that loads airavata-server.properties
     * from airavata.config.dir if set, otherwise from classpath.
     */
    public static class AiravataPropertySourceFactory implements PropertySourceFactory {
        private final DefaultPropertySourceFactory defaultFactory = new DefaultPropertySourceFactory();

        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(
                String name, EncodedResource resource) throws IOException {

            // Try to load from airavata.config.dir first
            String configDir = System.getProperty(AIRAVATA_CONFIG_DIR);
            if (configDir != null) {
                try {
                    String configDirPath = configDir.endsWith(File.separator) ? configDir : configDir + File.separator;
                    String filePath = configDirPath + SERVER_PROPERTIES;
                    File configFile = new File(filePath);

                    if (configFile.exists() && configFile.isFile()) {
                        logger.info("Loading airavata-server.properties from: {}", configFile.getAbsolutePath());
                        Properties props = new Properties();
                        try (InputStream is = new FileInputStream(configFile)) {
                            props.load(is);
                        }
                        return new org.springframework.core.env.PropertiesPropertySource(
                                "airavata-server-properties", props);
                    } else {
                        logger.debug("Properties file not found at {}, falling back to classpath", filePath);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to load properties from airavata.config.dir, falling back to classpath", e);
                }
            }

            // Fall back to classpath
            URL classpathUrl =
                    AiravataPropertiesConfiguration.class.getClassLoader().getResource(SERVER_PROPERTIES);
            if (classpathUrl != null) {
                logger.info("Loading airavata-server.properties from classpath: {}", classpathUrl);
                return defaultFactory.createPropertySource(name, resource);
            }

            logger.warn("airavata-server.properties not found in airavata.config.dir or classpath");
            // Return empty property source if not found
            return new org.springframework.core.env.PropertiesPropertySource(
                    "airavata-server-properties", new Properties());
        }
    }
}
