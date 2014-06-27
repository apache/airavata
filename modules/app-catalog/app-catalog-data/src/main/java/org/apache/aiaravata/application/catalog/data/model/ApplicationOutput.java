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

package org.apache.aiaravata.application.catalog.data.model;


import javax.persistence.*;

@Entity
@Table(name = "APPLICATION_OUTPUT")
@IdClass(AppOutput_PK.class)
public class ApplicationOutput {
    @Id
    @Column(name = "INTERFACE_ID")
    private String interfaceID;
    @Id
    @Column(name = "OUTPUT_KEY")
    private String outputKey;
    @Column(name = "OUTPUT_VALUE")
    private String outputVal;
    @Column(name = "DATA_TYPE")
    private String dataType;
    @Column(name = "METADATA")
    private String metadata;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "INTERFACE_ID")
    private ApplicationInterface applicationInterface;

    public String getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(String interfaceID) {
        this.interfaceID = interfaceID;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public ApplicationInterface getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(ApplicationInterface applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getOutputVal() {
        return outputVal;
    }

    public void setOutputVal(String outputVal) {
        this.outputVal = outputVal;
    }
}
