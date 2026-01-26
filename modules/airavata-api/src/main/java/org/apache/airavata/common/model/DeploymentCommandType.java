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
package org.apache.airavata.common.model;

/**
 * Enum representing the type of command in an application deployment.
 *
 * <p>This consolidates the separate command tables:
 * <ul>
 *   <li>PREJOB_COMMAND - Commands executed before the main job</li>
 *   <li>POSTJOB_COMMAND - Commands executed after the main job</li>
 *   <li>MODULE_LOAD_CMD - Module load commands for environment setup</li>
 * </ul>
 *
 * @see org.apache.airavata.registry.entities.appcatalog.ApplicationDeploymentCommandEntity
 */
public enum DeploymentCommandType {
    /**
     * Pre-job commands are executed before the main application runs.
     * These are typically used for setup tasks like creating directories,
     * copying input files, or setting environment variables.
     */
    PREJOB,

    /**
     * Post-job commands are executed after the main application completes.
     * These are typically used for cleanup tasks, file compression,
     * or moving output files.
     */
    POSTJOB,

    /**
     * Module load commands are used to load required software modules
     * on HPC systems. These are executed during environment setup
     * before any pre-job commands.
     */
    MODULE_LOAD
}
