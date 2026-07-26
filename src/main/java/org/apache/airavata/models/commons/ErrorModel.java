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
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.commons.proto.ErrorModel}.
 */
public record ErrorModel(
        String errorId,
        long creationTime,
        String actualErrorMessage,
        String userFriendlyMessage,
        boolean transientOrPersistent,
        List<String> rootCauseErrorIdList) {

    public ErrorModel {
        rootCauseErrorIdList = rootCauseErrorIdList == null ? List.of() : List.copyOf(rootCauseErrorIdList);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String errorId = "";
        private long creationTime;
        private String actualErrorMessage = "";
        private String userFriendlyMessage = "";
        private boolean transientOrPersistent;
        private List<String> rootCauseErrorIdList = new ArrayList<>();

        private Builder() {}

        private Builder(ErrorModel source) {
            this.errorId = source.errorId;
            this.creationTime = source.creationTime;
            this.actualErrorMessage = source.actualErrorMessage;
            this.userFriendlyMessage = source.userFriendlyMessage;
            this.transientOrPersistent = source.transientOrPersistent;
            this.rootCauseErrorIdList = new ArrayList<>(source.rootCauseErrorIdList);
        }

        public Builder setErrorId(String errorId) {
            this.errorId = errorId;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setActualErrorMessage(String actualErrorMessage) {
            this.actualErrorMessage = actualErrorMessage;
            return this;
        }

        public Builder setUserFriendlyMessage(String userFriendlyMessage) {
            this.userFriendlyMessage = userFriendlyMessage;
            return this;
        }

        public Builder setTransientOrPersistent(boolean transientOrPersistent) {
            this.transientOrPersistent = transientOrPersistent;
            return this;
        }

        public Builder addRootCauseErrorIdList(String value) {
            this.rootCauseErrorIdList.add(value);
            return this;
        }

        public Builder addAllRootCauseErrorIdList(Iterable<String> values) {
            values.forEach(this.rootCauseErrorIdList::add);
            return this;
        }

        public Builder clearRootCauseErrorIdList() {
            this.rootCauseErrorIdList.clear();
            return this;
        }

        public ErrorModel build() {
            return new ErrorModel(
                    errorId, creationTime, actualErrorMessage, userFriendlyMessage,
                    transientOrPersistent, rootCauseErrorIdList);
        }
    }
}
