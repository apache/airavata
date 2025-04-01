/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.model;

import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.research.service.model.entity.User;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    public static AuthzToken authzToken() {
        return AUTHZ_TOKEN.get();
    }

    public static void setAuthzToken(AuthzToken token) {
        AUTHZ_TOKEN.set(token);
    }

    public static User user() {
        return CURRENT_USER.get();
    }

    public static void setUser(User user) {
        CURRENT_USER.set(user);
    }

    public static String username() {
        return CURRENT_USER.get().getUsername();
    }
}
