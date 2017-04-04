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

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class TaskErrorPK implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(TaskErrorPK.class);
    private String errorId;
    private String taskId;

    @Column(name = "ERROR_ID")
    @Id
    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    @Column(name = "TASK_ID")
    @Id
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskErrorPK that = (TaskErrorPK) o;

        if (getErrorId() != null ? !getErrorId().equals(that.getErrorId()) : that.getErrorId() != null) return false;
        if (getTaskId() != null ? !getTaskId().equals(that.getTaskId()) : that.getTaskId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getErrorId() != null ? getErrorId().hashCode() : 0;
        result = 31 * result + (getTaskId() != null ? getTaskId().hashCode() : 0);
        return result;
    }
}