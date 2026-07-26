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
 * Thrown by Airavata Services when a call fails as a result of a problem that a client may be
 * able to resolve.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.error.proto.AiravataClientException}.
 *
 * @param airavataErrorType The message type indicating the error that occurred.
 * @param parameter If the error applied to a particular input parameter, this will indicate which
 *     parameter.
 */
public record AiravataClientException(AiravataErrorType airavataErrorType, String parameter) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private AiravataErrorType airavataErrorType = AiravataErrorType.AIRAVATA_ERROR_TYPE_UNKNOWN;
        private String parameter = "";

        private Builder() {}

        private Builder(AiravataClientException source) {
            this.airavataErrorType = source.airavataErrorType;
            this.parameter = source.parameter;
        }

        public Builder setAiravataErrorType(AiravataErrorType airavataErrorType) {
            this.airavataErrorType = airavataErrorType;
            return this;
        }

        public Builder setParameter(String parameter) {
            this.parameter = parameter;
            return this;
        }

        public AiravataClientException build() {
            return new AiravataClientException(airavataErrorType, parameter);
        }
    }
}
