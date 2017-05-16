/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "EXPERIMENT_ERROR")
@IdClass(ExperimentErrorPK.class)
public class ExperimentError {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentError.class);
    private String errorId;
    private String experimentId;
    private Timestamp creationTime;
    private String actualErrorMessage;
    private String userFriendlyMessage;
    private boolean transientOrPersistent;
    private String rootCauseErrorIdList;
    private Experiment experiment;

    @Id
    @Column(name = "ERROR_ID")
    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Lob
    @Column(name = "ACTUAL_ERROR_MESSAGE")
    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    @Lob
    @Column(name = "USER_FRIENDLY_MESSAGE")
    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    @Column(name = "TRANSIENT_OR_PERSISTENT")
    public boolean getTransientOrPersistent() {
        return transientOrPersistent;
    }

    public void setTransientOrPersistent(boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
    }

    @Lob
    @Column(name = "ROOT_CAUSE_ERROR_ID_LIST")
    public String getRootCauseErrorIdList() {
        return rootCauseErrorIdList;
    }

    public void setRootCauseErrorIdList(String rootCauseErrorIdList) {
        this.rootCauseErrorIdList = rootCauseErrorIdList;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ExperimentError that = (ExperimentError) o;
//
//        if (errorId != that.errorId) return false;
//        if (actualErrorMessage != null ? !actualErrorMessage.equals(that.actualErrorMessage) : that.actualErrorMessage != null)
//            return false;
//        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
//        if (experimentId != null ? !experimentId.equals(that.experimentId) : that.experimentId != null) return false;
//        if (rootCauseErrorIdList != null ? !rootCauseErrorIdList.equals(that.rootCauseErrorIdList) : that.rootCauseErrorIdList != null)
//            return false;
//        if (transientOrPersistent != null ? !transientOrPersistent.equals(that.transientOrPersistent) : that.transientOrPersistent != null)
//            return false;
//        if (userFriendlyMessage != null ? !userFriendlyMessage.equals(that.userFriendlyMessage) : that.userFriendlyMessage != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = errorId != null ? errorId.hashCode() : 0;
//        result = 31 * result + (experimentId != null ? experimentId.hashCode() : 0);
//        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
//        result = 31 * result + (actualErrorMessage != null ? actualErrorMessage.hashCode() : 0);
//        result = 31 * result + (userFriendlyMessage != null ? userFriendlyMessage.hashCode() : 0);
//        result = 31 * result + (transientOrPersistent != null ? transientOrPersistent.hashCode() : 0);
//        result = 31 * result + (rootCauseErrorIdList != null ? rootCauseErrorIdList.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID", nullable = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experimentByExperimentId) {
        this.experiment = experimentByExperimentId;
    }
}