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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: ErrorModel
 */
public class ErrorModel {
    private String errorId;
    private long creationTime;
    private String actualErrorMessage;
    private String userFriendlyMessage;
    private boolean transientOrPersistent;
    private List<String> rootCauseErrorIdList;

    public ErrorModel() {}

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
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

    public boolean getTransientOrPersistent() {
        return transientOrPersistent;
    }

    public void setTransientOrPersistent(boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
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
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(actualErrorMessage, that.actualErrorMessage)
                && Objects.equals(userFriendlyMessage, that.userFriendlyMessage)
                && Objects.equals(transientOrPersistent, that.transientOrPersistent)
                && Objects.equals(rootCauseErrorIdList, that.rootCauseErrorIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                errorId,
                creationTime,
                actualErrorMessage,
                userFriendlyMessage,
                transientOrPersistent,
                rootCauseErrorIdList);
    }

    @Override
    public String toString() {
        return "ErrorModel{" + "errorId=" + errorId + ", creationTime=" + creationTime + ", actualErrorMessage="
                + actualErrorMessage + ", userFriendlyMessage=" + userFriendlyMessage + ", transientOrPersistent="
                + transientOrPersistent + ", rootCauseErrorIdList=" + rootCauseErrorIdList + "}";
    }
}
