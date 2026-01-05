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
package org.apache.airavata;

import org.apache.airavata.cli.commands.AccountCommand;
import org.apache.airavata.cli.commands.ApplicationCommand;
import org.apache.airavata.cli.commands.ComputeCommand;
import org.apache.airavata.cli.commands.GroupCommand;
import org.apache.airavata.cli.commands.InitCommand;
import org.apache.airavata.cli.commands.ProjectCommand;
import org.apache.airavata.cli.commands.ServeCommand;
import org.apache.airavata.cli.commands.ServiceCommand;
import org.apache.airavata.cli.commands.StorageCommand;
import org.apache.airavata.cli.commands.TestCommand;
import org.apache.airavata.config.AiravataPropertiesConfiguration;
import org.apache.airavata.config.FlywayConfig;
import org.apache.airavata.config.JpaConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Airavata CLI for Server Startup and Configuration Management.
 */
@SpringBootApplication(
        scanBasePackages = {"org.apache.airavata.cli", "org.apache.airavata.bootstrap"},
        exclude = {
            org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@Import({AiravataPropertiesConfiguration.class, JpaConfig.class, FlywayConfig.class})
@Component
@Order(1)
@ConditionalOnProperty(name = "airavata.cli.enabled", havingValue = "true", matchIfMissing = true)
@CommandLine.Command(
        name = "airavata",
        description = "Airavata CLI for Server Startup and Configuration Management",
        subcommands = {
            InitCommand.class,
            AccountCommand.class,
            ProjectCommand.class,
            StorageCommand.class,
            ComputeCommand.class,
            GroupCommand.class,
            ApplicationCommand.class,
            ServeCommand.class,
            ServiceCommand.class,
            TestCommand.class
        },
        mixinStandardHelpOptions = true)
public class AiravataCommandLine implements CommandLineRunner {

    private final CommandLine commandLine;

    public AiravataCommandLine(IFactory factory) {
        this.commandLine = new CommandLine(this, factory);
    }

    @Override
    public void run(String... args) throws Exception {
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AiravataCommandLine.class);
        app.setDefaultProperties(java.util.Map.of(
                "spring.main.allow-bean-definition-overriding", "true",
                "spring.classformat.ignore", "true",
                "airavata.cli.enabled", "true",
                "airavata.server.enabled", "false",
                "services.thrift.enabled", "true"));
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        app.run(args);
    }
}
