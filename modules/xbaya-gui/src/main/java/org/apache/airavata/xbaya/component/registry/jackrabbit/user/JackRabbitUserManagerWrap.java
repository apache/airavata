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

package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.airavata.registry.api.impl.JCRRegistry;
import org.apache.airavata.registry.api.user.Authorizable;
import org.apache.airavata.registry.api.user.AuthorizableExistsException;
import org.apache.airavata.registry.api.user.Group;
import org.apache.airavata.registry.api.user.User;
import org.apache.airavata.registry.api.user.UserManager;
import org.apache.airavata.registry.api.user.UserManagerFactory;
import org.apache.jackrabbit.api.JackrabbitSession;

public class JackRabbitUserManagerWrap extends AbstractJackRabbitUMComponent implements UserManager {
    private JCRRegistry repository;
    private Session tempSession;
    static {
        UserManagerFactory.registerUserManager("Jackrabbit", JackRabbitUserManagerWrap.class);
    }

    @Override
    public User createUser(String userID, String password) throws AuthorizableExistsException, RepositoryException {
        createSession();
        org.apache.jackrabbit.api.security.user.User user = getJackRabbitUserManager().createUser(userID, password);
        closeSession();
        return new JackRabbitUserWrap(user);
    }

    @Override
    public User createUser(String userID, String password, Principal principal, String intermediatePath)
            throws AuthorizableExistsException, RepositoryException {
        createSession();
        org.apache.jackrabbit.api.security.user.User user = getJackRabbitUserManager().createUser(userID, password,
                principal, intermediatePath);
        closeSession();
        return new JackRabbitUserWrap(user);
    }

    @Override
    public Group createGroup(Principal principal) throws AuthorizableExistsException, RepositoryException {
        createSession();
        org.apache.jackrabbit.api.security.user.Group group = getJackRabbitUserManager().createGroup(principal);
        closeSession();
        return new JackRabbitGroupWrap(group);
    }

    @Override
    public Group createGroup(Principal principal, String intermediatePath) throws AuthorizableExistsException,
            RepositoryException {
        createSession();
        org.apache.jackrabbit.api.security.user.Group group = getJackRabbitUserManager().createGroup(principal,
                intermediatePath);
        closeSession();
        return new JackRabbitGroupWrap(group);
    }

    private org.apache.jackrabbit.api.security.user.UserManager getJackRabbitUserManager()
            throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        return ((JackrabbitSession) tempSession).getUserManager();
    }

    @Override
    public Authorizable getAuthorizable(String id) throws RepositoryException {
        createSession();
        org.apache.jackrabbit.api.security.user.Authorizable authorizable = getJackRabbitUserManager().getAuthorizable(
                id);
        closeSession();
        return new JackRabbitAuthorizableWrap(authorizable);
    }

    @Override
    public Authorizable getAuthorizable(Principal principal) throws RepositoryException {
        createSession();
        org.apache.jackrabbit.api.security.user.Authorizable authorizable = getJackRabbitUserManager().getAuthorizable(
                principal);
        closeSession();
        return new JackRabbitAuthorizableWrap(authorizable);
    }

    @Override
    public Iterator<Authorizable> findAuthorizables(String propertyName, String value) throws RepositoryException {
        createSession();
        Iterator<org.apache.jackrabbit.api.security.user.Authorizable> authorizables = getJackRabbitUserManager()
                .findAuthorizables(propertyName, value);
        closeSession();
        return getAuthorizableList(authorizables).iterator();
    }

    @Override
    public Iterator<Authorizable> findAuthorizables(String propertyName, String value, int searchType)
            throws RepositoryException {
        createSession();
        Iterator<org.apache.jackrabbit.api.security.user.Authorizable> authorizables = getJackRabbitUserManager()
                .findAuthorizables(propertyName, value, searchType);
        closeSession();
        return getAuthorizableList(authorizables).iterator();
    }

    @Override
    public boolean isAutoSave() throws RepositoryException {
        createSession();
        boolean autoSave = getJackRabbitUserManager().isAutoSave();
        closeSession();
        return autoSave;
    }

    @Override
    public void autoSave(boolean enable) throws UnsupportedRepositoryOperationException, RepositoryException {
        createSession();
        getJackRabbitUserManager().autoSave(enable);
        closeSession();
    }

    @Override
    public void setRepository(JCRRegistry repository) {
        this.repository = repository;
    }

    @Override
    public JCRRegistry getRepository() {
        return repository;
    }

    private Session createSession() throws RepositoryException {
        tempSession = getRepository().getSession();
        return tempSession;
    }

    private void closeSession() {
        if (tempSession != null && tempSession.isLive()) {
            tempSession.logout();
        }
    }
}
