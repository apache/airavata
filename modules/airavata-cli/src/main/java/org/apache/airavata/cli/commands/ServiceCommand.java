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
import org.apache.airavata.cli.handlers.ServiceHandler;
import org.apache.airavata.cli.handlers.ServiceHandler.ServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
        @Autowired
        private ServiceHandler serviceHandler;

        @Override
        public void run() {
            boolean airavataRunning = serviceHandler.isAiravataRunning();
            Long pid = serviceHandler.getAiravataPid();

            System.out.println("Airavata Process Status:");
            if (airavataRunning && pid != null) {
                System.out.println("  Status: Running (PID: " + pid + ")");
            } else {
                System.out.println("  Status: Not running");
            }
            System.out.println();

            java.util.List<ServiceStatus> services = serviceHandler.listServices();
            System.out.println("Background Services:");
            System.out.println(String.format("%-30s %-25s %-10s %-10s", "Service", "Thread", "Enabled", "Running"));
            System.out.println(String.format("%-30s %-25s %-10s %-10s", "-------", "------", "-------", "-------"));

            for (ServiceStatus status : services) {
                System.out.println(String.format(
                        "%-30s %-25s %-10s %-10s",
                        status.getServiceName(),
                        status.getThreadName(),
                        status.isEnabled() ? "Yes" : "No",
                        status.isRunning() ? "Yes" : "No"));
            }
        }
    }

    @Command(
            name = "status",
            description = "Check status of Airavata process and optionally a specific service",
            mixinStandardHelpOptions = true)
    public static class Status implements Runnable {
        @Autowired
        private ServiceHandler serviceHandler;

        @Option(
                names = {"--service"},
                description = "Service name to check status")
        private String serviceName;

        @Override
        public void run() {
            boolean airavataRunning = serviceHandler.isAiravataRunning();
            Long pid = serviceHandler.getAiravataPid();

            if (serviceName != null) {
                // Check specific service
                try {
                    ServiceStatus status = serviceHandler.getServiceStatus(serviceName);
                    System.out.println("Service: " + status.getServiceName());
                    System.out.println("  Thread: " + status.getThreadName());
                    System.out.println("  Enabled: " + (status.isEnabled() ? "Yes" : "No"));
                    System.out.println("  Running: " + (status.isRunning() ? "Yes" : "No"));
                    System.out.println(
                            "  Airavata Process: " + (airavataRunning ? "Running (PID: " + pid + ")" : "Not running"));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: " + e.getMessage());
                    System.err.println(
                            "Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                    System.exit(1);
                }
            } else {
                // Check overall process status
                System.out.println("Airavata Process Status:");
                if (airavataRunning && pid != null) {
                    System.out.println("  Status: Running");
                    System.out.println("  PID: " + pid);
                } else {
                    System.out.println("  Status: Not running");
                }
            }
        }
    }

    @Command(name = "start", description = "Start a service", mixinStandardHelpOptions = true)
    public static class Start implements Runnable {
        @Autowired
        private ServiceHandler serviceHandler;

        @Option(
                names = {"--service"},
                required = true,
                description = "Service name to start")
        private String serviceName;

        @Option(
                names = {"--restart-process"},
                description = "Restart Airavata process after starting service")
        private boolean restartProcess = false;

        @Override
        public void run() {
            try {
                serviceHandler.startService(serviceName);
                if (restartProcess) {
                    System.out.println("Note: Process restart functionality not yet implemented.");
                    System.out.println("Please restart Airavata manually for changes to take effect.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error updating properties: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(name = "stop", description = "Stop a service", mixinStandardHelpOptions = true)
    public static class Stop implements Runnable {
        @Autowired
        private ServiceHandler serviceHandler;

        @Option(
                names = {"--service"},
                required = true,
                description = "Service name to stop")
        private String serviceName;

        @Option(
                names = {"--restart-process"},
                description = "Restart Airavata process after stopping service")
        private boolean restartProcess = false;

        @Override
        public void run() {
            try {
                serviceHandler.stopService(serviceName);
                if (restartProcess) {
                    System.out.println("Note: Process restart functionality not yet implemented.");
                    System.out.println("Please restart Airavata manually for changes to take effect.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error updating properties: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(name = "restart", description = "Restart a service", mixinStandardHelpOptions = true)
    public static class Restart implements Runnable {
        @Autowired
        private ServiceHandler serviceHandler;

        @Option(
                names = {"--service"},
                required = true,
                description = "Service name to restart")
        private String serviceName;

        @Option(
                names = {"--restart-process"},
                description = "Restart Airavata process after restarting service")
        private boolean restartProcess = false;

        @Override
        public void run() {
            try {
                serviceHandler.restartService(serviceName);
                if (restartProcess) {
                    System.out.println("Note: Process restart functionality not yet implemented.");
                    System.out.println("Please restart Airavata manually for changes to take effect.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println("Available services: " + String.join(", ", ServiceHandler.getAvailableServices()));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error updating properties: " + e.getMessage());
                System.exit(1);
            }
        }
    }
}
