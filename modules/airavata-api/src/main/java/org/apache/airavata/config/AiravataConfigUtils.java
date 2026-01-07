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
import java.net.URLDecoder;
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
    private static final String SERVER_PROPERTIES = "airavata.properties";
    private static volatile Properties cachedAiravataProperties;

    private AiravataConfigUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Resolve the resources root path in IDE mode.
     * Attempts to find modules/distribution/src/main/resources by locating conf/airavata.properties on classpath.
     *
     * @return Absolute path to resources root, or null if not found
     */
    private static String resolveResourcesRoot() {
        try {
            // Try to locate conf/airavata.properties on classpath
            java.net.URL resourceUrl =
                    AiravataConfigUtils.class.getClassLoader().getResource("conf/airavata.properties");
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

    /**
     * Get the config directory path (always {airavataHome}/conf).
     * Resolution precedence:
     * <ol>
     *   <li>System property -Dairavata.home=XXX</li>
     *   <li>Resources root (IDE mode)</li>
     * </ol>
     *
     * @return The config directory path ({airavataHome}/conf)
     * @throws IllegalStateException if airavataHome cannot be resolved or {airavataHome}/conf does not exist
     */
    public static String getConfigDir() {
        // Check system property
        String systemPropertyHome = System.getProperty("airavata.home");
        if (systemPropertyHome != null && !systemPropertyHome.isEmpty()) {
            File confDir = new File(systemPropertyHome, "conf");
            if (confDir.exists() && confDir.isDirectory()) {
                return confDir.getAbsolutePath();
            }
            throw new IllegalStateException("Config directory does not exist at " + confDir.getAbsolutePath()
                    + ". Please ensure -Dairavata.home points to the correct Airavata installation directory.");
        }

        // IDE mode: Try to resolve resources root
        String resourcesRoot = resolveResourcesRoot();
        if (resourcesRoot != null) {
            File confDir = new File(resourcesRoot, "conf");
            if (confDir.exists() && confDir.isDirectory()) {
                logger.debug("IDE mode: using resources root configDir: {}", confDir.getAbsolutePath());
                return confDir.getAbsolutePath();
            }
        }

        throw new IllegalStateException(
                "airavata.home is not set. Please set -Dairavata.home=XXX or set airavata.home in airavata.properties.");
    }

    /**
     * Load a config file from configDir.
     * The fileName should NOT include "conf/" prefix - it will be resolved relative to configDir.
     *
     * @param fileName The filename (e.g., "email-config.yml", "logback.xml", "templates/PBS_Groovy.template")
     * @return URL to the file
     * @throws IllegalStateException if configDir cannot be resolved or file is not found
     */
    public static URL loadFile(String fileName) {
        String configDir = getConfigDir(); // Will throw if not found
        try {
            // Load from filesystem: {configDir}/{fileName}
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
     * Load and cache {@code airavata.properties} from {airavataHome}/conf.
     */
    public static Properties getAiravataProperties() {
        Properties props = cachedAiravataProperties;
        if (props != null) {
            return props;
        }
        synchronized (AiravataConfigUtils.class) {
            if (cachedAiravataProperties != null) {
                return cachedAiravataProperties;
            }
            Properties loaded = new Properties();
            URL url = loadFile(SERVER_PROPERTIES); // Will throw if configDir not found or file missing
            try (InputStream is = url.openStream()) {
                loaded.load(is);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load airavata.properties from " + url, e);
            }
            cachedAiravataProperties = loaded;
            return loaded;
        }
    }

    /**
     * Lightweight, non-Spring property access for tools/Helix tasks.
     * Order: system props -> env vars -> loaded airavata.properties.
     */
    public static String getSetting(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            value = getAiravataProperties().getProperty(key);
        }
        return value != null ? value : defaultValue;
    }

    public static String getSetting(String key) {
        return getSetting(key, null);
    }
}
