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
package org.apache.airavata.compute.provider;

import org.apache.airavata.compute.provider.aws.AwsComputeProvider;
import org.apache.airavata.compute.provider.local.LocalComputeProvider;
import org.apache.airavata.compute.provider.slurm.SlurmComputeProvider;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.execution.dag.DagTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link DagTask} beans that delegate to {@link ComputeProvider} lifecycle methods.
 *
 * <p>Bean names match those referenced in
 * {@link org.apache.airavata.execution.dag.DAGTemplates} so the DAG engine resolves
 * the correct provider method for each node.
 */
@Configuration
@ConditionalOnParticipant
public class ComputeProviderConfig {

    // ---- AWS ----------------------------------------------------------------

    @Bean("awsProvisioningTask")
    DagTask awsProvision(AwsComputeProvider p) {
        return p::provision;
    }

    @Bean("awsSubmitTask")
    DagTask awsSubmit(AwsComputeProvider p) {
        return p::submit;
    }

    @Bean("awsMonitoringTask")
    DagTask awsMonitor(AwsComputeProvider p) {
        return p::monitor;
    }

    @Bean("awsCancelTask")
    DagTask awsCancel(AwsComputeProvider p) {
        return p::cancel;
    }

    @Bean("awsDeprovisioningTask")
    DagTask awsDeprovision(AwsComputeProvider p) {
        return p::deprovision;
    }

    // ---- SLURM --------------------------------------------------------------

    @Bean("slurmProvisioningTask")
    DagTask slurmProvision(SlurmComputeProvider p) {
        return p::provision;
    }

    @Bean("slurmSubmitTask")
    DagTask slurmSubmit(SlurmComputeProvider p) {
        return p::submit;
    }

    @Bean("slurmMonitoringTask")
    DagTask slurmMonitor(SlurmComputeProvider p) {
        return p::monitor;
    }

    @Bean("slurmCancelTask")
    DagTask slurmCancel(SlurmComputeProvider p) {
        return p::cancel;
    }

    @Bean("slurmDeprovisioningTask")
    DagTask slurmDeprovision(SlurmComputeProvider p) {
        return p::deprovision;
    }

    // ---- Local (PLAIN) ------------------------------------------------------

    @Bean("localProvisioningTask")
    DagTask localProvision(LocalComputeProvider p) {
        return p::provision;
    }

    @Bean("localSubmitTask")
    DagTask localSubmit(LocalComputeProvider p) {
        return p::submit;
    }

    @Bean("localMonitoringTask")
    DagTask localMonitor(LocalComputeProvider p) {
        return p::monitor;
    }

    @Bean("localCancelTask")
    DagTask localCancel(LocalComputeProvider p) {
        return p::cancel;
    }

    @Bean("localDeprovisioningTask")
    DagTask localDeprovision(LocalComputeProvider p) {
        return p::deprovision;
    }
}
