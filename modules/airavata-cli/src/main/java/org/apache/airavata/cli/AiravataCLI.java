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
package org.apache.airavata.cli;

import org.apache.airavata.cli.commands.AccountCommand;
import org.apache.airavata.cli.commands.ApplicationCommand;
import org.apache.airavata.cli.commands.ComputeCommand;
import org.apache.airavata.cli.commands.GroupCommand;
import org.apache.airavata.cli.commands.InitCommand;
import org.apache.airavata.cli.commands.ProjectCommand;
import org.apache.airavata.cli.commands.ServiceCommand;
import org.apache.airavata.cli.commands.StorageCommand;
import org.apache.airavata.cli.commands.TestCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Airavata CLI - Command-line interface for Airavata administration.
 *
 * Provides commands for:
 * - Database initialization and management
 * - Root account management
 * - Storage and compute resource registration
 * - Group resource profile management
 * - Application management
 * - Service management (start/stop/restart background services)
 * - Testing and validation
 *
 * Usage: airavata-cli <command> [subcommand] [flags]
 *
 * Examples:
 *   airavata-cli init --clean
 *   airavata-cli account init --username=admin --password=pass --gateway=my-gateway
 *   airavata-cli storage register --name=storage1 --hostname=host.example.com
 *   airavata-cli compute register --name=compute1 --hostname=compute.example.com
 *   airavata-cli help
 */
@Component
@Order(1) // Run early, before other CommandLineRunners
@ConditionalOnProperty(name = "airavata.cli.enabled", havingValue = "true", matchIfMissing = true)
@CommandLine.Command(
        name = "airavata",
        description = "Airavata CLI - Command-line interface for Airavata administration",
        subcommands = {
            InitCommand.class,
            AccountCommand.class,
            ProjectCommand.class,
            StorageCommand.class,
            ComputeCommand.class,
            GroupCommand.class,
            ApplicationCommand.class,
            ServiceCommand.class,
            TestCommand.class
        },
        mixinStandardHelpOptions = true)
public class AiravataCLI implements CommandLineRunner {

    private final IFactory factory;
    private final CommandLine commandLine;

    public AiravataCLI(IFactory factory) {
        this.factory = factory;
        this.commandLine = new CommandLine(this, factory);
    }

    @Override
    public void run(String... args) throws Exception {
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
