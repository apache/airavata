/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.status.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.status.model.EventKind;

/**
 * Unified EventEntity that consolidates status and error records for processes.
 *
 * <p>All events are parent-scoped (process, task, job, etc.). The {@code parentType} column
 * discriminates the owner kind; {@code parentId} holds the owner identifier.
 * Experiment state is a direct column on the experiment table, mutated by the orchestration
 * layer in response to process events.
 *
 * <p>EVENT_KIND discriminates STATUS vs ERROR; status-specific and error-specific
 * columns are nullable.
 */
@Entity(name = "EventEntity")
@Table(
        name = "event",
        indexes = {
            @Index(name = "idx_event_parent", columnList = "parent_id"),
            @Index(name = "idx_event_kind", columnList = "event_kind"),
            @Index(
                    name = "idx_event_parent_type_kind_seq",
                    columnList = "parent_id, parent_type, event_kind, sequence_num"),
            @Index(name = "idx_event_time", columnList = "event_time")
        })
public class EventEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "parent_id", nullable = false)
    private String parentId;

    @Column(name = "parent_type", nullable = false, length = 20)
    private String parentType = "PROCESS";

    @Column(name = "event_kind", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventKind eventKind;

    @Column(
            name = "event_time",
            nullable = false,
            columnDefinition = "TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private Timestamp eventTime;

    @Column(name = "sequence_num", nullable = false)
    private Long sequenceNum;

    @Column(name = "state")
    private String state;

    @Column(name = "reason", columnDefinition = "MEDIUMTEXT")
    private String reason;

    @Column(name = "actual_error_message", columnDefinition = "MEDIUMTEXT")
    private String actualErrorMessage;

    @Column(name = "user_friendly_message", columnDefinition = "MEDIUMTEXT")
    private String userFriendlyMessage;

    @Column(name = "transient_or_persistent")
    private Boolean transientOrPersistent;

    @Column(name = "root_cause_error_id_list", columnDefinition = "MEDIUMTEXT")
    private String rootCauseErrorIdList;

    public EventEntity() {}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public EventKind getEventKind() {
        return eventKind;
    }

    public void setEventKind(EventKind eventKind) {
        this.eventKind = eventKind;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public Long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(Long sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public Boolean getTransientOrPersistent() {
        return transientOrPersistent;
    }

    public void setTransientOrPersistent(Boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
    }

    public String getRootCauseErrorIdList() {
        return rootCauseErrorIdList;
    }

    public void setRootCauseErrorIdList(String rootCauseErrorIdList) {
        this.rootCauseErrorIdList = rootCauseErrorIdList;
    }

    /** Alias for status compatibility: eventId maps to statusId. */
    public String getStatusId() {
        return eventId;
    }
    /** Alias for status compatibility: eventTime maps to timeOfStateChange. */
    public Timestamp getTimeOfStateChange() {
        return eventTime;
    }
    /** Alias for error compatibility: eventId maps to errorId. */
    public String getErrorId() {
        return eventId;
    }
    /** Alias for error compatibility: eventTime maps to creationTime. */
    public Timestamp getCreationTime() {
        return eventTime;
    }
    /** Alias for error compatibility. */
    public boolean isTransientOrPersistent() {
        return Boolean.TRUE.equals(transientOrPersistent);
    }

    @PrePersist
    void setDefaults() {
        if (this.eventTime == null) {
            this.eventTime = IdGenerator.getUniqueTimestamp();
        }
        if (this.sequenceNum == null) {
            this.sequenceNum = System.currentTimeMillis();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EventEntity that = (EventEntity) obj;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "EventEntity{eventId='" + eventId + "', parentId='" + parentId
                + "', parentType='" + parentType + "', eventKind=" + eventKind
                + ", state='" + state + "'}";
    }
}
