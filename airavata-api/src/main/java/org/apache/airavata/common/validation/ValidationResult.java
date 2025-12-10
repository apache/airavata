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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a validation operation.
 * 
 * <p>Contains information about whether validation passed or failed,
 * and a list of validation errors if any occurred.
 */
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;

    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }

    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }

    /**
     * Check if validation passed.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Set the validation status.
     *
     * @param valid true if valid, false otherwise
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Get the list of validation errors.
     *
     * @return List of validation errors
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Set the list of validation errors.
     *
     * @param errors List of validation errors
     */
    public void setErrors(List<ValidationError> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    /**
     * Add a validation error.
     *
     * @param field The field that failed validation
     * @param message The error message
     */
    public void addError(String field, String message) {
        this.valid = false;
        this.errors.add(new ValidationError(field, message));
    }

    /**
     * Add a validation error.
     *
     * @param error The validation error
     */
    public void addError(ValidationError error) {
        this.valid = false;
        this.errors.add(error);
    }

    /**
     * Check if there are any errors.
     *
     * @return true if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Represents a single validation error.
     */
    public static class ValidationError {
        private String field;
        private String message;

        public ValidationError() {
        }

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("ValidationError{field='%s', message='%s'}", field, message);
        }
    }
}

