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

/**
 * The primary key class for the grp_ssh_acc_prov_config database table.
 */
public class GroupSSHAccountProvisionerConfigPK implements Serializable{

    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String groupResourceProfileId;
    private String configName;


    public GroupSSHAccountProvisionerConfigPK() {
    }

    public GroupSSHAccountProvisionerConfigPK(String resourceId, String groupResourceProfileId, String configName) {
        this.resourceId = resourceId;
        this.groupResourceProfileId = groupResourceProfileId;
        this.configName = configName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupSSHAccountProvisionerConfigPK that = (GroupSSHAccountProvisionerConfigPK) o;

        if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) return false;
        if (groupResourceProfileId != null ? !groupResourceProfileId.equals(that.groupResourceProfileId) : that.groupResourceProfileId != null)
            return false;
        return configName != null ? configName.equals(that.configName) : that.configName == null;
    }

    @Override
    public int hashCode() {
        int result = resourceId != null ? resourceId.hashCode() : 0;
        result = 31 * result + (groupResourceProfileId != null ? groupResourceProfileId.hashCode() : 0);
        result = 31 * result + (configName != null ? configName.hashCode() : 0);
        return result;
    }
}
