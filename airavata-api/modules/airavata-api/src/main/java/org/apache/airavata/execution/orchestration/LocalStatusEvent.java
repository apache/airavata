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
package org.apache.airavata.execution.orchestration;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * Generic Spring application event fired when any status changes.
 * Wraps a typed status-changed event and the associated gateway ID.
 *
 * <p>Implements {@link ResolvableTypeProvider} so that Spring can distinguish
 * between different {@code LocalStatusEvent<T>} parameterizations at runtime
 * despite type erasure.
 *
 * @param <T> the specific status-changed event type
 */
public class LocalStatusEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    private final T statusEvent;
    private final String gatewayId;

    public LocalStatusEvent(Object source, T statusEvent, String gatewayId) {
        super(source);
        this.statusEvent = statusEvent;
        this.gatewayId = gatewayId;
    }

    public T getStatusEvent() {
        return statusEvent;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(this.statusEvent));
    }
}
