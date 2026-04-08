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

import java.util.List;
import org.apache.airavata.sharing.registry.models.proto.UserGroup;

/**
 * SPI contract for sharing-registry operations required by the security module.
 *
 * <p>This interface decouples the profile/security module from the sharing-service's
 * {@code SharingService} implementation, avoiding circular Maven
 * dependencies. Implementations are expected to be provided by the sharing module
 * and injected at runtime via Spring dependency injection.
 */
public interface SharingProvider {

    boolean isUserExists(String domainId, String userId) throws Exception;

    String createUser(String userId, String domainId, String userName) throws Exception;

    String createGroup(UserGroup group) throws Exception;

    List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws Exception;
}
