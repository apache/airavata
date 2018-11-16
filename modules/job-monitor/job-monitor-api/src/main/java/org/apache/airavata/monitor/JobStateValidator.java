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
 */
package org.apache.airavata.monitor;

import org.apache.airavata.model.status.JobState;

import static org.apache.airavata.model.status.JobState.SUBMITTED;
import static org.apache.airavata.model.status.JobState.QUEUED;
import static org.apache.airavata.model.status.JobState.ACTIVE;
import static org.apache.airavata.model.status.JobState.COMPLETE;
import static org.apache.airavata.model.status.JobState.CANCELED;
import static org.apache.airavata.model.status.JobState.FAILED;
import static org.apache.airavata.model.status.JobState.NON_CRITICAL_FAIL;
import static org.apache.airavata.model.status.JobState.SUSPENDED;
import static org.apache.airavata.model.status.JobState.UNKNOWN;

public class JobStateValidator {

    private static final Boolean[][] jobStateMachine = new Boolean[JobState.values().length][JobState.values().length];

    private static void setTransition(JobState previous, JobState now, Boolean value) {
        jobStateMachine[previous.getValue()][now.getValue()] = value;
    }

    static {
        int jobStates = JobState.values().length;
        for (int i = 0 ; i < jobStates; i++) {
            for (int j = 0; j < jobStates; j++) {
                jobStateMachine[i][j] = false;
            }
        }

        setTransition(SUBMITTED, QUEUED, true);
        setTransition(SUBMITTED, ACTIVE, true);
        setTransition(SUBMITTED, COMPLETE, true);
        setTransition(SUBMITTED, CANCELED, true);
        setTransition(SUBMITTED, FAILED, true);
        setTransition(SUBMITTED, SUSPENDED, true);
        setTransition(SUBMITTED, UNKNOWN, true);
        setTransition(SUBMITTED, NON_CRITICAL_FAIL, true);

        setTransition(QUEUED, ACTIVE, true);
        setTransition(QUEUED, COMPLETE, true);
        setTransition(QUEUED, CANCELED, true);
        setTransition(QUEUED, FAILED, true);
        setTransition(QUEUED, SUSPENDED, true);
        setTransition(QUEUED, UNKNOWN, true);
        setTransition(QUEUED, NON_CRITICAL_FAIL, true);

        setTransition(ACTIVE, COMPLETE, true);
        setTransition(ACTIVE, CANCELED, true);
        setTransition(ACTIVE, FAILED, true);
        setTransition(ACTIVE, SUSPENDED, true);
        setTransition(ACTIVE, UNKNOWN, true);
        setTransition(ACTIVE, NON_CRITICAL_FAIL, true);

        setTransition(NON_CRITICAL_FAIL, QUEUED, true);
        setTransition(NON_CRITICAL_FAIL, ACTIVE, true);
        setTransition(NON_CRITICAL_FAIL, COMPLETE, true);
        setTransition(NON_CRITICAL_FAIL, CANCELED, true);
        setTransition(NON_CRITICAL_FAIL, FAILED, true);
        setTransition(NON_CRITICAL_FAIL, SUSPENDED, true);
        setTransition(NON_CRITICAL_FAIL, UNKNOWN, true);
    }

    public static boolean isValid(JobState previousState, JobState newState) {
        if (previousState == null && newState != null) {
            return true;
        } else if (previousState != null && newState != null) {
            return jobStateMachine[previousState.getValue()][newState.getValue()];
        } else {
            return false;
        }
    }

}
