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

package org.apache.airavata.commons.gfac.type;

import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;

public class HostDescription implements Type {

    private HostDescriptionType hostDescriptionType;

    public HostDescription() {
        this.hostDescriptionType = HostDescriptionType.Factory.newInstance();
    }

    public HostDescription(HostDescriptionType hdt) {
        this.hostDescriptionType = hdt;
    }

    public String getId() {
        return hostDescriptionType.getName();
    }

    public void setId(String id) {
        this.hostDescriptionType.setName(id);
    }

    public String getAddress() {
        return hostDescriptionType.getAddress();
    }

    public void setAddress(String address) {
        this.hostDescriptionType.setAddress(address);
    }
}