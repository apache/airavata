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
package org.apache.airavata.helix.impl.task.aws.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A utility class to wait for a condition to be met, using exponential backoff with jitter.
 */
public class ExponentialBackoffWaiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackoffWaiter.class);
    private static final Random RANDOM = new Random();

    private final long initialDelayMillis;
    private final long maxDelayMillis;
    private final int maxRetries;
    private final String taskDescription;

    public ExponentialBackoffWaiter(String taskDescription, int maxRetries, long initialDelay, long maxDelay, TimeUnit unit) {
        this.taskDescription = taskDescription;
        this.maxRetries = maxRetries;
        this.initialDelayMillis = unit.toMillis(initialDelay);
        this.maxDelayMillis = unit.toMillis(maxDelay);
    }

    /**
     * Waits until the provided callable returns a non-null value.
     *
     * @param operation The operation to perform, which returns a result on success or throws an exception on failure.
     * @param <T>       The type of the result.
     * @return The result of the operation.
     * @throws Exception if the operation does not succeed within the max retries.
     */
    public <T> T waitUntil(Callable<T> operation) throws Exception {
        long currentDelay = initialDelayMillis;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                T result = operation.call();
                if (result != null) {
                    LOGGER.info("Successfully completed task '{}' on attempt {}.", taskDescription, attempt + 1);
                    return result;
                }
                // If the result is null, treat as a retryable condition without an exception
                LOGGER.warn("Task '{}' attempt {} returned a null result. Retrying...", taskDescription, attempt + 1);

            } catch (Exception e) {
                LOGGER.warn("Task '{}' attempt {} failed with error: {}. Retrying...", taskDescription, attempt + 1, e.getMessage());
            }

            attempt++;
            if (attempt >= maxRetries) {
                break;
            }

            try {
                long jitter = (long) (RANDOM.nextDouble() * currentDelay * 0.25);
                long waitTime = Math.min(currentDelay + jitter, maxDelayMillis);
                LOGGER.info("Waiting for {} ms before next attempt for task '{}'.", waitTime, taskDescription);
                Thread.sleep(waitTime);
                // Exponentially increase delay for the next attempt
                currentDelay = Math.min(currentDelay * 2, maxDelayMillis);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Task '{}' was interrupted while waiting for a retry. Cancelling wait.", taskDescription);
                throw new Exception("Waiter for task '" + taskDescription + "' was interrupted.", ie);
            }
        }

        LOGGER.error("Task '{}' failed to complete after {} attempts.", taskDescription, maxRetries);
        throw new Exception("Task '" + taskDescription + "' failed to complete after " + maxRetries + " attempts.");
    }
}