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
package org.apache.airavata.orchestration.task;

import java.lang.reflect.Method;
import org.apache.airavata.interfaces.JobManagerConfiguration;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;

/**
 * Bridge between execution tasks and compute-service's JobFactory.
 *
 * <p>Uses reflection to avoid a compile-time dependency from airavata-api on compute-service,
 * while compute-service is on the runtime classpath via the server module.
 */
public final class JobFactoryHelper {

    private static final String JOB_FACTORY_CLASS = "org.apache.airavata.compute.task.JobFactory";

    private JobFactoryHelper() {}

    /**
     * Create a {@link JobManagerConfiguration} for the given resource job manager
     * by delegating to {@code JobFactory.getJobManagerConfiguration(...)}.
     */
    public static JobManagerConfiguration getJobManagerConfiguration(ResourceJobManager resourceJobManager)
            throws Exception {
        try {
            Class<?> jobFactory = Class.forName(JOB_FACTORY_CLASS);
            Method method = jobFactory.getMethod("getJobManagerConfiguration", ResourceJobManager.class);
            return (JobManagerConfiguration) method.invoke(null, resourceJobManager);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new Exception("Failed to invoke JobFactory.getJobManagerConfiguration", cause);
        } catch (ReflectiveOperationException e) {
            throw new Exception(
                    "Failed to call JobFactory.getJobManagerConfiguration; is compute-service on the classpath?", e);
        }
    }
}
