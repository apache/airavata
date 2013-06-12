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
public class GFac_Job_Status {
    private String local_Job_ID;
    private Timestamp status_update_time;
    private String status;

    @ManyToOne()
    @JoinColumn(name = "local_Job_ID")
    private GFac_Job_Data gFac_job_data;


    public String getLocal_Job_ID() {
        return local_Job_ID;
    }

    public Timestamp getStatus_update_time() {
        return status_update_time;
    }

    public String getStatus() {
        return status;
    }

    public void setLocal_Job_ID(String local_Job_ID) {
        this.local_Job_ID = local_Job_ID;
    }

    public void setStatus_update_time(Timestamp status_update_time) {
        this.status_update_time = status_update_time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GFac_Job_Data getgFac_job_data() {
        return gFac_job_data;
    }

    public void setgFac_job_data(GFac_Job_Data gFac_job_data) {
        this.gFac_job_data = gFac_job_data;
    }
}
