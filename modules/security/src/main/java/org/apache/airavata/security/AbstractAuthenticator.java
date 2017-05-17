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
package org.apache.airavata.security;

import org.apache.airavata.common.context.RequestContext;
import org.apache.airavata.common.context.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * An abstract implementation of the authenticator.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractAuthenticator implements Authenticator {

    protected static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected static Logger log = LoggerFactory.getLogger(AbstractAuthenticator.class);

    public static int DEFAULT_AUTHENTICATOR_PRIORITY = 5;

    protected String authenticatorName;

    private int priority = DEFAULT_AUTHENTICATOR_PRIORITY;

    protected boolean enabled = true;

    protected UserStore userStore;

    public AbstractAuthenticator() {

    }

    public AbstractAuthenticator(String name) {
        this.authenticatorName = name;
    }

    public void setUserStore(UserStore store) {
        this.userStore = store;
    }

    public UserStore getUserStore() {
        return this.userStore;
    }

    public int getPriority() {
        return priority;
    }

    public boolean canProcess(Object credentials) {
        return false;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean authenticate(Object credentials) throws AuthenticationException {

        boolean authenticated = doAuthentication(credentials);

        if (authenticated) {
            onSuccessfulAuthentication(credentials);
        } else {
            onFailedAuthentication(credentials);
        }

        return authenticated;
    }

    /**
     * Gets the current time converted to format in DATE_TIME_FORMAT.
     * 
     * @return Current time as a string.
     */
    protected String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        return simpleDateFormat.format(cal.getTime());
    }

    /**
     * The actual authenticating logic goes here. If user is successfully authenticated this should return
     * <code>true</code> else this should return <code>false</code>. If an error occurred while authenticating this will
     * throw an exception.
     * 
     * @param credentials
     *            The object which contains request credentials. This could be request most of the time.
     * @return <code>true</code> if successfully authenticated else <code>false</code>.
     * @throws AuthenticationException
     *             If system error occurs while authenticating.
     */
    protected abstract boolean doAuthentication(Object credentials) throws AuthenticationException;

    /**
     * If authentication is successful we can do post authentication actions in following method. E.g :- adding user to
     * session, audit logging etc ...
     * 
     * @param authenticationInfo
     *            A generic object with authentication information.
     */
    public abstract void onSuccessfulAuthentication(Object authenticationInfo);

    /**
     * If authentication is failed we can do post authentication actions in following method. E.g :- adding user to
     * session, audit logging etc ...
     * 
     * @param authenticationInfo
     *            A generic object with authentication information.
     */
    public abstract void onFailedAuthentication(Object authenticationInfo);
}
