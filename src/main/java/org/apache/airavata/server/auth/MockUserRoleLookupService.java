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
package org.apache.airavata.server.auth;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Hardcoded stand-in for a database-backed user/role lookup.
 * TODO: replace with a repository-backed {@link UserRoleLookupService} once a
 * user/role schema exists.
 */
@Service
public class MockUserRoleLookupService implements UserRoleLookupService {

    private static final Map<String, Set<String>> USER_ROLES = Map.of(
            "admin", Set.of("ADMIN"),
            "default-admin", Set.of("ADMIN"));

    private static final Set<String> DEFAULT_ROLES = Set.of("USER");

    @Override
    public Set<String> getRoles(String username) {
        return USER_ROLES.getOrDefault(username, DEFAULT_ROLES);
    }
}
