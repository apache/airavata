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

package org.apache.airavata.workflow.tracking.util;

import java.util.Date;

public class ActivityTime implements Comparable {

    private Date clockTime;
    private int logicalTime;

    public ActivityTime(int logicalTime_, Date clockTime_) {
        logicalTime = logicalTime_;
        clockTime = clockTime_;
    }

    public Date getClockTime() {
        return clockTime;
    }

    public int getLogicalTime() {
        return logicalTime;
    }

    public int compareTo(Object o) {
        if (o == null)
            throw new NullPointerException();
        if (!ActivityTime.class.isAssignableFrom(o.getClass())) {
            throw new ClassCastException("cannot assign " + o.getClass() + " to " + ActivityTime.class);
        }
        // start comparison
        ActivityTime other = (ActivityTime) o;
        // compare logical time first if they are both positive
        if (this.logicalTime >= 0 && other.logicalTime >= 0) {
            if (this.logicalTime > other.logicalTime)
                return +1;
            if (this.logicalTime < other.logicalTime)
                return -1;
            assert this.logicalTime == other.logicalTime;
        }
        // both logical times are equal or not set
        // compare wallclock time
        return this.clockTime.compareTo(other.clockTime);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            return compareTo(o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "WF Timestep[" + logicalTime + "]  Timestamp[" + clockTime + "]";
    }
}
