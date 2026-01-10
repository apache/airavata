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
package org.apache.airavata.config.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

/**
 * Composite annotation for Helix participant components.
 * 
 * <p>Combines @Profile("!test") and @ConditionalOnProperty for participant enablement.
 * No matchIfMissing - property MUST be explicitly set.
 * 
 * <p>Usage:
 * <pre>
 * {@code @Component}
 * {@code @ConditionalOnParticipant}
 * public class MyTask extends AiravataTask { }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile("!test")
@ConditionalOnProperty(prefix = "services.participant", name = "enabled", havingValue = "true")
public @interface ConditionalOnParticipant {
}
