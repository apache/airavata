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
package org.apache.airavata.common.logging;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Utility class for standardized logging patterns across the Airavata API.
 * 
 * <p>This class provides helper methods to ensure consistent logging:
 * <ul>
 *   <li>Always include exceptions in error logs</li>
 *   <li>Use structured logging with MDC for correlation IDs</li>
 *   <li>Standardize log message format: [Operation] failed: [reason]</li>
 *   <li>Provide context-aware logging methods</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 *   LoggingUtil.logError(logger, "Experiment launch", "Failed to validate experiment", exception);
 *   LoggingUtil.logErrorWithContext(logger, "Process execution", "Task failed", exception, 
 *       Map.of("processId", processId, "taskId", taskId));
 * </pre>
 */
public class LoggingUtil {

    private LoggingUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Log an error with standardized format: [Operation] failed: [reason]
     * Always includes the exception in the log.
     *
     * @param logger The logger instance
     * @param operation The operation being performed (e.g., "Experiment launch", "Process execution")
     * @param reason The reason for the error
     * @param exception The exception that occurred (may be null)
     */
    public static void logError(Logger logger, String operation, String reason, Throwable exception) {
        String message = String.format("[%s] failed: %s", operation, reason);
        if (exception != null) {
            logger.error(message, exception);
        } else {
            logger.error(message);
        }
    }

    /**
     * Log an error with context information from MDC.
     * The MDC context is automatically included in the log output.
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param reason The reason for the error
     * @param exception The exception that occurred (may be null)
     */
    public static void logErrorWithMDC(Logger logger, String operation, String reason, Throwable exception) {
        logError(logger, operation, reason, exception);
    }

    /**
     * Log an error with additional context parameters.
     * Context parameters are included in the log message for better traceability.
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param reason The reason for the error
     * @param exception The exception that occurred (may be null)
     * @param context Context parameters to include in the log message (key-value pairs)
     */
    public static void logErrorWithContext(Logger logger, String operation, String reason, 
            Throwable exception, java.util.Map<String, String> context) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("[%s] failed: %s", operation, reason));
        
        if (context != null && !context.isEmpty()) {
            message.append(" | Context: ");
            context.forEach((key, value) -> 
                message.append(String.format("%s=%s, ", key, value)));
            // Remove trailing comma and space
            message.setLength(message.length() - 2);
        }
        
        if (exception != null) {
            logger.error(message.toString(), exception);
        } else {
            logger.error(message.toString());
        }
    }

    /**
     * Log a warning with standardized format: [Operation] warning: [message]
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param message The warning message
     */
    public static void logWarning(Logger logger, String operation, String message) {
        logger.warn(String.format("[%s] warning: %s", operation, message));
    }

    /**
     * Log a warning with context information.
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param message The warning message
     * @param context Context parameters to include in the log message
     */
    public static void logWarningWithContext(Logger logger, String operation, String message,
            java.util.Map<String, String> context) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format("[%s] warning: %s", operation, message));
        
        if (context != null && !context.isEmpty()) {
            logMessage.append(" | Context: ");
            context.forEach((key, value) -> 
                logMessage.append(String.format("%s=%s, ", key, value)));
            logMessage.setLength(logMessage.length() - 2);
        }
        
        logger.warn(logMessage.toString());
    }

    /**
     * Log an info message with standardized format: [Operation]: [message]
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param message The info message
     */
    public static void logInfo(Logger logger, String operation, String message) {
        logger.info(String.format("[%s]: %s", operation, message));
    }

    /**
     * Log an info message with context information.
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param message The info message
     * @param context Context parameters to include in the log message
     */
    public static void logInfoWithContext(Logger logger, String operation, String message,
            java.util.Map<String, String> context) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format("[%s]: %s", operation, message));
        
        if (context != null && !context.isEmpty()) {
            logMessage.append(" | Context: ");
            context.forEach((key, value) -> 
                logMessage.append(String.format("%s=%s, ", key, value)));
            logMessage.setLength(logMessage.length() - 2);
        }
        
        logger.info(logMessage.toString());
    }

    /**
     * Log a debug message with standardized format: [Operation]: [message]
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param message The debug message
     */
    public static void logDebug(Logger logger, String operation, String message) {
        logger.debug(String.format("[%s]: %s", operation, message));
    }

    /**
     * Set MDC context for experiment-related operations.
     * This helps with log correlation and tracing.
     *
     * @param experimentId The experiment ID
     * @param gatewayId The gateway ID (optional, may be null)
     * @param processId The process ID (optional, may be null)
     */
    public static void setExperimentContext(String experimentId, String gatewayId, String processId) {
        if (experimentId != null) {
            MDC.put(MDCConstants.EXPERIMENT_ID, experimentId);
        }
        if (gatewayId != null) {
            MDC.put(MDCConstants.GATEWAY_ID, gatewayId);
        }
        if (processId != null) {
            MDC.put(MDCConstants.PROCESS_ID, processId);
        }
    }

    /**
     * Clear MDC context.
     * Should be called at the end of request processing or in finally blocks.
     */
    public static void clearContext() {
        MDC.clear();
    }

    /**
     * Execute a runnable with MDC context preserved.
     * Useful for async operations where MDC context might be lost.
     *
     * @param runnable The runnable to execute
     * @return A wrapped runnable that preserves MDC context
     */
    public static Runnable withMDC(Runnable runnable) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> oldMdc = MDC.getCopyOfContextMap();

            if (mdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(mdc);
            }
            try {
                runnable.run();
            } finally {
                if (oldMdc == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(oldMdc);
                }
            }
        };
    }
}

