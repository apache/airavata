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
package org.apache.airavata;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

/**
 * IDE debugging entrypoint for Airavata.
 *
 * <p>This class is designed to be run from an IDE (IntelliJ IDEA, Eclipse, etc.)
 * for debugging purposes. It starts Airavata in server mode with development-friendly
 * settings.
 *
 * <p>Usage:
 * <ul>
 *   <li>Run this class directly from your IDE</li>
 *   <li>Optionally pass config directory as program argument: <code>--config-dir=/path/to/config</code></li>
 *   <li>If no config directory is provided, it will use the current working directory</li>
 * </ul>
 *
 * <p>Example IDE run configuration:
 * <ul>
 *   <li>Main class: <code>org.apache.airavata.AiravataDebug</code></li>
 *   <li>Program arguments: <code>--config-dir=/path/to/airavata-config</code> (optional)</li>
 *   <li>VM options: <code>-Xmx2g -Xms1g</code> (optional, adjust as needed)</li>
 * </ul>
 */
public class AiravataDebug {

    private static final Logger logger = LoggerFactory.getLogger(AiravataDebug.class);

    public static void main(String[] args) {
        logger.info("Starting Airavata in debug mode...");

        String configDir = findConfigDirectory(args);
        if (configDir != null) {
            File configDirFile = new File(configDir);
            if (!configDirFile.exists() || !configDirFile.isDirectory()) {
                logger.error("Config directory does not exist or is not a directory: {}", configDir);
                System.err.println("Error: Config directory does not exist: " + configDir);
                System.exit(1);
                return;
            }
            System.setProperty("airavata.config.dir", configDir);
            System.setProperty("airavata.home", configDirFile.getParent() != null ? configDirFile.getParent() : ".");
            logger.info("Using config directory: {}", configDir);
        } else {
            String currentDir = System.getProperty("user.dir");
            System.setProperty("airavata.config.dir", currentDir);
            System.setProperty("airavata.home", currentDir);
            logger.info("Using current working directory as config directory: {}", currentDir);
        }

        System.setProperty("airavata.cli.enabled", "false");
        System.setProperty("airavata.server.enabled", "true");

        SpringApplication app = new SpringApplication(AiravataServer.class);
        app.setDefaultProperties(java.util.Map.of(
                "spring.main.allow-bean-definition-overriding", "true",
                "spring.classformat.ignore", "true",
                "airavata.cli.enabled", "false",
                "airavata.server.enabled", "true"));
        app.setRegisterShutdownHook(true);
        app.run(args);
    }

    private static String findConfigDirectory(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--config-dir") && i + 1 < args.length) {
                return args[i + 1];
            } else if (arg.startsWith("--config-dir=")) {
                return arg.substring("--config-dir=".length());
            }
        }
        return null;
    }
}
