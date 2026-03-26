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
package org.apache.airavata.status.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: ErrorModel
 */
public class ErrorModel {
    private String errorId;
    private long createdAt;
    private String actualErrorMessage;
    private String userFriendlyMessage;
    private boolean transientError;
    private List<String> rootCauseErrorIdList;

    public ErrorModel() {}

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public boolean isTransientError() {
        return transientError;
    }

    public void setTransientError(boolean transientError) {
        this.transientError = transientError;
    }

    public List<String> getRootCauseErrorIdList() {
        return rootCauseErrorIdList;
    }

    public void setRootCauseErrorIdList(List<String> rootCauseErrorIdList) {
        this.rootCauseErrorIdList = rootCauseErrorIdList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorModel that = (ErrorModel) o;
        return Objects.equals(errorId, that.errorId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(actualErrorMessage, that.actualErrorMessage)
                && Objects.equals(userFriendlyMessage, that.userFriendlyMessage)
                && Objects.equals(transientError, that.transientError)
                && Objects.equals(rootCauseErrorIdList, that.rootCauseErrorIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                errorId, createdAt, actualErrorMessage, userFriendlyMessage, transientError, rootCauseErrorIdList);
    }

    @Override
    public String toString() {
        return "ErrorModel{" + "errorId=" + errorId + ", createdAt=" + createdAt + ", actualErrorMessage="
                + actualErrorMessage + ", userFriendlyMessage=" + userFriendlyMessage + ", transientError="
                + transientError + ", rootCauseErrorIdList=" + rootCauseErrorIdList + "}";
    }
}
