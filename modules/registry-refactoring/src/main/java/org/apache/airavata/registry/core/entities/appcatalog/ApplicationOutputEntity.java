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
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the application_output database table.
 */
@Entity
@Table(name = "APPLICATION_OUTPUT")
@IdClass(ApplicationOutputPK.class)
public class ApplicationOutputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="INTERFACE_ID")
    private String interfaceId;

    @Id
    @Column(name="OUTPUT_KEY")
    private String outputKey;

    @Column(name = "APP_ARGUMENT")
    private String appArgument;

    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;

    @Column(name = "DATA_NAME_LOCATION")
    private String dataNameLocation;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "OUTPUT_STREAMING")
    private boolean outputStreaming;

    @Column(name = "OUTPUT_VALUE")
    private String outputValue;

    @Column(name = "REQUIRED_TO_COMMANDLINE")
    private boolean requiredToCommandline;

    @Column(name = "SEARCH_QUERY")
    private String searchQuery;

    @ManyToOne(targetEntity = ApplicationInterfaceEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "INTERFACE_ID")
    private ApplicationInterfaceEntity applicationInterface;

    public ApplicationOutputEntity() {
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public boolean getDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean getOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }

    public String getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
    }

    public boolean getRequiredToCommandline() {
        return requiredToCommandline;
    }

    public void setRequiredToCommandline(boolean requiredToCommandline) {
        this.requiredToCommandline = requiredToCommandline;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public ApplicationInterfaceEntity getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(ApplicationInterfaceEntity applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

}