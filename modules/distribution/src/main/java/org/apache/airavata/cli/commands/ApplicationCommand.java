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

import org.apache.airavata.cli.handlers.ApplicationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "application",
        description = "Manage applications",
        subcommands = {
            HelpCommand.class,
            ApplicationCommand.Create.class,
            ApplicationCommand.Update.class,
            ApplicationCommand.Delete.class,
            ApplicationCommand.Get.class,
            ApplicationCommand.List.class,
            ApplicationCommand.Enable.class,
            ApplicationCommand.Disable.class
        })
public class ApplicationCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'application --help' to see available subcommands.");
    }

    @Command(name = "create", description = "Create a new application", mixinStandardHelpOptions = true)
    public static class Create implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--name"},
                required = true,
                description = "Application name")
        private String name;

        @Option(
                names = {"--module"},
                required = true,
                description = "Application module name")
        private String moduleName;

        @Option(
                names = {"--executable"},
                required = true,
                description = "Executable path")
        private String executable;

        @Option(
                names = {"--compute"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Option(
                names = {"--description"},
                description = "Application description")
        private String description;

        @Override
        public void run() {
            applicationHandler.createApplication(gatewayId, name, moduleName, executable, computeId, description);
        }
    }

    @Command(name = "update", description = "Update application", mixinStandardHelpOptions = true)
    public static class Update implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Application interface ID")
        private String appId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Option(
                names = {"--name"},
                description = "New application name")
        private String name;

        @Option(
                names = {"--description"},
                description = "New application description")
        private String description;

        @Override
        public void run() {
            applicationHandler.updateApplication(appId, gatewayId, name, description);
        }
    }

    @Command(name = "delete", description = "Delete application", mixinStandardHelpOptions = true)
    public static class Delete implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Application interface ID")
        private String appId;

        @Override
        public void run() {
            applicationHandler.deleteApplication(appId);
        }
    }

    @Command(name = "get", description = "Get application details", mixinStandardHelpOptions = true)
    public static class Get implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Application interface ID")
        private String appId;

        @Override
        public void run() {
            applicationHandler.getApplication(appId);
        }
    }

    @Command(name = "list", description = "List all applications", mixinStandardHelpOptions = true)
    public static class List implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            applicationHandler.listApplications(gatewayId);
        }
    }

    @Command(
            name = "enable",
            description = "Enable application for a group resource profile",
            mixinStandardHelpOptions = true)
    public static class Enable implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--app"},
                required = true,
                description = "Application interface ID")
        private String appId;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Override
        public void run() {
            applicationHandler.enableApplicationForGroup(appId, groupId);
        }
    }

    @Command(
            name = "disable",
            description = "Disable application for a group resource profile",
            mixinStandardHelpOptions = true)
    public static class Disable implements Runnable {
        @Autowired
        private ApplicationHandler applicationHandler;

        @Option(
                names = {"--app"},
                required = true,
                description = "Application interface ID")
        private String appId;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Override
        public void run() {
            applicationHandler.disableApplicationForGroup(appId, groupId);
        }
    }
}
