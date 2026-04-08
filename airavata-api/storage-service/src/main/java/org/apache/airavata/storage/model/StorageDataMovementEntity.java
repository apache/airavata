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
package org.apache.airavata.storage.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import org.apache.airavata.model.data.movement.proto.SecurityProtocol;

/**
 * Unified data movement entity replacing SCP_DATA_MOVEMENT, LOCAL_DATA_MOVEMENT,
 * GRIDFTP_DATA_MOVEMENT, GRIDFTP_ENDPOINT, and UNICORE_DATAMOVEMENT tables.
 *
 * <p>Type-specific fields are stored as JSON in CONFIG_JSON:
 * <ul>
 *   <li>SCP: {@code alternativeSCPHostName}, {@code queueDescription}, {@code sshPort}</li>
 *   <li>GRIDFTP: {@code gridFtpEndpoints} (list of endpoint strings)</li>
 *   <li>UNICORE: {@code unicoreEndpointUrl}</li>
 *   <li>LOCAL: empty map</li>
 * </ul>
 */
@Entity
@Table(name = "STORAGE_DATA_MOVEMENT")
public class StorageDataMovementEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DATA_MOVEMENT_ID")
    private String dataMovementId;

    @Column(name = "MOVEMENT_TYPE", nullable = false)
    private String movementType; // SCP, LOCAL, GRIDFTP, UNICORE

    @Column(name = "SECURITY_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private SecurityProtocol securityProtocol;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Lob
    @Column(name = "CONFIG_JSON")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> config;

    public StorageDataMovementEntity() {}

    public String getDataMovementId() {
        return dataMovementId;
    }

    public void setDataMovementId(String dataMovementId) {
        this.dataMovementId = dataMovementId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
