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
package org.apache.airavata.restapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.airavata.iam.exception.AuthExceptions;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.iam.exception.GroupManagerServiceException;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.core.exception.RegistryExceptions;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.exception.UserProfileServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "org.apache.airavata.restapi.controller")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        logger.debug("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {
        logger.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RegistryExceptions.RegistryException.class)
    public ResponseEntity<ApiErrorResponse> handleRegistryException(
            RegistryExceptions.RegistryException ex, HttpServletRequest request) {
        logger.error("Registry error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Registry error", request);
    }

    @ExceptionHandler(RegistryExceptions.AppRegistryException.class)
    public ResponseEntity<ApiErrorResponse> handleAppRegistryException(
            RegistryExceptions.AppRegistryException ex, HttpServletRequest request) {
        logger.error("App registry error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "App registry error", request);
    }

    @ExceptionHandler(RegistryExceptions.ExperimentRegistryException.class)
    public ResponseEntity<ApiErrorResponse> handleExperimentRegistryException(
            RegistryExceptions.ExperimentRegistryException ex, HttpServletRequest request) {
        logger.error("Experiment registry error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Experiment registry error", request);
    }

    @ExceptionHandler(RegistryExceptions.WorkflowRegistryException.class)
    public ResponseEntity<ApiErrorResponse> handleWorkflowRegistryException(
            RegistryExceptions.WorkflowRegistryException ex, HttpServletRequest request) {
        logger.error("Workflow registry error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Workflow registry error", request);
    }

    @ExceptionHandler(IamAdminServicesException.class)
    public ResponseEntity<ApiErrorResponse> handleIamAdminServicesException(
            IamAdminServicesException ex, HttpServletRequest request) {
        logger.error("IAM admin error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "IAM service error", request);
    }

    @ExceptionHandler(UserProfileServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleUserProfileServiceException(
            UserProfileServiceException ex, HttpServletRequest request) {
        logger.error("User profile error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "User profile service error", request);
    }

    @ExceptionHandler(CredentialStoreException.class)
    public ResponseEntity<ApiErrorResponse> handleCredentialStoreException(
            CredentialStoreException ex, HttpServletRequest request) {
        logger.error("Credential store error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Credential store error", request);
    }

    @ExceptionHandler(GroupManagerServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleGroupManagerServiceException(
            GroupManagerServiceException ex, HttpServletRequest request) {
        logger.error("Group manager error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Group manager error", request);
    }

    @ExceptionHandler(SharingRegistryException.class)
    public ResponseEntity<ApiErrorResponse> handleSharingRegistryException(
            SharingRegistryException ex, HttpServletRequest request) {
        logger.error("Sharing registry error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Sharing registry error", request);
    }

    @ExceptionHandler(AuthExceptions.AuthorizationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationException(
            AuthExceptions.AuthorizationException ex, HttpServletRequest request) {
        logger.warn("Authorization denied on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Bad request on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        logger.warn("Validation error on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error on {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(), status.getReasonPhrase(), message, System.currentTimeMillis(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
