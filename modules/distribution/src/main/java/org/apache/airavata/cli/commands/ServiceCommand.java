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

import java.io.IOException;
import java.util.Map;
import org.apache.airavata.cli.communication.ServiceSocketClient;
import org.apache.airavata.cli.handlers.ServiceHandler;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "service",
        description = "Manage Airavata services",
        subcommands = {
            HelpCommand.class,
            ServiceCommand.List.class,
            ServiceCommand.Status.class,
            ServiceCommand.Start.class,
            ServiceCommand.Stop.class,
            ServiceCommand.Restart.class
        })
public class ServiceCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'service --help' to see available subcommands.");
    }

    @Command(
            name = "list",
            description = "List all available services and their status",
            mixinStandardHelpOptions = true)
    public static class List implements Runnable {
        @Override
        public void run() {
            String configDir = getConfigDir();
            if (!ServiceSocketClient.socketExists(configDir)) {
                System.err.println("Error: Airavata service is not running (socket not found)");
                System.exit(1);
                return;
            }

            try {
                Map<String, Object> response = ServiceSocketClient.listServices(configDir);
                if (!"success".equals(response.get("status")) && !"OK".equals(response.get("status"))) {
                    System.err.println("Error: " + response.get("message"));
                    System.exit(1);
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> services =
                        (java.util.List<Map<String, Object>>) data.get("services");

                System.out.println("Airavata Services:");
                System.out.println(
                        String.format("%-30s %-25s %-10s %-10s", "Service", "Display Name", "Enabled", "Running"));
                System.out.println(
                        String.format("%-30s %-25s %-10s %-10s", "-------", "------------", "-------", "-------"));

                for (Map<String, Object> service : services) {
                    System.out.println(String.format(
                            "%-30s %-25s %-10s %-10s",
                            service.get("service"),
                            service.get("displayName"),
                            Boolean.TRUE.equals(service.get("enabled")) ? "Yes" : "No",
                            Boolean.TRUE.equals(service.get("running")) ? "Yes" : "No"));
                }
            } catch (IOException e) {
                System.err.println("Error: Failed to communicate with service: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(
            name = "status",
            description = "Check status of Airavata process and optionally a specific service",
            mixinStandardHelpOptions = true)
    public static class Status implements Runnable {
        @Option(
                names = {"--service"},
                description = "Service name to check status")
        private String serviceName;

        @Override
        public void run() {
            String configDir = getConfigDir();
            if (!ServiceSocketClient.socketExists(configDir)) {
                System.err.println("Error: Airavata service is not running (socket not found)");
                System.exit(1);
                return;
            }

            try {
                Map<String, Object> response = ServiceSocketClient.getServiceStatus(configDir, serviceName);
                if (!"success".equals(response.get("status")) && !"OK".equals(response.get("status"))) {
                    System.err.println("Error: " + response.get("message"));
                    System.exit(1);
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (serviceName != null) {
                    // Specific service status
                    System.out.println("Service: " + data.get("service"));
                    System.out.println("  Display Name: " + data.get("displayName"));
                    System.out.println("  Enabled: " + (Boolean.TRUE.equals(data.get("enabled")) ? "Yes" : "No"));
                    System.out.println("  Running: " + (Boolean.TRUE.equals(data.get("running")) ? "Yes" : "No"));
                } else {
                    // Overall process status
                    System.out.println("Airavata Process Status:");
                    System.out.println("  Running: " + (Boolean.TRUE.equals(data.get("running")) ? "Yes" : "No"));
                    if (data.get("pid") != null) {
                        System.out.println("  PID: " + data.get("pid"));
                    }
                }
            } catch (IOException e) {
                System.err.println("Error: Failed to communicate with service: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(name = "start", description = "Start a service", mixinStandardHelpOptions = true)
    public static class Start implements Runnable {
        @Option(
                names = {"--service"},
                required = true,
                description = "Service name to start")
        private String serviceName;

        @Override
        public void run() {
            String configDir = getConfigDir();
            if (!ServiceSocketClient.socketExists(configDir)) {
                System.err.println("Error: Airavata service is not running (socket not found)");
                System.exit(1);
                return;
            }

            try {
                Map<String, Object> response = ServiceSocketClient.startService(configDir, serviceName);
                if ("success".equals(response.get("status")) || "OK".equals(response.get("status"))) {
                    System.out.println("✓ " + response.get("message"));
                } else {
                    System.err.println("Error: " + response.get("message"));
                    System.exit(1);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error: Failed to communicate with service: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(name = "stop", description = "Stop a service", mixinStandardHelpOptions = true)
    public static class Stop implements Runnable {
        @Option(
                names = {"--service"},
                required = true,
                description = "Service name to stop")
        private String serviceName;

        @Override
        public void run() {
            String configDir = getConfigDir();
            if (!ServiceSocketClient.socketExists(configDir)) {
                System.err.println("Error: Airavata service is not running (socket not found)");
                System.exit(1);
                return;
            }

            try {
                Map<String, Object> response = ServiceSocketClient.stopService(configDir, serviceName);
                if ("success".equals(response.get("status")) || "OK".equals(response.get("status"))) {
                    System.out.println("✓ " + response.get("message"));
                } else {
                    System.err.println("Error: " + response.get("message"));
                    System.exit(1);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error: Failed to communicate with service: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(name = "restart", description = "Restart a service", mixinStandardHelpOptions = true)
    public static class Restart implements Runnable {
        @Option(
                names = {"--service"},
                required = true,
                description = "Service name to restart")
        private String serviceName;

        @Override
        public void run() {
            String configDir = getConfigDir();
            if (!ServiceSocketClient.socketExists(configDir)) {
                System.err.println("Error: Airavata service is not running (socket not found)");
                System.exit(1);
                return;
            }

            try {
                Map<String, Object> response = ServiceSocketClient.restartService(configDir, serviceName);
                if ("success".equals(response.get("status")) || "OK".equals(response.get("status"))) {
                    System.out.println("✓ " + response.get("message"));
                } else {
                    System.err.println("Error: " + response.get("message"));
                    System.exit(1);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error: Failed to communicate with service: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Get config directory from system property or environment.
     */
    private static String getConfigDir() {
        String configDir = System.getProperty("airavata.config.dir");
        if (configDir == null || configDir.isEmpty()) {
            configDir = System.getenv("AIRAVATA_CONFIG_DIR");
        }
        if (configDir == null || configDir.isEmpty()) {
            String airavataHome = System.getenv("AIRAVATA_HOME");
            if (airavataHome != null && !airavataHome.isEmpty()) {
                configDir = airavataHome;
            }
        }
        return configDir;
    }
}
