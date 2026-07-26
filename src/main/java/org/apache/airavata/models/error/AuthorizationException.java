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
 * Thrown for invalid authorization requests such as user does not have access to an application
 * or resource.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.error.proto.AuthorizationException}.
 */
public record AuthorizationException(String message) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String message = "";

        private Builder() {}

        private Builder(AuthorizationException source) {
            this.message = source.message;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public AuthorizationException build() {
            return new AuthorizationException(message);
        }
    }
}
