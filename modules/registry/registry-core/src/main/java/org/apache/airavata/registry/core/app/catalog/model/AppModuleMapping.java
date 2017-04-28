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
package org.apache.airavata.registry.core.app.catalog.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "APP_MODULE_MAPPING")
@IdClass(AppModuleMapping_PK.class)
public class AppModuleMapping implements Serializable {
    @Id
    @Column(name = "INTERFACE_ID")
    private String interfaceID;
    @Id
    @Column(name = "MODULE_ID")
    private String moduleID;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "INTERFACE_ID")
    private ApplicationInterface applicationInterface;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "MODULE_ID")
    private ApplicationModule applicationModule;

    public String getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(String interfaceID) {
        this.interfaceID = interfaceID;
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public ApplicationInterface getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(ApplicationInterface applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

    public ApplicationModule getApplicationModule() {
        return applicationModule;
    }

    public void setApplicationModule(ApplicationModule applicationModule) {
        this.applicationModule = applicationModule;
    }
}
