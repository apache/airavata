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
package org.apache.airavata.cli.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesManager {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesManager.class);

    /**
     * Get the path to application.properties file.
     * Checks in order:
     * 1. AIRAVATA_CONFIG_DIR environment variable (defaults to AIRAVATA_HOME/conf if not set)
     * 2. AIRAVATA_HOME/conf directory
     * 3. Current directory
     */
    public static Path getPropertiesFilePath() {
        String configDir = System.getenv("AIRAVATA_CONFIG_DIR");
        String airavataHome = System.getenv("AIRAVATA_HOME");

        // If AIRAVATA_CONFIG_DIR is not set, default to AIRAVATA_HOME/conf
        if ((configDir == null || configDir.isEmpty()) && airavataHome != null && !airavataHome.isEmpty()) {
            configDir = Paths.get(airavataHome, "conf").toString();
        }

        // Try AIRAVATA_CONFIG_DIR (or defaulted value) first
        if (configDir != null && !configDir.isEmpty()) {
            var path = Paths.get(configDir, "application.properties");
            if (Files.exists(path)) {
                return path;
            }
        }

        // Fallback to AIRAVATA_HOME/conf if AIRAVATA_CONFIG_DIR was not set and file doesn't exist
        if (airavataHome != null && !airavataHome.isEmpty()) {
            var path = Paths.get(airavataHome, "conf", "application.properties");
            if (Files.exists(path)) {
                return path;
            }
        }

        // Try current directory
        var path = Paths.get("application.properties");
        if (Files.exists(path)) {
            return path;
        }

        // Default to conf directory relative to current
        return Paths.get("conf", "application.properties");
    }

    /**
     * Read properties from file.
     */
    public static Properties readProperties() throws IOException {
        Path propertiesFile = getPropertiesFilePath();
        var props = new Properties();

        if (Files.exists(propertiesFile)) {
            try (InputStream is = Files.newInputStream(propertiesFile)) {
                props.load(is);
            }
        } else {
            logger.warn("Properties file not found at: {}", propertiesFile);
        }

        return props;
    }

    /**
     * Write properties to file.
     */
    public static void writeProperties(Properties props) throws IOException {
        Path propertiesFile = getPropertiesFilePath();

        // Create parent directories if they don't exist
        if (propertiesFile.getParent() != null) {
            Files.createDirectories(propertiesFile.getParent());
        }

        // Read existing file to preserve comments and order
        List<String> lines = new ArrayList<>();
        if (Files.exists(propertiesFile)) {
            lines = Files.readAllLines(propertiesFile);
        }

        // Update properties in the file
        List<String> updatedLines = updatePropertiesInLines(lines, props);

        // Write back to file
        try (BufferedWriter writer = Files.newBufferedWriter(propertiesFile)) {
            for (String line : updatedLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Update property value in lines, preserving comments and structure.
     */
    private static List<String> updatePropertiesInLines(List<String> lines, Properties props) {
        var updatedLines = new ArrayList<String>();

        for (String line : lines) {
            String trimmed = line.trim();

            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                updatedLines.add(line);
                continue;
            }

            // Check if this line contains a property we need to update
            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex > 0) {
                String key = trimmed.substring(0, equalsIndex).trim();
                String value = props.getProperty(key);

                if (value != null) {
                    // Update the property value
                    String updatedLine = key + "=" + value;
                    // Preserve original indentation
                    int leadingSpaces = line.length() - line.trim().length();
                    updatedLines.add(" ".repeat(leadingSpaces) + updatedLine);
                    props.remove(key); // Mark as processed
                    continue;
                }
            }

            updatedLines.add(line);
        }

        // Add any new properties that weren't in the file
        for (String key : props.stringPropertyNames()) {
            updatedLines.add(key + "=" + props.getProperty(key));
        }

        return updatedLines;
    }

    /**
     * Update a single property value.
     */
    public static void updateProperty(String key, String value) throws IOException {
        var props = readProperties();
        props.setProperty(key, value);
        writeProperties(props);
    }

    /**
     * Update a boolean property value.
     */
    public static void updateBooleanProperty(String key, boolean value) throws IOException {
        updateProperty(key, String.valueOf(value));
    }
}
