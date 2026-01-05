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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: EnvironmentSpecificPreferences
 * Union type that can be either SlurmComputeResourcePreference or AwsComputeResourcePreference.
 */
public class EnvironmentSpecificPreferences {
    private SlurmComputeResourcePreference slurm;
    private AwsComputeResourcePreference aws;

    public EnvironmentSpecificPreferences() {}

    public SlurmComputeResourcePreference getSlurm() {
        return slurm;
    }

    public void setSlurm(SlurmComputeResourcePreference slurm) {
        this.slurm = slurm;
        this.aws = null; // Clear aws when setting slurm
    }

    public AwsComputeResourcePreference getAws() {
        return aws;
    }

    public void setAws(AwsComputeResourcePreference aws) {
        this.aws = aws;
        this.slurm = null; // Clear slurm when setting aws
    }

    /**
     * Static factory method to create a slurm preference.
     */
    public static EnvironmentSpecificPreferences slurm(SlurmComputeResourcePreference slurm) {
        EnvironmentSpecificPreferences prefs = new EnvironmentSpecificPreferences();
        prefs.setSlurm(slurm);
        return prefs;
    }

    /**
     * Static factory method to create an aws preference.
     */
    public static EnvironmentSpecificPreferences aws(AwsComputeResourcePreference aws) {
        EnvironmentSpecificPreferences prefs = new EnvironmentSpecificPreferences();
        prefs.setAws(aws);
        return prefs;
    }

    public boolean isSlurm() {
        return slurm != null;
    }

    public boolean isAws() {
        return aws != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvironmentSpecificPreferences that = (EnvironmentSpecificPreferences) o;
        return Objects.equals(slurm, that.slurm) && Objects.equals(aws, that.aws);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slurm, aws);
    }

    @Override
    public String toString() {
        if (slurm != null) {
            return "EnvironmentSpecificPreferences{slurm=" + slurm + "}";
        } else if (aws != null) {
            return "EnvironmentSpecificPreferences{aws=" + aws + "}";
        } else {
            return "EnvironmentSpecificPreferences{empty}";
        }
    }
}
