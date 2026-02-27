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
import java.util.HashMap;
import org.apache.airavata.bootstrap.AiravataServer;
import org.apache.airavata.cli.communication.ServiceSocketClient;
import org.apache.airavata.cli.util.ProcessManager;
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
            names = {"-d", "--detach"},
            description = "Run server in background (daemon)")
    private boolean detach = false;

    @Option(
            names = {"--dev"},
            description = "Enable hot-reload (Spring Boot DevTools restart on classpath changes)")
    private boolean dev = false;

    @Override
    public void run() {
        // Resolve airavata.home from system property or environment variable
        String airavataHome = System.getProperty("airavata.home");
        if (airavataHome == null || airavataHome.isEmpty()) {
            // Fallback to AIRAVATA_HOME environment variable
            airavataHome = System.getenv("AIRAVATA_HOME");
            if (airavataHome == null || airavataHome.isEmpty()) {
                System.err.println(
                        "Error: airavata.home system property or AIRAVATA_HOME environment variable must be set.");
                System.exit(1);
                return;
            }
        }

        // Set airavata.home system property if not already set
        if (System.getProperty("airavata.home") == null) {
            System.setProperty("airavata.home", airavataHome);
        }

        // Derive configDir from airavata.home
        var airavataHomeFile = new File(airavataHome);
        var confDir = new File(airavataHomeFile, "conf");
        if (!confDir.exists() || !confDir.isDirectory()) {
            System.err.println("Error: Config directory does not exist at: " + confDir.getAbsolutePath());
            System.err.println("Please ensure airavata.home points to the correct Airavata installation directory.");
            System.exit(1);
            return;
        }
        String resolvedConfigDir = confDir.getAbsolutePath();

        if (ServiceSocketClient.socketExists(resolvedConfigDir)) {
            System.err.println("Error: Airavata service is already running (socket exists). Stop it first.");
            System.exit(1);
            return;
        }

        if (!detach) {
            logger.info(
                    "Starting Airavata services in foreground with airavata.home: {}, config directory: {}",
                    airavataHome,
                    resolvedConfigDir);
            System.setProperty("airavata.cli.enabled", "false");
            System.setProperty("airavata.server.enabled", "true");

            // Set ALL gRPC keepalive/duration properties as system properties to prevent NullPointerException
            // Spring Boot gRPC's DefaultServerFactoryPropertyMapper requires ALL Duration fields to be non-null
            System.setProperty("spring.grpc.server.port", "9090");
            System.setProperty("spring.grpc.server.enable-keep-alive", "true");
            System.setProperty("spring.grpc.server.keepalive-time", "30s");
            System.setProperty("spring.grpc.server.keepalive-timeout", "5s");
            System.setProperty("spring.grpc.server.permit-keepalive-time", "5m");
            System.setProperty("spring.grpc.server.permit-keepalive-without-calls", "true");
            System.setProperty("spring.grpc.server.max-connection-idle", "0s");
            System.setProperty("spring.grpc.server.max-connection-age", "0s");
            System.setProperty("spring.grpc.server.max-connection-age-grace", "0s");
            System.setProperty("spring.grpc.server.shutdown-grace-period", "30s");
            System.setProperty("spring.grpc.server.max-inbound-message-size", "100MB");
            System.setProperty("spring.grpc.server.max-inbound-metadata-size", "8KB");
            // Disable the property mapper to rely only on ServerBuilderCustomizer
            System.setProperty("spring.boot.grpc.server.property-mapper.enabled", "false");

            if (dev) {
                System.setProperty("spring.devtools.restart.enabled", "true");
            }

            var app = new SpringApplication(AiravataServer.class);
            var defaultProps = new HashMap<String, Object>();
            defaultProps.put("spring.main.allow-bean-definition-overriding", "true");
            defaultProps.put("spring.classformat.ignore", "true");
            defaultProps.put("spring.main.lazy-initialization", "true");
            defaultProps.put("airavata.cli.enabled", "false");
            defaultProps.put("airavata.server.enabled", "true");
            if (dev) {
                defaultProps.put("spring.devtools.restart.enabled", true);
            }

            app.setDefaultProperties(defaultProps);
            app.setRegisterShutdownHook(true);
            // Start the application and keep it running
            var context = app.run();
            // Keep the main thread alive so the application doesn't shut down
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                logger.info("Server interrupted, shutting down...");
                Thread.currentThread().interrupt();
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
                            var f = new File(path);
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

                Process process = ProcessManager.startServiceProcess(airavataHome, jarPath, nativeBinaryPath);
                System.out.println("Airavata service started in background (PID: " + process.pid() + ")");
                System.out.println("Socket: " + ServiceSocketClient.getSocketPath(resolvedConfigDir));
                System.out.println("Logs: " + new File(resolvedConfigDir, "logs").getAbsolutePath());
                System.exit(0);
            } catch (IOException e) {
                System.err.println("Error: Failed to start Airavata service: " + e.getMessage());
                logger.error("Failed to start service process", e);
                System.exit(1);
            }
        }
    }
}
