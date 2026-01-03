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

import org.apache.airavata.cli.handlers.ProjectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "project",
        description = "Manage projects",
        subcommands = {
            HelpCommand.class,
            ProjectCommand.Create.class,
            ProjectCommand.Update.class,
            ProjectCommand.Delete.class,
            ProjectCommand.List.class,
            ProjectCommand.Get.class,
            ProjectCommand.AddUser.class,
            ProjectCommand.RemoveUser.class,
            ProjectCommand.AddGroup.class,
            ProjectCommand.RemoveGroup.class
        })
public class ProjectCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'project --help' to see available subcommands.");
    }

    @Command(name = "create", description = "Create a new project", mixinStandardHelpOptions = true)
    public static class Create implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--name"},
                required = true,
                description = "Project name")
        private String name;

        @Option(
                names = {"--owner"},
                required = true,
                description = "Project owner")
        private String owner;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Option(
                names = {"--description"},
                description = "Project description")
        private String description;

        @Override
        public void run() {
            projectHandler.createProject(name, owner, gatewayId, description);
        }
    }

    @Command(name = "update", description = "Update project settings", mixinStandardHelpOptions = true)
    public static class Update implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

        @Option(
                names = {"--name"},
                description = "New project name")
        private String name;

        @Option(
                names = {"--description"},
                description = "New project description")
        private String description;

        @Option(
                names = {"--owner"},
                description = "New project owner")
        private String owner;

        @Override
        public void run() {
            projectHandler.updateProject(projectId, name, description, owner);
        }
    }

    @Command(name = "delete", description = "Delete a project", mixinStandardHelpOptions = true)
    public static class Delete implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

        @Override
        public void run() {
            projectHandler.deleteProject(projectId);
        }
    }

    @Command(name = "list", description = "List all projects", mixinStandardHelpOptions = true)
    public static class List implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            projectHandler.listProjects(gatewayId);
        }
    }

    @Command(name = "get", description = "Get project details", mixinStandardHelpOptions = true)
    public static class Get implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

        @Override
        public void run() {
            projectHandler.getProject(projectId);
        }
    }

    @Command(name = "add-user", description = "Add a user to a project", mixinStandardHelpOptions = true)
    public static class AddUser implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

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
            projectHandler.addUserToProject(projectId, userId, gatewayId);
        }
    }

    @Command(name = "remove-user", description = "Remove a user from a project", mixinStandardHelpOptions = true)
    public static class RemoveUser implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

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
            projectHandler.removeUserFromProject(projectId, userId, gatewayId);
        }
    }

    @Command(name = "add-group", description = "Add a group to a project", mixinStandardHelpOptions = true)
    public static class AddGroup implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group ID")
        private String groupId;

        @Override
        public void run() {
            projectHandler.addGroupToProject(projectId, groupId);
        }
    }

    @Command(name = "remove-group", description = "Remove a group from a project", mixinStandardHelpOptions = true)
    public static class RemoveGroup implements Runnable {

        @Autowired
        private ProjectHandler projectHandler;

        @Option(
                names = {"--id"},
                required = true,
                description = "Project ID")
        private String projectId;

        @Option(
                names = {"--group"},
                required = true,
                description = "Group ID")
        private String groupId;

        @Override
        public void run() {
            projectHandler.removeGroupFromProject(projectId, groupId);
        }
    }
}
