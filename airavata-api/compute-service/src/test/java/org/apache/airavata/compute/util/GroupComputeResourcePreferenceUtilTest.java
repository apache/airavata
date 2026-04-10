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
package org.apache.airavata.compute.util;

import java.util.Arrays;
import org.apache.airavata.interfaces.GroupComputeResourcePreferenceUtil;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.SlurmComputeResourcePreference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * GroupComputeResourcePreferenceUtilTest
 */
public class GroupComputeResourcePreferenceUtilTest {

    /** Helper: build a GroupComputeResourcePreference with SLURM reservations. */
    private static GroupComputeResourcePreference withReservations(ComputeResourceReservation... reservations) {
        SlurmComputeResourcePreference.Builder slurm = SlurmComputeResourcePreference.newBuilder();
        for (ComputeResourceReservation r : reservations) {
            slurm.addReservations(r);
        }
        return GroupComputeResourcePreference.newBuilder()
                .setResourceType(ResourceType.SLURM)
                .setSpecificPreferences(EnvironmentSpecificPreferences.newBuilder()
                        .setSlurm(slurm)
                        .build())
                .build();
    }

    @Test
    public void testGetActiveReservationForQueue() {
        ComputeResourceReservation res1 = ComputeResourceReservation.newBuilder()
                .setReservationId("id1")
                .setReservationName("res1")
                .addAllQueueNames(Arrays.asList("cpu", "gpu"))
                .setStartTime(System.currentTimeMillis() - 10000)
                .setEndTime(System.currentTimeMillis() + 10000)
                .build();

        GroupComputeResourcePreference pref = withReservations(res1);
        ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("id1", result.getReservationId());
    }

    @Test
    public void testGetActiveReservationForQueueWhenNoReservations() {
        GroupComputeResourcePreference pref = GroupComputeResourcePreference.getDefaultInstance();

        ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertNull(result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenReservationIsExpired() {
        ComputeResourceReservation res1 = ComputeResourceReservation.newBuilder()
                .setReservationId("id1")
                .setReservationName("res1")
                .addAllQueueNames(Arrays.asList("cpu", "gpu"))
                .setStartTime(System.currentTimeMillis() - 20000)
                .setEndTime(System.currentTimeMillis() - 10000)
                .build();

        GroupComputeResourcePreference pref = withReservations(res1);
        ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertNull(result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenReservationActiveButWrongQueue() {
        ComputeResourceReservation res1 = ComputeResourceReservation.newBuilder()
                .setReservationId("id1")
                .setReservationName("res1")
                .addAllQueueNames(Arrays.asList("cpu", "gpu"))
                .setStartTime(System.currentTimeMillis() - 10000)
                .setEndTime(System.currentTimeMillis() + 10000)
                .build();

        GroupComputeResourcePreference pref = withReservations(res1);
        ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "thirdqueue");

        Assertions.assertNull(result);
    }

    @Test
    public void testGetActiveReservationWithRandomOrder() {
        // active
        ComputeResourceReservation res1 = ComputeResourceReservation.newBuilder()
                .setReservationId("id1")
                .setReservationName("res1")
                .addAllQueueNames(Arrays.asList("cpu", "gpu"))
                .setStartTime(System.currentTimeMillis() - 10000)
                .setEndTime(System.currentTimeMillis() + 10000)
                .build();
        // expired
        ComputeResourceReservation res2 = ComputeResourceReservation.newBuilder()
                .setReservationId("id2")
                .setReservationName("res2")
                .addAllQueueNames(Arrays.asList("cpu", "gpu"))
                .setStartTime(System.currentTimeMillis() - 20000)
                .setEndTime(System.currentTimeMillis() - 10000)
                .build();
        // future
        ComputeResourceReservation res3 = ComputeResourceReservation.newBuilder()
                .setReservationId("id3")
                .setReservationName("res3")
                .addAllQueueNames(Arrays.asList("cpu", "gpu"))
                .setStartTime(System.currentTimeMillis() + 10000)
                .setEndTime(System.currentTimeMillis() + 20000)
                .build();
        // wrong queue
        ComputeResourceReservation res4 = ComputeResourceReservation.newBuilder()
                .setReservationId("id4")
                .setReservationName("res4")
                .addAllQueueNames(Arrays.asList("shared", "compute"))
                .setStartTime(System.currentTimeMillis() - 10000)
                .setEndTime(System.currentTimeMillis() + 10000)
                .build();

        GroupComputeResourcePreference pref = withReservations(res1, res2, res3, res4);
        ComputeResourceReservation result =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref, "cpu");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("id1", result.getReservationId());
    }
}
