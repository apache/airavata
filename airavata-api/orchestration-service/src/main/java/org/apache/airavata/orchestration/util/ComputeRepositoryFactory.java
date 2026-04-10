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
package org.apache.airavata.orchestration.util;

/**
 * Reflective factory for compute-service repository instances.
 *
 * <p>Decouples airavata-api from compile-time dependency on compute-service.
 * At runtime, compute-service must be on the classpath (provided by airavata-server).
 */
public final class ComputeRepositoryFactory {

    private static final String REPO_PACKAGE = "org.apache.airavata.compute.repository.";

    private ComputeRepositoryFactory() {}

    /**
     * Create a compute repository instance by simple class name.
     *
     * @param simpleClassName e.g. "ComputeResourceRepository"
     * @return the repository instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(String simpleClassName) {
        try {
            Class<?> clazz = Class.forName(REPO_PACKAGE + simpleClassName);
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to create " + simpleClassName + "; is compute-service on the classpath?", e);
        }
    }
}
