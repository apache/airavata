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
package org.apache.airavata.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

/**
 * Service role conditionals. Use nested annotations for controller, monitor, and participant.
 */
public final class ServiceConditionals {

    private ServiceConditionals() {}

    /**
     * Composite annotation for Dapr controller components.
     * Combines @Profile and @ConditionalOnProperty for controller enablement.
     * Active when "test" profile is NOT active OR "orchestrator-integration" profile IS active.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Profile({"!test", "orchestrator-integration"})
    @ConditionalOnProperty(prefix = "airavata.services.controller", name = "enabled", havingValue = "true")
    public @interface ConditionalOnController {}

    /**
     * Composite annotation for compute monitor components.
     * Combines @Profile and @ConditionalOnProperty for monitor enablement.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Profile("!test")
    @ConditionalOnProperty(prefix = "airavata.services.monitor.compute", name = "enabled", havingValue = "true")
    public @interface ConditionalOnMonitor {}

    /**
     * Composite annotation for Dapr participant components.
     * Combines @Profile and @ConditionalOnProperty for participant enablement.
     * Active when "test" profile is NOT active OR "orchestrator-integration" profile IS active.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Profile({"!test", "orchestrator-integration"})
    @ConditionalOnProperty(prefix = "airavata.services.participant", name = "enabled", havingValue = "true")
    public @interface ConditionalOnParticipant {}
}
