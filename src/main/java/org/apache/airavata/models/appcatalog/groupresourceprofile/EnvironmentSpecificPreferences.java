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
package org.apache.airavata.models.appcatalog.groupresourceprofile;

/**
 * Variant preferences, one per supported resource type.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.EnvironmentSpecificPreferences}, whose
 * sole content was a {@code oneof preferences} of {@code slurm} / {@code aws}. Modeled as a sealed
 * interface so the two alternatives are exhaustively pattern-matchable instead of relying on a
 * runtime case discriminator (the generated {@code PreferencesCase} enum).
 *
 * <pre>{@code
 * if (preferences instanceof EnvironmentSpecificPreferences.Slurm s) {
 *     SlurmComputeResourcePreference slurm = s.slurm();
 * }
 * }</pre>
 */
public sealed interface EnvironmentSpecificPreferences {

    record Slurm(SlurmComputeResourcePreference slurm) implements EnvironmentSpecificPreferences {}

    record Aws(AwsComputeResourcePreference aws) implements EnvironmentSpecificPreferences {}
}
