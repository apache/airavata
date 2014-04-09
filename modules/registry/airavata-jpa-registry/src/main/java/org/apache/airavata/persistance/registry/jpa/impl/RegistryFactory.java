/*
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
 *
 */

package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.registry.cpi.Registry;

public class RegistryFactory {
    private static Registry registry;

    public static Registry getRegistry(String gateway, String username, String password){
        if (registry == null){
            registry = new RegistryImpl(gateway, username, password);
        }
        return registry;
    }

    public static Registry getDefaultRegistry () {
        if (registry == null){
            registry = new RegistryImpl();
        }
        return registry;
    }

    public static Registry getLoggingRegistry() {
        if(registry == null) {
            registry = new LoggingRegistryImpl();
        }
        return registry;
    }
}
