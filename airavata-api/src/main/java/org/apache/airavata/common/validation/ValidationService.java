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
package org.apache.airavata.common.validation;

import org.apache.airavata.common.utils.NameValidator;

/**
 * Service for performing common validation operations.
 * 
 * <p>This service provides standardized validation methods that can be used
 * across the Airavata API. It centralizes common validation logic and provides
 * a consistent interface for validation operations.
 */
public class ValidationService {

    /**
     * Validate that a string is not null, empty, or blank.
     *
     * @param value The string to validate
     * @param fieldName The name of the field (for error messages)
     * @param result The validation result to add errors to
     * @return true if valid, false otherwise
     */
    public static boolean validateRequiredString(String value, String fieldName, ValidationResult result) {
        if (value == null || value.trim().isEmpty()) {
            result.addError(fieldName, String.format("%s is required and cannot be empty", fieldName));
            return false;
        }
        return true;
    }

    /**
     * Validate that a string matches a name pattern (alphanumeric, underscore, dot).
     *
     * @param value The string to validate
     * @param fieldName The name of the field (for error messages)
     * @param result The validation result to add errors to
     * @return true if valid, false otherwise
     */
    public static boolean validateName(String value, String fieldName, ValidationResult result) {
        if (!NameValidator.validate(value)) {
            result.addError(fieldName, 
                String.format("%s must start with a letter and contain only letters, numbers, underscores, and dots", fieldName));
            return false;
        }
        return true;
    }

    /**
     * Validate that a string is within a specified length range.
     *
     * @param value The string to validate
     * @param fieldName The name of the field
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @param result The validation result to add errors to
     * @return true if valid, false otherwise
     */
    public static boolean validateLength(String value, String fieldName, int minLength, int maxLength, ValidationResult result) {
        if (value == null) {
            result.addError(fieldName, String.format("%s cannot be null", fieldName));
            return false;
        }
        int length = value.length();
        if (length < minLength || length > maxLength) {
            result.addError(fieldName, 
                String.format("%s must be between %d and %d characters long", fieldName, minLength, maxLength));
            return false;
        }
        return true;
    }

    /**
     * Validate that an object is not null.
     *
     * @param value The object to validate
     * @param fieldName The name of the field
     * @param result The validation result to add errors to
     * @return true if valid, false otherwise
     */
    public static boolean validateNotNull(Object value, String fieldName, ValidationResult result) {
        if (value == null) {
            result.addError(fieldName, String.format("%s cannot be null", fieldName));
            return false;
        }
        return true;
    }

    /**
     * Validate that a number is within a specified range.
     *
     * @param value The number to validate
     * @param fieldName The name of the field
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @param result The validation result to add errors to
     * @return true if valid, false otherwise
     */
    public static boolean validateRange(long value, String fieldName, long min, long max, ValidationResult result) {
        if (value < min || value > max) {
            result.addError(fieldName, 
                String.format("%s must be between %d and %d", fieldName, min, max));
            return false;
        }
        return true;
    }

    /**
     * Validate that a number is positive (greater than zero).
     *
     * @param value The number to validate
     * @param fieldName The name of the field
     * @param result The validation result to add errors to
     * @return true if valid, false otherwise
     */
    public static boolean validatePositive(long value, String fieldName, ValidationResult result) {
        if (value <= 0) {
            result.addError(fieldName, String.format("%s must be greater than zero", fieldName));
            return false;
        }
        return true;
    }
}

