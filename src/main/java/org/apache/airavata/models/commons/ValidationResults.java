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
package org.apache.airavata.models.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.commons.proto.ValidationResults}.
 */
public record ValidationResults(boolean validationState, List<ValidatorResult> validationResultList) {

    public ValidationResults {
        validationResultList = validationResultList == null ? List.of() : List.copyOf(validationResultList);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private boolean validationState;
        private List<ValidatorResult> validationResultList = new ArrayList<>();

        private Builder() {}

        private Builder(ValidationResults source) {
            this.validationState = source.validationState;
            this.validationResultList = new ArrayList<>(source.validationResultList);
        }

        public Builder setValidationState(boolean validationState) {
            this.validationState = validationState;
            return this;
        }

        public Builder addValidationResultList(ValidatorResult value) {
            this.validationResultList.add(value);
            return this;
        }

        public Builder addAllValidationResultList(Iterable<ValidatorResult> values) {
            values.forEach(this.validationResultList::add);
            return this;
        }

        public Builder clearValidationResultList() {
            this.validationResultList.clear();
            return this;
        }

        public ValidationResults build() {
            return new ValidationResults(validationState, validationResultList);
        }
    }
}
