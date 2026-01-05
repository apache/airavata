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

/**
 * Domain exception: LaunchValidationException
 */
public class LaunchValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    private ValidationResults validationResult;
    private String errorMessage;

    public LaunchValidationException() {
        super();
    }

    public LaunchValidationException(String message) {
        super(message);
    }

    public LaunchValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LaunchValidationException(String message, ValidationResults validationResult, String errorMessage) {
        super(message);
        this.validationResult = validationResult;
        this.errorMessage = errorMessage;
    }

    public ValidationResults getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResults validationResult) {
        this.validationResult = validationResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
