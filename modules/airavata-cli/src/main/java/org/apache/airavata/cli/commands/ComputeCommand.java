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

import org.apache.airavata.cli.handlers.ComputeHandler;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "compute",
        description = "Manage compute resources",
        subcommands = {
            HelpCommand.class,
            ComputeCommand.Register.class,
            ComputeCommand.Update.class,
            ComputeCommand.Delete.class,
            ComputeCommand.List.class,
            ComputeCommand.Validate.class
        })
public class ComputeCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'compute --help' to see available subcommands.");
    }

    @Command(name = "register", description = "Register a new compute resource", mixinStandardHelpOptions = true)
    public static class Register implements Runnable {
        @Autowired
        private ComputeHandler computeHandler;

        @Option(
                names = {"--name"},
                required = true,
                description = "Compute resource name/ID")
        private String name;

        @Option(
                names = {"--hostname"},
                required = true,
                description = "Compute hostname")
        private String hostname;

        @Option(
                names = {"--username"},
                required = true,
                description = "Login username")
        private String username;

        @Option(
                names = {"--job-manager"},
                required = true,
                description = "Job manager type (SLURM/PBS/LSF/CONDOR/FORK/CLOUD)")
        private String jobManagerTypeStr;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Option(
                names = {"--port"},
                description = "SSH port (default: 22)",
                defaultValue = "22")
        private int port = 22;

        @Option(
                names = {"--ssh-key"},
                description = "Path to SSH private key")
        private String sshKeyPath;

        @Option(
                names = {"--passphrase"},
                description = "SSH key passphrase")
        private String passphrase;

        @Override
        public void run() {
            ResourceJobManagerType jobManagerType;
            try {
                jobManagerType = ResourceJobManagerType.valueOf(jobManagerTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid job manager type: " + jobManagerTypeStr
                        + ". Valid types: SLURM, PBS, LSF, CONDOR, FORK, CLOUD");
            }
            computeHandler.registerComputeResource(
                    gatewayId, name, hostname, port, jobManagerType, username, sshKeyPath, passphrase);
        }
    }

    @Command(name = "update", description = "Update compute resource", mixinStandardHelpOptions = true)
    public static class Update implements Runnable {
        @Autowired
        private ComputeHandler computeHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Option(
                names = {"--hostname"},
                description = "New hostname")
        private String hostname;

        @Option(
                names = {"--description"},
                description = "New description")
        private String description;

        @Option(
                names = {"--enabled"},
                description = "Enable/disable compute resource (true/false)")
        private Boolean enabled;

        @Override
        public void run() {
            computeHandler.updateComputeResource(computeId, hostname, description, enabled);
        }
    }

    @Command(name = "delete", description = "Delete compute resource", mixinStandardHelpOptions = true)
    public static class Delete implements Runnable {
        @Autowired
        private ComputeHandler computeHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Override
        public void run() {
            computeHandler.deleteComputeResource(computeId);
        }
    }

    @Command(name = "list", description = "List all registered compute resources", mixinStandardHelpOptions = true)
    public static class List implements Runnable {
        @Autowired
        private ComputeHandler computeHandler;

        @Override
        public void run() {
            computeHandler.listComputeResources();
        }
    }

    @Command(name = "validate", description = "Validate compute resource connection", mixinStandardHelpOptions = true)
    public static class Validate implements Runnable {
        @Autowired
        private ComputeHandler computeHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            computeHandler.validateComputeResource(computeId, gatewayId);
        }
    }
}
