/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.model.util;

import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.SlurmComputeResourcePreference;

public class GroupComputeResourcePreferenceUtil {

    public static ComputeResourceReservation getActiveReservationForQueue(
            GroupComputeResourcePreference groupComputeResourcePreference, String queueName) {

        // Only SLURM has reservations
        if (groupComputeResourcePreference.getResourceType() != ResourceType.SLURM) {
            return null;
        }

        EnvironmentSpecificPreferences esp = groupComputeResourcePreference.getSpecificPreferences();
        if (esp == null || !esp.isSetSlurm()) {
            return null;
        }

        SlurmComputeResourcePreference slurm = esp.getSlurm();
        if (!slurm.isSetReservations() || slurm.getReservationsSize() == 0) {
            return null;
        }

        long now = System.currentTimeMillis();
        for (ComputeResourceReservation reservation : slurm.getReservations()) {
            if (reservation.getQueueNames().contains(queueName)
                    && now > reservation.getStartTime()
                    && now < reservation.getEndTime()) {
                return reservation;
            }
        }
        return null;
    }
}
