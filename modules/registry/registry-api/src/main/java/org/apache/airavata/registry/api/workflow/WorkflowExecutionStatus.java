/*
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
 *
 */

package org.apache.airavata.registry.api.workflow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class WorkflowExecutionStatus {

    public WorkflowExecutionStatus() {
    }

    public WorkflowExecutionStatus(String experimentID, String workflowInstanceID) {
        workflowInstance.setExperimentId(experimentID);
        workflowInstance.setWorkflowExecutionId(workflowInstanceID);
    }

    public enum State {
        STARTED {
            public String toString() {
                return "STARTED";
            }
        },
        RUNNING {
            public String toString() {
                return "RUNNING";
            }
        },
        FAILED {
            public String toString() {
                return "FAILED";
            }
        },
        PAUSED {
            public String toString() {
                return "PAUSED";
            }
        },
        FINISHED {
            public String toString() {
                return "FINISHED";
            }
        },
        UNKNOWN {
            public String toString() {
                return "UNKNOWN";
            }
        }
    }

    private State executionStatus=State.UNKNOWN;
    private Date statusUpdateTime = null;
    private WorkflowExecution workflowInstance;

    public State getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(State executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Date getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Date statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public WorkflowExecutionStatus(WorkflowExecution workflowInstance, State executionStatus) {
        this(workflowInstance, executionStatus, null);
    }

    public WorkflowExecutionStatus(WorkflowExecution workflowInstance, State executionStatus, Date statusUpdateTime) {
        statusUpdateTime = statusUpdateTime == null ? Calendar.getInstance().getTime() : statusUpdateTime;
        setWorkflowInstance(workflowInstance);
        setExecutionStatus(executionStatus);
        setStatusUpdateTime(statusUpdateTime);
    }

    public WorkflowExecution getWorkflowInstance() {
        return workflowInstance;
    }

    public void setWorkflowInstance(WorkflowExecution workflowInstance) {
        this.workflowInstance = workflowInstance;
    }
}
