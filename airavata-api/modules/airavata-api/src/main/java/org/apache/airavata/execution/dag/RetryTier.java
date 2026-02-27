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
package org.apache.airavata.execution.dag;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import java.time.Duration;

/**
 * Retry tier for DAG task nodes. Determines the Temporal {@link RetryOptions}
 * applied when the node runs as an individual activity.
 */
public enum RetryTier {
    INFRASTRUCTURE(10, 5, 30, 2.0, Duration.ofMinutes(10)),
    DATA(3, 5, 15, 2.0, Duration.ofMinutes(5)),
    CHECK(3, 2, 10, 2.0, Duration.ofMinutes(2)),
    MONITOR(5, 30, 120, 2.0, Duration.ofMinutes(30)),
    CLEANUP(2, 2, 5, 1.5, Duration.ofMinutes(5));

    private final ActivityOptions activityOptions;

    RetryTier(int maxAttempts, int initialSec, int maxSec, double backoff, Duration startToClose) {
        this.activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(startToClose)
                .setRetryOptions(RetryOptions.newBuilder()
                        .setMaximumAttempts(maxAttempts)
                        .setInitialInterval(Duration.ofSeconds(initialSec))
                        .setMaximumInterval(Duration.ofSeconds(maxSec))
                        .setBackoffCoefficient(backoff)
                        .build())
                .build();
    }

    public ActivityOptions activityOptions() {
        return activityOptions;
    }
}
