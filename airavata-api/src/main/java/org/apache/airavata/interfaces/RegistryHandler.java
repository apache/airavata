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
package org.apache.airavata.interfaces;

/**
 * Composite interface for the registry handler, decoupling execution code from the
 * concrete RegistryServerHandler implementation (which lives in compute-service).
 *
 * <p>All method signatures use proto types to avoid entity/repository coupling.
 * Methods are organized into domain-specific sub-interfaces; this interface
 * aggregates them all.
 */
public interface RegistryHandler
        extends RegistryProvider,
                ExperimentRegistry,
                ProjectRegistry,
                AppCatalogRegistry,
                ComputeRegistry,
                ResourceProfileRegistry,
                StorageRegistry,
                GatewayRegistry,
                QueueStatusRegistry {}
