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
package org.apache.airavata.cli.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.airavata.cli.communication.ServiceSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for managing Airavata service processes.
 *
 * <p>Handles forking processes for both JAR and native binary modes,
 * and manages process lifecycle (start, stop, status).
 */
public class ProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(ProcessManager.class);
    private static final String PID_FILE_NAME = "pid-airavata";

    /**
     * Fork a new Airavata service process.
     *
     * @param airavataHome Airavata home directory (config dir will be {airavataHome}/conf)
     * @param jarPath Path to JAR file (null if using native binary)
     * @param nativeBinaryPath Path to native binary (null if using JAR)
     * @return Process handle
     * @throws IOException if process cannot be started
     */
    public static Process startServiceProcess(String airavataHome, String jarPath, String nativeBinaryPath)
            throws IOException {
        String normalizedAiravataHome = airavataHome;
        try {
            if (airavataHome != null && !airavataHome.isEmpty()) {
                normalizedAiravataHome =
                        Paths.get(airavataHome).toAbsolutePath().normalize().toString();
            }
        } catch (Exception e) {
            // Keep original if normalization fails.
            normalizedAiravataHome = airavataHome;
        }

        // Derive configDir from airavataHome
        String normalizedConfigDir = Paths.get(normalizedAiravataHome, "conf")
                .toAbsolutePath()
                .normalize()
                .toString();

        // Check if socket already exists (another process running)
        if (ServiceSocketClient.socketExists(normalizedConfigDir)) {
            throw new IOException("Airavata service is already running (socket exists). Stop it first.");
        }

        var command = new ArrayList<String>();
        ProcessBuilder pb;

        if (nativeBinaryPath != null && !nativeBinaryPath.isEmpty()) {
            // Native binary mode
            var binary = new File(nativeBinaryPath);
            if (!binary.exists() || !binary.canExecute()) {
                throw new IOException("Native binary not found or not executable: " + nativeBinaryPath);
            }
            command.add(nativeBinaryPath);
            command.add("serve");
            pb = new ProcessBuilder(command);
            pb.directory(binary.getParentFile());
        } else if (jarPath != null && !jarPath.isEmpty()) {
            // JAR mode
            var jar = new File(jarPath);
            if (!jar.exists()) {
                throw new IOException("JAR file not found: " + jarPath);
            }

            String javaHome = System.getProperty("java.home");
            String javaCmd = javaHome + File.separator + "bin" + File.separator + "java";
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                javaCmd += ".exe";
            }

            command.add(javaCmd);
            command.add("-Dairavata.home=" + normalizedAiravataHome);
            command.add("-jar");
            command.add(jarPath);
            command.add("serve");

            pb = new ProcessBuilder(command);
            pb.directory(jar.getParentFile());
        } else {
            throw new IllegalArgumentException("Either jarPath or nativeBinaryPath must be provided");
        }

        // Set environment variables
        pb.environment().put("AIRAVATA_HOME", normalizedAiravataHome);

        // Redirect output
        var logDir = Paths.get(normalizedConfigDir, "logs");
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir);
        }
        var stdoutLog = logDir.resolve("airavata-service.out");
        var stderrLog = logDir.resolve("airavata-service.err");

        pb.redirectOutput(stdoutLog.toFile());
        pb.redirectError(stderrLog.toFile());

        // Start process
        var process = pb.start();

        // Write PID file
        try {
            Path pidFile = getPidFilePath(normalizedConfigDir);
            Files.createDirectories(pidFile.getParent());
            Files.writeString(pidFile, String.valueOf(process.pid()));
            logger.info("Started Airavata service process (PID: {})", process.pid());
        } catch (IOException e) {
            logger.warn("Failed to write PID file", e);
        }

        return process;
    }

    /**
     * Get PID file path.
     */
    public static Path getPidFilePath(String configDir) {
        if (configDir != null && !configDir.isEmpty()) {
            return Paths.get(configDir, "bin", PID_FILE_NAME);
        }
        return Paths.get("bin", PID_FILE_NAME);
    }

    /**
     * Read PID from file.
     */
    public static Long readPid(String configDir) {
        Path pidFile = getPidFilePath(configDir);
        if (!Files.exists(pidFile)) {
            return null;
        }

        try {
            String pidStr = Files.readString(pidFile).trim();
            return Long.parseLong(pidStr);
        } catch (Exception e) {
            logger.debug("Error reading PID file", e);
            return null;
        }
    }

    /**
     * Check if process is running by PID.
     */
    public static boolean isProcessRunning(long pid) {
        var os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Windows: use tasklist
                var pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid);
                var process = pb.start();
                var exitCode = process.waitFor();
                return exitCode == 0;
            } else {
                // Unix/Linux/Mac: use kill -0
                var pb = new ProcessBuilder("kill", "-0", String.valueOf(pid));
                var process = pb.start();
                var exitCode = process.waitFor();
                return exitCode == 0;
            }
        } catch (Exception e) {
            logger.debug("Error checking process status", e);
            return false;
        }
    }

    /**
     * Stop process by PID.
     */
    public static boolean stopProcess(long pid) {
        var os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                var pb = new ProcessBuilder("taskkill", "/PID", String.valueOf(pid), "/F");
                var process = pb.start();
                var exitCode = process.waitFor();
                return exitCode == 0;
            } else {
                var pb = new ProcessBuilder("kill", String.valueOf(pid));
                var process = pb.start();
                var exitCode = process.waitFor();
                return exitCode == 0;
            }
        } catch (Exception e) {
            logger.error("Error stopping process", e);
            return false;
        }
    }

    /**
     * Detect if we're running from a JAR or native binary.
     */
    public static boolean isNativeBinary() {
        // Check if we're running as a native image
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    /**
     * Get the path to the current JAR or native binary.
     */
    public static String getCurrentExecutablePath() {
        if (isNativeBinary()) {
            // For native binary, try to get the executable path
            // On Unix systems, we can use /proc/self/exe or check PATH
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) {
                try {
                    // Try /proc/self/exe (Linux)
                    var exePath = Paths.get("/proc/self/exe");
                    if (Files.exists(exePath)) {
                        return Files.readSymbolicLink(exePath).toString();
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            // Fallback: try to find airavata in PATH or common locations
            String[] possiblePaths = {
                System.getProperty("user.dir") + File.separator + "airavata",
                System.getProperty("user.dir") + File.separator + "bin" + File.separator + "airavata",
                "/usr/local/bin/airavata",
                "/usr/bin/airavata"
            };
            for (String path : possiblePaths) {
                var f = new File(path);
                if (f.exists() && f.canExecute()) {
                    return path;
                }
            }
            // Last resort: return null and let caller handle it
            return null;
        } else {
            // For JAR, get the JAR file path
            try {
                String className = ProcessManager.class.getName().replace('.', '/') + ".class";
                URL resource = ProcessManager.class.getClassLoader().getResource(className);
                if (resource != null) {
                    String classPath = resource.toString();
                    int bang = classPath.indexOf("!");
                    if (bang > 0) {
                        String jarPath = null;
                        if (classPath.startsWith("jar:file:")) {
                            jarPath = classPath.substring(9, bang);
                        } else if (classPath.startsWith("jar:nested:")) {
                            // Spring Boot loader (3.x) uses jar:nested:/path/app.jar!/...
                            jarPath = classPath.substring("jar:nested:".length(), bang);
                        } else if (classPath.startsWith("jar:")) {
                            // Generic jar: scheme; trim and try to locate the underlying file path.
                            String remainder = classPath.substring("jar:".length(), bang);
                            if (remainder.startsWith("file:")) {
                                jarPath = remainder.substring("file:".length());
                            } else {
                                jarPath = remainder;
                            }
                        }

                        if (jarPath != null) {
                            // Handle URL encoding
                            if (jarPath.startsWith("/")
                                    && System.getProperty("os.name")
                                            .toLowerCase()
                                            .contains("win")) {
                                jarPath = jarPath.substring(1); // Remove leading slash on Windows
                            }
                            jarPath = URLDecoder.decode(jarPath, "UTF-8");
                            // Some URL formats may leave a trailing slash (e.g. "...app.jar/"); normalize it.
                            if (jarPath.endsWith(".jar/")) {
                                jarPath = jarPath.substring(0, jarPath.length() - 1);
                            } else if (jarPath.endsWith("/") && jarPath.contains(".jar")) {
                                jarPath = jarPath.substring(0, jarPath.length() - 1);
                            }
                            return jarPath;
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Error getting JAR path", e);
            }
        }
        return null;
    }
}
