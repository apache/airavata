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

package org.apache.airavata.persistance.registry.jpa.model;


import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name ="EXPERIMENT_SUMMARY")
public class Experiment_Summary {

    @Id
    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment_Metadata experiment_metadata;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "LAST_UPDATED_TIME")
    private Timestamp last_update_time;

    public Experiment_Metadata getExperiment_metadata() {
        return experiment_metadata;
    }

    public void setExperiment_metadata(Experiment_Metadata experiment_metadata) {
        this.experiment_metadata = experiment_metadata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getLast_update_time() {
        return last_update_time;
    }

    public void setLast_update_time(Timestamp last_update_time) {
        this.last_update_time = last_update_time;
    }
}
