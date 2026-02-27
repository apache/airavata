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
package org.apache.airavata.bootstrap;

import java.util.HashMap;
import org.apache.airavata.cli.commands.AccountCommand;
import org.apache.airavata.cli.commands.InitCommand;
import org.apache.airavata.cli.commands.ServeCommand;
import org.apache.airavata.cli.commands.ServiceCommand;
import org.apache.airavata.cli.commands.TestCommand;
import org.apache.airavata.cli.util.ApplicationContextHolder;
import org.apache.airavata.config.FlywayConfiguration;
import org.apache.airavata.config.JpaConfiguration;
import org.apache.airavata.config.ServerProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Airavata CLI for Server Startup and Configuration Management.
 */
@SpringBootApplication(
        scanBasePackages = "org.apache.airavata",
        exclude = {
            org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@EnableConfigurationProperties(ServerProperties.class)
@Import({JpaConfiguration.class, FlywayConfiguration.class})
@Component
@Order(1)
@ConditionalOnProperty(name = "airavata.cli.enabled", havingValue = "true", matchIfMissing = true)
@CommandLine.Command(
        name = "airavata",
        description = "Airavata CLI for Server Startup and Configuration Management",
        subcommands = {
            InitCommand.class,
            AccountCommand.class,
            ServeCommand.class,
            ServiceCommand.class,
            TestCommand.class
        },
        mixinStandardHelpOptions = true)
public final class AiravataCommandLine implements CommandLineRunner, ApplicationContextAware {

    private final CommandLine commandLine;

    /**
     * Constructor for Spring injection with picocli factory.
     */
    public AiravataCommandLine(IFactory factory) {
        this.commandLine = new CommandLine(this, factory);
    }

    /**
     * No-arg constructor for standalone CLI usage (e.g., testing).
     */
    public AiravataCommandLine() {
        this.commandLine = null; // Will be created by picocli during execution
    }

    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext ctx) {
        ApplicationContextHolder.set(ctx);
    }

    @Override
    public void run(String... args) throws Exception {
        // Create CommandLine if it wasn't injected (e.g., when Spring uses no-arg constructor)
        CommandLine cmd = this.commandLine;
        if (cmd == null) {
            cmd = new CommandLine(this);
        }
        int exitCode = cmd.execute(args);
        // Don't exit if serve command is running in foreground (default) - it will block
        // Only exit for other commands or when serve -d (detach) was used
        if (exitCode != 0 || !isServeCommandForeground(args)) {
            System.exit(exitCode);
        }
        // For serve (foreground default), the command will block, so we don't exit here
    }

    private boolean isServeCommandForeground(String... args) {
        if (args == null || args.length == 0) {
            return false;
        }
        boolean hasServe = false;
        boolean hasDetach = false;
        for (String arg : args) {
            if (arg != null) {
                if ("serve".equals(arg)) {
                    hasServe = true;
                } else if ("-d".equals(arg) || "--detach".equals(arg)) {
                    hasDetach = true;
                }
            }
        }
        return hasServe && !hasDetach;
    }

    public static void main(String[] args) {
        // Handle --help, -h, --version, -V BEFORE starting Spring Boot.
        // This allows these options to work without DB, config, or any infrastructure.
        if (args == null || args.length == 0) {
            // No args = show help
            new CommandLine(new AiravataCommandLine()).execute("--help");
            return;
        }
        for (String arg : args) {
            if (arg == null) continue;
            if ("-h".equals(arg) || "--help".equals(arg)) {
                new CommandLine(new AiravataCommandLine()).execute(args);
                return;
            }
            if ("-V".equals(arg) || "--version".equals(arg)) {
                System.out.println("Airavata version 0.21-SNAPSHOT");
                return;
            }
        }

        // Determine subcommand for DB requirement check
        String commandName = null;
        for (String arg : args) {
            if (arg != null && !arg.startsWith("-")) {
                commandName = arg;
                break;
            }
        }

        // Only a small subset of commands should require DB connectivity during CLI bootstrap.
        // Everything else (including `serve`) should remain usable without a running DB.
        boolean requiresDb = "init".equals(commandName);

        var app = new SpringApplication(AiravataCommandLine.class);
        var defaults = new HashMap<String, Object>();
        defaults.put("spring.main.allow-bean-definition-overriding", "true");
        defaults.put("spring.classformat.ignore", "true");
        defaults.put("airavata.cli.enabled", "true");
        defaults.put("airavata.server.enabled", "false");
        if (requiresDb) {
            defaults.put("airavata.cli.command", "init");
            defaults.put("spring.profiles.active", "init");
        }

        if (!requiresDb) {
            // Keep CLI usable even without DB connectivity by avoiding eager bean creation
            // and excluding JPA auto-config that would otherwise require an EntityManagerFactory.
            defaults.put("spring.main.lazy-initialization", "true");
            defaults.put("flyway.enabled", "false");
            defaults.put("airavata.flyway.enabled", "false");
            defaults.put(
                    "spring.autoconfigure.exclude",
                    String.join(
                            ",",
                            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
                            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"));
        } else {
            // init command: InitDataSourceConfiguration provides DataSource; InitHandler runs Flyway programmatically.
            defaults.put("airavata.flyway.enabled", "false");
        }

        app.setDefaultProperties(defaults);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
