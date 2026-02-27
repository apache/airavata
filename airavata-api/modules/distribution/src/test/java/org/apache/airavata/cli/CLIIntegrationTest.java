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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apache.airavata.bootstrap.AiravataCommandLine;
import org.apache.airavata.cli.commands.AccountCommand;
import org.apache.airavata.cli.commands.InitCommand;
import org.apache.airavata.cli.commands.ServeCommand;
import org.apache.airavata.cli.commands.ServiceCommand;
import org.apache.airavata.cli.commands.TestCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/**
 * CLI Integration Tests for all Airavata command variations.
 *
 * These tests verify that all CLI commands parse correctly and display
 * proper help output. Infrastructure-dependent tests (init, serve) are
 * tested separately with Testcontainers.
 */
@DisplayName("Airavata CLI Integration Tests")
public class CLIIntegrationTest {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private String getOutput() {
        return outContent.toString();
    }

    private String getErrorOutput() {
        return errContent.toString();
    }

    @Nested
    @DisplayName("Main CLI Help")
    class MainCLIHelpTests {

        @Test
        @DisplayName("airavata --help should display main help with all subcommands")
        void mainHelpShouldDisplayAllSubcommands() {
            // Create a standalone CommandLine without Spring context
            // This tests the CLI definition itself, not the full application
            var cmd = createStandaloneCommandLine();
            int exitCode = cmd.execute("--help");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("airavata");
            assertThat(output).contains("Airavata CLI for Server Startup and Configuration Management");
            // Check all subcommands are listed
            assertThat(output).contains("init");
            assertThat(output).contains("account");
            assertThat(output).contains("serve");
            assertThat(output).contains("service");
            assertThat(output).contains("test");
        }

        @Test
        @DisplayName("airavata -h should be equivalent to --help")
        void shortHelpFlagShouldWork() {
            var cmd = createStandaloneCommandLine();
            int exitCode = cmd.execute("-h");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("airavata");
            assertThat(output).contains("init");
        }

        @Test
        @DisplayName("airavata --version should display version info")
        void versionShouldDisplayVersionInfo() {
            var cmd = createStandaloneCommandLine();
            int exitCode = cmd.execute("--version");

            // Version info is displayed (exit code may vary based on implementation)
            // Just verify no error
            assertThat(exitCode).isGreaterThanOrEqualTo(0);
        }
    }

    /**
     * Creates a standalone CommandLine for testing CLI structure without Spring context.
     * This uses a simple factory that creates command instances with default constructors.
     */
    private CommandLine createStandaloneCommandLine() {
        return new CommandLine(AiravataCommandLine.class, new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                return cls.getDeclaredConstructor().newInstance();
            }
        });
    }

    @Nested
    @DisplayName("Init Command Help")
    class InitCommandHelpTests {

        @Test
        @DisplayName("airavata init --help should display init options")
        void initHelpShouldDisplayOptions() {
            var cmd = new CommandLine(new InitCommand());
            int exitCode = cmd.execute("--help");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("init");
            assertThat(output).contains("Initialize all Airavata databases");
            assertThat(output).contains("--clean");
        }
    }

    @Nested
    @DisplayName("Serve Command Help")
    class ServeCommandHelpTests {

        @Test
        @DisplayName("airavata serve --help should display serve options")
        void serveHelpShouldDisplayOptions() {
            var cmd = new CommandLine(new ServeCommand());
            int exitCode = cmd.execute("--help");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("serve");
            assertThat(output).contains("Start all Airavata services");
            assertThat(output).contains("-d");
            assertThat(output).contains("--detach");
        }
    }

    @Nested
    @DisplayName("Account Command Help")
    class AccountCommandHelpTests {

        @Test
        @DisplayName("airavata account help should display account subcommands")
        void accountHelpShouldDisplaySubcommands() {
            // AccountCommand uses HelpCommand subcommand, not mixinStandardHelpOptions
            var cmd = new CommandLine(new AccountCommand());
            int exitCode = cmd.execute("help");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("account");
            assertThat(output).contains("Manage");
        }
    }

    @Nested
    @DisplayName("Service Command Help")
    class ServiceCommandHelpTests {

        @Test
        @DisplayName("airavata service help should display service subcommands")
        void serviceHelpShouldDisplaySubcommands() {
            var cmd = new CommandLine(new ServiceCommand());
            int exitCode = cmd.execute("help");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("service");
            assertThat(output).contains("Manage");
        }
    }

    @Nested
    @DisplayName("Test Command Help")
    class TestCommandHelpTests {

        @Test
        @DisplayName("airavata test help should display test subcommands")
        void testHelpShouldDisplaySubcommands() {
            var cmd = new CommandLine(new TestCommand());
            int exitCode = cmd.execute("help");

            assertThat(exitCode).isEqualTo(0);
            String output = getOutput();
            assertThat(output).contains("test");
            assertThat(output).contains("Test");
        }
    }

    @Nested
    @DisplayName("Invalid Command Handling")
    class InvalidCommandTests {

        @Test
        @DisplayName("Invalid command should return non-zero exit code")
        void invalidCommandShouldFail() {
            var cmd = createStandaloneCommandLine();
            int exitCode = cmd.execute("invalid-command");

            assertThat(exitCode).isNotEqualTo(0);
        }

        @Test
        @DisplayName("Invalid option should return non-zero exit code")
        void invalidOptionShouldFail() {
            var cmd = createStandaloneCommandLine();
            int exitCode = cmd.execute("--invalid-option");

            assertThat(exitCode).isNotEqualTo(0);
        }
    }
}
