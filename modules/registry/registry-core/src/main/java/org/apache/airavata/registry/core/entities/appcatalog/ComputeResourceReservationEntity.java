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
package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

/**
 * The persistent class for the COMPUTE_RESOURCE_RESERVATION database table.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE_RESERVATION")
public class ComputeResourceReservationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESERVATION_ID")
    private String reservationId;

    @Column(name = "RESERVATION_NAME", nullable = false)
    private String reservationName;

    @Column(name = "START_TIME", nullable = false)
    private Timestamp startTime;

    @Column(name = "END_TIME", nullable = false)
    private Timestamp endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="COMPUTE_RESOURCE_RESERVATION_QUEUE", joinColumns = @JoinColumn(name="RESERVATION_ID"))
    @Column(name = "QUEUE_NAME", nullable = false)
    private List<String> queueNames;

    // TODO: FK queue table to BatchQueueEntity?

    @ManyToOne(targetEntity = GroupComputeResourcePrefEntity.class)
    @JoinColumns({
            @JoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID", nullable = false, updatable = false),
            @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", referencedColumnName = "GROUP_RESOURCE_PROFILE_ID", nullable = false, updatable = false)
    })
    @ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
    private GroupComputeResourcePrefEntity groupComputeResourcePref;

    public ComputeResourceReservationEntity() {
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationName() {
        return reservationName;
    }

    public void setReservationName(String reservationName) {
        this.reservationName = reservationName;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public List<String> getQueueNames() {
        return queueNames;
    }

    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    public GroupComputeResourcePrefEntity getGroupComputeResourcePref() {
        return groupComputeResourcePref;
    }

    public void setGroupComputeResourcePref(GroupComputeResourcePrefEntity groupComputeResourcePref) {
        this.groupComputeResourcePref = groupComputeResourcePref;
    }
}
