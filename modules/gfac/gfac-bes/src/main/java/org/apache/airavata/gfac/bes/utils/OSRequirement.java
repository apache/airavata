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
package org.apache.airavata.gfac.bes.utils;

import org.apache.airavata.gfac.core.provider.utils.ResourceRequirement;

public class OSRequirement implements ResourceRequirement {
    private OSType osType;
    private String version;
    protected boolean enabled;
    
    
    public OSRequirement() {
    }

    /**
     * 
     * @param type -
     *            the type of the O/S
     * @param version -
     *            the version of the O/S
     */
    public OSRequirement(OSType osType, String osVersion) {
        setOSType(osType);
        setOSVersion(osVersion);
    }

    /**
     * Set the type of the O/S
     * 
     * @param type -
     *            the type of the O/S
     */
    public void setOSType(OSType osType) {
        this.osType = osType;
    }

    /**
     * Get the type of the O/S
     * 
     * @return the type of the O/S
     */
    public OSType getOSType() {
        return osType;
    }

    /**
     * Set the version of the O/S
     * 
     * @param version -
     *            the version of the O/S
     */
    public void setOSVersion(String version) {
        this.version = version;
    }

    /**
     * Get the version of the O/S
     * 
     * @return the version of the O/S
     */
    public String getOSVersion() {
        return version;
    }

    /**
     * 
     * equals this instance of class with another instance
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj==null || getClass() != obj.getClass()) return false;
        final OSRequirement other = (OSRequirement) obj;
        boolean typeEqual = osType == null ? other.osType == null : osType.equals(other.osType);
        boolean versionEqual = version == null ? other.version == null : version.equals(other.version);
        return typeEqual && versionEqual && isEnabled() == other.isEnabled();
    }



	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}