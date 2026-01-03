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

import org.apache.airavata.cli.handlers.StorageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "storage",
        description = "Manage storage resources",
        subcommands = {
            HelpCommand.class,
            StorageCommand.Register.class,
            StorageCommand.Update.class,
            StorageCommand.Delete.class,
            StorageCommand.List.class,
            StorageCommand.Validate.class
        })
public class StorageCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'storage --help' to see available subcommands.");
    }

    @Command(name = "register", description = "Register a new storage resource", mixinStandardHelpOptions = true)
    public static class Register implements Runnable {
        @Autowired
        private StorageHandler storageHandler;

        @Option(
                names = {"--name"},
                required = true,
                description = "Storage resource name/ID")
        private String name;

        @Option(
                names = {"--hostname"},
                required = true,
                description = "Storage hostname")
        private String hostname;

        @Option(
                names = {"--username"},
                required = true,
                description = "Login username")
        private String username;

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
            storageHandler.registerStorageResource(gatewayId, name, hostname, port, username, sshKeyPath, passphrase);
        }
    }

    @Command(name = "update", description = "Update storage resource", mixinStandardHelpOptions = true)
    public static class Update implements Runnable {
        @Autowired
        private StorageHandler storageHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Storage resource ID")
        private String storageId;

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
                description = "Enable/disable storage resource (true/false)")
        private Boolean enabled;

        @Override
        public void run() {
            storageHandler.updateStorageResource(storageId, hostname, description, enabled);
        }
    }

    @Command(name = "delete", description = "Delete storage resource", mixinStandardHelpOptions = true)
    public static class Delete implements Runnable {
        @Autowired
        private StorageHandler storageHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Storage resource ID")
        private String storageId;

        @Override
        public void run() {
            storageHandler.deleteStorageResource(storageId);
        }
    }

    @Command(name = "list", description = "List all registered storage resources", mixinStandardHelpOptions = true)
    public static class List implements Runnable {
        @Autowired
        private StorageHandler storageHandler;

        @Override
        public void run() {
            storageHandler.listStorageResources();
        }
    }

    @Command(name = "validate", description = "Validate storage resource connection", mixinStandardHelpOptions = true)
    public static class Validate implements Runnable {
        @Autowired
        private StorageHandler storageHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Storage resource ID")
        private String storageId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            storageHandler.validateStorageResource(storageId, gatewayId);
        }
    }
}
