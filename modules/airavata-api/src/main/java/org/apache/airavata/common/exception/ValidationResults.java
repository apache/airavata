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

/**
 * Domain exception: ValidationResults
 */
public class ValidationResults extends Exception {

    private static final long serialVersionUID = 1L;

    private boolean validationState;
    private List<ValidatorResult> validationResultList;

    public ValidationResults() {
        super();
    }

    public ValidationResults(String message) {
        super(message);
    }

    public ValidationResults(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationResults(String message, boolean validationState, List<ValidatorResult> validationResultList) {
        super(message);
        this.validationState = validationState;
        this.validationResultList = validationResultList;
    }

    public boolean getValidationState() {
        return validationState;
    }

    public void setValidationState(boolean validationState) {
        this.validationState = validationState;
    }

    public List<ValidatorResult> getValidationResultList() {
        return validationResultList;
    }

    public void setValidationResultList(List<ValidatorResult> validationResultList) {
        this.validationResultList = validationResultList;
    }
}
