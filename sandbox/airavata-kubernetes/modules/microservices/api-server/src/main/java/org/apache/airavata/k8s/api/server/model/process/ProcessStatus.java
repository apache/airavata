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
package org.apache.airavata.k8s.api.server.model.process;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "PROCESS_STATUS")
public class ProcessStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private ProcessModel processModel;
    private ProcessState state;
    private long timeOfStateChange;
    private String reason;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public enum ProcessState {
        CREATED(0),
        VALIDATED(1),
        STARTED(2),
        PRE_PROCESSING(3),
        CONFIGURING_WORKSPACE(4),
        INPUT_DATA_STAGING(5),
        EXECUTING(6),
        MONITORING(7),
        OUTPUT_DATA_STAGING(8),
        POST_PROCESSING(9),
        COMPLETED(10),
        FAILED(11),
        CANCELLING(12),
        CANCELED(13);

        private final int value;

        private ProcessState(int value) {
            this.value = value;
        }

        private static Map<Integer, ProcessState> map = new HashMap<>();

        static {
            for (ProcessState state : ProcessState.values()) {
                map.put(state.value, state);
            }
        }

        public static ProcessState valueOf(int prcessState) {
            return map.get(prcessState);
        }

        public int getValue() {
            return value;
        }
    }
}
