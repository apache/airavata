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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.common.logging.LoggingUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.slf4j.Logger;

/**
 * Validation-related exceptions and utilities (ValidationResults, ValidatorResult,
 * LaunchValidationException, ErrorCodeGenerator, ExceptionHandlerUtil).
 */
public final class ValidationExceptions {

    private ValidationExceptions() {}

    public static class ValidationResults extends Exception {
        private static final long serialVersionUID = 1L;
        private boolean validationState;
        private List<ValidatorResult> validationResultList;

        public ValidationResults() { super(); }

        public ValidationResults(String message) { super(message); }

        public ValidationResults(String message, Throwable cause) { super(message, cause); }

        public ValidationResults(String message, boolean validationState, List<ValidatorResult> validationResultList) {
            super(message);
            this.validationState = validationState;
            this.validationResultList = validationResultList;
        }

        public boolean getValidationState() { return validationState; }
        public void setValidationState(boolean validationState) { this.validationState = validationState; }
        public List<ValidatorResult> getValidationResultList() { return validationResultList; }
        public void setValidationResultList(List<ValidatorResult> validationResultList) { this.validationResultList = validationResultList; }
    }

    public static class ValidatorResult extends Exception {
        private static final long serialVersionUID = 1L;
        private boolean result;
        private String errorDetails;

        public ValidatorResult() { super(); }

        public ValidatorResult(String message) { super(message); }

        public ValidatorResult(String message, Throwable cause) { super(message, cause); }

        public ValidatorResult(String message, boolean result, String errorDetails) {
            super(message);
            this.result = result;
            this.errorDetails = errorDetails;
        }

        public boolean getResult() { return result; }
        public void setResult(boolean result) { this.result = result; }
        public String getErrorDetails() { return errorDetails; }
        public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
    }

    public static class LaunchValidationException extends Exception {
        private static final long serialVersionUID = 1L;
        private ValidationResults validationResult;
        private String errorMessage;

        public LaunchValidationException() { super(); }

        public LaunchValidationException(String message) { super(message); }

        public LaunchValidationException(String message, Throwable cause) { super(message, cause); }

        public LaunchValidationException(String message, ValidationResults validationResult, String errorMessage) {
            super(message);
            this.validationResult = validationResult;
            this.errorMessage = errorMessage;
        }

        public ValidationResults getValidationResult() { return validationResult; }
        public void setValidationResult(ValidationResults validationResult) { this.validationResult = validationResult; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static final class ErrorCodeGenerator {
        private ErrorCodeGenerator() {}

        public static String generateErrorCode() {
            return UUID.randomUUID().toString();
        }

        public static String generateErrorCode(String prefix) {
            return String.format("%s-%s", prefix, UUID.randomUUID().toString());
        }

        public static String generateErrorCodeWithTimestamp(String prefix) {
            long timestamp = AiravataUtils.getUniqueTimestamp().getTime();
            return String.format("%s-%d-%s", prefix, timestamp, UUID.randomUUID().toString().substring(0, 8));
        }
    }

    public static final class ExceptionHandlerUtil {
        private ExceptionHandlerUtil() {}

        public static CoreExceptions.AiravataSystemException handleException(
                Logger logger, String operation, CoreExceptions.AiravataErrorType errorType, Throwable exception) {
            String message = String.format("[%s] failed: %s", operation, exception.getMessage());
            LoggingUtil.logError(logger, operation, exception.getMessage(), exception);
            return new CoreExceptions.AiravataSystemException(message, exception);
        }

        public static CoreExceptions.AiravataSystemException handleExceptionWithContext(
                Logger logger,
                String operation,
                CoreExceptions.AiravataErrorType errorType,
                Throwable exception,
                Map<String, String> context) {
            LoggingUtil.logErrorWithContext(logger, operation, exception.getMessage(), exception, context);
            var message = new StringBuilder();
            message.append(String.format("[%s] failed: %s", operation, exception.getMessage()));
            if (context != null && !context.isEmpty()) {
                message.append(" | Context: ");
                context.forEach((key, value) -> message.append(String.format("%s=%s, ", key, value)));
                message.setLength(message.length() - 2);
            }
            return new CoreExceptions.AiravataSystemException(message.toString(), exception);
        }

        public static CoreExceptions.AiravataSystemException handleExceptionWithMDC(
                Logger logger,
                String operation,
                CoreExceptions.AiravataErrorType errorType,
                Throwable exception,
                String experimentId,
                String gatewayId,
                String processId) {
            LoggingUtil.setExperimentContext(experimentId, gatewayId, processId);
            try {
                return handleException(logger, operation, errorType, exception);
            } finally {
                LoggingUtil.clearContext();
            }
        }

        public static CoreExceptions.AiravataSystemException wrapAsAiravataException(
                CoreExceptions.AiravataErrorType errorType, String message, Throwable cause) {
            return new CoreExceptions.AiravataSystemException(message, cause);
        }

        public static CoreExceptions.AiravataSystemException createExceptionWithErrorCode(
                CoreExceptions.AiravataErrorType errorType, String message, String errorCode, Throwable cause) {
            String messageWithCode = String.format("Error Code: %s, %s", errorCode, message);
            return wrapAsAiravataException(errorType, messageWithCode, cause);
        }
    }
}
