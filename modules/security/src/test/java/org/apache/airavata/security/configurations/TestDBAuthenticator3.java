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
package org.apache.airavata.security.configurations;

import org.apache.airavata.security.AbstractDatabaseAuthenticator;
import org.apache.airavata.security.AuthenticationException;

public class TestDBAuthenticator3 extends AbstractDatabaseAuthenticator {

    public TestDBAuthenticator3() {
        super();
    }

    @Override
    public void onSuccessfulAuthentication(Object authenticationInfo) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onFailedAuthentication(Object authenticationInfo) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean authenticate(Object credentials) throws AuthenticationException {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean doAuthentication(Object credentials) throws AuthenticationException {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthenticated(Object credentials) {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }
}
