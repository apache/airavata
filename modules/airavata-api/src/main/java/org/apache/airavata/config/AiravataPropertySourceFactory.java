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
import java.net.URLDecoder;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Custom PropertySourceFactory that loads airavata.properties from configDir.
 * 
 * <p>Resolution precedence for airavataHome:
 * <ol>
 *   <li>System property -Dairavata.home=XXX (highest priority)</li>
 *   <li>Resources root (IDE mode: modules/distribution/src/main/resources)</li>
 * </ol>
 * 
 * <p>Fails immediately if configDir is not set or airavata.properties is not found.
 */
public class AiravataPropertySourceFactory implements PropertySourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(AiravataPropertySourceFactory.class);
    private static final String SERVER_PROPERTIES = "airavata.properties";

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {

        // Resolve airavataHome (system property → IDE mode)
        String airavataHome = resolveAiravataHome();
        
        // Validate that {airavataHome}/conf exists
        File confDir = new File(airavataHome, "conf");
        if (!confDir.exists() || !confDir.isDirectory()) {
            throw new IllegalStateException("Config directory does not exist at " + confDir.getAbsolutePath()
                    + ". Please ensure airavata.home points to the correct Airavata installation directory.");
        }

        // Load airavata.properties from {airavataHome}/conf
        String configDirPath = confDir.getAbsolutePath();
        String filePath = configDirPath + File.separator + SERVER_PROPERTIES;
        File configFile = new File(filePath);

        if (!configFile.exists() || !configFile.isFile()) {
            throw new IllegalStateException("airavata.properties not found in configDir: " + filePath);
        }

        logger.info("Loading airavata.properties from: {}", configFile.getAbsolutePath());
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(configFile)) {
            props.load(is);
        }
        // Log a sample property to verify loading
        String registryUrl = props.getProperty("database.registry.url");
        logger.debug("Loaded database.registry.url: {}", registryUrl != null ? "found" : "not found");
        return new org.springframework.core.env.PropertiesPropertySource("airavata.properties", props);
    }

    /**
     * Resolve airavataHome using system property → IDE mode precedence.
     */
    private String resolveAiravataHome() {
        // Check system property first (highest priority)
        String systemPropertyHome = System.getProperty("airavata.home");
        if (systemPropertyHome != null && !systemPropertyHome.isEmpty()) {
            logger.debug("Using airavata.home from system property: {}", systemPropertyHome);
            return systemPropertyHome;
        }

        // IDE mode: Try to resolve resources root
        String resourcesRoot = resolveResourcesRoot();
        if (resourcesRoot != null) {
            logger.info("IDE mode detected: using resources root as airavata.home: {}", resourcesRoot);
            return resourcesRoot;
        }

        throw new IllegalStateException(
                "airavata.home is not set. Please set -Dairavata.home=XXX or ensure conf/airavata.properties is on classpath for IDE mode.");
    }

    /**
     * Resolve the resources root path in IDE mode.
     * Attempts to find modules/distribution/src/main/resources by locating conf/airavata.properties on classpath.
     *
     * @return Absolute path to resources root, or null if not found
     */
    private String resolveResourcesRoot() {
        try {
            // Try to locate conf/airavata.properties on classpath
            java.net.URL resourceUrl =
                    AiravataPropertySourceFactory.class.getClassLoader().getResource("conf/airavata.properties");
            if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
                // Extract filesystem path
                String resourcePath = resourceUrl.getPath();
                // Remove URL encoding if present
                if (resourcePath.contains("%20")) {
                    resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
                }
                // Remove leading slash on Windows
                if (resourcePath.startsWith("/")
                        && System.getProperty("os.name").toLowerCase().contains("win")) {
                    resourcePath = resourcePath.substring(1);
                }
                // Navigate up from conf/airavata.properties to resources root
                java.io.File resourceFile = new java.io.File(resourcePath);
                java.io.File confDir = resourceFile.getParentFile(); // conf/
                if (confDir != null && confDir.getName().equals("conf")) {
                    java.io.File resourcesRoot = confDir.getParentFile(); // resources root
                    if (resourcesRoot != null && resourcesRoot.exists() && resourcesRoot.isDirectory()) {
                        return resourcesRoot.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not resolve resources root", e);
        }
        return null;
    }
}

