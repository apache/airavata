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
package org.apache.airavata.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.airavata.common.model.ComputeResourceReservation;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.EnvironmentSpecificPreferences;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.SlurmComputeResourcePreference;
import org.apache.airavata.common.utils.AiravataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * GroupComputeResourcePreferenceUtilTest
 */
public class GroupComputeResourcePreferenceUtilTest {

    @Test
    public void testGetActiveReservationForQueue() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setResourceType(ComputeResourceType.SLURM);
        final ComputeResourceReservation res1 = new ComputeResourceReservation(
                "id1",
                "res1",
                Arrays.asList("cpu", "gpu"),
                AiravataUtils.getUniqueTimestamp().getTime() - 10000,
                AiravataUtils.getUniqueTimestamp().getTime() + 10000);

        SlurmComputeResourcePreference slurm = new SlurmComputeResourcePreference();
        slurm.setReservations(new ArrayList<>());
        slurm.getReservations().add(res1);
        EnvironmentSpecificPreferences esp = new EnvironmentSpecificPreferences();
        esp.setSlurm(slurm);
        pref.setSpecificPreferences(esp);

        final ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertSame(res1, result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenNoReservations() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();

        final ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertNull(result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenReservationIsExpired() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setResourceType(ComputeResourceType.SLURM);
        final ComputeResourceReservation res1 = new ComputeResourceReservation(
                "id1",
                "res1",
                Arrays.asList("cpu", "gpu"),
                AiravataUtils.getUniqueTimestamp().getTime() - 20000,
                AiravataUtils.getUniqueTimestamp().getTime() - 10000);

        SlurmComputeResourcePreference slurm = new SlurmComputeResourcePreference();
        slurm.setReservations(new ArrayList<>());
        slurm.getReservations().add(res1);
        EnvironmentSpecificPreferences esp = new EnvironmentSpecificPreferences();
        esp.setSlurm(slurm);
        pref.setSpecificPreferences(esp);

        final ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertNull(result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenReservationActiveButWrongQueue() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setResourceType(ComputeResourceType.SLURM);
        final ComputeResourceReservation res1 = new ComputeResourceReservation(
                "id1",
                "res1",
                Arrays.asList("cpu", "gpu"),
                AiravataUtils.getUniqueTimestamp().getTime() - 10000,
                AiravataUtils.getUniqueTimestamp().getTime() + 10000);

        SlurmComputeResourcePreference slurm = new SlurmComputeResourcePreference();
        slurm.setReservations(new ArrayList<>());
        slurm.getReservations().add(res1);
        EnvironmentSpecificPreferences esp = new EnvironmentSpecificPreferences();
        esp.setSlurm(slurm);
        pref.setSpecificPreferences(esp);

        final ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "thirdqueue");

        Assertions.assertNull(result);
    }

    @Test
    public void testGetActiveReservationWithRandomOrder() {

        final ComputeResourceReservation res1 = new ComputeResourceReservation(
                "id1",
                "res1",
                Arrays.asList("cpu", "gpu"),
                AiravataUtils.getUniqueTimestamp().getTime() - 10000,
                AiravataUtils.getUniqueTimestamp().getTime() + 10000);

        final ComputeResourceReservation res2 = new ComputeResourceReservation(
                "id2",
                "res2",
                Arrays.asList("cpu", "gpu"),
                AiravataUtils.getUniqueTimestamp().getTime() - 20000,
                AiravataUtils.getUniqueTimestamp().getTime() - 10000);

        final ComputeResourceReservation res3 = new ComputeResourceReservation(
                "id3",
                "res3",
                Arrays.asList("cpu", "gpu"),
                AiravataUtils.getUniqueTimestamp().getTime() + 10000,
                AiravataUtils.getUniqueTimestamp().getTime() + 20000);

        final ComputeResourceReservation res4 = new ComputeResourceReservation(
                "id3",
                "res3",
                Arrays.asList("shared", "compute"),
                AiravataUtils.getUniqueTimestamp().getTime() + 10000,
                AiravataUtils.getUniqueTimestamp().getTime() + 20000);
        final List<ComputeResourceReservation> reservations = Arrays.asList(res1, res2, res3, res4);

        Collections.shuffle(reservations);

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setResourceType(ComputeResourceType.SLURM);
        SlurmComputeResourcePreference slurm = new SlurmComputeResourcePreference();
        slurm.setReservations(new ArrayList<>());
        for (ComputeResourceReservation res : reservations) {
            slurm.getReservations().add(res);
        }
        EnvironmentSpecificPreferences esp = new EnvironmentSpecificPreferences();
        esp.setSlurm(slurm);
        pref.setSpecificPreferences(esp);

        final ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertSame(res1, result);
    }
}
