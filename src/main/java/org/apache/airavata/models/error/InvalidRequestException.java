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
 * Thrown for invalid requests that occur from any reasons like required input parameters are
 * missing, or a parameter is malformed.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.error.proto.InvalidRequestException}.
 */
public record InvalidRequestException(String message) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String message = "";

        private Builder() {}

        private Builder(InvalidRequestException source) {
            this.message = source.message;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public InvalidRequestException build() {
            return new InvalidRequestException(message);
        }
    }
}
