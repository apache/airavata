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
package org.apache.airavata.common.utils;

/*
These are the job statuses shared in database level between orchestrator and
gfac instances
 */
public class AiravataJobState {

    private State jobState;

    public State getJobState() {
        return jobState;
    }

    public void setJobState(State jobState) {
        this.jobState = jobState;
    }


    public enum State {
        CREATED {
            public String toString() {
                return "CREATED";
            }
        },
        ACCEPTED {
            public String toString() {
                return "ACCEPTED";
            }
        },
        FETCHED {
            public String toString() {
                return "FETCHED";
            }
        },
        INHANDLERSDONE {
            public String toString() {
                return "INHANDLERSDONE";
            }
        },
        SUBMITTED {
            public String toString() {
                return "SUBMITTED";
            }
        },
        OUTHANDLERSDONE {
            public String toString() {
                return "OUTHANDLERSDONE";
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
        PENDING {
            public String toString() {
                return "PENDING";
            }
        },
        ACTIVE {
            public String toString() {
                return "ACTIVE";
            }
        },
        DONE {
            public String toString() {
                return "DONE";
            }
        },
        CANCELLED {
            public String toString() {
                return "CANCELLED";
            }
        },
        UNKNOWN {
            public String toString() {
                return "UNKNOWN";
            }
        },
        HANGED {
            public String toString() {
                return "HANGED";
            }
        }
    }
}
