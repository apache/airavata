/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.security;

import org.apache.airavata.model.security.AuthzToken;

/**
 * This provides a thread local container for AuthzToken through out the execution of a particular thread.
 */
public class IdentityContext {
    private static ThreadLocal authzTokenContainer = new ThreadLocal();

    public static void set(AuthzToken authzToken){
        authzTokenContainer.set(authzToken);
    }

    public static void unset(){
        authzTokenContainer.remove();
    }

    public static AuthzToken get(){
        return (AuthzToken) authzTokenContainer.get();
    }

}
