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
package org.apache.airavata.cli.commands;

import java.io.File;
import java.io.IOException;
import org.apache.airavata.cli.communication.ServiceSocketClient;
import org.apache.airavata.cli.util.ProcessManager;
import org.apache.airavata.AiravataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

/**
 * Serve command to start all Airavata services.
 */
@Command(
        name = "serve",
        description = "Start all Airavata services",
        mixinStandardHelpOptions = true,
        subcommands = {HelpCommand.class})
public class ServeCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ServeCommand.class);

    @Option(
            names = {"--config-dir"},
            required = true,
            description = "Path to directory containing all configuration files")
    private String configDir;

    @Option(
            names = {"--foreground"},
            description = "Run server in foreground (current process, for debugging)")
    private boolean foreground = false;

    @Override
    public void run() {
        File configDirFile = new File(configDir);
        if (!configDirFile.exists() || !configDirFile.isDirectory()) {
            System.err.println("Error: Config directory does not exist or is not a directory: " + configDir);
            System.exit(1);
            return;
        }

        if (ServiceSocketClient.socketExists(configDir)) {
            System.err.println("Error: Airavata service is already running (socket exists). Stop it first.");
            System.exit(1);
            return;
        }

        if (foreground) {
            logger.info("Starting Airavata services in foreground with config directory: {}", configDir);
            System.setProperty("airavata.config.dir", configDir);
            System.setProperty("airavata.home", configDirFile.getParent() != null ? configDirFile.getParent() : ".");
            System.setProperty("airavata.cli.enabled", "false");
            System.setProperty("airavata.server.enabled", "true");
            SpringApplication app = new SpringApplication(AiravataServer.class);
            app.setDefaultProperties(java.util.Map.of(
                    "spring.main.allow-bean-definition-overriding", "true",
                    "spring.classformat.ignore", "true",
                    "airavata.cli.enabled", "false",
                    "airavata.server.enabled", "true"
            ));
            app.setRegisterShutdownHook(true);
            try {
                app.run();
            } catch (Exception e) {
                logger.error("Server failed to start", e);
                System.exit(1);
            }
        } else {
            try {
                String jarPath = null;
                String nativeBinaryPath = null;

                if (ProcessManager.isNativeBinary()) {
                    nativeBinaryPath = ProcessManager.getCurrentExecutablePath();
                    if (nativeBinaryPath == null) {
                        String[] possiblePaths = {
                            System.getProperty("user.dir") + "/airavata",
                            System.getProperty("user.dir") + "/bin/airavata",
                            "/usr/local/bin/airavata",
                            "/usr/bin/airavata"
                        };
                        for (String path : possiblePaths) {
                            File f = new File(path);
                            if (f.exists() && f.canExecute()) {
                                nativeBinaryPath = path;
                                break;
                            }
                        }
                    }
                } else {
                    jarPath = ProcessManager.getCurrentExecutablePath();
                }

                if (jarPath == null && nativeBinaryPath == null) {
                    System.err.println("Error: Cannot determine executable path (JAR or native binary)");
                    System.exit(1);
                    return;
                }

                Process process = ProcessManager.startServiceProcess(configDir, jarPath, nativeBinaryPath);
                System.out.println("Airavata service started in background (PID: " + process.pid() + ")");
                System.out.println("Socket: " + ServiceSocketClient.getSocketPath(configDir));
                System.out.println("Logs: " + new File(configDir, "logs").getAbsolutePath());
                System.exit(0);
            } catch (IOException e) {
                System.err.println("Error: Failed to start Airavata service: " + e.getMessage());
                logger.error("Failed to start service process", e);
                System.exit(1);
            }
        }
    }
}

