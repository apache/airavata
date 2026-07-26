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
package org.apache.airavata.models.error;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.error.proto.ValidatorResult}.
 *
 * <p>Distinct from {@link org.apache.airavata.models.commons.ValidatorResult}, which has the same
 * shape but belongs to the {@code commons} namespace, matching the two separate generated proto
 * types.
 */
public record ValidatorResult(boolean result, String errorDetails) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private boolean result;
        private String errorDetails = "";

        private Builder() {}

        private Builder(ValidatorResult source) {
            this.result = source.result;
            this.errorDetails = source.errorDetails;
        }

        public Builder setResult(boolean result) {
            this.result = result;
            return this;
        }

        public Builder setErrorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public ValidatorResult build() {
            return new ValidatorResult(result, errorDetails);
        }
    }
}
