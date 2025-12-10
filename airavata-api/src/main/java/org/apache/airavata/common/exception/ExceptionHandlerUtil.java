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
package org.apache.airavata.common.exception;

import org.apache.airavata.common.logging.LoggingUtil;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.slf4j.Logger;

/**
 * Utility class for standardized exception handling patterns.
 * 
 * <p>This class provides helper methods to ensure consistent exception handling:
 * <ul>
 *   <li>Convert exceptions to AiravataSystemException with proper error types</li>
 *   <li>Log exceptions with context using MDC</li>
 *   <li>Wrap exceptions with appropriate error codes</li>
 *   <li>Provide standardized error message formatting</li>
 * </ul>
 */
public class ExceptionHandlerUtil {

    private ExceptionHandlerUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Handle an exception by logging it and converting to AiravataSystemException.
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param errorType The Airavata error type
     * @param exception The exception that occurred
     * @return AiravataSystemException with the error details
     */
    public static AiravataSystemException handleException(
            Logger logger, String operation, AiravataErrorType errorType, Throwable exception) {
        String message = String.format("[%s] failed: %s", operation, exception.getMessage());
        LoggingUtil.logError(logger, operation, exception.getMessage(), exception);
        
        AiravataSystemException airavataException = new AiravataSystemException(errorType);
        airavataException.setMessage(message);
        airavataException.initCause(exception);
        return airavataException;
    }

    /**
     * Handle an exception with context information.
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param errorType The Airavata error type
     * @param exception The exception that occurred
     * @param context Context parameters (key-value pairs)
     * @return AiravataSystemException with the error details
     */
    public static AiravataSystemException handleExceptionWithContext(
            Logger logger, String operation, AiravataErrorType errorType, 
            Throwable exception, java.util.Map<String, String> context) {
        LoggingUtil.logErrorWithContext(logger, operation, exception.getMessage(), exception, context);
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("[%s] failed: %s", operation, exception.getMessage()));
        if (context != null && !context.isEmpty()) {
            message.append(" | Context: ");
            context.forEach((key, value) -> message.append(String.format("%s=%s, ", key, value)));
            message.setLength(message.length() - 2);
        }
        
        AiravataSystemException airavataException = new AiravataSystemException(errorType);
        airavataException.setMessage(message.toString());
        airavataException.initCause(exception);
        return airavataException;
    }

    /**
     * Handle an exception with MDC context (experiment, gateway, process IDs).
     *
     * @param logger The logger instance
     * @param operation The operation being performed
     * @param errorType The Airavata error type
     * @param exception The exception that occurred
     * @param experimentId The experiment ID (optional)
     * @param gatewayId The gateway ID (optional)
     * @param processId The process ID (optional)
     * @return AiravataSystemException with the error details
     */
    public static AiravataSystemException handleExceptionWithMDC(
            Logger logger, String operation, AiravataErrorType errorType, 
            Throwable exception, String experimentId, String gatewayId, String processId) {
        LoggingUtil.setExperimentContext(experimentId, gatewayId, processId);
        try {
            return handleException(logger, operation, errorType, exception);
        } finally {
            LoggingUtil.clearContext();
        }
    }

    /**
     * Wrap a generic exception as AiravataSystemException with a specific error type.
     *
     * @param errorType The Airavata error type
     * @param message The error message
     * @param cause The underlying exception
     * @return AiravataSystemException
     */
    public static AiravataSystemException wrapAsAiravataException(
            AiravataErrorType errorType, String message, Throwable cause) {
        AiravataSystemException exception = new AiravataSystemException(errorType);
        exception.setMessage(message);
        if (cause != null) {
            exception.initCause(cause);
        }
        return exception;
    }

    /**
     * Create an AiravataSystemException with an error code.
     *
     * @param errorType The Airavata error type
     * @param message The error message
     * @param errorCode The error code (generated by ErrorCodeGenerator)
     * @param cause The underlying exception
     * @return AiravataSystemException
     */
    public static AiravataSystemException createExceptionWithErrorCode(
            AiravataErrorType errorType, String message, String errorCode, Throwable cause) {
        String messageWithCode = String.format("Error Code: %s, %s", errorCode, message);
        return wrapAsAiravataException(errorType, messageWithCode, cause);
    }
}

