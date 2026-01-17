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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Airavata configuration directory and file access.
 * Provides static methods for resolving config directory, loading files, and accessing properties.
 *
 * <p>These utilities are used by tools, Helix tasks, and other non-Spring contexts
 * that need to access configuration files directly.
 */
public class AiravataConfigUtils {

    private static final Logger logger = LoggerFactory.getLogger(AiravataConfigUtils.class);
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static volatile Properties cachedProperties;

    private AiravataConfigUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Resolve the resources root path in IDE mode.
     * Attempts to find resources root by locating application.properties on classpath.
     *
     * @return Absolute path to resources root, or null if not found
     */
    private static String resolveResourcesRoot() {
        try {
            // Try to locate application.properties on classpath
            java.net.URL resourceUrl =
                    AiravataConfigUtils.class.getClassLoader().getResource(APPLICATION_PROPERTIES);
            if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
                String resourcePath = resourceUrl.getPath();
                // Remove URL encoding if present
                if (resourcePath.contains("%20")) {
                    resourcePath = java.net.URLDecoder.decode(resourcePath, "UTF-8");
                }
                // Remove leading slash on Windows
                if (resourcePath.startsWith("/")
                        && System.getProperty("os.name").toLowerCase().contains("win")) {
                    resourcePath = resourcePath.substring(1);
                }
                // Navigate up from application.properties to resources root
                java.io.File resourceFile = new java.io.File(resourcePath);
                java.io.File resourcesRoot = resourceFile.getParentFile();
                if (resourcesRoot != null && resourcesRoot.exists() && resourcesRoot.isDirectory()) {
                    return resourcesRoot.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not resolve resources root", e);
        }
        return null;
    }

    /**
     * Get the config directory path.
     * Resolution precedence:
     * <ol>
     *   <li>System property -Dairavata.home=XXX (returns {airavataHome}/conf)</li>
     *   <li>Resources root (IDE mode, returns resources directory)</li>
     * </ol>
     *
     * @return The config directory path
     * @throws IllegalStateException if config directory cannot be resolved
     */
    public static String getConfigDir() {
        // Check system property
        String systemPropertyHome = System.getProperty("airavata.home");
        if (systemPropertyHome != null && !systemPropertyHome.isEmpty()) {
            File confDir = new File(systemPropertyHome, "conf");
            if (confDir.exists() && confDir.isDirectory()) {
                return confDir.getAbsolutePath();
            }
            // Fall back to airavata.home itself if conf doesn't exist
            File homeDir = new File(systemPropertyHome);
            if (homeDir.exists() && homeDir.isDirectory()) {
                return homeDir.getAbsolutePath();
            }
            throw new IllegalStateException("Config directory does not exist at " + confDir.getAbsolutePath()
                    + ". Please ensure -Dairavata.home points to the correct Airavata installation directory.");
        }

        // IDE mode: Try to resolve resources root
        String resourcesRoot = resolveResourcesRoot();
        if (resourcesRoot != null) {
            logger.debug("IDE mode: using resources root configDir: {}", resourcesRoot);
            return resourcesRoot;
        }

        throw new IllegalStateException(
                "airavata.home is not set. Please set -Dairavata.home=XXX.");
    }

    /**
     * Load a config file from configDir.
     *
     * @param fileName The filename (e.g., "email-config.yml", "logback.xml")
     * @return URL to the file
     * @throws IllegalStateException if configDir cannot be resolved or file is not found
     */
    public static URL loadFile(String fileName) {
        String configDir = getConfigDir();
        try {
            String configDirPath = configDir.endsWith(File.separator) ? configDir : configDir + File.separator;
            String filePath = configDirPath + fileName;
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                logger.debug("Loading file from configDir: {}", file.getAbsolutePath());
                return file.toURI().toURL();
            }
            throw new IllegalStateException("Config file not found: " + fileName + " in configDir: " + configDir);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error parsing the file path from configDir: " + configDir, e);
        }
    }

    /**
     * Load and cache application.properties from classpath or config directory.
     */
    public static Properties getProperties() {
        Properties props = cachedProperties;
        if (props != null) {
            return props;
        }
        synchronized (AiravataConfigUtils.class) {
            if (cachedProperties != null) {
                return cachedProperties;
            }
            Properties loaded = new Properties();
            // Try classpath first
            try (InputStream is = AiravataConfigUtils.class.getClassLoader()
                    .getResourceAsStream(APPLICATION_PROPERTIES)) {
                if (is != null) {
                    loaded.load(is);
                    cachedProperties = loaded;
                    return loaded;
                }
            } catch (Exception e) {
                logger.debug("Could not load application.properties from classpath", e);
            }
            // Fall back to file system
            try {
                URL url = loadFile(APPLICATION_PROPERTIES);
                try (InputStream is = url.openStream()) {
                    loaded.load(is);
                }
                cachedProperties = loaded;
                return loaded;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load application.properties", e);
            }
        }
    }

    /**
     * Lightweight, non-Spring property access for tools/Helix tasks.
     * Order: system props -> env vars -> loaded application.properties.
     */
    public static String getSetting(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            value = getProperties().getProperty(key);
        }
        return value != null ? value : defaultValue;
    }

    public static String getSetting(String key) {
        return getSetting(key, null);
    }
}
