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
package org.apache.airavata.models.dbevent;

/**
 * Either variable set, depending on event-type.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.dbevent.proto.DBEventMessageContext},
 * whose sole content was a {@code oneof context} of {@code publisher} / {@code subscriber}. Modeled
 * as a sealed interface so the two alternatives are exhaustively pattern-matchable instead of
 * relying on a runtime case discriminator.
 *
 * <pre>{@code
 * switch (context) {
 *   case DBEventMessageContext.Publisher p -> ...
 *   case DBEventMessageContext.Subscriber s -> ...
 * }
 * }</pre>
 */
public sealed interface DBEventMessageContext {

    record Publisher(DBEventPublisher publisher) implements DBEventMessageContext {}

    record Subscriber(DBEventSubscriber subscriber) implements DBEventMessageContext {}
}
