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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "GATEWAY_WORKER")
@IdClass(GatewayWorkerPK.class)
public class GatewayWorker {
    private final static Logger logger = LoggerFactory.getLogger(GatewayWorker.class);
    private String gatewayId;
    private String userName;
    private Gateway gateway;

    @Id
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        GatewayWorker that = (GatewayWorker) o;
//
//        if (gatewayId != null ? !gatewayId.equals(that.gatewayId) : that.gatewayId != null) return false;
//        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = gatewayId != null ? gatewayId.hashCode() : 0;
//        result = 31 * result + (userName != null ? userName.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID", nullable = false)
    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gatewayByGatewayId) {
        this.gateway = gatewayByGatewayId;
    }
}