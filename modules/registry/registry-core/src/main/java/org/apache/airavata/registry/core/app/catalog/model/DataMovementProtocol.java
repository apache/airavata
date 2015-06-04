///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.aiaravata.application.catalog.data.model;
//
//import javax.persistence.*;
//import java.io.Serializable;
//
//@Entity
//@Table(name = "DATA_MOVEMENT_PROTOCOL")
//@IdClass(DataMovementProtocolPK.class)
//public class DataMovementProtocol implements Serializable {
//    @Id
//    @Column(name = "RESOURCE_ID")
//    private String resourceID;
//    @Id
//    @Column(name = "DATA_MOVE_ID")
//    private String dataMoveID;
//    @Id
//    @Column(name = "DATA_MOVE_TYPE")
//    private String dataMoveType;
//
//    @ManyToOne(cascade= CascadeType.MERGE)
//    @JoinColumn(name = "RESOURCE_ID")
//    private ComputeResource computeResource;
//
//    public String getResourceID() {
//        return resourceID;
//    }
//
//    public void setResourceID(String resourceID) {
//        this.resourceID = resourceID;
//    }
//
//    public String getDataMoveID() {
//        return dataMoveID;
//    }
//
//    public void setDataMoveID(String dataMoveID) {
//        this.dataMoveID = dataMoveID;
//    }
//
//    public String getDataMoveType() {
//        return dataMoveType;
//    }
//
//    public void setDataMoveType(String dataMoveType) {
//        this.dataMoveType = dataMoveType;
//    }
//
//    public ComputeResource getComputeResource() {
//        return computeResource;
//    }
//
//    public void setComputeResource(ComputeResource computeResource) {
//        this.computeResource = computeResource;
//    }
//}
