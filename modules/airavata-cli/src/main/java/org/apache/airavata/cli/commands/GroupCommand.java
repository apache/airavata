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

import org.apache.airavata.cli.handlers.GroupHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "group",
        description = "Manage group resource profiles",
        subcommands = {
            HelpCommand.class,
            GroupCommand.Create.class,
            GroupCommand.Update.class,
            GroupCommand.Delete.class,
            GroupCommand.Get.class,
            GroupCommand.List.class,
            GroupCommand.AddUser.class,
            GroupCommand.RemoveUser.class,
            GroupCommand.AddCompute.class,
            GroupCommand.RemoveCompute.class,
            GroupCommand.AddStorage.class,
            GroupCommand.RemoveStorage.class
        })
public class GroupCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'group --help' to see available subcommands.");
    }

    @Command(name = "create", description = "Create a new group resource profile", mixinStandardHelpOptions = true)
    public static class Create implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--name"},
                required = true,
                description = "Group name")
        private String name;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Option(
                names = {"--description"},
                description = "Group description")
        private String description;

        @Option(
                names = {"--owner"},
                description = "Group owner username")
        private String owner;

        @Override
        public void run() {
            groupHandler.createGroupResourceProfile(gatewayId, name, description, owner);
        }
    }

    @Command(name = "update", description = "Update group resource profile", mixinStandardHelpOptions = true)
    public static class Update implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Option(
                names = {"--name"},
                description = "New group name")
        private String name;

        @Override
        public void run() {
            groupHandler.updateGroupResourceProfile(groupId, name);
        }
    }

    @Command(name = "delete", description = "Delete group resource profile", mixinStandardHelpOptions = true)
    public static class Delete implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            groupHandler.deleteGroupResourceProfile(groupId, gatewayId);
        }
    }

    @Command(name = "get", description = "Get group resource profile details", mixinStandardHelpOptions = true)
    public static class Get implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Override
        public void run() {
            groupHandler.getGroupResourceProfile(groupId);
        }
    }

    @Command(name = "list", description = "List all group resource profiles", mixinStandardHelpOptions = true)
    public static class List implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--gateway"},
                description = "Filter by gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            if (gatewayId == null || gatewayId.isEmpty()) {
                System.out.println("Error: --gateway is required");
                return;
            }
            groupHandler.listGroupResourceProfiles(gatewayId);
        }
    }

    @Command(name = "add-user", description = "Add a user to a group", mixinStandardHelpOptions = true)
    public static class AddUser implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group ID")
        private String groupId;

        @Option(
                names = {"--user"},
                required = true,
                description = "User ID")
        private String userId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            groupHandler.addUserToGroup(groupId, userId, gatewayId);
        }
    }

    @Command(name = "remove-user", description = "Remove a user from a group", mixinStandardHelpOptions = true)
    public static class RemoveUser implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group ID")
        private String groupId;

        @Option(
                names = {"--user"},
                required = true,
                description = "User ID")
        private String userId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            groupHandler.removeUserFromGroup(groupId, userId, gatewayId);
        }
    }

    @Command(
            name = "add-compute",
            description = "Add a compute resource to a group resource profile",
            mixinStandardHelpOptions = true)
    public static class AddCompute implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Option(
                names = {"--compute"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Option(
                names = {"--login-user"},
                description = "Login username for this compute resource")
        private String loginUser;

        @Option(
                names = {"--credential-token"},
                description = "Credential store token")
        private String credentialToken;

        @Override
        public void run() {
            groupHandler.addComputeToGroup(groupId, computeId, loginUser, credentialToken);
        }
    }

    @Command(
            name = "remove-compute",
            description = "Remove compute resource from group resource profile",
            mixinStandardHelpOptions = true)
    public static class RemoveCompute implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Option(
                names = {"--compute"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Override
        public void run() {
            groupHandler.removeComputeFromGroup(groupId, computeId);
        }
    }

    @Command(
            name = "add-storage",
            description = "Add a storage resource to a group resource profile",
            mixinStandardHelpOptions = true)
    public static class AddStorage implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Option(
                names = {"--storage"},
                required = true,
                description = "Storage resource ID")
        private String storageId;

        @Override
        public void run() {
            groupHandler.addStorageToGroup(groupId, storageId);
        }
    }

    @Command(
            name = "remove-storage",
            description = "Remove storage resource from group resource profile",
            mixinStandardHelpOptions = true)
    public static class RemoveStorage implements Runnable {
        @Autowired
        private GroupHandler groupHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Group resource profile ID")
        private String groupId;

        @Option(
                names = {"--storage"},
                required = true,
                description = "Storage resource ID")
        private String storageId;

        @Override
        public void run() {
            groupHandler.removeStorageFromGroup(groupId, storageId);
        }
    }
}
