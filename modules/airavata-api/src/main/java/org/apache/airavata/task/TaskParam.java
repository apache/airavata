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
package org.apache.airavata.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking task fields as task parameters.
 *
 * <p>Fields annotated with {@code @TaskParam} are automatically serialized and deserialized
 * by {@link TaskUtil} when tasks are executed in Dapr workflows. This allows task parameters
 * to be passed between workflow activities and persisted in the Dapr State Store.
 *
 * <p>Usage example:
 * <pre>{@code
 * public class MyTask extends AbstractTask {
 *     @TaskParam(name = "inputFile", mandatory = true)
 *     private String inputFile;
 *
 *     @TaskParam(name = "outputDir", defaultValue = "/tmp")
 *     private String outputDir;
 * }
 * }</pre>
 *
 * <p>In Dapr workflows, task parameters are passed via activity inputs rather than
 * through task chaining. The {@code TaskUtil} class handles serialization/deserialization
 * of these parameters.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 * @see TaskUtil
 * @see org.apache.airavata.task.base.AbstractTask
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaskParam {
    public String name();

    public String defaultValue() default "";

    public boolean mandatory() default false;
}
