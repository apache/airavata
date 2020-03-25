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

package org.apache.airavata.model.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.junit.Assert;
import org.junit.Test;

/**
 * GroupComputeResourcePreferenceUtilTest
 */
public class GroupComputeResourcePreferenceUtilTest {

    @Test
    public void testGetActiveReservationForQueue() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        final ComputeResourceReservation res1 = new ComputeResourceReservation("id1", "res1",
                Arrays.asList("cpu", "gpu"), System.currentTimeMillis() - 10000, System.currentTimeMillis() + 10000);
        pref.addToReservations(res1);

        final ComputeResourceReservation result = GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref,
                "cpu");

        Assert.assertSame(res1, result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenNoReservations() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();

        final ComputeResourceReservation result = GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref,
                "cpu");

        Assert.assertNull(result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenReservationIsExpired() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        final ComputeResourceReservation res1 = new ComputeResourceReservation("id1", "res1",
                Arrays.asList("cpu", "gpu"), System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        pref.addToReservations(res1);

        final ComputeResourceReservation result = GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref,
                "cpu");

        Assert.assertNull(result);
    }

    @Test
    public void testGetActiveReservationForQueueWhenReservationActiveButWrongQueue() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        final ComputeResourceReservation res1 = new ComputeResourceReservation("id1", "res1",
                Arrays.asList("cpu", "gpu"), System.currentTimeMillis() - 10000, System.currentTimeMillis() + 10000);
        pref.addToReservations(res1);

        final ComputeResourceReservation result = GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref,
                "thirdqueue");

        Assert.assertNull(result);
    }

    @Test
    public void testGetActiveReservationWithRandomOrder() {

        final GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        final ComputeResourceReservation res1 = new ComputeResourceReservation("id1", "res1",
                Arrays.asList("cpu", "gpu"), System.currentTimeMillis() - 10000, System.currentTimeMillis() + 10000);
        // expired
        final ComputeResourceReservation res2 = new ComputeResourceReservation("id2", "res2",
                Arrays.asList("cpu", "gpu"), System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        // future
        final ComputeResourceReservation res3 = new ComputeResourceReservation("id3", "res3",
                Arrays.asList("cpu", "gpu"), System.currentTimeMillis() + 10000, System.currentTimeMillis() + 20000);
        // wrong queue
        final ComputeResourceReservation res4 = new ComputeResourceReservation("id3", "res3",
                Arrays.asList("shared", "compute"), System.currentTimeMillis() + 10000, System.currentTimeMillis() + 20000);
        final List<ComputeResourceReservation> reservations = Arrays.asList(res1, res2, res3, res4);

        Collections.shuffle(reservations);
        pref.setReservations(reservations);

        final ComputeResourceReservation result = GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(pref,
                "cpu");

        Assert.assertSame(res1, result);
    }
}
