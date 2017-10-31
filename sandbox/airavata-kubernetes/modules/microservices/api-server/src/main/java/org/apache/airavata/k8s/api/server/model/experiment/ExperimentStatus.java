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
package org.apache.airavata.k8s.api.server.model.experiment;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "EXPERIMENT_STATUS")
public class ExperimentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private ExperimentState state; // required
    private long timeOfStateChange; // optional
    private String reason; // optional

    public long getId() {
        return id;
    }

    public ExperimentStatus setId(long id) {
        this.id = id;
        return this;
    }

    public ExperimentState getState() {
        return state;
    }

    public ExperimentStatus setState(ExperimentState state) {
        this.state = state;
        return this;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public ExperimentStatus setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public ExperimentStatus setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public enum ExperimentState {
        CREATED(0),
        VALIDATED(1),
        SCHEDULED(2),
        LAUNCHED(3),
        EXECUTING(4),
        CANCELING(5),
        CANCELED(6),
        COMPLETED(7),
        FAILED(8);

        private final int value;

        private ExperimentState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
