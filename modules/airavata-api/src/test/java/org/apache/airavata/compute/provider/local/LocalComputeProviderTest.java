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
package org.apache.airavata.compute.provider.local;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.airavata.compute.provider.slurm.SlurmComputeProvider;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link LocalComputeProvider}.
 *
 * <p>Verifies that the local compute provider delegates provisioning,
 * cancellation, and deprovisioning to {@link SlurmComputeProvider} while
 * handling submission and monitoring locally without delegation.
 */
@ExtendWith(MockitoExtension.class)
class LocalComputeProviderTest {

    @Mock
    private SlurmComputeProvider slurmProvider;

    @Mock
    private TaskContext context;

    private LocalComputeProvider provider;

    @BeforeEach
    void setUp() {
        provider = new LocalComputeProvider(slurmProvider);
    }

    @Test
    void provision_delegatesToSlurmProvider() {
        DagTaskResult expectedResult = new DagTaskResult.Success("provisioned");
        when(slurmProvider.provision(context)).thenReturn(expectedResult);

        DagTaskResult result = provider.provision(context);

        verify(slurmProvider).provision(context);
        assertSame(expectedResult, result, "provision() must return the exact result from slurmProvider.provision()");
    }

    @Test
    void submit_returnsSuccessWithoutDelegating() {
        DagTaskResult result = provider.submit(context);

        verifyNoInteractions(slurmProvider);
        assertInstanceOf(
                DagTaskResult.Success.class,
                result,
                "submit() must return a Success result without delegating to slurmProvider");
    }

    @Test
    void monitor_returnsSuccessWithoutDelegating() {
        DagTaskResult result = provider.monitor(context);

        verifyNoInteractions(slurmProvider);
        assertInstanceOf(
                DagTaskResult.Success.class,
                result,
                "monitor() must return a Success result without delegating to slurmProvider");
    }

    @Test
    void cancel_delegatesToSlurmProvider() {
        DagTaskResult expectedResult = new DagTaskResult.Success("cancelled");
        when(slurmProvider.cancel(context)).thenReturn(expectedResult);

        DagTaskResult result = provider.cancel(context);

        verify(slurmProvider).cancel(context);
        assertSame(expectedResult, result, "cancel() must return the exact result from slurmProvider.cancel()");
    }

    @Test
    void deprovision_delegatesToSlurmProvider() {
        DagTaskResult expectedResult = new DagTaskResult.Success("deprovisioned");
        when(slurmProvider.deprovision(context)).thenReturn(expectedResult);

        DagTaskResult result = provider.deprovision(context);

        verify(slurmProvider).deprovision(context);
        assertSame(
                expectedResult, result, "deprovision() must return the exact result from slurmProvider.deprovision()");
    }
}
