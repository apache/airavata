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
package org.apache.airavata.api.server.security.interceptor;

import com.google.inject.matcher.Matchers;
import com.google.inject.AbstractModule;
import org.apache.airavata.api.server.security.interceptor.SecurityCheck;
import org.apache.airavata.api.server.security.interceptor.SecurityInterceptor;

/**
 * This does the plumbing work of integrating the interceptor with Guice framework for the methods to be
 * intercepted upon their invocation.
 */
public class SecurityModule extends AbstractModule {
    public void configure(){
        System.out.println("Security module reached...");
        SecurityInterceptor interceptor = new SecurityInterceptor();
        //requestInjection(interceptor);

        bindInterceptor(Matchers.any(), Matchers.annotatedWith(SecurityCheck.class), interceptor);
    }

}
